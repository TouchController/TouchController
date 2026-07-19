#include "ios.h"

#include <errno.h>
#include <fcntl.h>
#include <poll.h>
#include <pthread.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#include "touchcontroller/proxy/server/util/ringbuffer/ring_buffer.h"

// 4K 初始队列大小
#define MAX_QUEUE_SIZE (4 * 1024)

// 消息结构（与 Android 实现完全一致）
// bytes_processed = -1 是哨兵值，表示长度字节尚未写入 socket
typedef struct message {
    size_t size;
    ssize_t bytes_processed;
    uint8_t* data;
} message_t;

// 释放消息
static void free_message(message_t* msg) {
    if (msg) {
        if (msg->data) free(msg->data);
        free(msg);
    }
}

// 设置 fd 为 close-on-exec（替代 Linux 的 SOCK_CLOEXEC 标志）
static void set_cloexec(int fd) {
    int flags = fcntl(fd, F_GETFD, 0);
    if (flags >= 0) {
        fcntl(fd, F_SETFD, flags | FD_CLOEXEC);
    }
}

// 设置 fd 为非阻塞
static void set_nonblock(int fd) {
    int flags = fcntl(fd, F_GETFL, 0);
    if (flags >= 0) {
        fcntl(fd, F_SETFL, flags | O_NONBLOCK);
    }
}

// 设置 fd 为 close-on-exec 且非阻塞
static void set_cloexec_nonblock(int fd) {
    set_cloexec(fd);
    set_nonblock(fd);
}

// JNI 异常辅助函数
static void throw_exception(JNIEnv* env, const char* msg) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
}

static void throw_npe(JNIEnv* env, const char* msg) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/NullPointerException"), msg);
}

// 排空 pipe 读取端的所有待处理数据
// pipe 是非阻塞的，read 返回 -1（EAGAIN）时表示管道已空
static void drain_pipe(int fd) {
    uint8_t buf[64];
    while (read(fd, buf, sizeof(buf)) > 0) {
        // 持续读取直到管道为空
    }
}

