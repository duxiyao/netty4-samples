#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <capture_camera.h>
#include <jni_init.h>
#include <com_ffmpeg_CaptureCamera.h>
#include <threadsafe_queue.h>
#include <thread>
using namespace std;

#define __STDC_CONSTANT_MACROS

#ifdef _WIN32
//Windows
extern "C"
{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavdevice/avdevice.h"
#include "libavutil/opt.h"
#include "libavutil/imgutils.h"
};
#else
//Linux...
#ifdef __cplusplus
extern "C"
{
#endif
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavdevice/avdevice.h>
#include <libavutil/opt.h>
#include <libavutil/imgutils.h>
#ifdef __cplusplus
};
#endif
#endif

//'1' Use Dshow
//'0' Use VFW
#define USE_DSHOW 0
#define FPS "30"
//#define V_WIDTH 1280
//#define V_HEIGTH 720
//#define V_WIDTH 1920
//#define V_HEIGTH 1080
#define V_WIDTH 2880
#define V_HEIGTH 1800
#define V_WH "1280x720"
//#define V_WIDTH 2880
//#define V_HEIGTH 1800
//#define V_WH "2880x1800"
#define PX_FMT "uyvy422"

static int calccount =0;
static CaptureHelper *helper =0;
static threadsafe_queue<AVFrame*> safeq;

static AVFrame* create_frame(int width, int height){

    int ret = 0;
    AVFrame *frame = NULL;

    frame = av_frame_alloc();
    if(!frame){
        printf("Error, No Memory!\n");
        goto __ERROR;
    }

    //设置参数
    frame->width = width;
    frame->height = height;
    frame->format = AV_PIX_FMT_YUV420P;

    //alloc inner memory
    ret = av_frame_get_buffer(frame, 32); //按 32 位对齐
    if(ret < 0){
        printf("Error, Failed to alloc buffer for frame!\n");
        goto __ERROR;
    }

    return frame;

__ERROR:

    if(frame){
        av_frame_free(&frame);
    }

    return NULL;
}


static void encode(AVCodecContext *enc_ctx, AVPacket *pkt,
                   FILE *outfile)
{
    AVFrame *frame=0;
    safeq.wait_and_pop(frame);
    if(safeq.size()>0){
        fprintf(stderr, "encode safeq.size:%d\n", safeq.size());
    }
    int ret;
    /* send the frame to the encoder */
    // if (frame)
    //     printf("Send frame %3"PRId64"\n", frame->pts);

    // fprintf(stderr, "width:%d height:%d\n",enc_ctx->width,enc_ctx->height);
    ret = avcodec_send_frame(enc_ctx, frame);
    if (ret < 0) {
        fprintf(stderr, "Error sending a frame for encoding\n");
        exit(1);
    }
    while (ret >= 0) {
        ret = avcodec_receive_packet(enc_ctx, pkt);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF){
    	    av_frame_free(&frame);
            return;
        }
        else if (ret < 0) {
            fprintf(stderr, "Error during encoding\n");
    	    av_frame_free(&frame);
            exit(1);
        }
//        fprintf(stderr,"Write packet %lld (size=%5d)\n", pkt->pts, pkt->size);
//        fprintf(stderr,"pre4data is %d %d %d %d \n", pkt->data[0],pkt->data[1],pkt->data[2],pkt->data[3]);

//        fwrite(pkt->data, 1, pkt->size, outfile);

        helper->onGet264Data(THREAD_OTHER,pkt->size,pkt->data);

        av_packet_unref(pkt);
    }
}

static void ftencode(AVCodecContext *enc_ctx, AVPacket *pkt,
                                       FILE *outfile){
    for(;;){
//        helper->calcStart(THREAD_OTHER);
        encode(enc_ctx,pkt,outfile);
//        helper->calcEnd(THREAD_OTHER);
    }
}

