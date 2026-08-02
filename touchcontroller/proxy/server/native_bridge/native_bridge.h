#ifndef TOUCHCONTROLLER_NATIVE_BRIDGE_H
#define TOUCHCONTROLLER_NATIVE_BRIDGE_H

#include <jni.h>

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_native_Interface_init(JNIEnv* env,
                                                                                                 jclass clazz,
                                                                                                 jlong art_jvm_ptr,
                                                                                                 jlong art_application);

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_native_Interface_send(JNIEnv* env,
                                                                                                 jclass clazz,
                                                                                                 jbyteArray data,
                                                                                                 jint len);

JNIEXPORT jint JNICALL Java_top_fifthlight_touchcontroller_common_platform_native_Interface_poll(JNIEnv* env,
                                                                                                 jclass clazz,
                                                                                                 jbyteArray buf);

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_native_Interface_stop(JNIEnv* env,
                                                                                                 jclass clazz);

#endif
