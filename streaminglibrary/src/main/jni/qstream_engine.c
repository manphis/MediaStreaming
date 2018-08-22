//
// Created by eason on 2018/8/20.
//

#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <pthread.h>

#include "qstream_engine.h"
#include "qstream_callback.h"

static void detach_current_thread (void *env);
static jboolean native_class_init (JNIEnv* env, jclass klass);
static jint stream_start(JNIEnv* env, jobject thiz, jobject media_para);
static jint stream_stop(JNIEnv* env, jobject thiz);

static void audio_session_create(JNIEnv * env, SessionHandle* psession_handle, jclass java_audio_para_class, jobject audio_para);
static void video_session_create(JNIEnv * env, SessionHandle* psession_handle, jclass java_video_para_class, jobject video_para);
static void set_video_stream_para(JNIEnv * env, Stream_Para_t* para, jclass java_video_para_class, jobject video_para);
static void media_set_media_callback(MediaEngineCallback* cb);


static JavaVM *java_vm;
static pthread_key_t current_jni_env;
static JNINativeMethod native_methods[] = {
  {"nativeClassInit", "()Z", (void *) native_class_init},
  {"nativeStreamStart", "(Lwork/eason/streaminglibrary/MediaParameters;)I", (void *) stream_start},
  {"nativeStreamStop", "(I)I", (void *) stream_stop}
};


QStreamHandle* g_native_handle = NULL;


jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = NULL;

  java_vm = vm;

  if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
    __android_log_print (ANDROID_LOG_ERROR, "qstream_engine", "Could not retrieve JNIEnv");
    return 0;
  }
  jclass klass = (*env)->FindClass (env, "work/eason/streaminglibrary/StreamEngine");
  (*env)->RegisterNatives (env, klass, native_methods, sizeof(native_methods)/sizeof(native_methods[0]));

  pthread_key_create (&current_jni_env, detach_current_thread);

  return JNI_VERSION_1_4;
}

static jint stream_start(JNIEnv* env, jobject thiz, jobject media_para) {
    int result = -1;
    SessionHandle video_session_handle, audio_session_handle;
    QStreamHandle* native_handle = NULL;

    native_handle = (QStreamHandle*)malloc(sizeof(QStreamHandle));
    if (NULL == native_handle) {
        LOGE("stream_start malloc native handle failure");
        return -1;
    }

    session_mgr_construct();

    jclass java_media_para_class, java_audio_para_class, java_video_para_class;
    jfieldID audio_para_field, video_para_field;

    java_media_para_class = (*env)->FindClass(env, "work/eason/streaminglibrary/MediaParameters");
    if (java_media_para_class == NULL) {
        LOGE("Error on FindClass work/eason/streaminglibrary/MediaParameters");
        return result;
    }
    audio_para_field = (*env)->GetFieldID(env, java_media_para_class, "audioPara", "Lwork/eason/streaminglibrary/AudioParameters;");
    jobject audio_para = (*env)->GetObjectField(env, media_para, audio_para_field);
    java_audio_para_class = (*env)->FindClass(env, "work/eason/streaminglibrary/AudioParameters");
    if (java_audio_para_class == NULL) {
        LOGE("Error on FindClass work/eason/streaminglibrary/AudioParameters");
        return result;
    }

    video_para_field = (*env)->GetFieldID(env, java_media_para_class, "videoPara", "Lwork/eason/streaminglibrary/VideoParameters;");
    jobject video_para = (*env)->GetObjectField(env, media_para, video_para_field);
    java_video_para_class = (*env)->FindClass(env, "work/eason/streaminglibrary/VideoParameters");
    if (java_audio_para_class == NULL) {
        LOGE("Error on FindClass work/eason/streaminglibrary/VideoParameters");
        return result;
    }

    audio_session_create(env, &audio_session_handle, java_audio_para_class, audio_para);
    video_session_create(env, &video_session_handle, java_video_para_class, video_para);

    native_handle->_audio_session_handle = audio_session_handle;
    native_handle->_video_session_handle = video_session_handle;
    g_native_handle = native_handle;

    session_start(video_session_handle);
    session_start(audio_session_handle);

    return (jint)native_handle;
}

