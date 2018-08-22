#include <android/log.h>
#include <pthread.h>

#include "qstream_callback.h"

extern QStreamHandle* g_native_handle;

int media_VideoNewIdrFrame(SessionHandle session_handle) {
    LOGD("=> %s()\n", __func__);
    usleep(5000);
    return 0;
}

int media_VideoFrameRateControl(SessionHandle session_handle, int frame_fps) {
    LOGD("=> %s(fps = %d)\n", __func__, frame_fps);
    usleep(5000);
    return 0;
}

int media_VideoBitRateControl(SessionHandle session_handle, int bit_rate_per_sec) {
    LOGD("=> %s(bitrate = %d)\n", __func__, bit_rate_per_sec);
    return 0;
}

int media_SipSendIdrRequest(SessionHandle session_handle) {
    LOGD("=> %s()\n", __func__);
    usleep(5000);
    return 0;
}

int media_VideoPlaceholderNotify(SessionHandle session_handle, int flag) {
    LOGD("=> %s(%d)\n", __func__, flag);
    usleep(5000);
    return 0;
}

int media_decode_frame(SessionHandle session_handle, const unsigned char* frame_data, const int size_byte, const uint32_t timestamp) {
    LOGD("%s(%p,%p,%d,%u) \n",__func__,session_handle, frame_data,size_byte, timestamp);
    usleep(8000);
    return 0;
}