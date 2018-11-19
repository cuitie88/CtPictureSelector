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

### 创建图片选择器：

```
Ctp.build(MainActivity.this)
     .numColumns(2)
     .maxPictureNumber(10)
     .startForResult(REQUEST_CODE);
```

 方法  | 作用  
 ---- | -----
 1  | 2 
 3  | 4 