// ===== 工作线程 =====
// 服务器模式分两阶段运行：
//   阶段 1：poll listen_fd 等待客户端连接，accept 后关闭 listen_fd
//   阶段 2：poll socket_fd + pipe_read_fd，读写消息
// 客户端模式跳过阶段 1，直接进入阶段 2
static void* worker_thread(void* arg) {
    ios_transport_t* transport = (ios_transport_t*)arg;

    message_t* message_tx = NULL;
    message_t* message_rx = NULL;

    // ===== 阶段 1：服务器模式等待客户端连接 =====
    if (transport->is_server) {
        while (transport->running) {
            struct pollfd fds[2];
            fds[0].fd = transport->listen_fd;
            fds[0].events = POLLIN;
            fds[0].revents = 0;
            fds[1].fd = transport->pipe_read_fd;
            fds[1].events = POLLIN;
            fds[1].revents = 0;

            int ret = poll(fds, 2, -1);
            if (ret < 0) {
                if (errno == EINTR) continue;
                goto fail;
            }

            // 被 pipe 唤醒（可能是 destroy 请求停止，或 send 入队了消息）
            if (fds[1].revents & POLLIN) {
                drain_pipe(transport->pipe_read_fd);
                if (!transport->running) goto cleanup;
                // send 调用但尚未建立连接，消息暂留在 write_buffer 中
                // 连接建立后阶段 2 会自动处理
            }

            // 有客户端连接到来
            if (fds[0].revents & POLLIN) {
                int new_fd = accept(transport->listen_fd, NULL, NULL);
                if (new_fd < 0) {
                    if (errno == EINTR || errno == EWOULDBLOCK || errno == EAGAIN) continue;
                    goto fail;
                }
                // 设置已连接 socket 为非阻塞 + close-on-exec
                set_cloexec_nonblock(new_fd);
                transport->socket_fd = new_fd;
                // 关闭 listen socket，不再接受新连接
                close(transport->listen_fd);
                transport->listen_fd = -1;
                break; // 进入阶段 2
            }

            if (fds[0].revents & (POLLERR | POLLHUP)) goto fail;
        }

        // 如果在等待连接时被 destroy 请求停止，直接退出
        if (!transport->running) goto cleanup;
    }

    // ===== 阶段 2：正常通信 =====
    {
        struct pollfd fds[2];
        fds[0].fd = transport->socket_fd;
        fds[1].fd = transport->pipe_read_fd;
        fds[1].events = POLLIN;

        while (transport->running) {
            fds[0].events = POLLIN | (message_tx != NULL ? POLLOUT : 0);
            fds[0].revents = 0;
            fds[1].revents = 0;

            int ret = poll(fds, 2, -1);
            if (ret < 0) {
                if (errno == EINTR) continue;
                break;
            }

            // 处理 socket 可读：读取消息入 read_buffer
            if (fds[0].revents & POLLIN) {
                while (1) {
                    // 分配消息结构
                    if (message_rx == NULL) {
                        message_rx = malloc(sizeof(message_t));
                        if (message_rx == NULL) goto fail;
                        message_rx->size = 0;
                        message_rx->data = NULL;
                        message_rx->bytes_processed = 0;
                    }

                    // 读取消息长度（1 字节）
                    if (message_rx->size == 0) {
                        uint8_t buf;
                        ssize_t len = read(transport->socket_fd, &buf, sizeof(buf));
                        if (len <= 0) {
                            if (len < 0 && (errno == EWOULDBLOCK || errno == EAGAIN)) break;
                            goto fail;
                        }
                        if (buf == 0) continue; // 跳过 0 长度字节
                        message_rx->size = buf;
                        message_rx->data = malloc(buf);
                        if (message_rx->data == NULL) goto fail;
                        message_rx->bytes_processed = 0;
                    }

                    // 读取消息内容
                    size_t remaining = message_rx->size - message_rx->bytes_processed;
                    ssize_t len = read(transport->socket_fd,
                                       &message_rx->data[message_rx->bytes_processed],
                                       remaining);
                    if (len <= 0) {
                        if (len < 0 && (errno == EWOULDBLOCK || errno == EAGAIN)) break;
                        goto fail;
                    }
                    message_rx->bytes_processed += len;
                    remaining = message_rx->size - message_rx->bytes_processed;

                    // 消息读取完成，入队到 read_buffer
                    if (remaining == 0) {
                        pthread_mutex_lock(&transport->read_mutex);
                        ring_buffer_enqueue(transport->read_buffer, message_rx);
                        pthread_mutex_unlock(&transport->read_mutex);
                        message_rx = NULL;
                    }
                }
            }

            // 处理可写或 pipe 唤醒：从 write_buffer 取消息写入 socket
            if ((fds[0].revents & POLLOUT) || (fds[1].revents & POLLIN) || (message_tx != NULL)) {
                // 清除 pipe 事件
                if (fds[1].revents & POLLIN) {
                    drain_pipe(transport->pipe_read_fd);
                }

                while (1) {
                    // 从队列取出消息
                    if (message_tx == NULL) {
                        pthread_mutex_lock(&transport->write_mutex);
                        message_tx = ring_buffer_dequeue(transport->write_buffer);
                        pthread_mutex_unlock(&transport->write_mutex);
                    }
                    if (message_tx == NULL) break; // 队列为空

                    // 写入消息长度（1 字节）
                    if (message_tx->bytes_processed < 0) {
                        uint8_t buf = message_tx->size;
                        if (write(transport->socket_fd, &buf, sizeof(buf)) <= 0) {
                            if (errno == EWOULDBLOCK || errno == EAGAIN) break;
                            goto fail;
                        }
                        message_tx->bytes_processed = 0;
                    }

                    // 写入消息内容
                    size_t remaining = message_tx->size - message_tx->bytes_processed;
                    ssize_t len = write(transport->socket_fd,
                                        &message_tx->data[message_tx->bytes_processed],
                                        remaining);
                    if (len <= 0) {
                        if (len < 0 && (errno == EWOULDBLOCK || errno == EAGAIN)) break;
                        goto fail;
                    }
                    message_tx->bytes_processed += len;
                    remaining = message_tx->size - message_tx->bytes_processed;

                    if (remaining == 0) {
                        free_message(message_tx);
                        message_tx = NULL;
                    } else {
                        // 部分写入，等待下次 POLLOUT
                        break;
                    }
                }
            }

            // 错误处理：POLLERR 或 POLLHUP 时设置 failed 标志
            if (fds[0].revents & (POLLERR | POLLHUP)) goto fail;
        }
    }

    goto cleanup;

fail:
    transport->failed = 1;
cleanup:
    free_message(message_tx);
    free_message(message_rx);
    return NULL;
}

