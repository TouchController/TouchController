#include "ios.h"

#include <assert.h>
#include <pthread.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include "touchcontroller/proxy/server/util/ringbuffer/ring_buffer.h"

#define MAX_QUEUE_SIZE (4 * 1024)

typedef struct queue {
    ring_buffer_t* write_buffer;
    pthread_mutex_t write_mutex;
    ring_buffer_t* read_buffer;
    pthread_mutex_t read_mutex;
} queue_t;

static queue_t* queue = NULL;

typedef struct message {
    size_t size;
    void* data;
} message_t;

static void throw_exception(JNIEnv* env, const char* msg) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), msg);
}

static void throw_npe(JNIEnv* env, const char* msg) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/NullPointerException"), msg);
}

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_init(JNIEnv* env,
                                                                                              jclass clazz) {
    queue = malloc(sizeof(queue_t));
    if (queue == NULL) {
        throw_exception(env, "Failed to allocate queue");
        return;
    }

    queue->write_buffer = NULL;
    queue->read_buffer = NULL;

    int mutex_write_inited = 0;
    int mutex_read_inited = 0;

    queue->write_buffer = ring_buffer_alloc(MAX_QUEUE_SIZE);
    queue->read_buffer = ring_buffer_alloc(MAX_QUEUE_SIZE);
    if (queue->write_buffer == NULL || queue->read_buffer == NULL) {
        goto cleanup;
    }

    if (pthread_mutex_init(&queue->write_mutex, NULL) != 0) {
        goto cleanup;
    }
    mutex_write_inited = 1;

    if (pthread_mutex_init(&queue->read_mutex, NULL) != 0) {
        goto cleanup;
    }
    mutex_read_inited = 1;

    return;

cleanup:
    if (mutex_write_inited) pthread_mutex_destroy(&queue->write_mutex);
    if (mutex_read_inited) pthread_mutex_destroy(&queue->read_mutex);
    if (queue->write_buffer) ring_buffer_free(queue->write_buffer);
    if (queue->read_buffer) ring_buffer_free(queue->read_buffer);
    free(queue);
    queue = NULL;
    throw_exception(env, "Failed to allocate queue");
    return;
}

JNIEXPORT jint JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_receive(JNIEnv* env,
                                                                                                 jclass clazz,
                                                                                                 jbyteArray buffer) {
    if (buffer == NULL) {
        throw_npe(env, "Buffer is null");
        return 0;
    }
    if (queue == NULL) {
        throw_npe(env, "Queue handle is null. Make sure you have initialized the transport.");
        return 0;
    }

    pthread_mutex_lock(&queue->read_mutex);
    message_t* msg = ring_buffer_dequeue(queue->read_buffer);
    pthread_mutex_unlock(&queue->read_mutex);
    if (msg == NULL) {
        return 0;
    }

    (*env)->SetByteArrayRegion(env, buffer, 0, msg->size, msg->data);
    if ((*env)->ExceptionCheck(env)) {
        free(msg->data);
        free(msg);
        return -1;
    }

    size_t len = msg->size;
    free(msg->data);
    free(msg);
    return len;
}

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_send(JNIEnv* env, jclass clazz,
                                                                                              jbyteArray buffer,
                                                                                              jint off, jint len) {
    if (buffer == NULL) {
        throw_npe(env, "Buffer is null");
        return;
    }
    if (len <= 0 || len > UINT8_MAX) {
        throw_exception(env, "Bad message size");
        return;
    }
    if (queue == NULL) {
        throw_npe(env, "Queue handle is null. Make sure you have initialized the transport.");
        return;
    }

    message_t* msg = malloc(sizeof(message_t));
    if (msg == NULL) {
        throw_exception(env, "Failed to allocate message");
        return;
    }
    msg->size = len;
    msg->data = malloc(len);
    if (msg->data == NULL) {
        free(msg);
        throw_exception(env, "Failed to allocate message data");
        return;
    }

    (*env)->GetByteArrayRegion(env, buffer, off, len, msg->data);
    if ((*env)->ExceptionCheck(env)) {
        free(msg->data);
        free(msg);
        return;
    }

    pthread_mutex_lock(&queue->write_mutex);
    int ret = ring_buffer_enqueue(queue->write_buffer, msg);
    pthread_mutex_unlock(&queue->write_mutex);
    if (ret != 0) {
        throw_exception(env, "Failed to write message into write buffer");
        free(msg->data);
        free(msg);
        return;
    }
}

int touchcontroller_ios_receive(void* buf) {
    if (queue == NULL) {
        return -1;
    }

    pthread_mutex_lock(&queue->write_mutex);
    message_t* msg = ring_buffer_dequeue(queue->write_buffer);
    pthread_mutex_unlock(&queue->write_mutex);
    if (msg == NULL) {
        return 0;
    }

    memcpy(buf, msg->data, msg->size);
    size_t len = msg->size;

    free(msg->data);
    free(msg);
    return len;
}

int touchcontroller_ios_send(const void* buf, int len) {
    if (queue == NULL) {
        return -1;
    }

    message_t* msg = malloc(sizeof(message_t));
    if (msg == NULL) {
        return 1;
    }
    msg->size = len;
    msg->data = malloc(len);
    if (msg->data == NULL) {
        free(msg);
        return 1;
    }

    memcpy(msg->data, buf, len);

    pthread_mutex_lock(&queue->read_mutex);
    int ret = ring_buffer_enqueue(queue->read_buffer, msg);
    pthread_mutex_unlock(&queue->read_mutex);
    if (ret != 0) {
        free(msg->data);
        free(msg);
        return 1;
    }
    return 0;
}
