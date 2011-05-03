LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
    android_net_wimax_WimaxCommonAPI.cpp

LOCAL_SHARED_LIBRARIES := \
    libhardware_legacy \
    liblog \
    libnativehelper

LOCAL_C_INCLUDES := \
    $(call include-path-for, libhardware_legacy)/hardware_legacy \
    frameworks/base/include \
    system/core/include

LOCAL_PRELINK_MODULE := false

LOCAL_NDK_VERSION := 4
LOCAL_SDK_VERSION := 8

LOCAL_MODULE := libwimax_jni

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)

