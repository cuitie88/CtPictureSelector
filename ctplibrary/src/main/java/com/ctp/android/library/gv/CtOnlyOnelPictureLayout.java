package com.ctp.android.library.gv;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.ctp.android.library.ps.Ctp;
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
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * <p>创建时间：2018/11/28 0028 13:38
 * <p>创建者：  CuiTie
 * <p>功能描述：
 */
public class CtOnlyOnelPictureLayout extends RelativeLayout
{
    private LinearLayout ll_deleteBtn;
    private LinearLayout ll_magnifierBtn;
    private ImageView iv_delete;
    private ImageView iv_magnifier;
    private String uploadUrl;
    private LinkedHashMap<String, CtImageWithCancel> map;
    private CtHorizontalScrollPictureLayout.UploadDataParser uploadDataParser;

    private ImageView iv;
    private Context context;
    public CtOnlyOnelPictureLayout(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        this.context = ctx;
        LayoutInflater.from(context).inflate(R.layout.ct_only_one_picture_layout, this, true);
        map = new LinkedHashMap<String, CtImageWithCancel>();
        iv = findViewById(R.id.ct_oorl_iv);

        ll_deleteBtn = findViewById(R.id.ct_oorl_delete);
        ll_magnifierBtn = findViewById(R.id.ct_oorl_magnifier);
        iv_delete = findViewById(R.id.ct_oorl_delete_iv);
        iv_magnifier = findViewById(R.id.ct_oorl_magnifier_iv);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CtOnlyOnelPictureLayout);
        if(attributes != null)
        {
            iv.setBackgroundResource(attributes.getResourceId(R.styleable.CtOnlyOnelPictureLayout_image_background, R.drawable.hl_bg_id_card_front_photo));
        }

        ll_deleteBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clearImage();
                ll_deleteBtn.setClickable(false);
                ll_magnifierBtn.setClickable(false);
                iv_delete.setBackgroundResource(R.drawable.ct_icon_delete_white);
                iv_magnifier.setBackgroundResource(R.drawable.ct_icon_magnifier_white);
                iv.setClickable(true);
                map.clear();
            }
        });
        iv_magnifier.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                for(Map.Entry<String, CtImageWithCancel> entry : map.entrySet())
                {
                    Intent intent = new Intent(context,CtShowImageActivity.class);
                    Bundle b = new Bundle();
                    b.putString("url",entry.getValue().getUrl());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            }
        });

    }
    public void setOnClickChooseImageListener(OnClickListener onClickListener)
    {
        iv.setOnClickListener(onClickListener);
    }
    private void setImage(Context context,String url)
    {
        RequestOptions options = new RequestOptions()
                .error(R.drawable.ct_icon_error)
                .placeholder(R.drawable.ct_icon_stub)
                .fallback(R.drawable.ct_icon_empty);
        Glide.with(context).load(url).apply(options).into(iv);
        ll_deleteBtn.setClickable(true);
        ll_magnifierBtn.setClickable(true);
        iv_delete.setBackgroundResource(R.drawable.ct_icon_delete);
        iv_magnifier.setBackgroundResource(R.drawable.ct_icon_magnifier);
        iv.setClickable(false);
    }
    public void addImages(Context context, String path)
    {
        if(path == null || path.trim().length() <= 0)return;
        if(map.get(path) != null)return;
        CtImageWithCancel ctImageWithCancel = new CtImageWithCancel(context)
                .setImage(context, path);
        map.put(path,ctImageWithCancel);
        setImage(context, map.get(path).getUrl());
    }
    public void addImages(Context context, String path,String echoServer)
    {
        if(path == null || path.trim().length() <= 0)return;
        if(map.get(path) != null)return;
        CtImageWithCancel ctImageWithCancel = new CtImageWithCancel(context)
                .setImage(context, echoServer+path)
                .setUploadResult(path);
        map.put(path,ctImageWithCancel);
        setImage(context, map.get(path).getUrl());
    }
    public void uploadImages(String uploadUrl,String name,String echoServer)
    {
        this.uploadUrl = uploadUrl;
        for(Map.Entry<String, CtImageWithCancel> entry : map.entrySet())
        {
            Log.d("CCTV","path : "+entry.getKey());
            CtImageWithCancel ctImageWithCancel = entry.getValue();
            if(ctImageWithCancel.getUploadResult() != null)continue;
            MyTask mTask = new MyTask(entry.getKey(),echoServer);
            mTask.execute(entry.getKey(),uploadUrl,name);
        }
    }
    public void clearImage()
    {
        iv.setImageResource(0);
    }
    public CtImageWithCancel getImages()
    {
        if(map == null || map.size() <= 0)return null;
        ArrayList<CtImageWithCancel> images = new ArrayList<CtImageWithCancel>();
        for(Map.Entry<String, CtImageWithCancel> entry : map.entrySet())
        {
            images.add(entry.getValue());
        }
        return images.get(0);
    }
    private class MyTask extends AsyncTask<String, Integer, String>
    {
        private boolean isCancel = false;
        final private String path;
        private String echoServer;
        private MyTask(String path,String echoServer)
        {
            this.path = path;
            this.echoServer = echoServer;
        }
        @Override
        protected void onPostExecute(String result)
        {
            //最终结果的显示
            //            mTvProgress.setText(result);
            if(result == null)
            {
                if(map.get(path) != null)map.get(path).uploadFail();
            }else
            {
                if(uploadDataParser != null)
                {
                    try
                    {
                        result = uploadDataParser.parseData(result);
                        Log.d("CCTV", "result2 " + result);
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if(map.get(path) != null)
                {
                    map.get(path).uploadSuccess(result);
                    String url = echoServer+map.get(path).getUploadResult();
                    Log.d("CCTV","url : "+url);
                    setImage(context, url);
                }
            }
            Log.d("CCTV", "result " + result);
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
                File f_dir = new File(CtHorizontalScrollPictureLayout.APP_IMG_DIR);
                if(!f_dir.exists()) f_dir.mkdirs();
                //创建压缩文件存放路径
                File file = new File(filePath);
                String fileName = file.getName();
                String toPath = CtHorizontalScrollPictureLayout.APP_IMG_DIR + fileName;
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
    public void setUploadDataParser(CtHorizontalScrollPictureLayout.UploadDataParser uploadDataParser)
    {
        this.uploadDataParser = uploadDataParser;
    }
}
