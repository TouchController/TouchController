#include "ios.h"

#include <pthread.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include "touchcontroller/proxy/server/util/ringbuffer/ring_buffer.h"

// 4K 初始队列大小
#define MAX_QUEUE_SIZE (4 * 1024)

// 消息结构（与 Android 实现完全一致）
// bytes_processed 字段保留以兼容，但在新的内存队列实现中不使用
typedef struct message {
    size_t size;
    ssize_t bytes_processed;
    uint8_t* data;
} message_t;

// 全局共享 transport（同进程单例）
// iOS 上 Mod 与启动器运行在同一进程内，两者各自调用 new() 必须返回同一个
// transport 指针，否则会创建两套独立的 ring_buffer 队列，消息无法跨实例传递。
// 使用引用计数管理生命周期：destroy 仅减少引用，归零时真正释放资源。
static ios_transport_t* g_shared_transport = NULL;
static pthread_mutex_t g_init_mutex = PTHREAD_MUTEX_INITIALIZER;
static int g_ref_count = 0;

// 释放消息
static void free_message(message_t* msg) {
    if (msg) {
        if (msg->data) free(msg->data);
        free(msg);
    }
}

// JNI 异常辅助函数
static void throw_exception(JNIEnv* env, const char* msg) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
}

static void throw_npe(JNIEnv* env, const char* msg) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/NullPointerException"), msg);
}

// ===== 内部核心函数 =====

// 首次分配并初始化 transport（仅在 g_shared_transport 为 NULL 时调用）
// 返回值：成功返回 transport 指针，失败返回 NULL
static ios_transport_t* ios_transport_alloc(void) {
    ios_transport_t* transport = malloc(sizeof(ios_transport_t));
    if (transport == NULL) return NULL;

    // 初始化所有字段
    transport->to_launcher_queue = NULL;
    transport->to_mod_queue = NULL;
    transport->pending_message = NULL;

    int mutex_launcher_inited = 0;
    int mutex_mod_inited = 0;

    // 分配两个 ring buffer
    transport->to_launcher_queue = ring_buffer_alloc(MAX_QUEUE_SIZE);
    transport->to_mod_queue = ring_buffer_alloc(MAX_QUEUE_SIZE);
    if (transport->to_launcher_queue == NULL || transport->to_mod_queue == NULL) {
        goto cleanup;
    }

    // 初始化 mutex
    if (pthread_mutex_init(&transport->to_launcher_mutex, NULL) != 0) {
        goto cleanup;
    }
    mutex_launcher_inited = 1;

    if (pthread_mutex_init(&transport->to_mod_mutex, NULL) != 0) {
        goto cleanup;
    }
    mutex_mod_inited = 1;

    return transport;

cleanup:
    if (mutex_mod_inited) pthread_mutex_destroy(&transport->to_mod_mutex);
    if (mutex_launcher_inited) pthread_mutex_destroy(&transport->to_launcher_mutex);
    if (transport->to_mod_queue) ring_buffer_free(transport->to_mod_queue);
    if (transport->to_launcher_queue) ring_buffer_free(transport->to_launcher_queue);
    free(transport);
    return NULL;
}

// 创建 transport（同进程单例）
// path 参数仅保留兼容性（同进程内存队列无需 socket 文件路径）
// 多次调用返回同一个 transport 指针，并增加引用计数；
// 对应的 destroy 调用仅减少引用计数，归零时才真正释放资源
static ios_transport_t* ios_transport_create(const char* path) {
    (void)path;  // 显式忽略 path 参数

    pthread_mutex_lock(&g_init_mutex);
    if (g_shared_transport != NULL) {
        // 已存在共享实例，增加引用计数并返回
        g_ref_count++;
        pthread_mutex_unlock(&g_init_mutex);
        return g_shared_transport;
    }

    // 首次创建
    ios_transport_t* transport = ios_transport_alloc();
    if (transport == NULL) {
        pthread_mutex_unlock(&g_init_mutex);
        return NULL;
    }

    g_shared_transport = transport;
    g_ref_count = 1;
    pthread_mutex_unlock(&g_init_mutex);
    return transport;
}

