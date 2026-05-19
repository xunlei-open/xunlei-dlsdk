cmake_minimum_required(VERSION 3.10)
project(xl_dl_desktop_jni)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(JNI REQUIRED)

set(XL_DL_NATIVE_DIR "" CACHE PATH "Directory containing the current platform dk native library")
if(NOT XL_DL_NATIVE_DIR)
    message(FATAL_ERROR "Set XL_DL_NATIVE_DIR to a directory containing dk.lib/libdk.dylib/libdk.so")
endif()

if(WIN32)
    set(DK_LIBRARY "${XL_DL_NATIVE_DIR}/dk.lib")
elseif(APPLE)
    set(DK_LIBRARY "${XL_DL_NATIVE_DIR}/libdk.dylib")
else()
    set(DK_LIBRARY "${XL_DL_NATIVE_DIR}/libdk.so")
endif()

add_library(jni_dk SHARED
    jni_xl_dl_sdk.cpp
)

target_include_directories(jni_dk PRIVATE
    ${JNI_INCLUDE_DIRS}
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

target_link_libraries(jni_dk PRIVATE
    ${DK_LIBRARY}
    ${JNI_LIBRARIES}
)

set_target_properties(jni_dk PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
    RUNTIME_OUTPUT_DIRECTORY_RELEASE "${CMAKE_BINARY_DIR}/lib"
    LIBRARY_OUTPUT_DIRECTORY_RELEASE "${CMAKE_BINARY_DIR}/lib"
    RUNTIME_OUTPUT_DIRECTORY_DEBUG "${CMAKE_BINARY_DIR}/lib"
    LIBRARY_OUTPUT_DIRECTORY_DEBUG "${CMAKE_BINARY_DIR}/lib"
)

if(APPLE)
    set_target_properties(jni_dk PROPERTIES
        BUILD_RPATH "@loader_path"
        INSTALL_RPATH "@loader_path"
        BUILD_WITH_INSTALL_RPATH TRUE
    )
elseif(UNIX)
    set_target_properties(jni_dk PROPERTIES
        BUILD_RPATH "$ORIGIN"
        INSTALL_RPATH "$ORIGIN"
        BUILD_WITH_INSTALL_RPATH TRUE
    )
endif()
