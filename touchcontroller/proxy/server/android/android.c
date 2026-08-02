#include "android.h"

#include <android/log.h>
#include <errno.h>
#include <fcntl.h>
#include <poll.h>
#include <pthread.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/eventfd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#include "touchcontroller/proxy/server/util/ringbuffer/ring_buffer.h"

#define TAG "TouchControllerSocket"

// 4K initial queue size
#define MAX_QUEUE_SIZE (4 * 1024)

typedef struct android_poller {
    int socket_fd;
    int write_notify_fd;
    volatile int running;
    volatile int failed;
    ring_buffer_t* write_buffer;
    pthread_mutex_t write_mutex;
    ring_buffer_t* read_buffer;
    pthread_mutex_t read_mutex;
    pthread_t worker_thread;
} android_poller_t;

static void throw_exception(JNIEnv* env, const char* msg) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
}

static void throw_npe(JNIEnv* env, const char* msg) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/NullPointerException"), msg);
}

typedef struct message {
    size_t size;
    ssize_t bytes_processed;
    uint8_t* data;
} message_t;

static void free_message(message_t* msg) {
    if (msg) {
        if (msg->data) free(msg->data);
        free(msg);
    }
}

static void* worker_thread(void* arg) {
    android_poller_t* poller = (android_poller_t*)arg;

    __android_log_print(ANDROID_LOG_INFO, TAG, "Starting worker thread");

    struct pollfd fds[2];
    fds[0].fd = poller->socket_fd;
    fds[1].fd = poller->write_notify_fd;
    fds[1].events = POLLIN;

    message_t* message_tx = NULL;
    message_t* message_rx = NULL;

    while (poller->running) {
        fds[0].events = POLLIN | (message_tx != NULL ? POLLOUT : 0);

        int ret = poll(fds, 2, -1);
        if (ret < 0) {
            if (errno == EINTR) continue;
            break;
        }

        if (fds[0].revents & POLLIN) {
            // Read from socket
            while (1) {
                // Allocate message
                if (message_rx == NULL) {
                    message_rx = malloc(sizeof(message_t));
                    if (message_rx == NULL) {
                        __android_log_print(ANDROID_LOG_FATAL, TAG, "malloc failed: errno=%d (%s)", errno,
                                            strerror(errno));
                        goto fail;
                    }

                    message_rx->size = 0;
                    message_rx->data = NULL;
                }

                // Read message size
                if (message_rx->size == 0) {
                    uint8_t buf;
                    ssize_t len = read(poller->socket_fd, &buf, sizeof(buf));
                    if (len == 0) {
                        __android_log_print(ANDROID_LOG_FATAL, TAG, "read returned EOF: peer closed the connection");
                        goto fail;
                    }
                    if (len < 0) {
                        if (errno == EWOULDBLOCK || errno == EAGAIN) break;
                        __android_log_print(ANDROID_LOG_FATAL, TAG, "read failed: errno=%d (%s)", errno,
                                            strerror(errno));
                        goto fail;
                    }
                    if (buf == 0) continue;
                    message_rx->size = buf;
                    message_rx->data = malloc(buf);
                    if (message_rx->data == NULL) {
                        __android_log_print(ANDROID_LOG_FATAL, TAG, "malloc failed: errno=%d (%s)", errno,
                                            strerror(errno));
                        goto fail;
                    }
                    message_rx->bytes_processed = 0;
                }

                // Read the message
                size_t remaining = message_rx->size - message_rx->bytes_processed;
                ssize_t len = read(poller->socket_fd, &message_rx->data[message_rx->bytes_processed], remaining);
                if (len == 0) {
                    __android_log_print(ANDROID_LOG_FATAL, TAG, "read returned EOF: peer closed the connection");
                    goto fail;
                }
                if (len < 0) {
                    if (errno == EWOULDBLOCK || errno == EAGAIN) break;
                    __android_log_print(ANDROID_LOG_FATAL, TAG, "read failed: errno=%d (%s)", errno, strerror(errno));
                    goto fail;
                }
                message_rx->bytes_processed += len;
                remaining = message_rx->size - message_rx->bytes_processed;

                // Put the message to Java side
                if (remaining == 0) {
                    pthread_mutex_lock(&poller->read_mutex);
                    ring_buffer_enqueue(poller->read_buffer, message_rx);
                    pthread_mutex_unlock(&poller->read_mutex);
                    message_rx = NULL;
                }
            }
        }

        if ((fds[0].revents & POLLOUT) || (fds[1].revents & POLLIN) || (message_tx != NULL)) {
            // Write to socket
            if (fds[1].revents & POLLIN) {
                // Clear notify fd if it was set
                uint64_t val;
                read(poller->write_notify_fd, &val, sizeof(val));
            }

            while (1) {
                // Grab a message from queue if null
                if (message_tx == NULL) {
                    pthread_mutex_lock(&poller->write_mutex);
                    message_tx = ring_buffer_dequeue(poller->write_buffer);
                    pthread_mutex_unlock(&poller->write_mutex);
                }
                // No message in queue
                if (message_tx == NULL) break;

                // Write message length
                if (message_tx->bytes_processed < 0) {
                    uint8_t buf = message_tx->size;
                    if (write(poller->socket_fd, &buf, sizeof(buf)) <= 0) {
                        if (errno == EWOULDBLOCK || errno == EAGAIN) break;
                        __android_log_print(ANDROID_LOG_FATAL, TAG, "write failed: errno=%d (%s)", errno,
                                            strerror(errno));
                        goto fail;
                    }
                    message_tx->bytes_processed = 0;
                }

                // Write message
                size_t remaining = message_tx->size - message_tx->bytes_processed;
                ssize_t len = write(poller->socket_fd, &message_tx->data[message_tx->bytes_processed], remaining);
                if (len <= 0) {
                    if (len < 0 && (errno == EWOULDBLOCK || errno == EAGAIN)) break;
                    __android_log_print(ANDROID_LOG_FATAL, TAG, "write failed: errno=%d (%s)", errno, strerror(errno));
                    goto fail;
                }
                message_tx->bytes_processed += len;
                remaining = message_tx->size - message_tx->bytes_processed;

                if (remaining == 0) {
                    free_message(message_tx);
                    message_tx = NULL;
                } else {
                    // Because write() don't write the entire message, let's break here
                    break;
                }
            }
        }

        if (fds[0].revents & (POLLERR | POLLHUP)) {
            __android_log_print(ANDROID_LOG_FATAL, TAG, "poll failed: revents=%d", fds[0].revents);
            goto fail;
        }
    }

    goto cleanup;

fail:
    poller->failed = 1;
cleanup:
    free_message(message_tx);
    free_message(message_rx);
    __android_log_print(ANDROID_LOG_INFO, TAG, "Worker thread exiting");
    return NULL;
}

