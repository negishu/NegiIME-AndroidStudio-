# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

include_directories(
        src/main/cpp/
        src/main/cpp/google/protobuf/
        src/main/cpp/google/protobuf/stubs/
        src/main/cpp/google/protobuf/io/
        src/main/cpp/conv/
)
include_directories(${LOCAL_C_INCLUDES})

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/google/protobuf/stubs/common.cc
             src/main/cpp/google/protobuf/stubs/once.cc
             src/main/cpp/google/protobuf/stubs/structurally_valid.cc
             src/main/cpp/google/protobuf/stubs/strutil.cc
             src/main/cpp/google/protobuf/stubs/stringprintf.cc
             src/main/cpp/google/protobuf/stubs/substitute.cc
             src/main/cpp/google/protobuf/io/coded_stream.cc
             src/main/cpp/google/protobuf/io/printer.cc
             src/main/cpp/google/protobuf/io/tokenizer.cc
             src/main/cpp/google/protobuf/io/zero_copy_stream.cc
             src/main/cpp/google/protobuf/io/zero_copy_stream_impl.cc
             src/main/cpp/google/protobuf/io/zero_copy_stream_impl_lite.cc
             src/main/cpp/google/protobuf/io/strtod.cc
             src/main/cpp/google/protobuf/descriptor.cc
             src/main/cpp/google/protobuf/descriptor.pb.cc
             src/main/cpp/google/protobuf/descriptor_database.cc
             src/main/cpp/google/protobuf/dynamic_message.cc
             src/main/cpp/google/protobuf/extension_set.cc
             src/main/cpp/google/protobuf/extension_set_heavy.cc
             src/main/cpp/google/protobuf/generated_message_reflection.cc
             src/main/cpp/google/protobuf/generated_message_util.cc
             src/main/cpp/google/protobuf/message.cc
             src/main/cpp/google/protobuf/message_lite.cc
             src/main/cpp/google/protobuf/reflection_ops.cc
             src/main/cpp/google/protobuf/repeated_field.cc
             src/main/cpp/google/protobuf/service.cc
             src/main/cpp/google/protobuf/text_format.cc
             src/main/cpp/google/protobuf/unknown_field_set.cc
             src/main/cpp/google/protobuf/wire_format.cc
             src/main/cpp/google/protobuf/wire_format_lite.cc
             src/main/cpp/NegiIME.cpp
             src/main/cpp/NDK.cpp
             src/main/cpp/message.pb.cc
             src/main/cpp/conv/compile.cpp
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
