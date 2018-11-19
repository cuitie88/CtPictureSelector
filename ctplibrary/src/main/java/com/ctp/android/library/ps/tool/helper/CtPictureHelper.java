package com.ctp.android.library.ps.tool.helper;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.ctp.android.library.ps.tool.pojo.CtPicture;
import com.ctp.android.library.ps.tool.pojo.CtPictureDir;
import com.ctp.android.library.ps.tool.util.CtStringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public class CtPictureHelper
{
    private static boolean isRunning = false;
    //大图遍历字段
    private static final String[] STORE_IMAGES =
            {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.ORIENTATION
            };
    //小图遍历字段
    private static final String[] THUMBNAIL_STORE_IMAGE =
            {
                    MediaStore.Images.Thumbnails._ID,
                    MediaStore.Images.Thumbnails.DATA
            };
    public static synchronized ArrayList<CtPictureDir> initImage(Context context)
    {
        if(isRunning) return null;
        isRunning = true;
        //获取大图的游标
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  // 大图URI
                STORE_IMAGES,   // 字段
                null,         // No where clause
                null,         // No where clause
                MediaStore.Images.Media.DATE_ADDED + " DESC"); //根据时间升序
        if(cursor == null) return null;
        HashMap<String, CtPictureDir> mapCtPictureDirs = new HashMap<String, CtPictureDir>();
        ArrayList<CtPictureDir> ctPictureDirs = new ArrayList<CtPictureDir>();
        ArrayList<CtPictureDir> ctPictureDirsAche = new ArrayList<CtPictureDir>();
        CtPictureDir pictureDir = new CtPictureDir();
        ArrayList<CtPicture> pictures = new ArrayList<CtPicture>();
        while(cursor.moveToNext())
        {
            int id = cursor.getInt(0);//大图ID
            String path = cursor.getString(1);//大图路径
            File file = new File(path);
            //判断大图是否存在
            if(file.exists())
            {
                //小图URI
                String thumbUri = getThumbnail(context, id, path);
                //获取大图URI
                String uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().
                        appendPath(Integer.toString(id)).build().toString();
                if(CtStringUtils.isEmpty(uri)) continue;
                if(CtStringUtils.isEmpty(thumbUri)) thumbUri = uri;
                //获取目录路径
                String folderPath = file.getParentFile().getAbsolutePath();
                //获取目录名
                String folder = file.getParentFile().getName();
                CtPicture ctPicture = new CtPicture();
                ctPicture.setName(file.getName());
                ctPicture.setOriginalUri(uri);
                ctPicture.setThumbnailUri(thumbUri);
                ctPicture.setParent(folder);
                ctPicture.setPath(path);
                int degree = cursor.getInt(2);
                if(degree != 0)
                {
                    degree = degree + 180;
                }
                ctPicture.setOrientation(360 - degree);
                //判断文件夹是否已经存在
                if(mapCtPictureDirs.containsKey(folderPath))
                {
                    CtPictureDir ctPictureDir = mapCtPictureDirs.get(folderPath);
                    ctPictureDir.getPictures().add(ctPicture);
                }else
                {
                    CtPictureDir ctPictureDir = new CtPictureDir();
                    ctPictureDir.setPath(folderPath);
                    ctPictureDir.setName(folder);
                    ctPictureDir.setFirstPicturePath(ctPicture);
                    ArrayList<CtPicture> ctPictures = new ArrayList<CtPicture>();
                    ctPictures.add(ctPicture);
                    ctPictureDir.setPictures(ctPictures);
                    mapCtPictureDirs.put(folderPath, ctPictureDir);
                }
                pictures.add(ctPicture);
                if(pictureDir.getFirstPicturePath() == null)
                    pictureDir.setFirstPicturePath(ctPicture);
            }
        }
        cursor.close();
        isRunning = false;
        if(pictures != null && pictures.size() >0)
        {
            pictureDir.setPictures(pictures);
            pictureDir.setPath("所有图片");
            pictureDir.setName("所有图片");
            ctPictureDirs.add(pictureDir);
        }
        for(Map.Entry<String, CtPictureDir> entry : mapCtPictureDirs.entrySet())
        {
            String key = entry.getKey();
            CtPictureDir ctPictureDir = entry.getValue();
            if(key.endsWith("Camera"))
            {
                ctPictureDir.setName("照片");
                ctPictureDirs.add(ctPictureDir);
            }else
            {
                ctPictureDirsAche.add(ctPictureDir);
            }
        }
        ctPictureDirs.addAll(ctPictureDirsAche);
        return ctPictureDirs;
    }
    private static String getThumbnail(Context context, int id, String path)
    {
        //获取大图的缩略图
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, THUMBNAIL_STORE_IMAGE, MediaStore.Images.Thumbnails.IMAGE_ID + " = ?", new String[]{id + ""}, null);
        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            int thumId = cursor.getInt(0);
            String uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI.buildUpon().
                    appendPath(Integer.toString(thumId)).build().toString();
            cursor.close();
            return uri;
        }
        cursor.close();
        return null;
    }
    public static String getCameraImgPath(Context context)
    {
        String foloder = getCachePath(context) + "/PostPicture/";
        File savedir = new File(foloder);
        if(!savedir.exists())
        {
            savedir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // 照片命名
        String picName = timeStamp + ".jpg";
        //  裁剪头像的绝对路径
        String CameraImgPath = foloder + picName;
        return CameraImgPath;
    }
    private static String getCachePath(Context context)
    {
        File cacheDir;
        if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = context.getExternalCacheDir();
        else cacheDir = context.getCacheDir();
        if(!cacheDir.exists()) cacheDir.mkdirs();
        return cacheDir.getAbsolutePath();
    }
}