static jint stream_stop(JNIEnv* env, jobject thiz) {
    SessionHandle video_session_handle = g_native_handle->_video_session_handle;
    SessionHandle audio_session_handle = g_native_handle->_audio_session_handle;

    session_stop(video_session_handle);
    session_stop(audio_session_handle);
    session_lipsync_unbind(audio_session_handle, video_session_handle );
    session_destruct(video_session_handle);
    session_destruct(audio_session_handle);
    session_mgr_destruct();

    return 0;
}

//==================================================================================================

static void audio_session_create(JNIEnv * env, SessionHandle* psession_handle, jclass java_audio_para_class, jobject audio_para) {return;}

static void video_session_create(JNIEnv * env, SessionHandle* psession_handle, jclass java_video_para_class, jobject video_para) {
    int stype = SE_VIDEO_STREAM;
	Stream_Para_t stream_para;
    MediaEngineCallback media_engine_cb;

    session_construct(1, stype, psession_handle);

    set_video_stream_para(env, &stream_para, java_video_para_class, video_para);

    session_update(*psession_handle, stype, &stream_para);

    media_set_media_callback(&media_engine_cb);

    register_decode_frame_callback(*psession_handle, media_decode_frame);
    register_media_engine_callback(*psession_handle, &media_engine_cb);
}

