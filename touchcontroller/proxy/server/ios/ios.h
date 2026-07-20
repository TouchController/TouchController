#ifndef TOUCHCONTROLLER_IOS_H
#define TOUCHCONTROLLER_IOS_H

#include <jni.h>
#include <pthread.h>
#include <stdint.h>

#include "touchcontroller/proxy/server/util/ringbuffer/ring_buffer.h"

#ifdef __cplusplus
extern "C" {
#endif

// iOS transport 内部结构
// 同进程双端内存队列实现：iOS 上启动器与 Mod 在同一进程内，无需 socket 跨进程通信。
// 数据流方向：
//   - Mod → 启动器：Mod 调用 Transport.send()（JNI）入队到 to_launcher_queue；
//                  启动器调用 touchcontroller_ios_receive()（C API）出队。
//   - 启动器 → Mod：启动器调用 touchcontroller_ios_send()（C API）入队到 to_mod_queue；
//                  Mod 调用 Transport.receive()（JNI）出队。
typedef struct ios_transport {
    ring_buffer_t* to_launcher_queue;   // Mod → 启动器（Mod send 入队，启动器 receive 出队）
    ring_buffer_t* to_mod_queue;        // 启动器 → Mod（启动器 send 入队，Mod receive 出队）
    pthread_mutex_t to_launcher_mutex;  // 保护 to_launcher_queue 的互斥锁
    pthread_mutex_t to_mod_mutex;       // 保护 to_mod_queue 的互斥锁
    // 缓冲区不足处理：启动器 receive 时若发现消息 size > buffer_length，
    // 暂存到 pending_message，下次 receive 优先使用。
    // 仅在 to_launcher_queue 方向（启动器 receive）使用。
    // 返回值约定：>0=接收字节数，0=无消息，-1=错误，-2=缓冲区不足
    struct message* pending_message;
} ios_transport_t;

// ===== JNI API（供 Mod 通过 JVM JNI 调用）=====
// init: 预留 NeoForge registerNatives 扩展点，当前为 no-op
JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_init(JNIEnv* env,
                                                                                               jclass clazz);

// new: 创建 transport，返回句柄（0 表示失败）
JNIEXPORT jlong JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_new(JNIEnv* env,
                                                                                               jclass clazz,
                                                                                               jstring path);

// receive: 从 to_mod_queue 取出一条消息写入 buffer，返回字节数（0=无消息，负值=错误）
JNIEXPORT jint JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_receive(JNIEnv* env,
                                                                                                  jclass clazz,
                                                                                                  jlong handle,
                                                                                                  jbyteArray buffer);

// send: 将 buffer[off, off+len) 作为消息入队到 to_launcher_queue
JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_send(JNIEnv* env,
                                                                                               jclass clazz,
                                                                                               jlong handle,
                                                                                               jbyteArray buffer,
                                                                                               jint off,
                                                                                               jint len);

// destroy: 销毁 transport，释放所有资源
JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_destroy(JNIEnv* env,
                                                                                                  jclass clazz,
                                                                                                  jlong handle);

// ===== C API（供启动器通过 dlsym 直接调用，无 JNIEnv）=====
// 与 JNI API 共用同一套内部实现，函数签名匹配 TouchControllerBridge.m 的函数指针类型
void touchcontroller_ios_init(void);
long long touchcontroller_ios_new(const char* path);
int touchcontroller_ios_receive(long long handle, void* buffer, int buffer_length);
void touchcontroller_ios_send(long long handle, const void* buffer, int offset, int length);
void touchcontroller_ios_destroy(long long handle);

#ifdef __cplusplus
}
#endif

#endif
