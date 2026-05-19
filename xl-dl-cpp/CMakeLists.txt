cmake_minimum_required(VERSION 3.15)

project(xunlei-dlsdk-cpp VERSION 1.0.0 LANGUAGES C CXX)

include(CMakePackageConfigHelpers)
include(GNUInstallDirs)

find_package(CURL REQUIRED)

option(XL_DL_CPP_INSTALL_NATIVE "Install the current desktop platform native runtime" ON)
set(XL_DL_CPP_NATIVE_PLATFORM "" CACHE STRING "Override native platform id: windows-x64, windows-x86, macos-universal, linux-x64")
set(XUNLEI_DLSDK_CPP_INSTALL_CMAKEDIR "${CMAKE_INSTALL_LIBDIR}/cmake/xunlei-dlsdk-cpp" CACHE STRING "Install directory for the primary CMake package config")

add_library(xunlei_dlsdk_headers INTERFACE)
add_library(xunlei::headers ALIAS xunlei_dlsdk_headers)
set_target_properties(xunlei_dlsdk_headers PROPERTIES EXPORT_NAME headers)
target_include_directories(xunlei_dlsdk_headers INTERFACE
    "$<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>"
    "$<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>"
)
target_link_libraries(xunlei_dlsdk_headers INTERFACE CURL::libcurl)

function(xl_dl_detect_native_platform output_variable)
    if(XL_DL_CPP_NATIVE_PLATFORM)
        set(${output_variable} "${XL_DL_CPP_NATIVE_PLATFORM}" PARENT_SCOPE)
        return()
    endif()

    if(CMAKE_SYSTEM_NAME STREQUAL "Windows")
        if(CMAKE_SIZEOF_VOID_P EQUAL 8)
            set(${output_variable} "windows-x64" PARENT_SCOPE)
            return()
        endif()
        if(CMAKE_SIZEOF_VOID_P EQUAL 4)
            set(${output_variable} "windows-x86" PARENT_SCOPE)
            return()
        endif()
    elseif(CMAKE_SYSTEM_NAME STREQUAL "Darwin")
        set(${output_variable} "macos-universal" PARENT_SCOPE)
        return()
    elseif(CMAKE_SYSTEM_NAME STREQUAL "Linux")
        if(CMAKE_SYSTEM_PROCESSOR MATCHES "^(x86_64|amd64|AMD64)$")
            set(${output_variable} "linux-x64" PARENT_SCOPE)
            return()
        endif()
    endif()

    message(FATAL_ERROR "Unsupported C++ SDK native platform: ${CMAKE_SYSTEM_NAME}/${CMAKE_SYSTEM_PROCESSOR}")
endfunction()

