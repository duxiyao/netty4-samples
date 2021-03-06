package com.ffmpeg;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;

/**
 * https://blog.csdn.net/huachao1001/article/details/53906237
 * idea的preferences 的 Tools  里的 External Tools，面板上有个加号按钮点击添加，然后
 * name ： External Tools中的名称。如： genereate header file
 * description ：描述 。如：生成jni头文件
 * <p>
 * (tool settings里)
 * program : 执行的命令路径。如：$JDKPath$/bin/javah
 * arguments : 命令带的参数 。如：-jni -classpath $OutputPath$ -d ./jni $FileClass$
 * working directory : 项目路径 。如： $ProjectFileDir$
 * <p>
 * 添加成功后，点击右键-->External Tools-->找到$name
 * <p>
 * https://www.jetbrains.com/help/idea/2020.2/new-watcher-dialog.html#ws_file_watcher_dialog_working_directory_and_environment_variables
 * https://www.w3cschool.cn/search?w=%24ProjectFileDir%24
 * <p>
 * https://www.jetbrains.com/help/idea/2020.2/using-emacs-as-an-external-editor.html#opening
 * <p>
 * $ProjectFileDir$
 * $FileNameWithoutExtension$
 * $JDKPath$
 * $FileClass$
 * 以上macro，在 program arguments 对应输入框的后边 + 按钮可以选择插入
 * <p>
 * javah -jni -classpath (搜寻类目录) -d (输出目录) (类名)
 * javah -jni -classpath  E:\Porject\out\com\huachao\java -d E:\Project\jni  com.huachao.java.HelloJNI
 * 需要注意的是，使用javah来生成头文件（.h）时，-classpath指定的是编译后的java文件（.class）的目录，而不是源文件（.java）的目录，因此在使用javah指令之前，先build一下项目（或直接运行一下）。此时会生称out目录，所有编译后的文件都会存放在这个目录中。
 * <p>
 * $JDKPath$/bin/javah
 * -jni -classpath $OutputPath$ -d $ModuleFileDir$/src/main/cpp/include $FileClass$
 * <p>
 * <p>
 * name gcc
 * program:  /usr/bin/gcc
 * arg:  $FilePath$ -o $ModuleFileDir$/target/$FileNameWithoutExtension$.out -framework Cocoa -I /usr/local/include -L /usr/local/lib -lavformat -lavcodec -lavutil -lavdevice -lswscale
 * workingdir:  $ProjectFileDir$
 * <p>
 * name  generate header file
 * program:  $JDKPath$/bin/javah
 * arg:  -jni -classpath $OutputPath$ -d $ModuleFileDir$/src/main/cpp/include $FileClass$
 * workingdir:  $ProjectFileDir$
 * <p>
 * name  generate cxx file
 * program:  /usr/bin/touch
 * arg:  $ModuleFileDir$/src/main/cpp/$FileNameWithoutAllExtensions$.cpp
 * workingdir:  $ProjectFileDir$
 * <p>
 * name  generate so
 * program:  /usr/bin/g++
 * arg:  -I"$JDKPath$/include" -I"$JDKPath$/include/darwin" -dynamiclib -o ./jniLib/$FileNameWithoutExtension$.dylib ./cpp/$FileNameWithoutExtension$.cpp
 * workingdir:  $ModuleFileDir$/src/main
 * <p>
 * <p>
 * name  cmake
 * program:  /usr/local/bin/cmake
 * arg:  $ModuleFileDir$/src/main/cpp/
 * workingdir:  $ModuleFileDir$/src/main/cpp/build/
 */
public class HelloJni {

    static {
        /**
         * 告诉jvm库放在那里
         * run--edit configurations...
         * 在VM options中加入java.library.path，指定dll（或so）文件所在的目录，比如本文中dll放在项目目录中的lib中，如下:
         * -Djava.library.path=$ModuleFileDir$/src/main/jniLib
         * -Djava.library.path=/Users/dyz/AndroidStudioProjects/stduy_code/aboutnetty/netty4-samples/gupaoedu-vip-netty-chat/src/main/jniLib/
         *
         * On Windows, it maps to PATH
         * On Linux, it maps to LD_LIBRARY_PATH
         * On OS X, it maps to DYLD_LIBRARY_PATH
         */
//        System.out.println(System.getProperty("workdir"));
//        System.setProperty( "java.library.path", System.getProperty("java.library.path")+".:/Users/dyz/AndroidStudioProjects/stduy_code/aboutnetty/netty4-samples/gupaoedu-vip-netty-chat/src/main/jniLib/" );
//        System.out.println(System.getProperty("java.library.path"));
//        System.load("/Users/dyz/AndroidStudioProjects/stduy_code/aboutnetty/netty4-samples/gupaoedu-vip-netty-chat/src/main/jniLib/libtest.so");
        System.loadLibrary("myffmpeg");
//        System.loadLibrary("c");
    }

    public static void main(String[] args) throws InterruptedException {
//        Map<String, String> env = System.getenv();
//        for (String envName : env.keySet()) {
//            System.out.format("%s=%s%n", envName, env.get(envName));
//        }
//
//        System.out.println(System.getProperty("java.class.path"));
//        Properties properties=System.getProperties();
//        properties.keySet().forEach(key->{
//            System.out.println(key.toString()+"==="+System.getProperty(key.toString()));
//        });
//        System.out.println(HelloJni.class.get().get);
        new HelloJni().sayHello();
        new HelloJni2().stopPublish();
        ByteBuffer data = ByteBuffer.allocateDirect(2);
        data.put((byte) 1);
        data.put((byte) 3);
        new HelloJni2().onPreviewFrame(new byte[]{1, 2}, 0, 0, data);
//        Thread.sleep(1000);
        new HelloJni().sayHello1();
        String s = "";
    }

    private native void sayHello();
    private native void sayHello1();

    /**
     * windows linux mac loadLibrary
     * java.lang.UnsatisfiedLinkError: no com_ffmpeg_HelloJni in java.library.path
     * https://stackoverflow.com/questions/16506297/how-to-use-dylib-file
     * -Djava.library.path=
     * windows abc.dll System.loadLibrary("abc")
     * linux
     * mac  libabc.dylib System.loadLibrary("abc")
     */

    /**
     * 给JNI 方式调用的方法，准备成功
     */
    public void onPrepared() {
        new HelloJni2().log("cb onPrepared");
    }

    /**
     * 给JNI 方式回调的方法（进行错误的回调）
     */
    public void onError(int errorCode) {
//        System.out.println("cb onError " + errorCode);
        new HelloJni2().log("cb onError " + errorCode);
    }

    // native层传递上来的 进度值
    public void onProgress(byte[] data) {
//        new HelloJni2().log("cb onProgress1 " + data[0] + "  " + data[1]);
//        new HelloJni2().log("cb onProgress1 "+data.length );
        System.out.println("cb onProgress1 "+data.length );
        String s="";
    }

    public void onProgress(ByteBuffer byteBuffer) {
        new HelloJni2().log("cb onProgress2 "+byteBuffer.get()+ "  " +byteBuffer.get());
//        byte[] data = byteBuffer.array();
//        System.out.println("cb onProgress2 " + data[0] + "  " + data[1]);
    }
}
