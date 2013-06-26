#include <stdio.h>

#include <jni.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>
//#include <JNIHelp.h>
#include <android/log.h>

#include "encodedetect/autodetect.h"

#define LOG_TAG "Encodedetect"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)



jstring possibleEncoding(JNIEnv* env, jobject thiz, jbyteArray jbArr) {
    // get the test string.
    char *chArr = (char*)env->GetByteArrayElements(jbArr,0);
    int nArrLen = env->GetArrayLength(jbArr);

    char *szStrBuf =(char*)malloc(nArrLen + 1);
    memset(szStrBuf, 0, nArrLen + 1);
    memcpy(szStrBuf, chArr, nArrLen);
    env->ReleaseByteArrayElements(jbArr, (jbyte*)chArr, JNI_ABORT);

    // convert \0 which in the middle of string.
    char *ptr = szStrBuf + strlen(szStrBuf) + 1;
    while ((size_t)(ptr - szStrBuf) <= nArrLen) {
            *(ptr - 1) = 0X02;
            ptr += strlen(ptr) + 1;
    }

    // detect.
    const char *enc = "UTF-8";
    int gbkcount = 0;
    if (CharacterEncoder::is_ascii(szStrBuf)) {
        enc = "ASCII";
    } else if(CharacterEncoder::isUTF8(szStrBuf)) {
        enc = "UTF-8";
    } else if (CharacterEncoder::is_gbk_code(szStrBuf, &gbkcount)) {
        enc = "gbk";
    } else {
        uint32_t encoding = possibleEncodings(szStrBuf);
        // LOGD("20, szStrBuf len = %d, %d, encoding = %u",
        //     strlen(szStrBuf), nArrLen, encoding);
        switch (encoding) {
        case kEncodingShiftJIS:
            enc = "shift-jis";
            break;
        case kEncodingGBK:
            enc = "gbk";
            break;
        case kEncodingBig5:
            enc = "Big5";
            break;
        case kEncodingEUCKR:
            enc = "EUC-KR";
            break;
        }
    }

    // return the result.
    jstring rtn;
    rtn = (env)->NewStringUTF(enc);
    return rtn;
}

static JNINativeMethod nativeMethods[] = {
    {"native_possibleEncoding", "([B)Ljava/lang/String;", (void*)possibleEncoding}
};


int registerNativeMethods_Encode(JNIEnv* env) {
    int result = -1;
    jclass clazz = env->FindClass("com/ivy/appshare/engin/im/simpleimp/util/EncodeDetector");

    if (NULL != clazz) {
        if (env->RegisterNatives(clazz, nativeMethods, sizeof(nativeMethods)
                / sizeof(nativeMethods[0])) == JNI_OK) {
            result = 0;
        }
    }

    return result;
}
