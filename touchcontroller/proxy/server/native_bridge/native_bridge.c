#include "native_bridge.h"

#include <android/log.h>
#include <jni.h>
#include <pthread.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include "touchcontroller/proxy/server/util/blockingqueue/blocking_queue.h"
#include "touchcontroller/proxy/server/util/ringbuffer/ring_buffer.h"

#define TAG "TouchControllerBridge"
#define QUEUE_CAPACITY 4096

static void throw_exception(JNIEnv* env, const char* msg) {
    jclass ex_class = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (ex_class) {
        (*env)->ThrowNew(env, ex_class, msg);
    }
}

typedef struct message {
    size_t len;  // length = 0 for close
    int is_error;
    char buf[];
} message_t;

static blocking_queue_t* mod_to_launcher_queue = NULL;
static ring_buffer_t* launcher_to_mod_queue = NULL;
static pthread_mutex_t launcher_to_mod_lock = PTHREAD_MUTEX_INITIALIZER;

static void launcher_send_event(JNIEnv* env, jclass clazz, jbyteArray data) {
    // Send data to launcher_to_mod_queue
    jsize len = (*env)->GetArrayLength(env, data);
    message_t* message = (message_t*)malloc(sizeof(message_t) + (size_t)len);
    if (message == NULL) {
        return;
    }
    message->is_error = 0;
    message->len = (size_t)len;
    (*env)->GetByteArrayRegion(env, data, 0, len, (jbyte*)message->buf);

    pthread_mutex_lock(&launcher_to_mod_lock);
    ring_buffer_t* queue = launcher_to_mod_queue;
    if (queue == NULL || ring_buffer_try_enqueue(queue, message) != 0) {
        pthread_mutex_unlock(&launcher_to_mod_lock);
        free(message);
        return;
    }
    pthread_mutex_unlock(&launcher_to_mod_lock);
}

static void send_error_event(char* msg) {
    __android_log_write(ANDROID_LOG_FATAL, TAG, msg);

    size_t len = strlen(msg);
    message_t* message = (message_t*)malloc(sizeof(message_t) + (size_t)len);
    if (message == NULL) {
        return;
    }
    message->is_error = 1;
    message->len = (size_t)len;
    memcpy(message->buf, msg, len);

    pthread_mutex_lock(&launcher_to_mod_lock);
    ring_buffer_t* queue = launcher_to_mod_queue;
    if (queue == NULL || ring_buffer_try_enqueue(queue, message) != 0) {
        pthread_mutex_unlock(&launcher_to_mod_lock);
        free(message);
        return;
    }
    pthread_mutex_unlock(&launcher_to_mod_lock);
}

typedef struct launcher_arg {
    JavaVM* vm;
    jobject application;
} launcher_arg_t;

