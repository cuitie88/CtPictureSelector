package com.ctp.android.library.gv;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ctp.android.R;
/**
 * <p>创建时间：2018/11/24 0024 14:49
 * <p>功能描述：带删除按钮的imageView
 */
public class CtImageWithCancel extends RelativeLayout
{
    private ImageView iv_bg;
    private LinearLayout ll_cancelBtn;
    private ImageView iv_cancel;
    private LinearLayout ll_uploadBg;
    private TextView tv_upload;
    private Button btn_reUpload;
    private LinearLayout ll_uploadSuccess;
    private RequestOptions options ;
    private String url;
    private String uploadResult;
    private OnClickPictureListener onClickPictureListener;
    public CtImageWithCancel(Context context)
    {
        super(context);
        loadView(context);
    }
    public CtImageWithCancel(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        loadView(context);
    }
    private void loadView(final Context context)
    {
        LayoutInflater.from(context).inflate(R.layout.ct_image_with_cancel, this, true);
        iv_bg = findViewById(R.id.ct_ciwc_iv);
        ll_cancelBtn = findViewById(R.id.ct_ciwc_cancel_ll);
        iv_cancel = findViewById(R.id.ct_ciwc_cancel_iv);
        ll_uploadBg = findViewById(R.id.ct_ciwc_uplaod_bg);
        tv_upload = findViewById(R.id.ct_ciwc_uplaod_tv);
        btn_reUpload = findViewById(R.id.ct_ciwc_uplaod_reupload);
        ll_uploadSuccess = findViewById(R.id.ct_ciwc_uplaod_success_bg);
        setGlideOptions();
        iv_bg.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context,CtShowImageActivity.class);
                Bundle b = new Bundle();
                b.putString("url",url);
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
        ll_uploadBg.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d("CCTV","完事！");
            }
        });
        ll_cancelBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(onClickPictureListener != null)onClickPictureListener.onDeletePicture(url,CtImageWithCancel.this);
            }
        });
        btn_reUpload.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(onClickPictureListener != null)onClickPictureListener.onReUpload();
            }
        });
    }
    private void setGlideOptions()
    {
        options = new RequestOptions()
                .error(R.drawable.ct_icon_error)
                .placeholder(R.drawable.ct_icon_stub)
                .fallback(R.drawable.ct_icon_empty);
    }
    public CtImageWithCancel setImage(Context context,String url)
    {
        this.url = url;
        Glide.with(context).load(this.url).apply(options).into(iv_bg);
        return this;
    }
    public CtImageWithCancel setImage(Context context,int url)
    {
        Glide.with(context).load(url).apply(options).into(iv_bg);
        return this;
    }
    public CtImageWithCancel setImageSize(int width,int height)
    {
        iv_bg.getLayoutParams().width = width;
        iv_bg.getLayoutParams().height = height;
        ll_uploadBg.getLayoutParams().width = width;
        ll_uploadBg.getLayoutParams().height = height;
        ll_uploadSuccess.getLayoutParams().width = width;
        ll_uploadSuccess.getLayoutParams().height = height;
        return this;
    }
    public CtImageWithCancel setOnClickPictureListener(OnClickPictureListener onClickPictureListener)
    {
        this.onClickPictureListener = onClickPictureListener;
        return this;
    }
    public String getUrl()
    {
        return url;
    }
    public String getUploadResult()
    {
        return uploadResult;
    }
    public CtImageWithCancel setUploadResult(String uploadResult)
    {
        this.uploadResult = uploadResult;
        return this;
    }
    public CtImageWithCancel setUploadProgress(String msg)
    {
        tv_upload.setText("---- "+msg+" ----");
        return this;
    }
    public CtImageWithCancel setCancelImage(int ids)
    {
        iv_cancel.setBackgroundResource(ids);
        return this;
    }
    public void initUpload()
    {
        uploadResult = null;
        ll_uploadBg.setVisibility(View.VISIBLE);
        btn_reUpload.setVisibility(View.GONE);
        setUploadProgress("等待上传");
    }
    public void startUpload()
    {
        uploadResult = null;
        ll_uploadBg.setVisibility(View.VISIBLE);
        btn_reUpload.setVisibility(View.GONE);
//        setUploadProgress("开始上传");
    }
    public void updateUpload(String s)
    {
        setUploadProgress(s);
    }
    public void uploadFail()
    {
        ll_uploadBg.setVisibility(View.VISIBLE);
        btn_reUpload.setVisibility(View.VISIBLE);
        setUploadProgress("上传失败");
    }
    public void uploadSuccess(String result)
    {
        uploadResult = result;
        ll_uploadBg.setVisibility(View.GONE);
        btn_reUpload.setVisibility(View.GONE);
        ll_uploadSuccess.setVisibility(View.GONE);

    }
    public CtImageWithCancel showDelete(boolean isDelete)
    {
        if(isDelete)
        {
            ll_cancelBtn.setVisibility( View.VISIBLE );
        }else
        {
            ll_cancelBtn.setVisibility( View.GONE );
        }
        return this;
    }
    public interface OnClickPictureListener
    {
        void onDeletePicture(String url,View view);
        void onReUpload();
    }
}
