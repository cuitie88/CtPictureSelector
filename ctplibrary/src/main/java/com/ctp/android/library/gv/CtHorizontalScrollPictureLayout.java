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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
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
    private UploadDataParser uploadDataParser;
    private int imageSize = 500;
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
    public void addImages(Context context, ArrayList<String> paths, final String mame,final String paramKey,final String paramVlaue)
    {
        if(paths == null || paths.size() <= 0)return;
        for(final String path : paths)
        {
            if(map.get(path) != null)continue;
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
                            MyTaskTow mTask = new MyTaskTow(path);
                            mTask.execute(path,uploadUrl,mame,paramKey,paramVlaue);
                        }
                    }).setImageSize(DisplayUtil.px2dip(context, imageWidthPx), DisplayUtil.px2dip(context, imageHeightPx));
            map.put(path,ctImageWithCancel);
            ll_imagesRoot.addView(map.get(path));
        }
    }
    public void addImages(Context context, ArrayList<String> paths, String echoServer, final String mame,final String paramKey,final String paramVlaue)
    {
        addImages( context ,paths ,echoServer ,mame,paramKey,paramVlaue,true);
    }
    public void addImages(Context context, ArrayList<String> paths, String echoServer, final String mame,final String paramKey,final String paramVlaue,boolean isDelete)
    {
        addImages(context, paths, echoServer, mame,paramKey,paramVlaue,isDelete,false);
    }
    public void addImages(Context context, ArrayList<String> paths, String echoServer, final String mame,final String paramKey,final String paramVlaue,boolean isDelete,boolean isCanAdd)
    {
        if(isCanAdd)
        {
            iv_addBtn.setVisibility(View.VISIBLE);
        }else
        {
            iv_addBtn.setVisibility(View.GONE);
        }
        if(paths == null || paths.size() <= 0)return;
        for(final String path : paths)
        {
            if(map.get(path) != null)continue;
            CtImageWithCancel ctImageWithCancel = new CtImageWithCancel(context)
                    .setImage(context, echoServer+path)
                    .showDelete( isDelete )
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
                            MyTaskTow mTask = new MyTaskTow(path);
                            mTask.execute(path,uploadUrl,mame,paramKey,paramVlaue);
                        }
                    }).setImageSize(DisplayUtil.px2dip(context, imageWidthPx), DisplayUtil.px2dip(context, imageHeightPx)).setUploadResult(path);
            map.put(path,ctImageWithCancel);
            ll_imagesRoot.addView(map.get(path));
        }
    }
    public void uploadImages(String uploadUrl,String name,String paramKey,String paramVlaue)
    {
        this.uploadUrl = uploadUrl;
        for(Map.Entry<String, CtImageWithCancel> entry : map.entrySet())
        {
            Log.d("CCTV","path : "+entry.getKey());
            CtImageWithCancel ctImageWithCancel = entry.getValue();
            if(ctImageWithCancel.getUploadResult() != null)continue;
            ctImageWithCancel.initUpload();
            MyTaskTow mTask = new MyTaskTow(entry.getKey());
            mTask.execute(entry.getKey(),uploadUrl,name,paramKey,paramVlaue);
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
    public void clear()
    {
        if(map != null)map.clear();
        if(ll_imagesRoot != null)ll_imagesRoot.removeAllViews();
    }
    public interface OnClickAddImageListener
    {
        void onAdd();
    }
    /**
     * <p>创建时间：2018/10/23 0023 09:13
     * <p>功能描述：将图片压缩后存储
     * @param filePath 原图片路径
     * @param path 保存路径
     * @param size     图片大小(KB)
     * @throws Exception
     */
    private void compressBmpToFile(String filePath, String path,int size) throws Exception
    {
        Bitmap bmp = decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;//从百分之八十开始压缩
        bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while(baos.toByteArray().length / 1024 > size)//压缩500KB以下
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
            Log.d("CCTV","f.length() : "+f.length());
            if((f.length() / 1024) > 500)
            {
                Log.d("CCTV","???????????");
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }
        }
        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();
        return b;
    }
    public void setUploadDataParser(UploadDataParser uploadDataParser)
    {
        this.uploadDataParser = uploadDataParser;
    }
    public interface UploadDataParser
    {
        String parseData(String data) throws Exception;
    }
    public void setImageSize(int imageSize)
    {
        this.imageSize = imageSize;
    }




    private class MyTaskTow extends AsyncTask<String, Integer, String>
    {
        private boolean isCancel = false;
        final private String path;
        private MyTaskTow(String path)
        {
            this.path = path;
        }
        @Override
        protected void onPostExecute(String result)
        {
            //最终结果的显示
            //            mTvProgress.setText(result);

            Log.d("CCTV", "result11111111111111 " + result);
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
                if(map.get(path) != null)map.get(path).uploadSuccess(result);
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
            try {
                //这里params[0]和params[1]是execute传入的两个参数
                String filePath = params[0];
                String url = params[1];
                String name = params[2];
                String paramKey = params[3];
                String paramVlaue = params[4];
                //查看目录是否存在，不存在创建
                File f_dir = new File(APP_IMG_DIR);
                if(!f_dir.exists()) f_dir.mkdirs();
                //创建压缩文件存放路径
                File file2 = new File(filePath);
                String fileName = file2.getName();
                String toPath = APP_IMG_DIR + fileName;
                //处理文件并保存
                compressBmpToFile(filePath,toPath,imageSize);

                ArrayList<String> list = new ArrayList<>();
                list.add(toPath);


                String boundary = UUID.randomUUID().toString(); //边界标识 随机生成
                //            String url = "http://zuulga.hbjzz.com:7200/api-population-file/";
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("Charsert", "UTF-8");
                conn.setRequestProperty("Content-Type","multipart/form-data;boundary=" + boundary);
                // 指定流的大小，当内容达到这个值的时候就把流输出
                conn.setChunkedStreamingMode(10240);
                OutputStream out = new DataOutputStream(conn.getOutputStream());
                byte[] end_data = ("\r\n--" + boundary + "--\r\n").getBytes();// 定义最后数据分隔线
                StringBuilder sb = new StringBuilder();
                //添加form属性
                sb.append("--");
                sb.append(boundary);
                sb.append("\r\n");
                sb.append("Content-Disposition: form-data; name=\""+paramKey+"\"");
                sb.append("\r\n\r\n");
                sb.append(paramVlaue);
                out.write(sb.toString().getBytes("utf-8"));
                out.write("\r\n".getBytes("utf-8"));

                int leng = list.size();
                for (int i = 0; i < leng; i++) {
                    String fname = list.get(i);
                    File file = new File(fname);
                    sb = new StringBuilder();
                    sb.append("--");
                    sb.append(boundary);
                    sb.append("\r\n");
                    sb.append("Content-Disposition: form-data;name=\""+name+"\";filename=\"" + file.getName() + "\"\r\n");
                    sb.append("Content-Type:application/octet-stream\r\n\r\n");
                    byte[] data = sb.toString().getBytes();
                    out.write(data);
                    DataInputStream in = new DataInputStream(new FileInputStream(
                            file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
                    in.close();
                }
                out.write(end_data);
                out.flush();
                out.close();
                // 定义BufferedReader输入流来读取URL的响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String line = null;
                StringBuffer stringBuffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {

                    Log.d("CCTV","line : "+line);

                    stringBuffer.append(line);
                }
                ret = stringBuffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ret;
        }
    }
}
