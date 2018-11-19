package com.ctp.android.library.ps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ctp.android.R;
import com.ctp.android.library.ps.tool.pojo.CtPicture;
import com.ctp.android.library.ps.tool.pojo.CtPictureDir;

import java.util.ArrayList;
public class ChooseDirListAdapter extends BaseAdapter
{
    private ArrayList<CtPictureDir> data = null;
    private Context context = null;
    private String chooseName = "所有图片";
    public ChooseDirListAdapter(ArrayList<CtPictureDir> ctPictureDirs, Context context)
    {
        this.data = ctPictureDirs;
        this.context = context;
    }
    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public Object getItem(int position)
    {
        return data.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup arg2)
    {
        ViewHolder viewHolder = null;
        CtPictureDir ctPictureDir = data.get(position);
        CtPicture firstPicture = ctPictureDir.getFirstPicturePath();
        String name = ctPictureDir.getName();
        if (view == null)
        {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.ct_choose_dir_item, null);
            viewHolder.leftIv = (ImageView) view.findViewById(R.id.ct_choose_dir_left_iv);
            viewHolder.tvName = (TextView) view.findViewById(R.id.ct_choose_dir_title_tv);
            viewHolder.tvNumber = (TextView) view.findViewById(R.id.ct_choose_dir_number_tv);
            viewHolder.tvpath = (TextView) view.findViewById(R.id.ct_choose_dir_path_tv);
            viewHolder.choose = (RadioButton) view.findViewById(R.id.ct_choose_dir_right_iv);
            view.setTag(R.string.app_name,viewHolder);
        } else
        {
            viewHolder = (ViewHolder) view.getTag(R.string.app_name);
        }
        RequestOptions options = new RequestOptions().error(R.drawable.ct_icon_error).placeholder(R.drawable.ct_icon_stub).fallback(R.drawable.ct_icon_empty);
        Glide.with(context).load(firstPicture.getThumbnailUri()).apply(options).into(viewHolder.leftIv);
        viewHolder.tvName.setText(name);
        if("所有图片".equalsIgnoreCase(name))
        {
            viewHolder.tvNumber.setVisibility(View.VISIBLE);
            viewHolder.tvpath.setVisibility(View.GONE);
        }else
        {
            viewHolder.tvNumber.setVisibility(View.VISIBLE);
            viewHolder.tvpath.setVisibility(View.VISIBLE);
            viewHolder.tvpath.setText(ctPictureDir.getPath());
        }
        viewHolder.tvNumber.setText("("+ctPictureDir.getPictures().size()+"张)");
        if(chooseName.equalsIgnoreCase(name))
        {
            viewHolder.choose.setVisibility(View.VISIBLE);
        }else
        {
            viewHolder.choose.setVisibility(View.GONE);
        }
        return view;
    }

    final static class ViewHolder
    {
        ImageView leftIv;
        TextView tvName;
        TextView tvNumber;
        TextView tvpath;
        RadioButton choose;
    }
    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param data
     */
    public void updateListView(ArrayList<CtPictureDir> data)
    {
        this.data = data;
        notifyDataSetChanged();
    }
    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param chooseName
     */
    public void updateListView(String chooseName)
    {
        this.chooseName = chooseName;
        notifyDataSetChanged();
    }
}
