#include <jni.h>
#include <iostream>
#include <string>
#include <string.h>
#include <stdlib.h>

#include <android/log.h>

#include "NDK.h"

#define LOG_TAG "NDK"
#define LOGD(...) (__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGV(...) (__android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__))
#define LOGI(...) (__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))
#define LOGE(...) (__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
#define LOGW(...) (__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))

#include "NegiIME.h"

struct ContextCallback
{
	JNIEnv* env;
	JNIEnv* backEnv;
	jobject obj;
	jmethodID doCallback;
};
static ContextCallback g_sContextCallback = { 0, };

static NegiIME* GetNegiIME()
{
	static NegiIME *mNegiIME = NULL;

	if (mNegiIME == NULL) {

		mNegiIME = new NegiIME();
	}

	return mNegiIME;
}

JNIEXPORT jlong JNICALL Java_negi_android_NDK_NDK_initialize(JNIEnv * env, jclass thiz, jobject target)
{
	NegiIME *mNegiIME = GetNegiIME();
    LOGV(">Java_negi_android_NDK_NDK_initialize()");
    jclass clazz = env->GetObjectClass(target);
    g_sContextCallback.env = env;
    g_sContextCallback.doCallback = env->GetMethodID(clazz, "callback","(I)I");
    g_sContextCallback.obj = env->NewGlobalRef(target);
    LOGV("<Java_negi_android_NDK_NDK_initialize()");

	return (jlong)mNegiIME;
}

JNIEXPORT jlong JNICALL Java_negi_android_NDK_NDK_communicate(JNIEnv * env, jclass thiz, jobject target, jlong gRef, jobject arrayByteIn, jlong inSize, jobject arrayByteOut, jlong outSize)
{
	NegiIME *gNegiIME = (NegiIME*)gRef;

	if (gNegiIME == 0) {
		LOGI("gNegiIME error");
		return 0;
	}

	char* newInArray  = reinterpret_cast<char*>(env->GetDirectBufferAddress(arrayByteIn));
	char* newOutArray = reinterpret_cast<char*>(env->GetDirectBufferAddress(arrayByteOut));

	int nSize = gNegiIME->ProcessData(newInArray, inSize, newOutArray, outSize);

	env->CallIntMethod(g_sContextCallback.obj, g_sContextCallback.doCallback, (int)nSize);

	return nSize;
}
