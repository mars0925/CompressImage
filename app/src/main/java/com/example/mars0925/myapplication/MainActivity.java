package com.example.mars0925.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView i_actualImager;
    private ImageView i_compressImage;
    private byte[] imagedata;

    private int imageViewWidth, imageViewHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        i_actualImager = findViewById(R.id.i_actualpic);
        i_compressImage = findViewById(R.id.i_compresspci);

        Button bt1 = findViewById(R.id.button);
        Button bt2 = findViewById(R.id.button2);

        bt1.setOnClickListener(this);
        bt2.setOnClickListener(this);

        //再度聯手保羅？傳小喬丹盼加盟火箭
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button2://壓縮

                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Bitmap minbitmap = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            minbitmap = decodeSampledBitmapFromFile(imagedata, imageViewWidth,imageViewHeight);
                        }
                        /*
                        1.第一個參數輸出檔的格式， 一般應當選擇JPEG，壓縮出來的容量小，經過測試WEBP壓縮很耗時，
                          耗時時間比較：WEBP>PNG>JPEG，壓縮大小：PNG>WEBP>JPEG。
                        2.第二個參數是壓縮的品質比例，可以按照自己的需求進行設置，但建議一般不要大於60。
                        3.第三個參數就是想要寫入圖片資料的位元組流陣列。
                         */
                        minbitmap.compress(Bitmap.CompressFormat.JPEG,60,baos);//壓縮成PEG格式 壓縮像素质量为60%
                        i_compressImage.setImageBitmap(minbitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                break;
            case R.id.button://選擇照片
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    try {
                        imagedata = readStream(inputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length, options);
                    i_actualImager.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**壓縮照片*/
    @RequiresApi(api = Build.VERSION_CODES.N)
    static Bitmap decodeSampledBitmapFromFile(byte[] imageData, int reqWidth, int reqHeight) throws IOException {
        // 先取得Bitmap的尺寸
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//指測量圖片的參數 不加载到記憶體
        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);//测量image
        options.inPreferredConfig= Bitmap.Config.RGB_565;//設置565編碼格式 省記憶體
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);//獲得壓縮比 可以根據所需要的寬高去壓縮圖片
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);//用計算出來的inSampleSize,將圖片解碼到記憶體

        InputStream stream2 = new ByteArrayInputStream(imageData);
        /*檢查圖像的方向並正確的顯示*/
        ExifInterface exif;
        exif = new ExifInterface(stream2);

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                matrix.postRotate(0);
                break;
        }

        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return scaledBitmap;
    }

    /**
     * 计算inSampleSize
     * @param options BitmapFactory對圖片進行解碼時,所使用的參數集合
     * @param reqWidth 壓縮後的寬度
     * @param reqHeight 壓縮後的高度
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        /*圖片的原始高度*/
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;//代表要壓縮的比例 1就是不壓縮

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //解出照片格式用的readStream方法
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        imageViewWidth = i_compressImage.getWidth();
        imageViewHeight = i_compressImage.getHeight();
    }
}
