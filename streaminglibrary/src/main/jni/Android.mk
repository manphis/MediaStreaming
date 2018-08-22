LOCAL_PATH := $(call my-dir)

#common_cflags := \
	-Wall -ffast-math -ffunction-sections -funwind-tables -fno-short-enums \
	-mfloat-abi=softfp -mfpu=neon \
	-fstrict-aliasing -fno-rtti -fno-exceptions \
	-D__ARM_NEON__ -DIPC_UNIX_IMPLEMENT

#common_cflags += -DCONFIG_HAVE_STREAM_ENGINE -D__ANDROID__

include $(CLEAR_VARS)
LOCAL_MODULE := libStreamEngine
LOCAL_SRC_FILES := StreamEngine/libqStream.a
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := libqstream_engine
LOCAL_C_INCLUDES := $(LOCAL_PATH)/StreamEngine \
					$(LOCAL_PATH)/include
LOCAL_SRC_FILES := qstream_engine.c \
					qstream_callback.c
#LOCAL_SHARED_LIBRARIES := gstreamer_android
#LOCAL_CFLAGS := $(common_cflags)
LOCAL_LDLIBS := -llog
#LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -lc -lm -lz -ldl -llog
LOCAL_STATIC_LIBRARIES := libStreamEngine

include $(BUILD_SHARED_LIBRARY)