// 发送消息（核心函数）
// 将 buffer[offset, offset+length) 作为一条消息入队到指定队列
// 返回值：0=成功，非零=失败
static int ios_transport_send_core(ring_buffer_t* queue,
                                   pthread_mutex_t* mutex,
                                   const void* buffer, int offset, int length) {
    if (length <= 0 || length > UINT8_MAX) return -1;
    if (buffer == NULL) return -1;

    // 构造消息
    message_t* message = malloc(sizeof(message_t));
    if (message == NULL) return -1;
    message->size = length;
    message->bytes_processed = 0;  // 内存队列实现不使用此字段，初始化为 0
    message->data = malloc(length);
    if (message->data == NULL) {
        free(message);
        return -1;
    }
    memcpy(message->data, (const uint8_t*)buffer + offset, length);

    // 加锁入队
    pthread_mutex_lock(mutex);
    int ret = ring_buffer_enqueue(queue, message);
    pthread_mutex_unlock(mutex);

    if (ret != 0) {
        free_message(message);
        return -1;
    }

    return 0;
}

// 接收消息（核心函数）
// 从指定队列 dequeue 一条消息写入 buffer
// use_pending 标志：是否使用 pending_message 暂存机制（仅启动器 receive 方向使用）
// 返回值约定：
//   >0 = 接收字节数（已写入 buffer）
//    0 = 无消息可读
//   -1 = 错误（保留，当前实现中不会发生）
//   -2 = 缓冲区不足（消息 size > buffer_length）
//        use_pending=1 时消息暂存到 pending_message，下次调用优先处理；
//        use_pending=0 时消息被丢弃（理论上不会发生，Mod readBuffer 已为 256 字节）
static int ios_transport_receive_core(ios_transport_t* transport,
                                       ring_buffer_t* queue, pthread_mutex_t* mutex,
                                       void* buffer, int buffer_length, int use_pending) {
    // 优先处理上一次因缓冲区不足而暂存的消息（仅启动器 receive 方向）
    if (use_pending && transport->pending_message != NULL) {
        message_t* msg = transport->pending_message;
        if ((int)msg->size > buffer_length) {
            // 缓冲区仍然不足，保留消息，等待下次调用
            return -2;
        }
        // 缓冲区足够，复制并释放暂存消息
        memcpy(buffer, msg->data, msg->size);
        int copy_len = (int)msg->size;
        free_message(msg);
        transport->pending_message = NULL;
        return copy_len;
    }

    // 从 ring_buffer 取出新消息
    pthread_mutex_lock(mutex);
    message_t* message = ring_buffer_dequeue(queue);
    pthread_mutex_unlock(mutex);

    if (message == NULL) return 0;

    // 检查消息大小是否超过缓冲区
    if ((int)message->size > buffer_length) {
        if (use_pending) {
            // 启动器方向：暂存到 pending_message，下次调用优先处理
            transport->pending_message = message;
            return -2;
        } else {
            // Mod 方向：直接返回 0，让 Kotlin 端下一帧重试
            // 注意：Mod 的 readBuffer 已为 256 字节，正常情况下不会触发此分支
            free_message(message);
            return 0;
        }
    }

    // 缓冲区足够，复制并释放消息
    memcpy(buffer, message->data, message->size);
    int copy_len = (int)message->size;
    free_message(message);
    return copy_len;
}