xl_dl_detect_native_platform(XL_DL_CPP_DETECTED_NATIVE_PLATFORM)
set(XL_DL_CPP_NATIVE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/prebuilt/${XL_DL_CPP_DETECTED_NATIVE_PLATFORM}")

function(xl_dl_add_imported_runtime_target target_name native_dir include_dir)
    if(TARGET "${target_name}")
        return()
    endif()

    if(XL_DL_CPP_DETECTED_NATIVE_PLATFORM STREQUAL "windows-x64"
            OR XL_DL_CPP_DETECTED_NATIVE_PLATFORM STREQUAL "windows-x86")
        set(runtime_path "${native_dir}/bin/dk.dll")
        set(import_path "${native_dir}/lib/dk.lib")
        if(EXISTS "${runtime_path}" AND EXISTS "${import_path}")
            add_library("${target_name}" SHARED IMPORTED GLOBAL)
            set_target_properties("${target_name}" PROPERTIES
                IMPORTED_LOCATION "${runtime_path}"
                IMPORTED_IMPLIB "${import_path}"
                INTERFACE_INCLUDE_DIRECTORIES "${include_dir}"
                INTERFACE_LINK_LIBRARIES CURL::libcurl
            )
        endif()
    elseif(XL_DL_CPP_DETECTED_NATIVE_PLATFORM STREQUAL "macos-universal")
        set(runtime_path "${native_dir}/lib/libdk.dylib")
        if(EXISTS "${runtime_path}")
            add_library("${target_name}" SHARED IMPORTED GLOBAL)
            set_target_properties("${target_name}" PROPERTIES
                IMPORTED_LOCATION "${runtime_path}"
                INTERFACE_INCLUDE_DIRECTORIES "${include_dir}"
                INTERFACE_LINK_LIBRARIES CURL::libcurl
            )
        endif()
    elseif(XL_DL_CPP_DETECTED_NATIVE_PLATFORM STREQUAL "linux-x64")
        set(runtime_path "${native_dir}/lib/libdk.so")
        if(EXISTS "${runtime_path}")
            add_library("${target_name}" SHARED IMPORTED GLOBAL)
            set_target_properties("${target_name}" PROPERTIES
                IMPORTED_LOCATION "${runtime_path}"
                INTERFACE_INCLUDE_DIRECTORIES "${include_dir}"
                INTERFACE_LINK_LIBRARIES CURL::libcurl
            )
        endif()
    endif()
endfunction()

xl_dl_add_imported_runtime_target(xunlei::dlsdk "${XL_DL_CPP_NATIVE_DIR}" "${CMAKE_CURRENT_SOURCE_DIR}/include")

if(XL_DL_CPP_INSTALL_NATIVE)
    if(XL_DL_CPP_DETECTED_NATIVE_PLATFORM STREQUAL "windows-x64"
            OR XL_DL_CPP_DETECTED_NATIVE_PLATFORM STREQUAL "windows-x86")
        install(FILES "${XL_DL_CPP_NATIVE_DIR}/bin/dk.dll" DESTINATION "${CMAKE_INSTALL_BINDIR}" COMPONENT native)
        install(FILES "${XL_DL_CPP_NATIVE_DIR}/lib/dk.lib" DESTINATION "${CMAKE_INSTALL_LIBDIR}" COMPONENT native)
    elseif(XL_DL_CPP_DETECTED_NATIVE_PLATFORM STREQUAL "macos-universal")
        install(FILES "${XL_DL_CPP_NATIVE_DIR}/lib/libdk.dylib" DESTINATION "${CMAKE_INSTALL_LIBDIR}" COMPONENT native)
    elseif(XL_DL_CPP_DETECTED_NATIVE_PLATFORM STREQUAL "linux-x64")
        install(FILES "${XL_DL_CPP_NATIVE_DIR}/lib/libdk.so" DESTINATION "${CMAKE_INSTALL_LIBDIR}" COMPONENT native)
    else()
        message(FATAL_ERROR "Unsupported native package: ${XL_DL_CPP_DETECTED_NATIVE_PLATFORM}")
    endif()
endif()

install(TARGETS xunlei_dlsdk_headers EXPORT xunlei-dlsdk-cpp-targets)
install(DIRECTORY include/ DESTINATION "${CMAKE_INSTALL_INCLUDEDIR}" PATTERN "README.md" EXCLUDE)
install(EXPORT xunlei-dlsdk-cpp-targets
    NAMESPACE xunlei::
    FILE xunlei-dlsdk-cpp-targets.cmake
    DESTINATION "${XUNLEI_DLSDK_CPP_INSTALL_CMAKEDIR}"
)

write_basic_package_version_file(
    "${CMAKE_CURRENT_BINARY_DIR}/xunlei-dlsdk-cpp-config-version.cmake"
    VERSION "${PROJECT_VERSION}"
    COMPATIBILITY SameMajorVersion
)

configure_package_config_file(
    "${CMAKE_CURRENT_SOURCE_DIR}/cmake/xunlei-dlsdk-cpp-config.cmake.in"
    "${CMAKE_CURRENT_BINARY_DIR}/xunlei-dlsdk-cpp-config.cmake"
    INSTALL_DESTINATION "${XUNLEI_DLSDK_CPP_INSTALL_CMAKEDIR}"
)

install(FILES
    "${CMAKE_CURRENT_BINARY_DIR}/xunlei-dlsdk-cpp-config.cmake"
    "${CMAKE_CURRENT_BINARY_DIR}/xunlei-dlsdk-cpp-config-version.cmake"
    DESTINATION "${XUNLEI_DLSDK_CPP_INSTALL_CMAKEDIR}"
)