JNIEXPORT jlong JNICALL Java_top_fifthlight_touchcontroller_common_platform_android_Transport_new(JNIEnv* env,
                                                                                                  jclass clazz,
                                                                                                  jstring name) {
    // Initialize poller
    android_poller_t* poller = malloc(sizeof(android_poller_t));
    if (poller == NULL) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to malloc android_poller_t: errno=%d (%s)", errno,
                            strerror(errno));
        throw_exception(env, "Failed to malloc android_poller_t");
        return 0;
    }
    poller->socket_fd = -1;
    poller->write_notify_fd = -1;
    poller->running = 1;
    poller->failed = 0;
    poller->read_buffer = NULL;
    poller->write_buffer = NULL;

    int mutex_read_inited = 0;
    int mutex_write_inited = 0;

    // Create socket
    const char* path = (*env)->GetStringUTFChars(env, name, NULL);
    if (path == NULL) {
        __android_log_write(ANDROID_LOG_FATAL, TAG, "Failed to get socket path");
        throw_exception(env, "Failed to get socket path");
        goto cleanup_poller;
    }

    poller->socket_fd = socket(AF_UNIX, SOCK_STREAM | SOCK_CLOEXEC, 0);
    if (poller->socket_fd == -1) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to create socket: errno=%d (%s)", errno, strerror(errno));
        throw_exception(env, "Failed to create socket");
        goto cleanup_path;
    }

    struct sockaddr_un addr;
    size_t path_len = strlen(path);
    if (path_len > sizeof(addr.sun_path) - 2) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Socket path too long: %zu > %zu", path_len,
                            sizeof(addr.sun_path) - 2);
        throw_exception(env, "Socket path too long");
        goto cleanup_path;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    addr.sun_path[0] = '\0';
    strncpy(addr.sun_path + 1, path, sizeof(addr.sun_path) - 2);

    socklen_t addr_len = offsetof(struct sockaddr_un, sun_path) + 1 + path_len;
    if (connect(poller->socket_fd, (struct sockaddr*)&addr, addr_len) == -1) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to connect to unix socket: errno=%d (%s)", errno,
                            strerror(errno));
        throw_exception(env, "Failed to connect to unix socket");
        goto cleanup_path;
    }

    int flags = fcntl(poller->socket_fd, F_GETFL, 0);
    if (flags < 0) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to get the socket's status: errno=%d (%s)", errno,
                            strerror(errno));
        throw_exception(env, "Failed to get the socket's status");
        goto cleanup_path;
    }
    if (fcntl(poller->socket_fd, F_SETFL, flags | O_NONBLOCK) < 0) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to set the socket as O_NONBLOCK: errno=%d (%s)", errno,
                            strerror(errno));
        throw_exception(env, "Failed to set the socket as O_NONBLOCK");
        goto cleanup_path;
    }

    (*env)->ReleaseStringUTFChars(env, name, path);
    path = NULL;

    // Create event fd
    poller->write_notify_fd = eventfd(0, EFD_NONBLOCK | EFD_CLOEXEC);
    if (poller->write_notify_fd == -1) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to create eventfd: errno=%d (%s)", errno, strerror(errno));
        throw_exception(env, "Failed to create eventfd");
        goto cleanup_all;
    }

    // Allocate ring buffer
    poller->read_buffer = ring_buffer_alloc(MAX_QUEUE_SIZE);
    poller->write_buffer = ring_buffer_alloc(MAX_QUEUE_SIZE);
    if (!poller->read_buffer || !poller->write_buffer) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to allocate buffers: errno=%d (%s)", errno,
                            strerror(errno));
        throw_exception(env, "Failed to allocate buffers");
        goto cleanup_all;
    }

    // Initialze mutex
    int rc = pthread_mutex_init(&poller->read_mutex, NULL);
    if (rc != 0) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to init read mutex: error=%d (%s)", rc, strerror(rc));
        throw_exception(env, "Failed to init read mutex");
        goto cleanup_all;
    }
    mutex_read_inited = 1;

    rc = pthread_mutex_init(&poller->write_mutex, NULL);
    if (rc != 0) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to init write mutex: error=%d (%s)", rc, strerror(rc));
        throw_exception(env, "Failed to init write mutex");
        goto cleanup_all;
    }
    mutex_write_inited = 1;

    // Start thread
    rc = pthread_create(&poller->worker_thread, NULL, worker_thread, poller);
    if (rc != 0) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to create worker thread: error=%d (%s)", rc, strerror(rc));
        throw_exception(env, "Failed to create worker thread");
        goto cleanup_all;
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "Transport created");
    return (jlong)poller;