// 销毁 transport（核心函数）
// 采用引用计数：减少引用，归零时才真正释放资源
// 这样 Mod 端和启动器端各自 destroy 时不会相互影响
static void ios_transport_destroy(ios_transport_t* transport) {
    if (transport == NULL) return;

    // 非共享实例不应出现，但安全起见直接返回
    pthread_mutex_lock(&g_init_mutex);
    if (transport != g_shared_transport) {
        pthread_mutex_unlock(&g_init_mutex);
        return;
    }

    // 减少引用计数
    g_ref_count--;
    if (g_ref_count > 0) {
        // 仍有其他引用，保留实例
        pthread_mutex_unlock(&g_init_mutex);
        return;
    }

    // 引用计数归零，真正销毁
    g_shared_transport = NULL;
    pthread_mutex_unlock(&g_init_mutex);

    // 清理 to_launcher_queue 中的剩余消息
    if (transport->to_launcher_queue) {
        message_t* msg;
        while ((msg = ring_buffer_dequeue(transport->to_launcher_queue))) free_message(msg);
    }

    // 清理 to_mod_queue 中的剩余消息
    if (transport->to_mod_queue) {
        message_t* msg;
        while ((msg = ring_buffer_dequeue(transport->to_mod_queue))) free_message(msg);
    }

    // 释放因缓冲区不足而暂存的 pending_message
    if (transport->pending_message != NULL) {
        free_message(transport->pending_message);
        transport->pending_message = NULL;
    }

    // 销毁 ring buffer
    if (transport->to_launcher_queue) ring_buffer_free(transport->to_launcher_queue);
    if (transport->to_mod_queue) ring_buffer_free(transport->to_mod_queue);

    // 销毁 mutex
    pthread_mutex_destroy(&transport->to_launcher_mutex);
    pthread_mutex_destroy(&transport->to_mod_mutex);

    free(transport);
}

// ===== JNI API（供 Mod 通过 JVM JNI 调用）=====
// Mod 端调用方向：
//   Transport.send()     → 入队到 to_launcher_queue（供启动器 receive）
//   Transport.receive()  → 从 to_mod_queue 出队（来自启动器 send）

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_init(JNIEnv* env,
                                                                                               jclass clazz) {
    // no-op，预留 NeoForge registerNatives 扩展点
    // 同进程内存队列无需全局初始化
    (void)env;
    (void)clazz;
}

JNIEXPORT jlong JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_new(JNIEnv* env,
                                                                                               jclass clazz,
                                                                                               jstring path) {
    (void)clazz;
    if (path == NULL) {
        throw_npe(env, "Path is null");
        return 0;
    }
    // jstring → const char*（path 仅保留兼容性，实现中忽略）
    const char* native_path = (*env)->GetStringUTFChars(env, path, NULL);
    if (native_path == NULL) {
        return 0; // OutOfMemoryError 已抛出
    }
    ios_transport_t* transport = ios_transport_create(native_path);
    (*env)->ReleaseStringUTFChars(env, path, native_path);
    if (transport == NULL) {
        throw_exception(env, "Failed to create transport");
        return 0;
    }
    return (jlong)transport;
}