static void decodeandencode(AVCodecContext *dec_ctx, AVFrame *frame, AVPacket *pkt
                    ,struct SwsContext *img_convert_ctx
                    )
{
    // char buf[1024];
    int ret;
    ret = avcodec_send_packet(dec_ctx, pkt);
    if (ret < 0) {
        fprintf(stderr, "Error sending a packet for decoding\n");
        exit(1);
    }
    while (ret >= 0) {
        ret = avcodec_receive_frame(dec_ctx, frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF){
            return;
        }
        else if (ret < 0) {
            fprintf(stderr, "Error during decoding\n");
            exit(1);
        }
        // printf("saving frame %3d  pktsize:%d\n", dec_ctx->frame_number,(frame)->pkt_size);
//		fwrite(frame->data[0],1,frame->pkt_size,fp_yuv);
        // fflush(stdout);
        /* the picture is allocated by the decoder. no need to
           free it */
        // snprintf(buf, sizeof(buf), "%s-%d", filename, dec_ctx->frame_number);
        // pgm_save(frame->data[0], frame->linesize[0],
        //          frame->width, frame->height, buf);

//        printf("pre4data is %d %d %d %d \n", frame->data[0][0],frame->data[0][1],frame->data[0][2],frame->data[0][3]);

//        printf("avcodec_receive_frame success.\n");
    	AVFrame *dstFrame=create_frame(V_WIDTH,V_HEIGTH);
        sws_scale(img_convert_ctx, (const uint8_t * const*)frame->data,
                  frame->linesize, 0, dec_ctx->height, dstFrame->data, dstFrame->linesize);
//        dstFrame->pts=frame->pts++;  用这种方式硬编时候会有问题，软编没问题
        dstFrame->pts=calccount;
         /* encode the image */

         safeq.push(dstFrame);
//        printf("push frame to safeq success.\n");
          //todo 软编的时候，跳帧发送，会降低延迟，但是还有延迟。
//        if(calccount%5==0){
//            safeq.push(dstFrame);
//            fprintf(stderr, "dencode safeq.size:%d\n", safeq.size());
//        }
        calccount++;
    }
}

//Show Dshow Device
void show_dshow_device(){
	AVFormatContext *pFormatCtx = avformat_alloc_context();
	AVDictionary* options = NULL;
	av_dict_set(&options,"list_devices","true",0);
	AVInputFormat *iformat = av_find_input_format("dshow");
	printf("========Device Info=============\n");
	avformat_open_input(&pFormatCtx,"video=dummy",iformat,&options);
	printf("================================\n");
}

//Show Dshow Device Option
void show_dshow_device_option(){
	AVFormatContext *pFormatCtx = avformat_alloc_context();
	AVDictionary* options = NULL;
	av_dict_set(&options,"list_options","true",0);
	AVInputFormat *iformat = av_find_input_format("dshow");
	printf("========Device Option Info======\n");
	avformat_open_input(&pFormatCtx,"video=Integrated Camera",iformat,&options);
	printf("================================\n");
}

//Show VFW Device
void show_vfw_device(){
	AVFormatContext *pFormatCtx = avformat_alloc_context();
	AVInputFormat *iformat = av_find_input_format("vfwcap");
	printf("========VFW Device Info======\n");
	avformat_open_input(&pFormatCtx,"list",iformat,NULL);
	printf("=============================\n");
}

//Show AVFoundation Device
void show_avfoundation_device(){
    AVFormatContext *pFormatCtx = avformat_alloc_context();
    AVDictionary* options = NULL;
    av_dict_set(&options,"list_devices","true",0);
    AVInputFormat *iformat = av_find_input_format("avfoundation");
    printf("==AVFoundation Device Info===\n");
    avformat_open_input(&pFormatCtx,"",iformat,&options);
    printf("=============================\n");
}

static int mac_open_input(AVFormatContext * pFormatCtx){
    show_avfoundation_device();
    AVDictionary *options = NULL;

    av_dict_set(&options, "video_size", V_WH, 0);
    av_dict_set(&options, "framerate", FPS, 0);
    av_dict_set(&options, "pixel_format", PX_FMT, 0);
    // av_dict_set(&options, "pixel_format", "uyvy422", 0);
    //Mac
    AVInputFormat *ifmt=av_find_input_format("avfoundation");
    //Avfoundation
    //[video]:[audio]
//    if(avformat_open_input(&pFormatCtx,"FaceTime HD Camera",ifmt, &options)!=0){
//    if(avformat_open_input(&pFormatCtx,"0",ifmt, &options)!=0){
//    if(avformat_open_input(&pFormatCtx,"Capture screen 1",ifmt, &options)!=0){
    if(avformat_open_input(&pFormatCtx,"Capture screen 0",ifmt, &options)!=0){
        printf("Couldn't open input stream.\n");
        return -1;
    }
	return 0;
}

static int linux_open_input(AVFormatContext * pFormatCtx){
 //Linux
	AVInputFormat *ifmt=av_find_input_format("video4linux2");
	if(avformat_open_input(&pFormatCtx,"/dev/video0",ifmt,NULL)!=0){
		printf("Couldn't open input stream.\n");
		return -1;
	}
	return 0;
}

