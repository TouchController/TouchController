#ifndef TOUCHCONTROLLER_IOS_H
#define TOUCHCONTROLLER_IOS_H

#include <jni.h>
#include <pthread.h>
#include <stdint.h>

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_init(JNIEnv* env,
                                                                                              jclass clazz);

JNIEXPORT jint JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_receive(JNIEnv* env,
                                                                                                 jclass clazz,
                                                                                                 jbyteArray buffer);

JNIEXPORT void JNICALL Java_top_fifthlight_touchcontroller_common_platform_ios_Transport_send(JNIEnv* env, jclass clazz,
                                                                                              jbyteArray buffer,
                                                                                              jint off, jint len);

// Make sure you have a buffer larger or equal than 255 bytes
int touchcontroller_ios_receive(void* buf);

int touchcontroller_ios_send(const void* buf, int len);

#endif
