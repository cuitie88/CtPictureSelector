package com.ctp.android.library.ps.tool.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
public class CtPicture implements Serializable, Parcelable
{
    private String originalUri;//原图URI
    private String thumbnailUri;//缩略图URI
    private int orientation;//图片旋转角度
    private String name;
    private String parent;
    private String path;
    public CtPicture(){}
    public CtPicture(String originalUri, String thumbnailUri, int orientation, String name, String parent,String path)
    {
        this.originalUri = originalUri;
        this.thumbnailUri = thumbnailUri;
        this.orientation = orientation;
        this.name = name;
        this.parent = parent;
        this.path = path;
    }

    protected CtPicture(Parcel in)
    {
        originalUri = in.readString();
        thumbnailUri = in.readString();
        orientation = in.readInt();
        name = in.readString();
        parent = in.readString();
        path = in.readString();
    }

    public static final Creator<CtPicture> CREATOR = new Creator<CtPicture>()
    {
        @Override
        public CtPicture createFromParcel(Parcel in)
        {
            return new CtPicture(in);
        }

        @Override
        public CtPicture[] newArray(int size)
        {
            return new CtPicture[size];
        }
    };

    public String getOriginalUri() {return originalUri;}
    public void setOriginalUri(String originalUri) {this.originalUri = originalUri;}
    public String getThumbnailUri() {return thumbnailUri;}
    public void setThumbnailUri(String thumbnailUri) {this.thumbnailUri = thumbnailUri;}
    public int getOrientation() {return orientation;}
    public void setOrientation(int orientation) {this.orientation = orientation;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getParent() {return parent;}
    public void setParent(String parent) {this.parent = parent;}
    public String getPath() {return path;}
    public void setPath(String path) {this.path = path;}

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(originalUri);
        dest.writeString(thumbnailUri);
        dest.writeInt(orientation);
        dest.writeString(name);
        dest.writeString(parent);
        dest.writeString(path);
    }
}