static int win_open_input(AVFormatContext * pFormatCtx){
	//Windows
    //Show Dshow Device
    show_dshow_device();
    //Show Device Options
    show_dshow_device_option();
    //Show VFW Options
    show_vfw_device();

    #if USE_DSHOW
    	AVInputFormat *ifmt = av_find_input_format("dshow");
    	//Set own video device's name
    	if(avformat_open_input(&pFormatCtx,"video=Integrated Camera",ifmt,NULL)!=0){
    		printf("Couldn't open input stream.\n");
    		return -1;
    	}
    #else
    	AVInputFormat *ifmt = av_find_input_format("vfwcap");
    	if(avformat_open_input(&pFormatCtx,"0",ifmt,NULL)!=0){
    		printf("Couldn't open input stream.\n");
    		return -1;
    	}
	#endif
	return 0;
}

static int open_input(AVFormatContext *ps){
    int ret = -1;
    #ifdef _WIN32
        ret = win_open_input(ps);
    #elif defined linux
        ret = linux_open_input(ps);
    #else
        ret = mac_open_input(ps);
    #endif
    return ret;
}

static int open_decode(AVFormatContext	*pFormatCtx,int *videoindex
    ,AVCodecParameters *pCodecParameters,AVCodecContext * &pDecodeCodecCtx,AVCodec *pDecodeCodec){
    if(avformat_find_stream_info(pFormatCtx,NULL)<0)
    {
    	printf("Couldn't find stream information.\n");
    	return -1;
    }
    printf("nb_streams=%d.\n",pFormatCtx->nb_streams);
    *videoindex = -1;
    for(int i=0; i<pFormatCtx->nb_streams; i++) {

    	pCodecParameters = pFormatCtx->streams[i]->codecpar;
    	if(pCodecParameters->codec_type==AVMEDIA_TYPE_VIDEO)
    	{
    		*videoindex=i;
    		break;
    	}
    }
    if(*videoindex==-1)
    {
    	printf("Couldn't find a video stream.\n");
    	return -1;
    }

    if (av_find_best_stream(pFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, &pDecodeCodec, 0) < 0) {
        fprintf(stderr, "Cannot find a video stream in the input file\n");
        return -1;
    }
    // pDecodeCodec=avcodec_find_decoder(AV_CODEC_ID_RAWVIDEO);
    if(pDecodeCodec==NULL)
    {
    	printf("%d decode Codec not found.\n",AV_CODEC_ID_RAWVIDEO);
    	printf("%d.\n",pFormatCtx->video_codec_id);
    	// printf("%d.\n",pFormatCtx->streams[videoindex]->codec->codec_id);
    	return -1;
    }
    pDecodeCodecCtx=avcodec_alloc_context3(pDecodeCodec);
    avcodec_parameters_to_context(pDecodeCodecCtx, pCodecParameters); //初始化AVCodecContext
    pDecodeCodecCtx->gop_size=0;
    pDecodeCodecCtx->thread_count=8;
    if(avcodec_open2(pDecodeCodecCtx, pDecodeCodec,NULL)<0)
    {
    	printf("Could not open decoder codec.\n");
    	printf("%d.\n",pDecodeCodecCtx->codec_id);
    	printf("%d.\n",pDecodeCodecCtx->pix_fmt);
    	// printf("%d.\n",pFormatCtx->streams[videoindex]->codec->pix_fmt);
    	printf("w:%d,h:%d.\n",pDecodeCodecCtx->width,pDecodeCodecCtx->height);
    	// printf("w:%d,h:%d.\n",pFormatCtx->streams[videoindex]->codec->width,pFormatCtx->streams[videoindex]->codec->height);
    	return -1;
    }
    printf("width:%d height:%d.\n",pDecodeCodecCtx->width,pDecodeCodecCtx->height);
    return 0;
}

