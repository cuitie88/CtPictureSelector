# 图片选择器

## 添加依赖：

### Project中的build.gradle中添加：

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
### app中的build.gradle中添加：

```
dependencies {
    implementation 'com.github.cuitie88:CtPictureSelector:v1.0.2'
}
```

## 使用方法

### 创建图片选择器

```
Ctp.build(MainActivity.this)
     .numColumns(2)
     .maxPictureNumber(10)
     .startForResult(REQUEST_CODE);
```

 方法  | 参数  | 作用  
 ---- | ----- | -----
 build | (Activivty 或 Fragment)  | 创建Ctp实例用于操作图片选择器 
 numColumns  | int | 图片网格显示列数
 maxPictureNumber  | int | 最大选择的图片数量
 startForResult  | int | 启动图片选择器
 
 ### 接收返回值 
 
 ```
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
            Log.d("TAG",sb.toString());

        }
    }
```