static void* launcher_thread(void* arg) {
    launcher_arg_t* launcher_arg = (launcher_arg_t*)arg;
    JavaVM* launcher_vm = launcher_arg->vm;
    jobject application = launcher_arg->application;
    free(launcher_arg);

    void* venv = NULL;
    (*launcher_vm)->AttachCurrentThread(launcher_vm, &venv, NULL);
    JNIEnv* env = (JNIEnv*)venv;

    // Get Application's ClassLoader in launcher VM
    __android_log_print(ANDROID_LOG_INFO, TAG, "Getting Application's ClassLoader in launcher side");
    jclass context_class = (*env)->FindClass(env, "android/content/Context");
    if (!context_class) {
        send_error_event("Failed to get Context class in launcher VM");
        return NULL;
    }
    jmethodID get_classloader_method =
        (*env)->GetMethodID(env, context_class, "getClassLoader", "()Ljava/lang/ClassLoader;");
    if ((*env)->ExceptionCheck(env) || !get_classloader_method) {
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
        }
        send_error_event("Failed to get loadClass method in launcher VM");
        return NULL;
    }
    jobject classloader = (*env)->CallObjectMethod(env, application, get_classloader_method);
    if ((*env)->ExceptionCheck(env) || !classloader) {
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
        }
        send_error_event("Failed to call Application#getClassLoader method in launcher VM");
        return NULL;
    }

    // Get ClassLoader#loadClass method in launcher VM
    __android_log_print(ANDROID_LOG_INFO, TAG, "Getting ClassLoader#loadClass method in launcher side");
    jclass classloader_class = (*env)->FindClass(env, "java/lang/ClassLoader");
    if (!context_class) {
        send_error_event("Failed to get ClassLoader class in launcher VM");
        return NULL;
    }
    jmethodID load_class_method =
        (*env)->GetMethodID(env, classloader_class, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    if ((*env)->ExceptionCheck(env) || !load_class_method) {
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
        }
        send_error_event("Failed to get loadClass method in launcher VM");
        return NULL;
    }

    // Register launcher_send_event to launcher side NativeInterface
    __android_log_print(ANDROID_LOG_INFO, TAG, "Registering NativeInterface#sendEvent in launcher side");
    jstring class_name =
        (*env)->NewStringUTF(env, "top.fifthlight.touchcontroller.proxy.client.native.NativeInterface");
    jclass interface_class = (jclass)(*env)->CallObjectMethod(env, classloader, load_class_method, class_name);
    if ((*env)->ExceptionCheck(env) || !interface_class) {
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
        }
        send_error_event("Failed to get NativeInterface class in launcher VM due to exception");
        return NULL;
    }
    __android_log_print(ANDROID_LOG_INFO, TAG, "Got NativeInterface class in launcher VM");
    JNINativeMethod methods[1] = {{"sendEvent", "([B)V", launcher_send_event}};
    int code = (*env)->RegisterNatives(env, interface_class, methods, sizeof(methods) / sizeof(JNINativeMethod));
    if (code != JNI_OK) {
        send_error_event("Failed register methods to NativeInterface in launcher VM");
        return NULL;
    }

    // Call initNative method
    __android_log_print(ANDROID_LOG_INFO, TAG, "Calling NativeInterface#initNative in launcher side");
    jmethodID init_method = (*env)->GetStaticMethodID(env, interface_class, "initNative", "()Z");
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        send_error_event("Failed to get initNative method in launcher VM");
        return NULL;
    }
    (*env)->CallStaticBooleanMethod(env, interface_class, init_method);
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        send_error_event("Failed to call NativeInterface#initNative on launcher side");
        return NULL;
    }

    // Get receiveEvent method
    jmethodID receive_method = (*env)->GetStaticMethodID(env, interface_class, "receiveEvent", "([B)V");
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        send_error_event("Failed to get receiveEvent method in launcher VM");
        return NULL;
    }

    // poll events from mod thread, and send it to launcher
    __android_log_print(ANDROID_LOG_INFO, TAG, "Polling events");
    message_t* message = blocking_queue_pop(mod_to_launcher_queue);
    while (message->len > 0) {
        // Call receiveEvent method
        jbyteArray array = (*env)->NewByteArray(env, (jsize)message->len);
        if (array == NULL) {
            (*env)->ExceptionClear(env);
            free(message);
            message = blocking_queue_pop(mod_to_launcher_queue);
            continue;
        }
        (*env)->SetByteArrayRegion(env, array, 0, (jsize)message->len, (jbyte*)message->buf);
        (*env)->CallStaticVoidMethod(env, interface_class, receive_method, array);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
        }
        (*env)->DeleteLocalRef(env, array);

        free(message);
        message = blocking_queue_pop(mod_to_launcher_queue);
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "Stopping thread");
    free(message);

    (*launcher_vm)->DetachCurrentThread(launcher_vm);
    blocking_queue_destroy(mod_to_launcher_queue);
    mod_to_launcher_queue = NULL;
    return NULL;
}

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_native_Interface_init(
    JNIEnv* env, jclass clazz, jlong art_jvm_ptr, jlong art_application) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "Initializing native bridge");

    JavaVM* launcher_vm = (JavaVM*)(intptr_t)art_jvm_ptr;
    if (!launcher_vm) {
        throw_exception(env, "ART JavaVM pointer is null");
        return;
    }
    if (!art_application) {
        throw_exception(env, "ART Application pointer is null");
        return;
    }

    // JNI has limitations: one native thread cannot be attached simultaneously to two JavaVM
    // So we start a thread attached to launcher and post events to it to solve this problem.
    launcher_to_mod_queue = ring_buffer_alloc(QUEUE_CAPACITY);
    if (launcher_to_mod_queue == NULL) {
        throw_exception(env, "Failed to allocate launcher -> mod queue");
        return;
    }
    mod_to_launcher_queue = blocking_queue_create(QUEUE_CAPACITY);
    if (mod_to_launcher_queue == NULL) {
        ring_buffer_free(launcher_to_mod_queue);
        launcher_to_mod_queue = NULL;
        throw_exception(env, "Failed to allocate mod -> launcher queue");
        return;
    }

    launcher_arg_t* launcher_arg = malloc(sizeof(launcher_arg_t));
    if (launcher_arg == NULL) {
        ring_buffer_free(launcher_to_mod_queue);
        launcher_to_mod_queue = NULL;
        blocking_queue_destroy(mod_to_launcher_queue);
        mod_to_launcher_queue = NULL;
        throw_exception(env, "Failed to allocate launcher thread arg");
        return;
    }
    launcher_arg->vm = launcher_vm;
    launcher_arg->application = (jobject)(intptr_t)art_application;

    pthread_t thread;
    int result = pthread_create(&thread, NULL, launcher_thread, launcher_arg);
    if (result != 0) {
        free(launcher_arg);
        ring_buffer_free(launcher_to_mod_queue);
        launcher_to_mod_queue = NULL;
        blocking_queue_destroy(mod_to_launcher_queue);
        mod_to_launcher_queue = NULL;
        throw_exception(env, "Failed to create launcher bridge thread");
        return;
    }
    pthread_detach(thread);
}

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_native_Interface_send(JNIEnv* env,
                                                                                                 jclass clazz,
                                                                                                 jbyteArray data,
                                                                                                 jint len) {
    // Send to mod_to_launcher_queue
    if (mod_to_launcher_queue == NULL) {
        return;
    }
    message_t* message = (message_t*)malloc(sizeof(message_t) + (size_t)len);
    if (message == NULL) {
        return;
    }
    message->len = (size_t)len;
    (*env)->GetByteArrayRegion(env, data, 0, len, (jbyte*)message->buf);
    if (blocking_queue_try_push(mod_to_launcher_queue, message) != 0) {
        free(message);
    }
}

