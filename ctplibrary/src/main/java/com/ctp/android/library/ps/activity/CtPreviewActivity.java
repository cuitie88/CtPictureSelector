package com.ctp.android.library.ps.activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.ctp.android.R;
import com.ctp.android.library.ps.tool.constant.CtActivityAction;
import com.ctp.android.library.ps.tool.constant.CtReceiverAction;
import com.ctp.android.library.ps.tool.pojo.CtPicture;
import com.ctp.android.library.ps.view.ZoomImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class CtPreviewActivity extends FragmentActivity
{
    private boolean isFirst  = true;
    private int maxPictureChooseNumber = 20;
    private int nowPageNumber = 1;
    private int fromPage = 1;
    private LinearLayout backBtn = null;
    private TextView tv_back = null;
    private TextView tv_btnOk = null;
    private TextView tv_leftNo = null;
    private ViewPager viewPager = null;
    private CheckBox checkBox = null;
    private HashMap<String,CtPicture> mapCtPictures = null;
    private ArrayList<CtPicture> ctPictures = null;
    private ZoomImageView ctMatrixImageView1 = null;
    private ZoomImageView ctMatrixImageView2 = null;
    private ZoomImageView ctMatrixImageView3 = null;
    private CtPreviewViewPaperAdapter ctPreviewViewPaperAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ct_activity_preview);
        try
        {
            initData();
            initView();
            initViewData();
            initListener();
            viewPager.setCurrentItem(fromPage - 1);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    private void initData() throws Exception
    {
        ctMatrixImageView1 = new ZoomImageView(this);
        ctMatrixImageView2 = new ZoomImageView(this);
        ctMatrixImageView3 = new ZoomImageView(this);
        ctPreviewViewPaperAdapter = new CtPreviewViewPaperAdapter();
        Bundle bundle = getIntent().getExtras();
        if(bundle == null){error();return;}
        maxPictureChooseNumber = bundle.getInt(CtActivityAction.CT_KEY_MAX_NUMBER);
        int jump = bundle.getInt(CtActivityAction.CT_KEY_JUMP_PREVIEW_FOR_BOTTON);
        mapCtPictures = (HashMap<String,CtPicture>)bundle.getSerializable(CtActivityAction.CT_KEY_NOW_CHOOSE_PICTURES);
        if(jump == CtActivityAction.CT_VALUE_JUMP_PREVIEW_FOR_BOTTON_PREVIEW)
        {
            if(mapCtPictures == null || mapCtPictures.size() <= 0){error();return;}
            ctPictures = new ArrayList<CtPicture>();
            for(Map.Entry<String, CtPicture> entry : mapCtPictures.entrySet())
            {
                ctPictures.add(entry.getValue());
            }
        }else if(jump == CtActivityAction.CT_VALUE_JUMP_PREVIEW_FOR_BOTTON_IMAGEVIEW)
        {
            fromPage = bundle.getInt(CtActivityAction.CT_KEY_JUMP_FROM_NO);
            ctPictures = (ArrayList<CtPicture>)bundle.getSerializable(CtActivityAction.CT_KEY_NOW_ALL_PICTURES);
        }else
        {
            error();return;
        }
        if(ctPictures == null || ctPictures.size() <= 0){error();return;}
    }
    private void initView() throws Exception
    {
        viewPager = (ViewPager)findViewById(R.id.ct_image_viewpager);viewPager.setOffscreenPageLimit(1);
        backBtn = (LinearLayout)findViewById(R.id.ct_top_left_back);
        tv_back = (TextView)findViewById(R.id.ct_top_left_back_iv);
        tv_btnOk = (TextView)findViewById(R.id.ct_top_left_ok_btn);
        tv_leftNo = (TextView)findViewById(R.id.ct_top_left_back_tv);
        checkBox = (CheckBox)findViewById(R.id.ct_bottom_cb);
    }
    private void initListener() throws Exception
    {
        backBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CtPreviewActivity.this.finish();
            }
        });
        tv_btnOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int no = mapCtPictures.size();
                if(no <= 0)return;
                if(no > maxPictureChooseNumber)return;
                CtPreviewActivity.this.sendBroadcast(new Intent(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_FINISH_FOR_RESULT));
                CtPreviewActivity.this.finish();
            }
        });
        viewPager.setAdapter(ctPreviewViewPaperAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int page)
            {
                try
                {
                    RequestOptions options = new RequestOptions().error(R.drawable.ct_icon_error).placeholder(R.drawable.ct_icon_stub).fallback(R.drawable.ct_icon_empty);
                    if(page % 3 == 0)
                    {
                        ctMatrixImageView1.setImageBitmap(decodeFile(ctPictures.get(page).getPath()));
                    }else if(page % 3 == 1)
                    {
                        ctMatrixImageView2.setImageBitmap(decodeFile(ctPictures.get(page).getPath()));
                    }else
                    {
                        ctMatrixImageView3.setImageBitmap(decodeFile(ctPictures.get(page).getPath()));
                    }
                    nowPageNumber = page+1;
                    changeLeftTopTv();
                    changeCheckBox();
                }catch(Exception e){e.printStackTrace();}
            }
            @Override public void onPageScrollStateChanged(int state) {}
        });
        checkBox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    CtPicture picture = ctPictures.get(nowPageNumber-1);
                    if(checkBox.isChecked())
                    {
                        if(mapCtPictures.size() >= maxPictureChooseNumber)
                        {
                            Toast toast = Toast.makeText(CtPreviewActivity.this, "选择图片不能超过"+maxPictureChooseNumber+"张！", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 50);
                            toast.show();
                            checkBox.setChecked(false);
                            return;
                        }
                        if(mapCtPictures.get(picture.getPath()) != null)mapCtPictures.remove(picture.getPath());
                        mapCtPictures.put(picture.getPath(),picture);
                        Intent intent = new Intent(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_ADD_CHOOSE);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("picture",picture);
                        intent.putExtras(bundle);
                        CtPreviewActivity.this.sendBroadcast(intent);
                    }else
                    {
                        mapCtPictures.remove(picture.getPath());
                        Intent intent = new Intent(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_DELETE_CHOOSE);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("picture",picture);
                        intent.putExtras(bundle);
                        CtPreviewActivity.this.sendBroadcast(intent);
                    }
                    changeOkBtn();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    private void initViewData() throws Exception
    {
        changeLeftTopTv();
        changeOkBtn();
        int i = fromPage -1;
        RequestOptions options = new RequestOptions().error(R.drawable.ct_icon_error).placeholder(R.drawable.ct_icon_stub).fallback(R.drawable.ct_icon_empty);
        if(i % 3 == 0)
        {
            ctMatrixImageView1.setImageBitmap(decodeFile(ctPictures.get(i).getPath()));
        }else if(i % 3 == 1)
        {
            ctMatrixImageView2.setImageBitmap(decodeFile(ctPictures.get(i).getPath()));
        }else
        {
            ctMatrixImageView3.setImageBitmap(decodeFile(ctPictures.get(i).getPath()));
        }
        changeCheckBox();
    }
    private void changeLeftTopTv()
    {
        tv_leftNo.setText(nowPageNumber+"/"+ctPictures.size());
    }
    /**
     * 顶部右侧按钮状态更改
     */
    private void changeOkBtn() throws Exception
    {
        int no = mapCtPictures.size();
        if(no <= 0)
        {
            tv_btnOk.setText("完成");
            tv_btnOk.setBackgroundResource(R.drawable.ct_shape_btn_right_green_dis);
        }else
        {
            tv_btnOk.setText("(" + no + "/" + maxPictureChooseNumber + ")"+"完成");
            tv_btnOk.setBackgroundResource(R.drawable.ct_btn_right_green_selector);
        }
    }
    private void changeCheckBox()
    {
        CtPicture now_ctPicture = ctPictures.get(nowPageNumber - 1);
        checkBox.setChecked(mapCtPictures.get(now_ctPicture.getPath()) != null);
    }
    private void error() throws Exception
    {
        Toast toast = Toast.makeText(this, "预览图片失败！", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 50);
        toast.show();
        finish();
    }
    public class CtPreviewViewPaperAdapter extends PagerAdapter
    {
        //viewpager中的组件数量
        @Override
        public int getCount() {
            return ctPictures.size();
        }
        //滑动切换的时候销毁当前的组件
        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            ((ViewPager) container).removeView((ImageView)object);
        }
        //每次滑动的时候生成的组件
        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            if(position % 3 == 0)
            {
//                ((ViewPager) container).removeView(ctMatrixImageView3);
                if(!isFirst)
                {
                    ctMatrixImageView1= new ZoomImageView(CtPreviewActivity.this);
                }else
                {
                    isFirst = false;
                }
                ((ViewPager) container).addView(ctMatrixImageView1);
                return ctMatrixImageView1;
            }else if(position % 3 == 1)
            {
                if(!isFirst)
                {
                    ctMatrixImageView2= new ZoomImageView(CtPreviewActivity.this);
                }else
                {
                    isFirst = false;
                }
                ((ViewPager) container).addView(ctMatrixImageView2);
                return ctMatrixImageView2;
            }else
            {
                if(!isFirst)
                {
                    ctMatrixImageView3= new ZoomImageView(CtPreviewActivity.this);
                }else
                {
                    isFirst = false;
                }
                ((ViewPager) container).addView(ctMatrixImageView3);
                return ctMatrixImageView3;
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
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
