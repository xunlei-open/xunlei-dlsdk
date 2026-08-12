cmake_minimum_required(VERSION 3.22.1)
project(xl_dl_android_jni)

if(NOT XL_DL_NATIVE_DIR)
    message(FATAL_ERROR "Set XL_DL_NATIVE_DIR to the directory containing libdk.so")
endif()
if(NOT XL_DL_COMMON_NATIVE_DIR)
    message(FATAL_ERROR "Set XL_DL_COMMON_NATIVE_DIR to common/src/main/native")
endif()

set(DK_LIBRARY "${XL_DL_NATIVE_DIR}/libdk.so")
if(NOT EXISTS "${DK_LIBRARY}")
    message(FATAL_ERROR "Missing libdk.so at ${DK_LIBRARY}")
endif()

add_library(dk SHARED IMPORTED)
set_target_properties(dk PROPERTIES IMPORTED_LOCATION "${DK_LIBRARY}")

add_library(jni_dk SHARED
    "${XL_DL_COMMON_NATIVE_DIR}/jni_xl_dl_sdk.cpp"
)

target_include_directories(jni_dk PRIVATE
    "${XL_DL_COMMON_NATIVE_DIR}"
    "${XL_DL_COMMON_NATIVE_DIR}/include"
)

target_link_libraries(jni_dk PRIVATE
    dk
    android
    log
)
