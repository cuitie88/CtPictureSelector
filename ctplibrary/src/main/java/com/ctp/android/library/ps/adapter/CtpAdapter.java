package com.ctp.android.library.ps.adapter;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ctp.android.R;
import com.ctp.android.library.ps.tool.pojo.CtPicture;
import com.ctp.android.library.ps.tool.pojo.CtPictureDir;

import java.util.HashMap;
public class CtpAdapter extends BaseAdapter
{
    /**
     * 图片集合，key为图片的path
     */
    private HashMap<String, CtPicture> mapCtPictures = null;
    /**
     * 上下文
     */
    private Context context;
    /**
     * 当前展示的图片的文件夹
     */
    private CtPictureDir dir;
    /**
     * 布局
     */
    private LayoutInflater inflater;
    /**
     * 可以选择的图片最大数
     */
    private int maxPictureChooseNumber;
    /**
     * 点击事件监听
     */
    private OnCbChangeListener onCbChangeListener;
    /**
     * 列数
     */
    private int numColumns;
    /**
     * <p>功能描述：构造方法 初始化数据
     * @param context 上下文
     * @param inflater 布局
     * @param dir 图片集合所在文件夹
     * @param maxPictureChooseNumber 最大可选数量
     * @param numColumns 列数
     */
    public CtpAdapter(Context context, LayoutInflater inflater, CtPictureDir dir, int maxPictureChooseNumber, int numColumns)
    {
        this.context = context.getApplicationContext();
        this.inflater = inflater;
        this.dir = dir;
        this.maxPictureChooseNumber = maxPictureChooseNumber;
        this.numColumns = numColumns;
        mapCtPictures = new HashMap<String, CtPicture>();
    }
    @Override
    public int getCount()
    {
        return dir.getPictures().size()+1;
    }
    @Override
    public CtPicture getItem(int i)
    {
        if(i == 0)return null;
        return dir.getPictures().get(i-1);
    }
    @Override
    public long getItemId(int i)
    {
        return 0;
    }
    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup)
    {
        try
        {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();if(i == 0)
            {
                convertView = inflater.inflate(R.layout.ct_picture_selector_gv_taka_photo_item, null);
                LinearLayout root = convertView.findViewById(R.id.ps_ll);
                root.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dm.widthPixels / numColumns));
                root.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        onCbChangeListener.takePhoto();
                    }
                });
                return convertView;
            }




            final ViewHolder viewHolder;
            if(convertView == null || convertView.getTag() == null)
            {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.ct_picture_selector_gv_item, null);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.ps_iv);
                viewHolder.cbBg = (RelativeLayout) convertView.findViewById(R.id.ps_cb_bg);
                viewHolder.cbBg.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dm.widthPixels / numColumns));
                viewHolder.cbBtn = (LinearLayout) convertView.findViewById(R.id.ps_cb_bgbtn);
                viewHolder.cb = (CheckBox) convertView.findViewById(R.id.ps_cb);
                convertView.setTag(R.string.app_name, viewHolder);
            }else
            {
                viewHolder = (ViewHolder) convertView.getTag(R.string.app_name);
            }
            final CtPicture picture = dir.getPictures().get(i-1);
            RequestOptions options = new RequestOptions().error(R.drawable.ct_icon_error).placeholder(R.drawable.ct_icon_stub).fallback(R.drawable.ct_icon_empty);
            Glide.with(context).load(picture.getThumbnailUri()).apply(options).into(viewHolder.imageView);
            viewHolder.imageView.setTag(picture);
            if(mapCtPictures.get(picture.getPath()) != null)
            {
                viewHolder.cbBg.setBackgroundResource(R.color.ctColorBg3);
                viewHolder.cb.setChecked(true);
            }else
            {
                viewHolder.cbBg.setBackgroundResource(R.color.ctColorBottomBgDef);
                viewHolder.cb.setChecked(false);
            }
            viewHolder.cbBg.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(onCbChangeListener != null)
                        onCbChangeListener.doshowAllImagePreview( i-1);
                }
            });
            viewHolder.cbBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    boolean isChecked = viewHolder.cb.isChecked();
                    viewHolder.cb.setChecked(!isChecked);
                    if(viewHolder.cb.isChecked())
                    {
                        if(mapCtPictures.size() >= maxPictureChooseNumber)
                        {
                            Toast toast = Toast.makeText(context, "选择图片不能超过" + maxPictureChooseNumber + "张！", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 50);
                            toast.show();
                            viewHolder.cb.setChecked(false);
                            return;
                        }
                        if(mapCtPictures.get(picture.getPath()) != null)
                            mapCtPictures.remove(picture.getPath());
                        mapCtPictures.put(picture.getPath(), picture);
                        viewHolder.cbBg.setBackgroundResource(R.color.ctColorBg3);
                    }else
                    {
                        mapCtPictures.remove(picture.getPath());
                        viewHolder.cbBg.setBackgroundResource(R.color.ctColorBottomBgDef);
                    }
                    if(onCbChangeListener != null)
                        onCbChangeListener.changeTopRightBtnAndBottomRightBtn(mapCtPictures);
                }
            });
            viewHolder.cb.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(viewHolder.cb.isChecked())
                    {
                        if(mapCtPictures.size() >= maxPictureChooseNumber)
                        {
                            Toast toast = Toast.makeText(context, "选择图片不能超过" + maxPictureChooseNumber + "张！", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 50);
                            toast.show();
                            viewHolder.cb.setChecked(false);
                            return;
                        }
                        if(mapCtPictures.get(picture.getPath()) != null)
                            mapCtPictures.remove(picture.getPath());
                        mapCtPictures.put(picture.getPath(), picture);
                        viewHolder.cbBg.setBackgroundResource(R.color.ctColorBg3);
                    }else
                    {
                        mapCtPictures.remove(picture.getPath());
                        viewHolder.cbBg.setBackgroundResource(R.color.ctColorBottomBgDef);
                    }
                    if(onCbChangeListener != null)
                        onCbChangeListener.changeTopRightBtnAndBottomRightBtn(mapCtPictures);
                }
            });
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return convertView;
    }
    private class ViewHolder
    {
        ImageView imageView;
        RelativeLayout cbBg;
        LinearLayout cbBtn;
        CheckBox cb;
    }
    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param data
     */
    public void updateGirdView(HashMap<String, CtPicture> mapCtPictures,CtPictureDir data)
    {
        this.mapCtPictures = mapCtPictures;
        this.dir = data;
        notifyDataSetChanged();
    }
    public CtpAdapter setOnCbChangeListener(OnCbChangeListener onCbChangeListener)
    {
        this.onCbChangeListener = onCbChangeListener;
        return this;
    }
    public interface OnCbChangeListener
    {
        void doshowAllImagePreview(int i);
        void changeTopRightBtnAndBottomRightBtn(HashMap<String, CtPicture> mapCtPictures);
        void takePhoto();
    }
}
