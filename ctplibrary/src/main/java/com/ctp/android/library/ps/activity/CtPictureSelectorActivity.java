package com.ctp.android.library.ps.activity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctp.android.R;
import com.ctp.android.library.ps.adapter.CtpAdapter;
import com.ctp.android.library.ps.tool.constant.CtActivityAction;
import com.ctp.android.library.ps.tool.constant.CtIntentAction;
import com.ctp.android.library.ps.tool.constant.CtReceiverAction;
import com.ctp.android.library.ps.tool.helper.CtPictureHelper;
import com.ctp.android.library.ps.tool.pojo.CtPicture;
import com.ctp.android.library.ps.tool.pojo.CtPictureDir;
import com.ctp.android.library.ps.tool.util.CtMeasureUtils;
import com.ctp.android.library.ps.tool.util.CtStringUtils;
import com.ctp.android.library.ps.tool.util.PermissionUtil;
import com.ctp.android.library.ps.view.CtDirChoosePopupWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
public class CtPictureSelectorActivity extends AppCompatActivity
{
    public static final String CT_KEY_BACK_PICTURES = "ct_back_pictures";
    /**
     * loading
     */
    private AlertDialog alertDialog;
    /**
     * 拒绝权限是否勾选不再提示
     */
    private boolean isNoMoreInquiries = false;
    /**
     * 是否已经授权
     */
    private boolean isPermissions = false;
    /**
     * 权限组
     */
    private String[] str_permissions =
    {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    /**
     * 权限请求码
     */
    public final static int PERMISSION_CODE_FIRST = 0x38;
    /**
     * 列数
     */
    private int numColumns = 4;
    /**
     * 最大选取图片数
     */
    private int maxPictureChooseNumber = 3;
    /**
     * 图片集合
     */
    private ArrayList<CtPictureDir> ctPictureDirs;
    /**
     * 网格适配器
     */
    private CtpAdapter adapter = null;
    /**
     * 当前选中的文件夹下标
     */
    private int choosePosition = 0;
    /**
     * 弹出选择文件夹列表
     */
    private CtDirChoosePopupWindow ppupWindow = null;
    /**
     * 当前选中的图片集合
     */
    private HashMap<String, CtPicture> mapCtPictures = new HashMap<String, CtPicture>();
    private CtPSBroadcastReceiver broadcastReceiver = null;
    private String cameraPath = null;
    private CtPicture cameraPicture = null;
    private ContentResolver mContentResolver;

    /* ***************** UI ******************* */
    /**
     * 网格
     */
    private GridView gridView;
    /**
     * 载入成功界面
     */
    private RelativeLayout rl_successRoot;
    /**
     * 载入失败界面
     */
    private RelativeLayout rl_errorRoot;
    /**
     * 错误页面信息
     */
    private TextView tv_error;
    /**
     * 错误页面按钮
     */
    private Button btn_error;
    /**
     * 右上角完成按钮
     */
    private TextView tv_btnOk;
    /**
     * 右下角预览文字
     */
    private TextView tv_preview;
    /**
     * 右下角预览按钮
     */
    private LinearLayout ll_previewBtn;
    /**
     * 左下角选择文件夹按钮
     */
    private LinearLayout btnAllPicture;
    /**
     * 底部
     */
    private RelativeLayout bottom;
    /**
     * 左上角返回
     */
    private LinearLayout backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ct_picture_selector);
        loadView();
        loadIntentData();
        broadcastReceiver = new CtPSBroadcastReceiver();
        registerBroadcastReceiver();
        mContentResolver = getContentResolver();
    }
    private void registerBroadcastReceiver()
    {
        registerReceiver(broadcastReceiver,new IntentFilter(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_FINISH));
        registerReceiver(broadcastReceiver,new IntentFilter(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_FINISH_FOR_RESULT));
        registerReceiver(broadcastReceiver,new IntentFilter(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_FINISH_FOR_TAKE_PHOTO));
        registerReceiver(broadcastReceiver,new IntentFilter(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_ADD_CHOOSE));
        registerReceiver(broadcastReceiver,new IntentFilter(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_DELETE_CHOOSE));
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        if(isPermissions)return;
        rl_successRoot.setVisibility(View.GONE);
        rl_errorRoot.setVisibility(View.GONE);
        applyForPermissions();
    }
    private void onApplyForPermissionsSuccess()
    {
        initView();
        initListener();
        initData();
    }
    /**
     * <p>功能描述：载入view
     */
    private void loadView()
    {
        gridView       = findViewById(R.id.ct_p_s_content_gv);
        rl_successRoot = findViewById(R.id.ct_p_s_rl_root);
        rl_errorRoot   = findViewById(R.id.ct_p_s_rl_error_root);
        tv_error       = findViewById(R.id.ct_p_s_rl_error_tv);
        btn_error      = findViewById(R.id.ct_p_s_rl_error_btn);
        tv_btnOk = findViewById(R.id.ct_top_left_ok_btn);
        tv_preview = findViewById(R.id.ct_bootom_right_tv);
        ll_previewBtn = findViewById(R.id.ct_bootom_right);
        btnAllPicture = findViewById(R.id.ct_bootom_left);
        bottom = findViewById(R.id.ct_p_s_bottom_root);
        backBtn = findViewById(R.id.pp_p_s_top_left_back_btn_ll);
    }
    /**
     * <p>功能描述：载入Intent传值数据
     */
    private void loadIntentData()
    {
        if(getIntent() == null || getIntent().getExtras() == null || getIntent().getExtras().isEmpty()) return;
        Bundle extras = getIntent().getExtras();
        numColumns = extras.getInt(CtIntentAction.PARAMETER_NUMCOLUMNS, numColumns);
        maxPictureChooseNumber = extras.getInt(CtIntentAction.PARAMETER_MAX_PICTURE_CHOOSE_NUMBER, maxPictureChooseNumber);
    }
    /**
     * <p>功能描述：权限申请
     */
    private void applyForPermissions()
    {
        /*动态请求需要的权限*/
        boolean checkPermissionFirst = PermissionUtil.checkPermissionFirst(this, PERMISSION_CODE_FIRST, str_permissions);
        if(checkPermissionFirst)
        {
            onApplyForPermissionsSuccess();
        }
    }
    /**
     * <p>功能描述：初始化view
     */
    private void initView()
    {
        gridView.setNumColumns(numColumns);
    }
    /**
     * <p>功能描述：注册所有监听
     */
    private void initListener()
    {
        backBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CtPictureSelectorActivity.this.finish();
            }
        });
        btnAllPicture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int height = CtMeasureUtils.getViewHeight(bottom);
                createdpPpupWindow();
                //显示窗口
                ppupWindow.showAtLocation(bottom, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, height); //设置layout在PopupWindow中显示的位置
            }
        });
        ll_previewBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int no = mapCtPictures.size();
                if(no <= 0)return;
                if(no > maxPictureChooseNumber)return;
                Intent intent = new Intent(CtPictureSelectorActivity.this,CtPreviewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(CtActivityAction.CT_KEY_MAX_NUMBER,maxPictureChooseNumber);
                bundle.putInt(CtActivityAction.CT_KEY_JUMP_PREVIEW_FOR_BOTTON, CtActivityAction.CT_VALUE_JUMP_PREVIEW_FOR_BOTTON_PREVIEW);
                bundle.putSerializable(CtActivityAction.CT_KEY_NOW_CHOOSE_PICTURES,mapCtPictures);
                intent.putExtras(bundle);
                CtPictureSelectorActivity.this.startActivity(intent);
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
                doFinishForResult();
            }
        });
    }
    /**
     * <p>功能描述：初始化数据
     */
    private void initData()
    {
        showLoadingDialog();
        try
        {
            Observable.create(new ObservableOnSubscribe<ArrayList<CtPictureDir>>()
            {
                @Override
                public void subscribe(ObservableEmitter<ArrayList<CtPictureDir>> emitter) throws Exception
                {
                    emitter.onNext(CtPictureHelper.initImage(CtPictureSelectorActivity.this.getApplicationContext()));
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ArrayList<CtPictureDir>>()
                    {
                        @Override public void onSubscribe(Disposable d){}
                        @Override
                        public void onNext(ArrayList<CtPictureDir> ctPictureDirs)
                        {
                            dismissLoadingDialog();
                            if(ctPictureDirs == null || ctPictureDirs.size() <= 0)
                            {
                                showError("当前没有图片！","关闭",new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        CtPictureSelectorActivity.this.finish();
                                    }
                                });
                                return;
                            }
                            CtPictureSelectorActivity.this.ctPictureDirs = ctPictureDirs;
                            initDataComplete();
                        }
                        @Override
                        public void onError(Throwable e)
                        {
                            dismissLoadingDialog();
                            showError("载入图片失败！","重试",new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    onApplyForPermissionsSuccess();
                                }
                            });
                        }
                        @Override public void onComplete(){}
                    });
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
     * <p>功能描述：初始化数据完毕
     */
    private void initDataComplete()
    {
        rl_successRoot.setVisibility(View.VISIBLE);
        rl_errorRoot.setVisibility(View.GONE);
        update();
    }
    /**
     * 更新界面内容
     */
    private void update()
    {
        try
        {

            if(adapter == null)
            {
                adapter = new CtpAdapter(this,getLayoutInflater(),ctPictureDirs.get(choosePosition),maxPictureChooseNumber,numColumns);
                adapter.setOnCbChangeListener(new CtpAdapter.OnCbChangeListener()
                {
                    @Override
                    public void doshowAllImagePreview(int i)
                    {
                        ArrayList<CtPicture> ctPictures = ctPictureDirs.get(choosePosition).getPictures();
                        Intent intent = new Intent(CtPictureSelectorActivity.this,CtPreviewActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt(CtActivityAction.CT_KEY_MAX_NUMBER,maxPictureChooseNumber);
                        bundle.putInt(CtActivityAction.CT_KEY_JUMP_FROM_NO,i+1);
                        bundle.putInt(CtActivityAction.CT_KEY_JUMP_PREVIEW_FOR_BOTTON, CtActivityAction.CT_VALUE_JUMP_PREVIEW_FOR_BOTTON_IMAGEVIEW);
                        bundle.putSerializable(CtActivityAction.CT_KEY_NOW_CHOOSE_PICTURES,mapCtPictures);
                        bundle.putSerializable(CtActivityAction.CT_KEY_NOW_ALL_PICTURES,ctPictures);
                        intent.putExtras(bundle);
                        CtPictureSelectorActivity.this.startActivity(intent);
                    }
                    @Override
                    public void changeTopRightBtnAndBottomRightBtn(HashMap<String, CtPicture> mapCtPictures)
                    {
                        CtPictureSelectorActivity.this.mapCtPictures = mapCtPictures;
                        CtPictureSelectorActivity.this.changeTopRightBtnAndBottomRightBtn();
                    }
                    @Override
                    public void takePhoto()
                    {
                        cameraPath = null;
                        cameraPicture = null;
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        ContentValues values = new ContentValues(4);
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, "testing");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "this is description");
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        String b_dir= Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM+File.separator + "Camera" + File.separator;
                        cameraPath = b_dir + "asinio_" + System.currentTimeMillis() + ".jpg";
                        values.put(MediaStore.Images.Media.DATA, cameraPath);
                        Uri imageFilePath = mContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath);
                        CtPictureSelectorActivity.this.startActivityForResult(intent, CtActivityAction.CT_VALUE_TACKE_PHOTO_REQUESTCODE);

                    }
                });
                gridView.setAdapter(adapter);
            }else
            {
                adapter.updateGirdView(mapCtPictures,ctPictureDirs.get(choosePosition));
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
     * 处理请求权限的响应
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 请求权限结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode != PERMISSION_CODE_FIRST) return;
        isPermissions = true;
        isNoMoreInquiries = false;
        for(int i = 0; i < permissions.length; i++)
        {
            if(grantResults[i] == PackageManager.PERMISSION_DENIED)
            {
                isPermissions = false;
                boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]);
                if(!showRequestPermission)isNoMoreInquiries = true;
            }
        }
        if(isPermissions)
        {
            onApplyForPermissionsSuccess();
        }else
        {
            if(isNoMoreInquiries)//有勾选了不再询问的拒绝权限
            {
                showError("获取权限失败，需手动修改！","去设置",new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showAppPermissionSetting();
                    }
                });
            }else
            {
                showError("还未获取权限！","重新获取",new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        applyForPermissions();
                    }
                });
            }
        }
    }
    /**
     * <p>功能描述：显示loading
     */
    public void showLoadingDialog()
    {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        alertDialog.setCancelable(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if(keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK)
                    return true;
                return false;
            }
        });
        alertDialog.show();
        alertDialog.setContentView(R.layout.loading_alert);
        alertDialog.setCanceledOnTouchOutside(false);
    }
    /**
     * <p>功能描述：关闭loading
     */
    public void dismissLoadingDialog()
    {
        if(null != alertDialog && alertDialog.isShowing())
        {
            alertDialog.dismiss();
        }
    }
    /**
     * <p>功能描述：
     * @param message            错误信息
     * @param btnText            错误按钮文字
     * @param btnOnClickListener 错误按钮事件
     */
    private void showError(String message, String btnText, View.OnClickListener btnOnClickListener)
    {
        rl_successRoot.setVisibility(View.GONE);
        rl_errorRoot.setVisibility(View.VISIBLE);
        tv_error.setText(message);
        btn_error.setText(btnText);
        btn_error.setOnClickListener(btnOnClickListener);
    }
    /**
     * <p>功能描述：打开系统设置权限
     */
    private void showAppPermissionSetting()
    {
        /*跳转到应用详情，让用户去打开权限*/
         Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT >= 9)
        {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        }else if(Build.VERSION.SDK_INT <= 8)
        {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
    }
    /**
     * 顶部右侧按钮状态更改
     */
    private void changeOkBtn() throws Exception
    {
        if(mapCtPictures == null)return;
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
    /**
     * 底部右侧按钮状态更改
     */
    private void changePreviewBtn() throws Exception
    {
        if(mapCtPictures == null)return;
        int no = mapCtPictures.size();
        if(no <= 0)
        {
            tv_preview.setText("预览");
            ll_previewBtn.setClickable(false);
            ll_previewBtn.setBackgroundResource(R.color.ctColorBottomBgDef);
        }else
        {
            tv_preview.setText("预览("+no+")");
            ll_previewBtn.setClickable(true);
            ll_previewBtn.setBackgroundResource(R.drawable.ct_btn_bottom_bg_selector);
        }
    }
    /**
     * 创建文件夹列表弹出窗体
     */
    private void createdpPpupWindow()
    {
        if(ppupWindow == null)
        {
            ppupWindow = new CtDirChoosePopupWindow(CtPictureSelectorActivity.this, ctPictureDirs, new CtDirChoosePopupWindow.OnChooseFinish()
            {
                @Override
                public void finish(int position)
                {
                    choosePosition = position;
                    update();
                }
            });
            ppupWindow.setBackgroundDrawable(new ColorDrawable(0xffffffff));
            ppupWindow.setAnimationStyle(R.style.ct_popWindow_animation);
        }
    }
    public class CtPSBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                String action = intent.getAction().toString();
                if(action.equalsIgnoreCase(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_FINISH)) {doFinish();}
                if(action.equalsIgnoreCase(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_FINISH_FOR_RESULT)) {doFinishForResult();}
                else if(action.equalsIgnoreCase(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_ADD_CHOOSE)) {doAddChoose(intent);}
                else if(action.equalsIgnoreCase(CtReceiverAction.CT_PICTURE_SELECTOR_ACTIVITY_DELETE_CHOOSE)) {doDeleteChoose(intent);}
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
    private void changeTopRightBtnAndBottomRightBtn()
    {
        try
        {
            changeOkBtn();
            changePreviewBtn();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    private void doAddChoose(Intent intent)
    {
        Bundle bundle = intent.getExtras();
        if(bundle == null)return;
        CtPicture picture = bundle.getParcelable("picture");
        if(mapCtPictures.get(picture.getPath()) != null)mapCtPictures.remove(picture.getPath());
        mapCtPictures.put(picture.getPath(),picture);
        changeTopRightBtnAndBottomRightBtn();
        adapter.updateGirdView(mapCtPictures,ctPictureDirs.get(choosePosition));
    }
    private void doDeleteChoose(Intent intent)
    {
        Bundle bundle = intent.getExtras();
        if(bundle == null)return;
        CtPicture picture = bundle.getParcelable("picture");
        mapCtPictures.remove(picture.getPath());
        changeTopRightBtnAndBottomRightBtn();
        adapter.updateGirdView(mapCtPictures,ctPictureDirs.get(choosePosition));
    }
    private void doFinish() throws Exception
    {
        finish();
    }
    private void doFinishForResult()
    {
        Intent intent=new Intent();
        Bundle bundle = new Bundle();
        ArrayList<CtPicture> list_path = new ArrayList<CtPicture>();
        ArrayList<String> paths = new ArrayList<String>();
        for(Map.Entry<String, CtPicture> entry : mapCtPictures.entrySet())
        {
            CtPicture picture = entry.getValue();
            list_path.add(entry.getValue());
            paths.add(picture.getPath());
        }
        bundle.putParcelableArrayList(CtPictureSelectorActivity.CT_KEY_BACK_PICTURES,list_path);
        bundle.putStringArrayList("path",paths);
        intent.putExtras(bundle);
        setResult(CtActivityAction.CT_RESULT_CODE,intent);
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CtActivityAction.CT_VALUE_TACKE_PHOTO_REQUESTCODE)//调系统拍照返回
        {
            if(resultCode == RESULT_CANCELED)
            {
                cameraPath = null;
                Toast.makeText(this, "取消拍照", Toast.LENGTH_SHORT).show();
                return;
            }//取消拍照
            if (CtStringUtils.isEmpty(cameraPath))
            {
                Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
                return;
            }
            File file = new File(cameraPath);
            if(!file.exists())
            {
                cameraPath = null;
                Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if(file.length() <= 0)
            {
                file.delete();
                cameraPath = null;
                Toast.makeText(this, "图片获取失败", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = Uri.fromFile(file);
            cameraPicture = new CtPicture();
            cameraPicture.setOriginalUri(uri.toString());
            cameraPicture.setThumbnailUri(uri.toString());
            cameraPicture.setPath(cameraPath);
            cameraPicture.setName(file.getName());
            cameraPicture.setParent(file.getParent());
            mapCtPictures.put(cameraPicture.getPath(),cameraPicture);
            changeTopRightBtnAndBottomRightBtn();
            adapter.updateGirdView(mapCtPictures,ctPictureDirs.get(choosePosition));
        }
    }
}
