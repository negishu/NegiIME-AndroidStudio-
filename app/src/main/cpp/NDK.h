#ifndef _Included_negi_android_NDK_NDK
#define _Included_negi_android_NDK_NDK
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jlong JNICALL Java_negi_android_NDK_NDK_initialize(JNIEnv * env, jclass thiz, jobject target);
JNIEXPORT jlong JNICALL Java_negi_android_NDK_NDK_communicate(JNIEnv * env, jclass thiz, jobject target, jlong gRef, jobject arrayByteIn, jlong inSize, jobject arrayByteOut, jlong outSize);
#ifdef __cplusplus
}
#endif
#endif