static int open_encode(AVCodec *pCodec,AVCodecContext * &pCodecCtx,AVCodecContext	*pDecodeCodecCtx){
//硬编时候，数据写入文件然后播放是正常的，网络发出去是对方是花的；软编的时候写入文件网络发送都正常。原因是pts的问题
    pCodec=avcodec_find_encoder_by_name("h264_videotoolbox");//mac  264 硬编码
//    pCodec=avcodec_find_encoder_by_name("hevc_videotoolbox");//mac 265硬编码
//    pCodec=avcodec_find_encoder_by_name("libx264");
//    pCodec=avcodec_find_encoder_by_name("libx265");
    pCodecCtx=avcodec_alloc_context3(pCodec);
    if(pCodec==NULL)
    {
    	printf("Codec not found.\n");
    	return -1;
    }
    pCodecCtx->thread_count=8;
     /* put sample parameters */
//    pCodecCtx->bit_rate = pDecodeCodecCtx->bit_rate;
    /* resolution must be a multiple of two */
    pCodecCtx->width = pDecodeCodecCtx->width;
    pCodecCtx->height = pDecodeCodecCtx->height;
//    printf("width:%d height:%d.\n",pDecodeCodecCtx->width,pDecodeCodecCtx->height);

    /* frames per second */
    pCodecCtx->time_base = (AVRational){1, 30};
    pCodecCtx->framerate = (AVRational){30, 1};
    /* emit one intra frame every ten frames
     * check frame pict_type before passing frame
     * to encoder, if frame->pict_type is AV_PICTURE_TYPE_I
     * then gop_size is ignored and the output of encoder
     * will always be I frame irrespective to gop_size
     */
    pCodecCtx->gop_size = 15;
    pCodecCtx->max_b_frames = 0;
    pCodecCtx->bit_rate=800000;
//    printf("pCodecCtx->gop_size:%d pCodecCtx->max_b_frames:%d.\n",pCodecCtx->gop_size,pCodecCtx->max_b_frames);
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;
    if (pCodec->id == AV_CODEC_ID_H264){
        //硬编时候感觉这个参数不起作用
        //https://www.jianshu.com/p/b46a33dd958d  刚开始preset是slow，导致播放跟快进似的
        av_opt_set(pCodecCtx->priv_data, "preset", "ultrafast", 0);
//        av_opt_set(pCodecCtx->priv_data, "preset", "slow", 0);
        av_opt_set(pCodecCtx->priv_data, "tune", "zerolatency", 0);
    }
    if(pCodec->id == AV_CODEC_ID_H265){
        av_opt_set(pCodecCtx->priv_data, "x265-params", "qp=20", 0);
        av_opt_set(pCodecCtx->priv_data, "preset", "ultrafast", 0);
        av_opt_set(pCodecCtx->priv_data, "tune", "zero-latency", 0);
    }

    if(avcodec_open2(pCodecCtx, pCodec,NULL)<0)
    {
    	printf("Could not open codec.\n");
    	return -1;
    }
    return 0;
}

int capture()
{
	AVFormatContext	*pFormatCtx;
	int			     videoindex;
	AVCodecParameters *pCodecParameters;
	AVCodecContext	*pCodecCtx,*pDecodeCodecCtx;
	AVCodec			*pCodec,*pDecodeCodec;

	avformat_network_init();
	pFormatCtx = avformat_alloc_context();

	//Register Device
	avdevice_register_all();

    int ret = open_input(pFormatCtx);
    if(ret==-1){
       printf("open_input failer.\n");
       return ret;
    }

    ret = open_decode(pFormatCtx,&videoindex,pCodecParameters,pDecodeCodecCtx,pDecodeCodec);
    if(ret==-1){
       printf("open_decode failer.\n");
       return ret;
    }
    printf("open_decode success.\n");

    ret = open_encode(pCodec,pCodecCtx,pDecodeCodecCtx);
    if(ret==-1){
        printf("open_encode failer.\n");
        return ret;
    }
    printf("open_encode success.\n");

    AVFrame	*pFrame=av_frame_alloc();
    AVPacket *packet=(AVPacket *)av_malloc(sizeof(AVPacket));
    AVPacket *encodedPkt =  av_packet_alloc();

    FILE *test264=fopen("test.h264","wb+");

    struct SwsContext *img_convert_ctx;
    img_convert_ctx = sws_getContext(pDecodeCodecCtx->width, pDecodeCodecCtx->height, pDecodeCodecCtx->pix_fmt,
    								 pCodecCtx->width, pCodecCtx->height, AV_PIX_FMT_YUV420P, SWS_BICUBIC, NULL, NULL, NULL);

	thread tencode(ftencode,pCodecCtx,encodedPkt,test264);
	tencode.detach();
//	thread tencode1(ftencode,pCodecCtx,encodedPkt,test264);
//	tencode1.detach();
//	thread tencode2(ftencode,pCodecCtx,encodedPkt,test264);
//	tencode2.detach();

    printf("start capture.\n");
    int frame_count=0;
    //采集摄像头数据以及编解码，需要分开线程处理。因为若采集 后者 编码 互相占用时间，会影响真实的帧率，会导致播放时候像是在快进
    for (;;) {
    	if(av_read_frame(pFormatCtx, packet)>=0){
//        	printf("readed packet.\n");
    		if(packet->stream_index==videoindex){
        			frame_count++;
//        			if(frame_count>=100){
//        				break;
//        			}

        		//采集摄像头数据以及编解码，需要分开线程处理。因为若采集 后者 编码 互相占用时间，会影响真实的帧率，会导致播放时候像是在快进
            	decodeandencode(pDecodeCodecCtx, pFrame, packet,img_convert_ctx);

    		}
    		av_packet_unref(packet);
    	}else{
    		//Exit Thread
    	}
    }

    sws_freeContext(img_convert_ctx);

    // av_free(pFrame);
    // avcodec_close(pCodecCtx);
    // avformat_close_input(&pFormatCtx);

     /* flush the encoder */
    //todo
    // encode(pCodecCtx, NULL, encodedPkt, test264);
    /* add sequence end code to have a real MPEG file */
//    uint8_t endcode[] = { 0, 0, 1, 0xb7 };
//    fwrite(endcode, 1, sizeof(endcode), test264);

    fclose(test264);
    avcodec_free_context(&pCodecCtx);
    avcodec_free_context(&pDecodeCodecCtx);
    av_frame_free(&pFrame);
    av_packet_free(&encodedPkt);
    avformat_close_input(&pFormatCtx);

    return 0;
}

