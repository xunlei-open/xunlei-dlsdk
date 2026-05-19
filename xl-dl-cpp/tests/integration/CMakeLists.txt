cmake_minimum_required(VERSION 3.15)

project(xunlei-dlsdk-cpp-integration-test LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

find_package(xunlei-dlsdk-cpp CONFIG REQUIRED)

if(NOT TARGET xunlei::dlsdk)
    message(FATAL_ERROR "xunlei::dlsdk not found. Install the current platform native package first.")
endif()

add_executable(xl_dl_cpp_integration_test main.cpp)
target_link_libraries(xl_dl_cpp_integration_test PRIVATE xunlei::dlsdk)
