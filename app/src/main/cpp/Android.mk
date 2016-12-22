
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

CC_SRC_FILES := \
./google/protobuf/stubs/common.cc \
./google/protobuf/stubs/once.cc \
./google/protobuf/stubs/structurally_valid.cc \
./google/protobuf/stubs/strutil.cc \
./google/protobuf/stubs/stringprintf.cc \
./google/protobuf/stubs/substitute.cc \
./google/protobuf/io/coded_stream.cc \
./google/protobuf/io/printer.cc \
./google/protobuf/io/tokenizer.cc \
./google/protobuf/io/zero_copy_stream.cc \
./google/protobuf/io/zero_copy_stream_impl.cc \
./google/protobuf/io/zero_copy_stream_impl_lite.cc \
./google/protobuf/io/strtod.cc \
./google/protobuf/descriptor.cc \
./google/protobuf/descriptor.pb.cc \
./google/protobuf/descriptor_database.cc \
./google/protobuf/dynamic_message.cc \
./google/protobuf/extension_set.cc \
./google/protobuf/extension_set_heavy.cc \
./google/protobuf/generated_message_reflection.cc \
./google/protobuf/generated_message_util.cc \
./google/protobuf/message.cc \
./google/protobuf/message_lite.cc \
./google/protobuf/reflection_ops.cc \
./google/protobuf/repeated_field.cc \
./google/protobuf/service.cc \
./google/protobuf/text_format.cc \
./google/protobuf/unknown_field_set.cc \
./google/protobuf/wire_format.cc \
./google/protobuf/wire_format_lite.cc \
./NegiIME.cpp \
./NDK.cpp \
./message.pb.cc \
./conv/compile.cpp \

# C++ full library (gnustl)
# =======================================================
#include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_CPP_FEATURES := exceptions

LOCAL_CPP_EXTENSION := .cc .cpp

LOCAL_SRC_FILES := $(CC_SRC_FILES)

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/android \
    $(LOCAL_PATH)/src

LOCAL_SHARED_LIBRARIES := \
    libz libcutils libutils 

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog 

LOCAL_CPPFLAGS += -std=gnu++11

LOCAL_CFLAGS := -frtti

LOCAL_SDK_VERSION := 19

LOCAL_NDK_STL_VARIANT := gnustl_shared

LOCAL_MODULE := NegiIME

include $(BUILD_SHARED_LIBRARY)
