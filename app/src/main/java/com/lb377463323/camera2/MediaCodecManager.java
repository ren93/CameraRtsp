package com.lb377463323.camera2;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG;

/**
 * @author Created by Renny on on 2018/6/1
 *         description:
 */

public class MediaCodecManager {
    MediaCodec mediaCodec;
    private int count = 0;
    public static final byte[] frame = new byte[230400];
    FileOutputStream fileOutputStream;
    public MediaCodecManager(Context context){
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            Log.d("jinwei",codecInfo.getName());
        }
        initCodec();
        try {
            File file=new File(Environment.getExternalStorageDirectory(), "test.avc");
            if(!file.exists()){
                file.createNewFile();
            }
            fileOutputStream  = new FileOutputStream(file,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void initCodec(){
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 480, 320);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        mediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {
                Log.d("jinwei","onInputBufferAvailable:"+index);
                ByteBuffer byteBuffer = codec.getInputBuffer(index);
                byteBuffer.put(frame);
                codec.queueInputBuffer(index,0,frame.length,1,BUFFER_FLAG_CODEC_CONFIG);
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                Log.d("jinwei","onOutputBufferAvailable:"+index);
                if(index>-1){
                    ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                    byte[] bb = new byte[info.size];
                    outputBuffer.get(bb);
                    try {
                        fileOutputStream.write(bb);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    codec.releaseOutputBuffer(index,false);
                }
            }

            @Override
            public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                Log.d("jinwei","onError");
                codec.reset();
            }

            @Override
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                Log.d("jinwei","onOutputFormatChanged");
            }
        });
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
    }
    public void release(){
        mediaCodec.release();
    }
}
