/**
https://blog.csdn.net/leixiaohua1020/article/details/11979565?ops_request_misc=%7B%22request%5Fid%22%3A%22160707931419724847151994%22%2C%22scm%22%3A%2220140713.130102334.pc%5Fblog.%22%7D&request_id=160707931419724847151994&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~blog~first_rank_v1~rank_blog_v1-20-11979565.pc_v1_rank_blog_v1&utm_term=ffmpeg&spm=1018.2118.3001.4450
https://blog.csdn.net/leixiaohua1020/article/details/39702113?ops_request_misc=%7B%22request%5Fid%22%3A%22160707883419725222486415%22%2C%22scm%22%3A%2220140713.130102334..%22%7D&request_id=160707883419725222486415&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~baidu_landing_v2~default-5-39702113.first_rank_v2_pc_rank_v29&utm_term=av_find_input_format&spm=1018.2118.3001.4449

gcc testyuv2h264.c -g -o testyuv2h264.out -framework Cocoa -I /usr/local/include -L /usr/local/lib -lavformat -lavcodec -lavutil -lavdevice -lswscale
./testyuv2h264.out
ffplay -i output.yuv -pix_fmt uyvy422 -s 1280x720

ffplay -stats -f h264 test.h264
ffplay -stats -f hevc test.h264

https://blog.csdn.net/zhangbangqian/article/details/78753126
jni java 操作byte 与 bytebuffer

https://www.jianshu.com/p/c867e4abcb5f
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

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

//Output YUV420P
#define OUTPUT_YUV420P 1
//'1' Use Dshow
//'0' Use VFW
#define USE_DSHOW 0
#define V_WIDTH 1280
#define V_HEIGTH 720
#define FPS "30"
#define V_WH "1280x720"
#define PX_FMT "uyvy422"

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


static void encode(AVCodecContext *enc_ctx, AVFrame *frame, AVPacket *pkt,
                   FILE *outfile)
{
    // printf("----- pktsize:%d\n", (frame)->pkt_size);
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
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
            return;
        else if (ret < 0) {
            fprintf(stderr, "Error during encoding\n");
            exit(1);
        }
        // printf("Write packet %3"PRId64" (size=%5d)\n", pkt->pts, pkt->size);
        printf("pre4data is %d %d %d %d \n", pkt->data[0],pkt->data[1],pkt->data[2],pkt->data[3]);
        fwrite(pkt->data, 1, pkt->size, outfile);
        av_packet_unref(pkt);
    }
}

static void decodeandencode(AVCodecContext *dec_ctx, AVFrame *frame, AVPacket *pkt
                    , FILE *test264,AVCodecContext *enc_ctx,AVPacket *encodedPkt,struct SwsContext *img_convert_ctx
                    ,FILE *fp_yuv)
{
    // char buf[1024];
    int ret;
    ret = avcodec_send_packet(dec_ctx, pkt);
    if (ret < 0) {
        fprintf(stderr, "Error sending a packet for decoding\n");
        exit(1);
    }
 //    uint8_t  *dst_data[4];
	// int  dst_linesize[4];
	//  /* buffer is going to be written to rawvideo file, no alignment */
 //    if ((ret = av_image_alloc(dst_data, dst_linesize,
 //                              frame->width, frame->height, AV_PIX_FMT_YUV420P, 1)) < 0) {
 //        fprintf(stderr, "Could not allocate destination image\n");
 //        exit(1);
 //    }
        // printf("-----====\n");
    while (ret >= 0) {
        ret = avcodec_receive_frame(dec_ctx, frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF){
        	// printf("----- pktsize:%d\n", (frame)->pkt_size);
            return;
        }
        else if (ret < 0) {
            fprintf(stderr, "Error during decoding\n");
            exit(1);
        }
        // printf("saving frame %3d  pktsize:%d\n", dec_ctx->frame_number,(frame)->pkt_size);
		fwrite(frame->data[0],1,frame->pkt_size,fp_yuv);
        // fflush(stdout);
        /* the picture is allocated by the decoder. no need to
           free it */
        // snprintf(buf, sizeof(buf), "%s-%d", filename, dec_ctx->frame_number);
        // pgm_save(frame->data[0], frame->linesize[0],
        //          frame->width, frame->height, buf);

    	AVFrame *dstFrame=create_frame(V_WIDTH,V_HEIGTH);


        sws_scale(img_convert_ctx, (const uint8_t * const*)frame->data,
                  frame->linesize, 0, dec_ctx->height, dstFrame->data, dstFrame->linesize);
        // dstFrame->pts=frame->pts;
         /* encode the image */
		encode(enc_ctx, dstFrame, encodedPkt, test264);
    	av_frame_free(&dstFrame);
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


int main(int argc, char* argv[])
{

	AVFormatContext	*pFormatCtx;
	int				i, videoindex;
	AVCodecParameters *pCodecParameters;
	AVCodecContext	*pCodecCtx,*pDecodeCodecCtx;
	AVCodec			*pCodec,*pDecodeCodec;

	// av_register_all();
	avformat_network_init();
	pFormatCtx = avformat_alloc_context();
	//Open File
	//char filepath[]="src01_480x272_22.h265";
	//avformat_open_input(&pFormatCtx,filepath,NULL,NULL)

	//Register Device
	avdevice_register_all();
	// avcodec_register_all();
//Windows
#ifdef _WIN32

	//Show Dshow Device
	show_dshow_device();
	//Show Device Options
	show_dshow_device_option();
    //Show VFW Options
    show_vfw_device();

	#if USE_DSHOW
		AVInputFormat *ifmt=av_find_input_format("dshow");
		//Set own video device's name
		if(avformat_open_input(&pFormatCtx,"video=Integrated Camera",ifmt,NULL)!=0){
			printf("Couldn't open input stream.\n");
			return -1;
		}
	#else
		AVInputFormat *ifmt=av_find_input_format("vfwcap");
		if(avformat_open_input(&pFormatCtx,"0",ifmt,NULL)!=0){
			printf("Couldn't open input stream.\n");
			return -1;
		}
	#endif
#elif defined linux
    //Linux
	AVInputFormat *ifmt=av_find_input_format("video4linux2");
	if(avformat_open_input(&pFormatCtx,"/dev/video0",ifmt,NULL)!=0){
		printf("Couldn't open input stream.\n");
		return -1;
	}
#else
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
    if(avformat_open_input(&pFormatCtx,"0",ifmt, &options)!=0){
        printf("Couldn't open input stream.\n");
        return -1;
    }
#endif


	if(avformat_find_stream_info(pFormatCtx,NULL)<0)
	{
		printf("Couldn't find stream information.\n");
		return -1;
	}
	printf("nb_streams=%d.\n",pFormatCtx->nb_streams);
	videoindex=-1;
	for(i=0; i<pFormatCtx->nb_streams; i++) {

		pCodecParameters = pFormatCtx->streams[i]->codecpar;
		if(pCodecParameters->codec_type==AVMEDIA_TYPE_VIDEO)
		{
			videoindex=i;
			break;
		}
	}
	if(videoindex==-1)
	{
		printf("Couldn't find a video stream.\n");
		return -1;
	}

    if (av_find_best_stream(pFormatCtx, AVMEDIA_TYPE_VIDEO, -1, -1, &pDecodeCodec, 0) < 0) {
        fprintf(stderr, "Cannot find a video stream in the input file\n");
        return -1;
    }
	// printf("%d.\n",pDecodeCodec->id);
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

	// pCodec=avcodec_find_encoder_by_name("libx264");
	pCodec=avcodec_find_encoder_by_name("libx265");
	pCodecCtx=avcodec_alloc_context3(pCodec);
	if(pCodec==NULL)
	{
		printf("Codec not found.\n");
		return -1;
	}
	 /* put sample parameters */
    pCodecCtx->bit_rate = pDecodeCodecCtx->bit_rate;
    /* resolution must be a multiple of two */
    pCodecCtx->width = pDecodeCodecCtx->width;
    pCodecCtx->height = pDecodeCodecCtx->height;
	printf("width:%d height:%d.\n",pDecodeCodecCtx->width,pDecodeCodecCtx->height);
    /* frames per second */
    pCodecCtx->time_base = (AVRational){1, 30};
    pCodecCtx->framerate = (AVRational){30, 1};
    /* emit one intra frame every ten frames
     * check frame pict_type before passing frame
     * to encoder, if frame->pict_type is AV_PICTURE_TYPE_I
     * then gop_size is ignored and the output of encoder
     * will always be I frame irrespective to gop_size
     */
    pCodecCtx->gop_size = pDecodeCodecCtx->gop_size;
    pCodecCtx->max_b_frames = pDecodeCodecCtx->max_b_frames;
    pCodecCtx->pix_fmt =  AV_PIX_FMT_YUV420P;
    if (pCodec->id == AV_CODEC_ID_H264){
        av_opt_set(pCodecCtx->priv_data, "preset", "slow", 0);
        av_opt_set(pCodecCtx->priv_data, "tune", "zerolatency", 0);
		// av_dict_set(pCodecCtx->priv_data, "tune", "zerolatency", 0);
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

	// AVFrame	*pFrame;
	// pFrame=av_frame_alloc();
	AVFrame	*pFrame=av_frame_alloc();
	//pFrameYUV=av_frame_alloc();
	//unsigned char *out_buffer=(unsigned char *)av_malloc(avpicture_get_size(AV_PIX_FMT_YUV420P, pCodecCtx->width, pCodecCtx->height));
	//avpicture_fill((AVPicture *)pFrameYUV, out_buffer, AV_PIX_FMT_YUV420P, pCodecCtx->width, pCodecCtx->height);

	int ret, got_picture;

	AVPacket *packet=(AVPacket *)av_malloc(sizeof(AVPacket));
	AVPacket *encodedPkt =  av_packet_alloc();

#if OUTPUT_YUV420P
    FILE *fp_yuv=fopen("output.yuv","wb+");
#endif
    FILE *test264=fopen("test.h264","wb+");

	struct SwsContext *img_convert_ctx;
	img_convert_ctx = sws_getContext(pDecodeCodecCtx->width, pDecodeCodecCtx->height, pDecodeCodecCtx->pix_fmt,
									 pCodecCtx->width, pCodecCtx->height, AV_PIX_FMT_YUV420P, SWS_BICUBIC, NULL, NULL, NULL);

    int quitcount=0;
	for (;;) {
			//------------------------------
			if(av_read_frame(pFormatCtx, packet)>=0){
        		//printf("readed packet.\n");
				if(packet->stream_index==videoindex){
					// int y_size=pCodecParameters->width*pCodecParameters->height;
					// printf("y_size:%d.\n",y_size*2);
					// printf("pktsize:%d.\n",packet->size);
					// fwrite(packet->data,1,packet->size,fp_yuv);
        				quitcount++;
        				if(quitcount>=100){
        					break;
        				}
					// ret = avcodec_decode_video2(pDecodeCodecCtx, pFrame, &got_picture, packet);

                		decodeandencode(pDecodeCodecCtx, pFrame, packet,test264,pCodecCtx,encodedPkt,img_convert_ctx,fp_yuv);
						// printf("pktsize:%d.\n",packet->size);
                		// printf("pkt_size:%d.\n",pFrame->pkt_size);
						// fwrite(pFrame->data[0],1,pFrame->pkt_size,fp_yuv);
				        /* encode the image */
				        // encode(pCodecCtx, pFrame, encodedPkt, test264);

     //    			// printf("readed pFrame.\n");
     //    			if(got_picture){
     //    				quitcount++;
     //    				if(quitcount>=100){
     //    					break;
     //    				}
     //    				printf("got_picture.\n");
     //    				printf("width=%d height=%d.\n",pCodecCtx->width,pCodecCtx->height);
     //    				#if OUTPUT_YUV420P
					// 	int y_size=pCodecCtx->width*pCodecCtx->height;
					// 	// int length=sizeof(pFrame->data)/sizeof(pFrame->data[0]);
     //  //   				printf("length:%d.\n",length);
     //    				printf("width=%d height=%d.\n",pFrame->width,pFrame->height);
     //    				printf("format:%d.\n",pFrame->format);
     //    				printf("format1:%d.\n",AV_PIX_FMT_UYVY422);
     //    				printf("pict_type:%u.\n",pFrame->pict_type);
     //    				printf("linesize:%d.\n",pFrame->linesize[0]);
     //    				printf("linesize:%d.\n",pFrame->linesize[1]);
     //    				printf("y_size:%d.\n",y_size);
     //    				printf("keyframe:%d.\n",pFrame->key_frame);
     //    				printf("rawdata:%d.\n",pFrame->data[0][y_size*2-1]);
					// 	// fwrite(pFrame->data[0],1,y_size*2,fp_yuv);    //Y
					// 	// fwrite(pFrame->data[0],1,pFrame->linesize[0],fp_yuv);    //Y
					// 	// fwrite(pFrame->data[0],1,y_size,fp_yuv);    //Y
					// 	// fwrite(pFrame->data[1],1,y_size/4,fp_yuv);  //U
					// 	// fwrite(pFrame->data[2],1,y_size/4,fp_yuv);  //V
					// 	#endif
     //    			}else{

     //    				printf("no got_picture.\n");
     //    			}
					// if(ret < 0){
					// 	printf("Decode Error.\n");
					// 	return -1;
					// }
				}
				av_packet_unref(packet);
				// printf("pktsize:%d.\n",packet->size);
			}else{
				//Exit Thread
			}
	}

	sws_freeContext(img_convert_ctx);

#if OUTPUT_YUV420P
    fclose(fp_yuv);
#endif

	// av_free(pFrame);
	// avcodec_close(pCodecCtx);
	// avformat_close_input(&pFormatCtx);

	 /* flush the encoder */
    //todo
    // encode(pCodecCtx, NULL, encodedPkt, test264);
    /* add sequence end code to have a real MPEG file */
    uint8_t endcode[] = { 0, 0, 1, 0xb7 };
    fwrite(endcode, 1, sizeof(endcode), test264);
    fclose(test264);
    avcodec_free_context(&pCodecCtx);
    avcodec_free_context(&pDecodeCodecCtx);
    av_frame_free(&pFrame);
    av_packet_free(&encodedPkt);
	avformat_close_input(&pFormatCtx);

	return 0;
}
