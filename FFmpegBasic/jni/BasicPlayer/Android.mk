LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libbasicplayer
LOCAL_SRC_FILES := BasicPlayer.c Interface.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../ffmpeg/ \
                    $(LOCAL_PATH)/../ffmpeg/libavcodec \
                    $(LOCAL_PATH)/../ffmpeg/libavformat \
                    $(LOCAL_PATH)/../ffmpeg/libswscale

LOCAL_CFLAGS := -DHAVE_NEON=1
LOCAL_STATIC_LIBRARIES := libavformat libavcodec libswscale libavutil cpufeatures

LOCAL_LDLIBS := -lz -ljnigraphics

LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)
LOCAL_DISABLE_FATAL_LINKER_WARNINGS=true
