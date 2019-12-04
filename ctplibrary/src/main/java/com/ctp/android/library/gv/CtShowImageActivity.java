package com.ctp.android.library.gv;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.ctp.android.R;
import com.ctp.android.library.ps.view.ZoomImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
public class CtShowImageActivity extends AppCompatActivity
{
    private LinearLayout ll_backBtn;
    private ZoomImageView zoomImageView;

    private String tokenName;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ct_show_image);
        zoomImageView = findViewById(R.id.ct_ctsi_iv);
        ll_backBtn = findViewById(R.id.ct_ctsi_top_left_back_btn_ll);
        ll_backBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        try
        {
            if(getIntent().getExtras() != null)
            {
                final String s_url = getIntent().getExtras().getString("url", null);
                tokenName = getIntent().getExtras().getString("tokenName", null);
                token = getIntent().getExtras().getString("token", null);
                if(s_url != null)
                {
                    if(s_url.startsWith("http"))
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final Bitmap bitmap = getBitMBitmap(s_url);
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        zoomImageView.setImageBitmap(bitmap);
                                    }
                                });
                            }
                        }).start();
                    }else
                    {
                        zoomImageView.setImageBitmap(decodeFile(s_url));
                    }
                    return;
                }
                int i_url = getIntent().getExtras().getInt("url", -1);
                Bitmap bmp= BitmapFactory.decodeResource(getResources(), i_url);
                zoomImageView.setImageBitmap(bmp);
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    private Bitmap decodeFile(String filePath) throws IOException
    {
        Bitmap b = null;
//        int IMAGE_MAX_SIZE = 600;
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
//        if(o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE)
//        {
//            scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
//        }
        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();
        return b;
    }
    /**
     * <p>创建时间：2018/10/23 0023 09:14
     * <p>功能描述：根据路径 转bitmap
     * @param urlpath 图片地址
     * @return
     */
    public Bitmap getBitMBitmap(String urlpath)
    {
        Bitmap map = null;
        try
        {
            URL url = new URL(urlpath);
            URLConnection conn = url.openConnection();
            if(token != null && tokenName != null)conn.setRequestProperty(tokenName, token);
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            map = BitmapFactory.decodeStream(in);
            // TODO Auto-generated catch block
        }catch(IOException e)
        {
            e.printStackTrace();
        }
        return map;
    }
}
