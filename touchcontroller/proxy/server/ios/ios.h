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
// 使用文件系统路径 Unix 域套接字实现启动器与 Mod 之间的双向通信
typedef struct ios_transport {
    int socket_fd;               // 已连接的 socket（accept 或 connect 后获得）
    int listen_fd;               // listen socket（仅服务器模式使用，accept 后关闭）
    int pipe_read_fd;            // pipe 读端（替代 Linux eventfd，用于唤醒工作线程）
    int pipe_write_fd;           // pipe 写端（发送端写入以唤醒工作线程）
    ring_buffer_t* read_buffer;  // 接收消息队列（工作线程入队，调用方出队）
    ring_buffer_t* write_buffer; // 发送消息队列（调用方入队，工作线程出队）
    pthread_mutex_t read_mutex;  // 保护 read_buffer 的互斥锁
    pthread_mutex_t write_mutex; // 保护 write_buffer 的互斥锁
    pthread_t worker_thread;     // 工作线程（负责 socket 读写）
    volatile int running;        // 运行标志（1=运行中，0=请求停止）
    volatile int failed;         // 错误标志（1=工作线程发生错误）
    int is_server;               // 是否服务器模式（1=服务器，0=客户端）
    // 待处理消息：当 receive 调用时缓冲区不足（message->size > buffer_length），
    // 消息会被暂存在此字段，下一次 receive 调用会优先使用它。
    // 这避免了"静默截断并丢弃"的 bug（参考 Android 的 SetByteArrayRegion 会抛异常）。
    // 返回值约定：>0=接收字节数，0=无消息，-1=错误（transport failed），-2=缓冲区不足
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

// receive: 从 read_buffer 取出一条消息写入 buffer，返回字节数（0=无消息，负值=错误）
JNIEXPORT jint JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_receive(JNIEnv* env,
                                                                                                  jclass clazz,
                                                                                                  jlong handle,
                                                                                                  jbyteArray buffer);

// send: 将 buffer[off, off+len) 作为消息入队到 write_buffer
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