JNIEXPORT jint JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_receive(JNIEnv* env,
                                                                                                  jclass clazz,
                                                                                                  jlong handle,
                                                                                                  jbyteArray buffer) {
    (void)clazz;
    if (buffer == NULL) {
        throw_npe(env, "Buffer is null");
        return 0;
    }
    ios_transport_t* transport = (ios_transport_t*)handle;
    if (transport == NULL) {
        throw_npe(env, "Transport handle is null");
        return 0;
    }

    // jbyteArray → void*
    jsize buffer_len = (*env)->GetArrayLength(env, buffer);
    jbyte* native_buffer = (*env)->GetByteArrayElements(env, buffer, NULL);
    if (native_buffer == NULL) {
        return 0; // OutOfMemoryError 已抛出
    }

    // Mod 接收：从 to_mod_queue 出队，use_pending=0（缓冲区不足时返回 0 让 Kotlin 重试）
    int ret = ios_transport_receive_core(transport,
                                          transport->to_mod_queue, &transport->to_mod_mutex,
                                          native_buffer, buffer_len, 0);
    // mode=0：复制回 Java 数组并释放 native buffer
    (*env)->ReleaseByteArrayElements(env, buffer, native_buffer, 0);

    if (ret == -2) {
        // 缓冲区不足（理论上不会发生，Mod readBuffer 已为 256 字节）
        // 返回 0 让 Kotlin 端视为"无消息"，下一帧重试
        return 0;
    }
    if (ret < 0) {
        // -1 错误（当前实现中不会发生），抛异常保护
        throw_exception(env, "Transport failed");
        return 0;
    }
    return ret;
}

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_send(JNIEnv* env,
                                                                                               jclass clazz,
                                                                                               jlong handle,
                                                                                               jbyteArray buffer,
                                                                                               jint off,
                                                                                               jint len) {
    (void)clazz;
    if (buffer == NULL) {
        throw_npe(env, "Buffer is null");
        return;
    }
    if (len <= 0 || len > UINT8_MAX) {
        throw_exception(env, "Bad message size");
        return;
    }
    ios_transport_t* transport = (ios_transport_t*)handle;
    if (transport == NULL) {
        throw_npe(env, "Transport handle is null");
        return;
    }

    // jbyteArray → void*
    jbyte* native_buffer = (*env)->GetByteArrayElements(env, buffer, NULL);
    if (native_buffer == NULL) {
        return; // OutOfMemoryError 已抛出
    }

    // Mod 发送：入队到 to_launcher_queue（供启动器 receive）
    int ret = ios_transport_send_core(transport->to_launcher_queue,
                                       &transport->to_launcher_mutex,
                                       native_buffer, off, len);
    // JNI_ABORT：不复制回 Java 数组（只读访问）
    (*env)->ReleaseByteArrayElements(env, buffer, native_buffer, JNI_ABORT);

    if (ret != 0) {
        throw_exception(env, "Failed to send message");
    }
}

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_destroy(JNIEnv* env,
                                                                                                  jclass clazz,
                                                                                                  jlong handle) {
    (void)clazz;
    ios_transport_t* transport = (ios_transport_t*)handle;
    if (transport == NULL) {
        throw_npe(env, "Transport handle is null");
        return;
    }
    ios_transport_destroy(transport);
}

// ===== C API（供启动器通过 dlsym 直接调用）=====
// 启动器端调用方向：
//   touchcontroller_ios_send()     → 入队到 to_mod_queue（供 Mod receive）
//   touchcontroller_ios_receive()  → 从 to_launcher_queue 出队（来自 Mod send）

void touchcontroller_ios_init(void) {
    // no-op，预留 NeoForge registerNatives 扩展点
    // 同进程内存队列无需全局初始化
}

long long touchcontroller_ios_new(const char* path) {
    ios_transport_t* transport = ios_transport_create(path);
    return (long long)transport;
}

int touchcontroller_ios_receive(long long handle, void* buffer, int buffer_length) {
    ios_transport_t* transport = (ios_transport_t*)handle;
    if (transport == NULL) return -1;
    if (buffer == NULL || buffer_length <= 0) return -1;
    // 启动器接收：从 to_launcher_queue 出队，use_pending=1（缓冲区不足时暂存到 pending_message）
    return ios_transport_receive_core(transport,
                                       transport->to_launcher_queue, &transport->to_launcher_mutex,
                                       buffer, buffer_length, 1);
}

void touchcontroller_ios_send(long long handle, const void* buffer, int offset, int length) {
    ios_transport_t* transport = (ios_transport_t*)handle;
    if (transport == NULL || buffer == NULL) return;
    // 启动器发送：入队到 to_mod_queue（供 Mod receive）
    ios_transport_send_core(transport->to_mod_queue,
                             &transport->to_mod_mutex,
                             buffer, offset, length);
}

void touchcontroller_ios_destroy(long long handle) {
    ios_transport_t* transport = (ios_transport_t*)handle;
    ios_transport_destroy(transport);
}