// ===== 内部核心函数 =====

// 创建 transport
// 先尝试 connect（客户端模式），失败（ECONNREFUSED 或 ENOENT）则转为服务器模式
static ios_transport_t* ios_transport_create(const char* path) {
    if (path == NULL) return NULL;

    ios_transport_t* transport = malloc(sizeof(ios_transport_t));
    if (transport == NULL) return NULL;

    // 初始化所有字段
    transport->socket_fd = -1;
    transport->listen_fd = -1;
    transport->pipe_read_fd = -1;
    transport->pipe_write_fd = -1;
    transport->read_buffer = NULL;
    transport->write_buffer = NULL;
    transport->running = 1;
    transport->failed = 0;
    transport->is_server = 0;
    // 初始化 pending_message 为 NULL：用于缓冲区不足时暂存消息
    transport->pending_message = NULL;

    int mutex_read_inited = 0;
    int mutex_write_inited = 0;

    // 检查路径长度（文件系统路径，非 abstract namespace）
    struct sockaddr_un addr;
    size_t path_len = strlen(path);
    if (path_len >= sizeof(addr.sun_path)) {
        goto cleanup;
    }

    // 准备地址（iOS 不支持 abstract namespace，必须用文件系统路径）
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, path, sizeof(addr.sun_path) - 1);

    // 创建 socket（不使用 SOCK_CLOEXEC，改用 fcntl 设置 FD_CLOEXEC）
    int sock_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sock_fd == -1) {
        goto cleanup;
    }
    set_cloexec(sock_fd);

    // 尝试 connect（客户端模式）
    if (connect(sock_fd, (struct sockaddr*)&addr, sizeof(addr)) == 0) {
        // 客户端模式：connect 成功，直接进入阶段 2
        transport->is_server = 0;
        transport->socket_fd = sock_fd;
        set_nonblock(transport->socket_fd);
    } else if (errno == ECONNREFUSED || errno == ENOENT) {
        // 服务器模式：无服务器在监听，转为服务器
        // 关闭原 socket（connect 失败后 socket 状态不可靠）
        close(sock_fd);
        sock_fd = -1;

        // 创建新 socket
        sock_fd = socket(AF_UNIX, SOCK_STREAM, 0);
        if (sock_fd == -1) {
            goto cleanup;
        }
        set_cloexec(sock_fd);

        // unlink 旧 socket 文件（防止残留文件导致 bind 失败）
        unlink(path);

        // bind
        if (bind(sock_fd, (struct sockaddr*)&addr, sizeof(addr)) == -1) {
            close(sock_fd);
            goto cleanup;
        }

        // listen
        if (listen(sock_fd, 1) == -1) {
            close(sock_fd);
            goto cleanup;
        }

        // 设置 listen socket 为非阻塞（配合 poll() 处理虚假唤醒）
        set_nonblock(sock_fd);

        transport->is_server = 1;
        transport->listen_fd = sock_fd;
        transport->socket_fd = -1; // 等待 accept 后设置
    } else {
        // 其他 connect 错误，直接失败
        close(sock_fd);
        goto cleanup;
    }

    // 创建 pipe（替代 Linux 的 eventfd）
    // pipefd[0] 为读端，pipefd[1] 为写端
    int pipefd[2];
    if (pipe(pipefd) == -1) {
        goto cleanup;
    }
    transport->pipe_read_fd = pipefd[0];
    transport->pipe_write_fd = pipefd[1];
    set_cloexec_nonblock(transport->pipe_read_fd);
    set_cloexec_nonblock(transport->pipe_write_fd);

    // 分配 ring buffer
    transport->read_buffer = ring_buffer_alloc(MAX_QUEUE_SIZE);
    transport->write_buffer = ring_buffer_alloc(MAX_QUEUE_SIZE);
    if (transport->read_buffer == NULL || transport->write_buffer == NULL) {
        goto cleanup;
    }

    // 初始化 mutex
    if (pthread_mutex_init(&transport->read_mutex, NULL) != 0) {
        goto cleanup;
    }
    mutex_read_inited = 1;

    if (pthread_mutex_init(&transport->write_mutex, NULL) != 0) {
        goto cleanup;
    }
    mutex_write_inited = 1;

    // 启动工作线程
    if (pthread_create(&transport->worker_thread, NULL, worker_thread, transport) != 0) {
        goto cleanup;
    }

    return transport;

