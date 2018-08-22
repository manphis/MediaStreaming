#ifndef __QSTREAM_CALLBACK_H_
#define __QSTREAM_CALLBACK_H_

#include "qstream_engine.h"

int media_VideoNewIdrFrame(SessionHandle session_handle);
int media_VideoFrameRateControl(SessionHandle session_handle, int frame_fps);
int media_VideoBitRateControl(SessionHandle session_handle, int bit_rate_per_sec);
int media_SipSendIdrRequest(SessionHandle session_handle);
int media_VideoPlaceholderNotify(SessionHandle session_handle, int flag);
int media_decode_frame(SessionHandle session_handle, const unsigned char* frame_data, const int size_byte, const uint32_t timestamp);


#endif