JNIEXPORT jint

    JNICALL
    Java_top_fifthlight_touchcontroller_common_platform_native_Interface_poll(JNIEnv* env, jclass clazz,
                                                                              jbyteArray buf) {
    // Poll from launcher_to_mod_queue
    pthread_mutex_lock(&launcher_to_mod_lock);
    ring_buffer_t* queue = launcher_to_mod_queue;
    message_t* message = (queue != NULL) ? (message_t*)ring_buffer_dequeue(queue) : NULL;
    pthread_mutex_unlock(&launcher_to_mod_lock);
    if (message == NULL) {
        return 0;
    }
    if (message->is_error) {
        throw_exception(env, message->buf);
        return -1;
    }
    (*env)->SetByteArrayRegion(env, buf, 0, (jsize)message->len, (jbyte*)message->buf);
    jint result = (jint)message->len;
    free(message);
    return result;
}

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_native_Interface_stop(JNIEnv* env,
                                                                                                 jclass clazz) {
    __android_log_print(ANDROID_LOG_INFO, TAG, "Stopping native bridge!");

    __android_log_print(ANDROID_LOG_INFO, TAG, "Cleanup launcher -> mod queue");
    pthread_mutex_lock(&launcher_to_mod_lock);
    ring_buffer_t* queue = launcher_to_mod_queue;
    launcher_to_mod_queue = NULL;
    pthread_mutex_unlock(&launcher_to_mod_lock);
    if (queue != NULL) {
        message_t* message;
        while ((message = ring_buffer_dequeue(queue)) != NULL) {
            free(message);
        }
        ring_buffer_free(queue);
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "Cleanup mod -> launcher queue");
    if (mod_to_launcher_queue != NULL) {
        // Close mod_to_launcher_queue
        message_t* close_message = (message_t*)malloc(sizeof(message_t));
        if (close_message == NULL) {
            throw_exception(env, "Failed to allocate close message");
            return;
        }
        close_message->is_error = 0;
        close_message->len = 0;
        blocking_queue_push(mod_to_launcher_queue, close_message);
    }
}
