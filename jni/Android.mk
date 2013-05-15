LOCAL_PATH:= $(call my-dir)

###################################################################
#LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional eng
LOCAL_LDLIBS := -ldl -llog
LOCAL_SRC_FILES := \
	    global.c \
        EventHub.cpp \
        TouchInject.cpp \
        InputAdapter.cpp \
        com_jnselectronics_ime_jni_InputAdapter.cpp

LOCAL_SHARED_LIBRARIES := \
    libandroid_runtime \
    libcutils \
    libutils \
    libhardware \
    libhardware_legacy \
    libskia \
    libgui \
    libui \

LOCAL_C_INCLUDES := \
    ../../../../vmshare/64bit/jb/external/skia/include/core \
     ../../../../vmshare/64bit/jb/frameworks/base/include/androidfw
	

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE:= libjni_input_adapter

include $(BUILD_SHARED_LIBRARY)
#include $(LOCAL_PATH)/Android.mk.bk
############################################################
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := eng
LOCAL_MODULE:=libjni_console
LOCAL_LDLIBS := -ldl -llog
LOCAL_SRC_FILES:= \
  termExec.cpp
LOCAL_SHARED_LIBRARIES := \
	libutils
LOCAL_LDLIBS := -ldl -llog
LOCAL_STATIC_LIBRARIES :=
LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE)
# No special compiler flags.
LOCAL_CFLAGS +=
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)
############################################################
############################################################
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := eng
LOCAL_MODULE:=screenshot
LOCAL_SRC_FILES:= \
  SaveBitmap.c \
  com_jnselectronics_ime_jni_ScreenShot.c 

LOCAL_SHARED_LIBRARIES := \
	libutils \
	libcutils \
	libz
	
LOCAL_STATIC_LIBRARIES := libpng
LOCAL_LDLIBS := -ldl -llog
LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) 
# No special compiler flags.
LOCAL_CFLAGS +=
LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)