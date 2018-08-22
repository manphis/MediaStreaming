#ifndef __QSTREAM_ENGINE_H_
#define __QSTREAM_ENGINE_H_

#include "stream_engine_api.h"

#define TAG "qstream_engine_jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , TAG, __VA_ARGS__)

typedef struct _QStreamHandle QStreamHandle;
struct _QStreamHandle {
    SessionHandle _audio_session_handle;
    SessionHandle _video_session_handle;
};

#endif