cleanup_path:
    if (path) (*env)->ReleaseStringUTFChars(env, name, path);

cleanup_all:
    if (mutex_write_inited) pthread_mutex_destroy(&poller->write_mutex);
    if (mutex_read_inited) pthread_mutex_destroy(&poller->read_mutex);
    if (poller->write_buffer) ring_buffer_free(poller->write_buffer);
    if (poller->read_buffer) ring_buffer_free(poller->read_buffer);
    if (poller->write_notify_fd != -1) close(poller->write_notify_fd);
    if (poller->socket_fd != -1) close(poller->socket_fd);

cleanup_poller:
    free(poller);
    return 0;
}

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_android_Transport_destroy(JNIEnv* env,
                                                                                                     jclass clazz,
                                                                                                     jlong handle) {
    android_poller_t* poller = (android_poller_t*)handle;
    if (poller == NULL) {
        throw_npe(env, "Poller handle is null");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "Destroying transport");

    // Set the running flag to 0, and kick the poller thread
    poller->running = 0;
    uint64_t val = 1;
    write(poller->write_notify_fd, &val, sizeof(val));

    // Wait the thread
    pthread_join(poller->worker_thread, NULL);

    // Release resources
    if (poller->socket_fd != -1) close(poller->socket_fd);
    if (poller->write_notify_fd != -1) close(poller->write_notify_fd);
    pthread_mutex_destroy(&poller->read_mutex);
    pthread_mutex_destroy(&poller->write_mutex);

    // Cleanup all messages
    message_t* msg;
    while ((msg = ring_buffer_dequeue(poller->read_buffer))) free_message(msg);
    while ((msg = ring_buffer_dequeue(poller->write_buffer))) free_message(msg);
    if (poller->read_buffer != NULL) ring_buffer_free(poller->read_buffer);
    if (poller->write_buffer != NULL) ring_buffer_free(poller->write_buffer);

    free(poller);
}

