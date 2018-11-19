package com.ctp.android.library.ps.tool.pojo;

import java.util.ArrayList;
public class CtPictureDir
{
    private String path;
    private String name;
    private CtPicture firstPicture = null;
    private ArrayList<CtPicture> pictures = null;
    public CtPictureDir(){}
    public CtPictureDir(String path,String name, CtPicture firstPicture, ArrayList<CtPicture> pictures)
    {
        this.path = path;
        this.name = name;
        this.firstPicture = firstPicture;
        this.pictures = pictures;
    }
    public String getPath() {return path;}
    public void setPath(String path) {this.path = path;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public CtPicture getFirstPicturePath() {return firstPicture;}
    public void setFirstPicturePath(CtPicture firstPicture) {this.firstPicture = firstPicture;}
    public ArrayList<CtPicture> getPictures() {return pictures;}
    public void setPictures(ArrayList<CtPicture> pictures) {this.pictures = pictures;}
}
