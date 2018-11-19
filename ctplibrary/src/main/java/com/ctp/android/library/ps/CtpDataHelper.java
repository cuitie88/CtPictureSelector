package com.ctp.android.library.ps;
import android.content.Intent;
import android.os.Bundle;

import com.ctp.android.library.ps.activity.CtPictureSelectorActivity;
import com.ctp.android.library.ps.tool.pojo.CtPicture;

import java.util.ArrayList;
public class CtpDataHelper
{
    public static ArrayList<String> formatDataToPath(Intent data)
    {
        ArrayList<String> paths = null;
        if(data != null)
        {
            Bundle bundle = data.getExtras();
            if(data.getExtras() != null)
            {
                paths = bundle.getStringArrayList("path");
            }
        }
        return paths;
    }
    public static ArrayList<CtPicture> formatDataToCtPicture(Intent data)
    {
        ArrayList<CtPicture> list_path = null;
        if(data != null)
        {
            Bundle bundle = data.getExtras();
            if(data.getExtras() != null)
            {
                list_path = bundle.getParcelableArrayList(CtPictureSelectorActivity.CT_KEY_BACK_PICTURES);
            }
        }
        return list_path;
    }
}
