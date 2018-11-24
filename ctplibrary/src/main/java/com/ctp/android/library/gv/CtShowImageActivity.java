package com.ctp.android.library.gv;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ctp.android.R;
import com.ctp.android.library.ps.view.ZoomImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
public class CtShowImageActivity extends AppCompatActivity
{
    private ZoomImageView zoomImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ct_show_image);
        zoomImageView = findViewById(R.id.ct_ctsi_iv);
        try
        {
            if(getIntent().getExtras() != null)
            {
                String s_url = getIntent().getExtras().getString("url", null);
                if(s_url != null)
                {
                    zoomImageView.setImageBitmap(decodeFile(s_url));
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
