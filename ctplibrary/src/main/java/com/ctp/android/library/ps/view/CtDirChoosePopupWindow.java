package com.ctp.android.library.ps.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.ctp.android.R;
import com.ctp.android.library.ps.adapter.ChooseDirListAdapter;
import com.ctp.android.library.ps.tool.pojo.CtPictureDir;
import com.ctp.android.library.ps.tool.util.DisplayUtil;

import java.util.ArrayList;
public class CtDirChoosePopupWindow extends PopupWindow
{
    private ListView listView = null;
    private View mMenuView;
    private ArrayList<CtPictureDir> ctPictureDirs = null;
    private Context context = null;
    private ChooseDirListAdapter adapter = null;
    private OnChooseFinish onChooseFinish = null;
    public CtDirChoosePopupWindow(Context context, ArrayList<CtPictureDir> ctPictureDirs ,OnChooseFinish onChooseFinish)
    {
        super(context);
        this.ctPictureDirs = ctPictureDirs;
        this.context = context;
        this.onChooseFinish = onChooseFinish;
        initData();
        initView();
        initListener();
    }
    private void initData()
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.ct_pw_choose_dir, null);
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(DisplayUtil.dip2px(context,300));
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        adapter = new ChooseDirListAdapter(ctPictureDirs,context);
    }

    private void initView()
    {
        listView = (ListView)mMenuView.findViewById(R.id.ct_choose_dir_content_lv);
    }
    private void initListener()
    {
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                CtPictureDir pictureDir = ctPictureDirs.get(position);
                adapter.updateListView(pictureDir.getName());
                CtDirChoosePopupWindow.this.dismiss();
                if(onChooseFinish != null)onChooseFinish.finish(position);
            }
        });
    }
    public interface OnChooseFinish
    {
        void finish(int position);
    }

}