static void set_video_stream_para(JNIEnv * env, Stream_Para_t* para, jclass java_video_para_class, jobject video_para) {
    jstring local_java_string;
    const char* local_str = NULL;

    jfieldID video_fec_ctrl_field = (*env)->GetFieldID(env, java_video_para_class, "fec_ctrl", "I");
    jfieldID video_h264_rate_ctrl_field = (*env)->GetFieldID(env, java_video_para_class, "h264_rate_ctrl", "I");

    jfieldID video_host_ipaddr_field = (*env)->GetFieldID(env, java_video_para_class, "host_ipaddr", "Ljava/lang/String;");
    jfieldID video_host_port_field = (*env)->GetFieldID(env, java_video_para_class, "host_port", "I");
    jfieldID video_listen_port_field = (*env)->GetFieldID(env, java_video_para_class, "listen_port", "I");
    jfieldID video_tos_field = (*env)->GetFieldID(env, java_video_para_class, "tos", "I");

    jfieldID video_codec_payload_number_field = (*env)->GetFieldID(env, java_video_para_class, "codec_payload_type", "I");
    jfieldID video_codec_payload_name_field = (*env)->GetFieldID(env, java_video_para_class, "codec_payload_name", "Ljava/lang/String;");
    jfieldID video_codec_sample_rate_field = (*env)->GetFieldID(env, java_video_para_class, "codec_sample_rate", "I");
    jfieldID video_width_field = (*env)->GetFieldID(env, java_video_para_class, "width", "I");
    jfieldID video_height_field = (*env)->GetFieldID(env, java_video_para_class, "height", "I");
    jfieldID video_fps_field = (*env)->GetFieldID(env, java_video_para_class, "fps", "I");
    jfieldID video_h264_gop_field = (*env)->GetFieldID(env, java_video_para_class, "h264_gop", "I");
    jfieldID video_fec_sendPayloadType_field = (*env)->GetFieldID(env, java_video_para_class, "fec_sendPayloadType", "I");
    jfieldID video_fec_receivePayloadType_field = (*env)->GetFieldID(env, java_video_para_class, "fec_receivePayloadType", "I");
    jfieldID video_h264_max_kbit_rate_field = (*env)->GetFieldID(env, java_video_para_class, "h264_max_kbit_rate", "I");
    jfieldID video_h264_min_kbit_rate_field = (*env)->GetFieldID(env, java_video_para_class, "h264_min_kbit_rate", "I");
    jfieldID video_rtcp_pli_ctrl_field = (*env)->GetFieldID(env, java_video_para_class, "rtcp_pli_ctrl", "I");
    jfieldID video_rtcp_fir_ctrl_field = (*env)->GetFieldID(env, java_video_para_class, "rtcp_fir_ctrl", "I");
    jfieldID video_rtcp_ctrl_field = (*env)->GetFieldID(env, java_video_para_class, "rtcp_ctrl", "I");

    /* Socket parameters */
    local_java_string = (jstring) (*env)->GetObjectField(env, video_para, video_host_ipaddr_field);
    local_str = (*env)->GetStringUTFChars(env, local_java_string, NULL);
    if (local_str != NULL) {
        strncpy((char*)para->ip_v4, local_str ,sizeof(para->ip_v4));
        (*env)->ReleaseStringUTFChars(env, local_java_string, local_str);
    }
//    strncpy((char*)para->ip_v4, host_ip ,sizeof(para->ip_v4));
    para->rtp_tx_port   = (*env)->GetIntField(env, video_para, video_host_port_field);
    para->rtp_rx_port   = (*env)->GetIntField(env, video_para, video_listen_port_field);
    para->tos           = (*env)->GetIntField(env, video_para, video_tos_field);
//    para->mtu           = TEST_MTU;

    /* Codec parameters */
    para->payload_type = (*env)->GetIntField(env, video_para, video_codec_payload_number_field);
    para->codec_clock_rate_hz = (*env)->GetIntField(env, video_para, video_codec_sample_rate_field);
    para->codec_fps = (*env)->GetIntField(env, video_para, video_fps_field);

    /* FEC parameters */
    para->fec_support = (*env)->GetIntField(env, video_para, video_fec_ctrl_field);
    para->fec_tx_payload_type = (*env)->GetIntField(env, video_para, video_fec_sendPayloadType_field);
    para->fec_rx_payload_type = (*env)->GetIntField(env, video_para, video_fec_receivePayloadType_field);

    /* RFC4588 parameters */
    para->rfc4588_support = 0;
    para->rfc4588_tx_payload_type = 122;
    para->rfc4588_rx_payload_type = 122;
    para->rfc4588_rtx_time_msec = 500;

    /* Instantaneous Decoding Refresh frame request */
    para->rfc4585_rtcp_fb_pli_support = (*env)->GetIntField(env, video_para, video_rtcp_pli_ctrl_field);
    para->rfc5104_rtcp_fb_fir_support = (*env)->GetIntField(env, video_para, video_rtcp_fir_ctrl_field);

    /* private parameters */
    para->product_type.local = 5;//PX2_PRODUCT
    para->product_type.remote = 5;//PX2_PRODUCT
    para->auto_bandwidth_support = (*env)->GetIntField(env, video_para, video_h264_rate_ctrl_field);
    para->private_header_support = 1;

    /* bit rate control parameters, only used by video stream */
    para->max_outgoing_kbps = (*env)->GetIntField(env, video_para, video_h264_max_kbit_rate_field);
    para->min_outgoing_kbps = (*env)->GetIntField(env, video_para, video_h264_min_kbit_rate_field);
    para->initial_outgoing_kbps = 500;

    /* SRTP parameters */
    para->srtp_support = 0;
    strncpy(para->srtp_tx_key,"1234567812345678123456781234567812345678", SRTP_KEY_LENGTH+1 );
    strncpy(para->srtp_rx_key,"1234567812345678123456781234567812345678", SRTP_KEY_LENGTH+1 );

/*
//	strcpy(table->videoPara.codec_payload_name, "H264");

    table->videoPara.width = (*env)->GetIntField(env, video_para, video_width_field);
    table->videoPara.height = (*env)->GetIntField(env, video_para, video_height_field);

    table->videoPara.h264_gop = (*env)->GetIntField(env, video_para, video_h264_gop_field);

    local_java_string = (jstring) (*env)->GetObjectField(env, video_para, video_codec_payload_name_field);
    local_str = (*env)->GetStringUTFChars(env, local_java_string, NULL);
    if (local_str != NULL) {
        strcpy(table->videoPara.codec_payload_name, local_str);
        (*env)->ReleaseStringUTFChars(env, local_java_string, local_str);
    }
*/
}

static void media_set_media_callback(MediaEngineCallback* cb) {
    memset(cb,0,sizeof(MediaEngineCallback));
    cb->video_new_idr_frame_cb    = media_VideoNewIdrFrame;
    cb->video_frame_rate_control_cb = media_VideoFrameRateControl;
    cb->video_bit_rate_control_cb   = media_VideoBitRateControl;
    cb->sip_send_idr_request_cb     = media_SipSendIdrRequest;
    cb->video_placeholder_notify_cb = media_VideoPlaceholderNotify;
}


static jboolean native_class_init(JNIEnv* env, jclass klass) {
    __android_log_print (ANDROID_LOG_ERROR, "qstream_engine", "native_class_init");

    return JNI_TRUE;
}

/* Unregister this thread from the VM */
static void detach_current_thread (void *env) {
  (*java_vm)->DetachCurrentThread (java_vm);
}