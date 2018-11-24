package com.ctp.android.library.gv;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ctp.android.R;
import com.ctp.android.library.ps.tool.util.DisplayUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * <p>创建时间：2018/11/24 0024 14:49
 * <p>功能描述：带删除按钮的imageView
 */
public class CtHorizontalScrollPictureLayout extends RelativeLayout
{
    /**
     * 应用文件存放根目录
     */
    public static final String APP_ROOT_DIR = Environment.getExternalStorageDirectory().getPath()+"/ct/ache/";
    /**
     * 应用图片存放路径
     */
    public static final String APP_IMG_DIR = APP_ROOT_DIR+"img/";

    private LinearLayout ll_imagesRoot;
    private ImageView iv_addBtn;
    private int imageWidthPx = 300;
    private int imageHeightPx = 300;
    private LinkedHashMap<String, CtImageWithCancel> map;
    private OnClickAddImageListener onClickAddImageListener;
    private String uploadUrl;
    public CtHorizontalScrollPictureLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.ct_horizontal_scroll_picture_layout, this, true);
        map = new LinkedHashMap<String, CtImageWithCancel>();
        ll_imagesRoot = findViewById(R.id.ct_hsv_pictures_root);
        iv_addBtn = findViewById(R.id.ct_hsv_pictures_add_iv);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CtHorizontalScrollPictureLayout);
        if(attributes != null)
        {
            imageWidthPx = attributes.getDimensionPixelSize(R.styleable.CtHorizontalScrollPictureLayout_image_width, imageWidthPx);
            imageHeightPx = attributes.getDimensionPixelSize(R.styleable.CtHorizontalScrollPictureLayout_image_height, imageHeightPx);
            iv_addBtn.setImageResource(attributes.getResourceId(R.styleable.CtHorizontalScrollPictureLayout_add_button_src, R.drawable.hl_icon_add_picture));
        }
        iv_addBtn.getLayoutParams().width = DisplayUtil.px2dip(context, imageWidthPx);
        iv_addBtn.getLayoutParams().height = DisplayUtil.px2dip(context, imageHeightPx);
        iv_addBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(onClickAddImageListener != null) onClickAddImageListener.onAdd();
            }
        });
    }
    public void setOnClickAddImageListener(OnClickAddImageListener onClickAddImageListener)
    {
        this.onClickAddImageListener = onClickAddImageListener;
    }
    public void addImages(Context context, ArrayList<String> paths)
    {
        if(paths == null || paths.size() <= 0)return;
        for(final String path : paths)
        {
            if(map.get(path) != null)return;
            CtImageWithCancel ctImageWithCancel = new CtImageWithCancel(context)
                    .setImage(context, path)
                    .setOnClickPictureListener(new CtImageWithCancel.OnClickPictureListener()
                    {
                        @Override
                        public void onDeletePicture(String url, View view)
                        {
                            Log.d("CCTV","onDeletePicture !");
                            ll_imagesRoot.removeView(map.get(path));
                            map.remove(path);
                        }
                        @Override
                        public void onReUpload()
                        {
                            MyTask mTask = new MyTask(path);
                            mTask.execute(path,uploadUrl);
                        }
                    }).setImageSize(DisplayUtil.px2dip(context, imageWidthPx), DisplayUtil.px2dip(context, imageHeightPx));
            map.put(path,ctImageWithCancel);
            ll_imagesRoot.addView(map.get(path));
        }
    }
    public void uploadImages(String uploadUrl,String name)
    {
        this.uploadUrl = uploadUrl;
        for(Map.Entry<String, CtImageWithCancel> entry : map.entrySet())
        {
            Log.d("CCTV","path : "+entry.getKey());
            CtImageWithCancel ctImageWithCancel = entry.getValue();
            if(ctImageWithCancel.getUploadResult() != null)continue;
            ctImageWithCancel.initUpload();
            MyTask mTask = new MyTask(entry.getKey());
            mTask.execute(entry.getKey(),uploadUrl,name);
        }
    }
    public ArrayList<CtImageWithCancel> getImages()
    {
        ArrayList<CtImageWithCancel> images = new ArrayList<CtImageWithCancel>();
        for(Map.Entry<String, CtImageWithCancel> entry : map.entrySet())
        {
            images.add(entry.getValue());
        }
        return images;
    }
    public interface OnClickAddImageListener
    {
        void onAdd();
    }
    private class MyTask extends AsyncTask<String, Integer, String>
    {
        private boolean isCancel = false;
        final private String path;
        private MyTask(String path)
        {
            this.path = path;
        }
        @Override
        protected void onPostExecute(String result)
        {
            //最终结果的显示
            //            mTvProgress.setText(result);
            Log.d("CCTV", "result " + result);
            if(result == null)
            {
                if(map.get(path) != null)map.get(path).uploadFail();
            }else
            {
                if(map.get(path) != null)map.get(path).uploadSuccess(result);
            }
        }
        @Override
        protected void onPreExecute()
        {
            //开始前的准备工作
            //            mTvProgress.setText("loading...");
//            Log.d("CCTV", "loading...");
            if(map.get(path) != null)map.get(path).startUpload();
        }
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            //显示进度
            //            mPgBar.setProgress(values[0]);
            //            mTvProgress.setText("loading..." + values[0] + "%");
//            Log.d("CCTV", "values[0] " + values[0]);
            Log.d("CCTV", path + " --> loading..." + values[0] + "%");
            if(map.get(path) == null)
            {
                isCancel = true;
                this.cancel(true);
                return;
            }
            map.get(path).updateUpload(values[0] + "%");
        }
        @Override
        protected String doInBackground(String... params)
        {
            String ret = null;
            //这里params[0]和params[1]是execute传入的两个参数
            String filePath = params[0];
            String uploadUrl = params[1];
            String name = params[2];
            //下面即手机端上传文件的代码
            String end = "\r\n";
            String twoHyphens = "--";
            String boundary = "******";
            try
            {
                //查看目录是否存在，不存在创建
                File f_dir = new File(APP_IMG_DIR);
                if(!f_dir.exists()) f_dir.mkdirs();
                //创建压缩文件存放路径
                File file = new File(filePath);
                String fileName = file.getName();
                String toPath = APP_IMG_DIR + fileName;
                //处理文件并保存
                compressBmpToFile(filePath,toPath);

                URL url = new URL(uploadUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(6 * 1000);
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("Charset", "UTF-8");
                httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + end);
                dos.writeBytes("Content-Disposition: form-data; name=\""+name+"\"; filename=\"" + toPath.substring(toPath.lastIndexOf("/") + 1) + "\"" + end);
                dos.writeBytes(end);
                //获取文件总大小
                FileInputStream fis = new FileInputStream(toPath);
                long total = fis.available();
                byte[] buffer = new byte[819200]; // 8k
                int count = 0;
                int length = 0;
                while((count = fis.read(buffer)) != -1)
                {
                    if(isCancel)
                    {
                        fis.close();
                        dos.close();
                        return ret;
                    }
                    dos.write(buffer, 0, count);
                    //获取进度，调用publishProgress()
                    length += count;
                    publishProgress((int) ((length / (float) total) * 99));
                    //这里是测试时为了演示进度,休眠500毫秒，正常应去掉
                    Thread.sleep(500);
                }
                fis.close();
                dos.writeBytes(end);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
                dos.flush();
                InputStream is = httpURLConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = br.readLine()) != null) {
                    if(isCancel)
                    {
                        dos.close();
                        is.close();
                        return ret;
                    }
                    sb.append(line);
                }
                dos.close();
                is.close();
                ret = sb.toString();
                File f = new File(toPath);
                if(f.exists())f.delete();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            return ret;
        }
    }
    /**
     * <p>创建时间：2018/10/23 0023 09:13
     * <p>功能描述：将图片压缩后存储
     * @param filePath 原图片路径
     * @param path 保存路径
     * @throws Exception
     */
    private void compressBmpToFile(String filePath, String path) throws Exception
    {
        Bitmap bmp = decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 80;//从百分之八十开始压缩
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while(baos.toByteArray().length / 1024 > 500)//压缩500KB以下
        {
            baos = new ByteArrayOutputStream();
            options -= 10;
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        try
        {
            File file = new File(path);
            if(file.exists())
            {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
     * 根据 路径 得到 file 得到 bitmap
     * @param filePath
     * @return
     * @throws IOException
     */
    private Bitmap decodeFile(String filePath) throws IOException
    {
        Bitmap b = null;
        int IMAGE_MAX_SIZE = 600;
        File f = new File(filePath);
        if(f == null)
        {
            return null;
        }
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        FileInputStream fis = new FileInputStream(f);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();
        int scale = 1;
        if(o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE)
        {
            scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }
        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();
        return b;
    }
}
