#这是一个图片选择器。


添加依赖：

项目的Project中的build.gradle中添加：

allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

在app中的build.gradle中添加：

dependencies {
	        implementation 'com.github.cuitie88:CtPictureSelector:v1.0.2'
}

使用：

创建图片选择器：

//
Ctp.build(MainActivity.this)//创建实例
     .numColumns(2)//设置显示列数
     .maxPictureNumber(10)//设置最大选取图片数
     .startForResult(REQUEST_CODE);//设置Activity返回值requestCode
              
              
接收：
//
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
