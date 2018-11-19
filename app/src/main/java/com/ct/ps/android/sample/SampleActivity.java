package com.ct.ps.android.sample;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ctp.android.library.ps.Ctp;
import com.ctp.android.library.ps.CtpDataHelper;
import com.ctp.android.library.ps.tool.constant.CtActivityAction;
import com.ctp.android.library.ps.tool.pojo.CtPicture;

import java.util.ArrayList;
public class SampleActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE = 0x123;

    private Button button ;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Ctp.build(SampleActivity.this)//创建实例
                        .numColumns(2)//设置显示列数
                        .maxPictureNumber(10)//设置最大选取图片数
                        .startForResult(REQUEST_CODE);//设置Activity返回值requestCode
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == CtActivityAction.CT_RESULT_CODE)
        {
            ArrayList<String> paths =  CtpDataHelper.formatDataToPath(data);
            StringBuffer sb = new StringBuffer();

            //获取路径集合
            for(String path : paths)
            {
                sb.append("path : "+path);
                sb.append("\n");
            }

            sb.append("-------------------------------------------\n");
            sb.append("-------------------------------------------\n");

            //获取完整的图片对象集合
            ArrayList<CtPicture> ctPictures = CtpDataHelper.formatDataToCtPicture(data);
            for(CtPicture ctPicture : ctPictures)
            {
                sb.append("[\n");
                sb.append("\tname = "+ctPicture.getName());
                sb.append("\n");
                sb.append("\tpath = "+ctPicture.getPath());
                sb.append("\n");
                sb.append("\tparent = "+ctPicture.getParent());
                sb.append("\n");
                sb.append("\toriginalUri = "+ctPicture.getOriginalUri());
                sb.append("\n");
                sb.append("\tthumbnailUri = "+ctPicture.getThumbnailUri());
                sb.append("\n");
                sb.append("\torientation = "+ctPicture.getOrientation());
                sb.append("\n");
                sb.append("]\n");
            }

            textView.setText(sb.toString());

        }
    }
}
