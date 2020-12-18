cmake_minimum_required(VERSION 3.4.1)
#使用$ENV{NAME}指令就可以调用系统的环境变量了。
#设置环境变量的方式是:
#SET(ENV{变量名} 值)
SET(ENV{JAVA_HOME} /Library/Java/JavaVirtualMachines/jdk1.8.0_251.jdk/Contents/Home)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ../../jniLib/)
#set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI})

MESSAGE(STATUS "HOME dir:$ENV{HOME}")
MESSAGE(STATUS "JAVA_HOME dir:$ENV{JAVA_HOME}")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11") # set C++ 11

#找到包含的所有cpp文件,不包含.h文件
file(GLOB allCpp *.cpp)
#file(GLOB allC *.c)

#添加自己编写的 cpp源文件生成动态库
add_library(
        myffmpeg
        SHARED
        ${allCpp}
        ${allC})

#找到系统中 NDK log库
#find_library(
#        android
#        android
#)

#引入ffmpeg头文件
include_directories(
    $ENV{JAVA_HOME}/include
    $ENV{JAVA_HOME}/include/darwin
    ${CMAKE_SOURCE_DIR}/include
    /usr/local/include
)

#添加ffmpeg 库
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L/usr/local/lib}")
link_directories("/usr/local/lib" "/usr/local/lib")

#指定 rtmp静态库文件目录
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/librtmp/libs/${CMAKE_ANDROID_ARCH_ABI}")

add_library(
        avformat SHARED IMPORTED
)
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        /usr/local/lib/libavformat.dylib
        )

add_library(
        avcodec SHARED IMPORTED
)
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        /usr/local/lib/libavcodec.dylib
        )

add_library(
        avutil SHARED IMPORTED
)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        /usr/local/lib/libavutil.dylib
        )

add_library(
        avdevice SHARED IMPORTED
)
set_target_properties(avdevice
        PROPERTIES IMPORTED_LOCATION
        /usr/local/lib/libavdevice.dylib
        )

add_library(
        swscale SHARED IMPORTED
)
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        /usr/local/lib/libswscale.dylib
        )

#最后开始链接库
target_link_libraries(
        myffmpeg
        pthread
        avformat
        avcodec
        avutil
        avdevice
        swscale
)

#        -Wl,--start-group
#        avcodec avfilter avformat avutil swresample swscale
#        -Wl,--end-group