JNIEXPORT void JNICALL Java_com_ffmpeg_CaptureCamera_startCapture
  (JNIEnv *env, jobject instance){
    helper=new CaptureHelper(javaVM,env,instance);
    capture();
}

CaptureHelper::CaptureHelper(JavaVM *javaVM_, JNIEnv *env_, jobject instance_){
    this->javaVM = javaVM_;
    this->env = env_;
//    this->instance = instance_;//不能直接赋值！
    //一旦涉及到 jobject 跨方法、跨线程，需要创建全局引用
    this->instance = env->NewGlobalRef(instance_);
    jclass clazz = env->GetObjectClass(instance);
//    cd 进入 class所在的目录 执行： javap -s 全限定名,查看输出的 descriptor
//    xx\app\build\intermediates\classes\debug>javap -s com.netease.jnitest.Helper
    jmd_on_get_data = env->GetMethodID(clazz, "onGetData", "([B)V");
    jmd_on_calc_start = env->GetMethodID(clazz, "onCalcStart", "()V");
    jmd_on_calc_end = env->GetMethodID(clazz, "onCalcEnd", "()V");
}

CaptureHelper::~CaptureHelper(){
    javaVM = 0;
    env->DeleteGlobalRef(instance);
    instance = 0;
}

void CaptureHelper::onGet264Data(int threadMode,int size,uint8_t *d){
    if (threadMode == THREAD_PARENT) {
        jbyteArray data = env->NewByteArray(size);                  //创建与buffer容量一样的byte[]
        env->SetByteArrayRegion(data, 0, size, (jbyte*)d);                //数据拷贝到data中
        env->CallVoidMethod(instance, jmd_on_get_data, data);
        env->DeleteLocalRef(data);
    } else {
        //子线程
        //当前子线程的 JNIEnv
        JNIEnv *env_child;
        javaVM->AttachCurrentThread((void**)&env_child, 0);
        jbyteArray data = env_child->NewByteArray(size);                  //创建与buffer容量一样的byte[]
        env_child->SetByteArrayRegion(data, 0, size, (jbyte*)d);                //数据拷贝到data中
        env_child->CallVoidMethod(instance, jmd_on_get_data,data);
        env_child->DeleteLocalRef(data);
        javaVM->DetachCurrentThread();
    }
}

void CaptureHelper::calcEnd(int threadMode){
    if (threadMode == THREAD_PARENT) {
        env->CallVoidMethod(instance, jmd_on_calc_end);
    } else {
        //子线程
        //当前子线程的 JNIEnv
        JNIEnv *env_child;
        javaVM->AttachCurrentThread((void**)&env_child, 0);
        env_child->CallVoidMethod(instance, jmd_on_calc_end);
        javaVM->DetachCurrentThread();
    }
}
void CaptureHelper::calcStart(int threadMode){
    if (threadMode == THREAD_PARENT) {
        env->CallVoidMethod(instance, jmd_on_calc_start);
    } else {
        //子线程
        //当前子线程的 JNIEnv
        JNIEnv *env_child;
        javaVM->AttachCurrentThread((void**)&env_child, 0);
        env_child->CallVoidMethod(instance, jmd_on_calc_start);
        javaVM->DetachCurrentThread();
    }
}