cleanup:
    if (mutex_write_inited) pthread_mutex_destroy(&transport->write_mutex);
    if (mutex_read_inited) pthread_mutex_destroy(&transport->read_mutex);
    if (transport->write_buffer) ring_buffer_free(transport->write_buffer);
    if (transport->read_buffer) ring_buffer_free(transport->read_buffer);
    if (transport->pipe_write_fd != -1) close(transport->pipe_write_fd);
    if (transport->pipe_read_fd != -1) close(transport->pipe_read_fd);
    if (transport->listen_fd != -1) close(transport->listen_fd);
    if (transport->socket_fd != -1) close(transport->socket_fd);
    // 服务器模式下清理 socket 文件
    if (transport->is_server) {
        unlink(path);
    }
    free(transport);
    return NULL;
}

// 接收消息（核心函数）
// 返回值约定：
//   >0 = 接收字节数（已写入 buffer）
//    0 = 无消息可读
//   -1 = 错误（transport->failed）
//   -2 = 缓冲区不足（消息已暂存到 pending_message，下次调用需更大缓冲区）
// 使用 pending_message 字段暂存超限消息，避免静默截断并丢弃的 bug。
// 参考 Android 实现：SetByteArrayRegion 在缓冲区不足时会抛 ArrayIndexOutOfBoundsException，
// 而不是截断数据。
static int ios_transport_receive_core(ios_transport_t* transport, void* buffer, int buffer_length) {
    if (transport->failed) return -1;

    // 优先处理上一次因缓冲区不足而暂存的消息
    if (transport->pending_message != NULL) {
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
    pthread_mutex_lock(&transport->read_mutex);
    message_t* message = ring_buffer_dequeue(transport->read_buffer);
    pthread_mutex_unlock(&transport->read_mutex);

    if (message == NULL) return 0;

    // 检查消息大小是否超过缓冲区
    if ((int)message->size > buffer_length) {
        // 缓冲区不足：暂存消息到 pending_message，下次调用优先处理
        transport->pending_message = message;
        return -2;
    }

    // 缓冲区足够，复制并释放消息
    memcpy(buffer, message->data, message->size);
    int copy_len = (int)message->size;
    free_message(message);
    return copy_len;
}

// 发送消息（核心函数）
// 返回值：0=成功，非零=失败
static int ios_transport_send_core(ios_transport_t* transport, const void* buffer, int offset, int length) {
    if (transport->failed) return -1;
    if (!transport->running) return -1;
    if (length <= 0 || length > UINT8_MAX) return -1;
    if (buffer == NULL) return -1;

    // 构造消息
    message_t* message = malloc(sizeof(message_t));
    if (message == NULL) return -1;
    message->size = length;
    message->bytes_processed = -1; // 哨兵值：表示长度字节尚未写入 socket
    message->data = malloc(length);
    if (message->data == NULL) {
        free(message);
        return -1;
    }
    memcpy(message->data, (const uint8_t*)buffer + offset, length);

    // 入队到 write_buffer
    pthread_mutex_lock(&transport->write_mutex);
    int ret = ring_buffer_enqueue(transport->write_buffer, message);
    pthread_mutex_unlock(&transport->write_mutex);

    if (ret != 0) {
        free_message(message);
        return -1;
    }

    // 唤醒工作线程（写入 pipe 写端）
    if (transport->pipe_write_fd != -1) {
        uint8_t val = 1;
        ssize_t wret = write(transport->pipe_write_fd, &val, sizeof(val));
        (void)wret; // 忽略返回值：即使管道满，工作线程仍会被之前的数据唤醒
    }

    return 0;
}

// 销毁 transport（核心函数）
static void ios_transport_destroy(ios_transport_t* transport) {
    if (transport == NULL) return;

    // 设置运行标志为 0，请求工作线程停止
    transport->running = 0;
    // 写入 pipe 唤醒工作线程（无论在阶段 1 还是阶段 2 都能被唤醒）
    if (transport->pipe_write_fd != -1) {
        uint8_t val = 1;
        ssize_t wret = write(transport->pipe_write_fd, &val, sizeof(val));
        (void)wret;
    }
    // 等待工作线程退出
    pthread_join(transport->worker_thread, NULL);

    // 关闭所有 fd
    if (transport->socket_fd != -1) close(transport->socket_fd);
    if (transport->listen_fd != -1) close(transport->listen_fd);
    if (transport->pipe_read_fd != -1) close(transport->pipe_read_fd);
    if (transport->pipe_write_fd != -1) close(transport->pipe_write_fd);

    // 清理消息队列中的剩余消息
    message_t* msg;
    while ((msg = ring_buffer_dequeue(transport->read_buffer))) free_message(msg);
    while ((msg = ring_buffer_dequeue(transport->write_buffer))) free_message(msg);

    // 释放因缓冲区不足而暂存的 pending_message
    if (transport->pending_message != NULL) {
        free_message(transport->pending_message);
        transport->pending_message = NULL;
    }

    // 销毁 ring buffer
    if (transport->read_buffer) ring_buffer_free(transport->read_buffer);
    if (transport->write_buffer) ring_buffer_free(transport->write_buffer);

    // 销毁 mutex
    pthread_mutex_destroy(&transport->read_mutex);
    pthread_mutex_destroy(&transport->write_mutex);

    free(transport);
}

// ===== JNI API（供 Mod 通过 JVM JNI 调用）=====
// 负责 JNI 类型转换，然后委托给内部核心函数

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_init(JNIEnv* env,
                                                                                               jclass clazz) {
    // no-op，预留 NeoForge registerNatives 扩展点
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
    // jstring → const char*
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
    if (transport->failed) {
        throw_exception(env, "Transport thread failed");
        return 0;
    }

    // jbyteArray → void*
    jsize buffer_len = (*env)->GetArrayLength(env, buffer);
    jbyte* native_buffer = (*env)->GetByteArrayElements(env, buffer, NULL);
    if (native_buffer == NULL) {
        return 0; // OutOfMemoryError 已抛出
    }

    int ret = ios_transport_receive_core(transport, native_buffer, buffer_len);
    // mode=0：复制回 Java 数组并释放 native buffer
    (*env)->ReleaseByteArrayElements(env, buffer, native_buffer, 0);

    if (ret == -2) {
        // 缓冲区不足：消息已暂存在 pending_message，不抛异常
        // 返回 0 让 Kotlin 端视为"无消息"，下一帧会再次调用 receive 重试
        // （IosPlatform.kt 的 readBuffer 已为 256 字节，正常情况下不会触发此分支；
        //  此处理仅作为安全网，防止异常情况下抛出 Java 异常导致 Mod 崩溃）
        return 0;
    }
    if (ret < 0) {
        throw_exception(env, "Transport thread failed");
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
    if (transport->failed) {
        throw_exception(env, "Transport thread failed");
        return;
    }

    // jbyteArray → void*
    jbyte* native_buffer = (*env)->GetByteArrayElements(env, buffer, NULL);
    if (native_buffer == NULL) {
        return; // OutOfMemoryError 已抛出
    }

    int ret = ios_transport_send_core(transport, native_buffer, off, len);
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
// 直接委托给内部核心函数，无 JNIEnv

void touchcontroller_ios_init(void) {
    // no-op，预留 NeoForge registerNatives 扩展点
}

long long touchcontroller_ios_new(const char* path) {
    if (path == NULL) return 0;
    ios_transport_t* transport = ios_transport_create(path);
    return (long long)transport;
}

int touchcontroller_ios_receive(long long handle, void* buffer, int buffer_length) {
    ios_transport_t* transport = (ios_transport_t*)handle;
    if (transport == NULL) return -1;
    if (buffer == NULL || buffer_length <= 0) return -1;
    return ios_transport_receive_core(transport, buffer, buffer_length);
}

void touchcontroller_ios_send(long long handle, const void* buffer, int offset, int length) {
    ios_transport_t* transport = (ios_transport_t*)handle;
    if (transport == NULL || buffer == NULL) return;
    ios_transport_send_core(transport, buffer, offset, length);
}

void touchcontroller_ios_destroy(long long handle) {
    ios_transport_t* transport = (ios_transport_t*)handle;
    ios_transport_destroy(transport);
}
