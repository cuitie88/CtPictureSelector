package com.ctp.android.library.ps;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.ctp.android.library.ps.activity.CtPictureSelectorActivity;
import com.ctp.android.library.ps.tool.constant.CtIntentAction;

import java.lang.ref.WeakReference;
/**
 * <p>功能描述：入口
 *
 */
public class Ctp
{
    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> mFragment;

    private int numColumns = 3;
    private int maxPictureChooseNumber = 10;

    private Ctp(Activity activity){this(activity,null); }
    private Ctp(Fragment fragment) { this(fragment.getActivity(), fragment); }
    private Ctp(Activity activity, Fragment fragment)
    {
        mContext = new WeakReference<>(activity);
        mFragment = new WeakReference<>(fragment);
    }
    public static Ctp build(Activity activity) { return new Ctp(activity); }
    public static Ctp build(Fragment fragment){ return new Ctp(fragment); }
    /**
     * <p>功能描述：启动UI，并返回参数
     * @param requestCode Activity请求返回标志
     */
    public void startForResult(int requestCode)
    {
        Activity activity = getActivity();
        if (activity == null){return; }

        Intent intent = new Intent(activity, CtPictureSelectorActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(CtIntentAction.PARAMETER_NUMCOLUMNS,numColumns);
        extras.putInt(CtIntentAction.PARAMETER_MAX_PICTURE_CHOOSE_NUMBER, maxPictureChooseNumber);
        intent.putExtras(extras);

        Fragment fragment = getFragment();
        if (fragment != null)
        {
            fragment.startActivityForResult(intent, requestCode);
        }else
        {
            activity.startActivityForResult(intent, requestCode);
        }
    }
    /**
     * 列数
     * @param numColumns
     * @return
     */
    public Ctp numColumns(int numColumns)
    {
        this.numColumns = numColumns;
        return this;
    }
    /**
     * 最大选取图片数
     * @param maxPictureChooseNumber
     * @return
     */
    public Ctp maxPictureNumber(int maxPictureChooseNumber)
    {
        this.maxPictureChooseNumber = maxPictureChooseNumber;
        return this;
    }

    @Nullable Activity getActivity() { return mContext.get(); }

    @Nullable Fragment getFragment() { return mFragment != null ? mFragment.get() : null; }
}
