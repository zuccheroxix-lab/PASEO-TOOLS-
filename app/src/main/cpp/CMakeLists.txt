cmake_minimum_required(VERSION 3.22.1)

project("sensitivity_engine")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Shared library for Android JNI
add_library(sensitivity_engine SHARED
    sensitivity_engine.cpp
    sensitivity_bridge_jni.cpp
)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(sensitivity_engine
    ${log-lib}
    ${android-lib}
)

# Optional standalone executable for desktop / CLI verification
if (NOT ANDROID)
    add_executable(sensitivity_engine_cli
        sensitivity_engine.cpp
        sensitivity_engine_desktop_cli.cpp
    )
endif()