JNIEXPORT jint JNICALL Java_top_fifthlight_touchcontroller_common_platform_android_Transport_receive(
    JNIEnv* env, jclass clazz, jlong handle, jbyteArray buffer) {
    if (buffer == NULL) {
        throw_npe(env, "Buffer is null");
        return 0;
    }

    android_poller_t* poller = (android_poller_t*)handle;
    if (poller == NULL) {
        throw_npe(env, "Poller handle is null");
        return 0;
    }
    if (poller->failed) {
        __android_log_write(ANDROID_LOG_FATAL, TAG, "Poller thread failed");
        throw_exception(env, "Poller thread failed");
        return 0;
    }

    // Dequeue
    pthread_mutex_lock(&poller->read_mutex);
    message_t* message = ring_buffer_dequeue(poller->read_buffer);
    pthread_mutex_unlock(&poller->read_mutex);
    if (message == NULL) {
        return 0;
    }

    // Copy
    (*env)->SetByteArrayRegion(env, buffer, 0, message->size, message->data);
    if ((*env)->ExceptionCheck(env)) {
        free(message->data);
        free(message);
        return -1;
    }

    // Free the message and return
    size_t len = message->size;
    free_message(message);
    return len;
}

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_android_Transport_send(
    JNIEnv* env, jclass clazz, jlong handle, jbyteArray buffer, jint off, jint len) {
    if (buffer == NULL) {
        throw_npe(env, "Buffer is null");
        return;
    }
    if (len <= 0 || len > UINT8_MAX) {
        throw_exception(env, "Bad message size");
        return;
    }

    android_poller_t* poller = (android_poller_t*)handle;
    if (poller == NULL) {
        throw_npe(env, "Poller handle is null");
        return;
    }
    if (poller->failed) {
        __android_log_write(ANDROID_LOG_FATAL, TAG, "Poller thread failed");
        throw_exception(env, "Poller thread failed");
        return;
    }

    // Construct the message
    message_t* message = malloc(sizeof(message_t));
    if (message == NULL) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to allocate message: errno=%d (%s)", errno,
                            strerror(errno));
        throw_exception(env, "Failed to allocate message");
        return;
    }
    message->size = len;
    message->bytes_processed = -1;
    message->data = malloc(len);
    if (message->data == NULL) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to allocate message data: errno=%d (%s)", errno,
                            strerror(errno));
        throw_exception(env, "Failed to allocate message data");
        free(message);
        return;
    }

    (*env)->GetByteArrayRegion(env, buffer, off, len, message->data);
    if ((*env)->ExceptionCheck(env)) {
        free(message->data);
        free(message);
        return;
    }

    // Enqueue
    pthread_mutex_lock(&poller->write_mutex);
    int ret = ring_buffer_enqueue(poller->write_buffer, message);
    pthread_mutex_unlock(&poller->write_mutex);
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to write message into write buffer: ret=%d", ret);
        throw_exception(env, "Failed to write message into write buffer");
        free(message->data);
        free(message);
        return;
    }

    // Kick the poller thread
    uint64_t val = 1;
    ret = write(poller->write_notify_fd, &val, sizeof(val));
    if (ret != sizeof(val)) {
        __android_log_print(ANDROID_LOG_FATAL, TAG, "Failed to kick the poller thread: errno=%d (%s)", errno,
                            strerror(errno));
        throw_exception(env, "Failed to kick the poller thread");
        return;
    }
}
