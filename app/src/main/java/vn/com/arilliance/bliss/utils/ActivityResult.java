package vn.com.arilliance.bliss.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Hoang Tran on 11/16/2016.
 */

public class ActivityResult {
    static File cameraFile;
    public static Object onActivityResult(int requestCode, int resultCode, Intent data, Activity activity){
        switch (requestCode){
            case Constants.ACTIVITY_CODE.GET_IMAGE_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    try {
                        Bitmap bitmap = createImageFileFromCamera(data);
                        try {
                            putImageToGallery(cameraFile, activity);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    } catch (RuntimeException | IOException e) {
                        e.printStackTrace();
                    }
                }
                return cameraFile;
        }
        return null;
    }


    private static void putImageToGallery(File cameraFile, Activity activity){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "");
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, cameraFile.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, cameraFile.getName().toLowerCase(Locale.US));
        values.put("_data", cameraFile.getAbsolutePath());

        ContentResolver cr = activity.getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private static Bitmap createImageFileFromCamera(Intent data) throws IOException {
        Bundle b = data.getExtras();
        Bitmap bitmap = (Bitmap) b.get("data");
        // this part to save captured image on provided path
        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String fileName = df.format(now).replaceAll("[-: ]", "") + ".jpg";
        File folder = new File(Environment.getExternalStorageDirectory(), "Bliss");
        if (!folder.exists()) {
            System.out.println("Create Bliss Picture folder : " + folder.mkdirs());
        } else {
            System.out.println("Folder exists!");
        }
        cameraFile = new File(folder, fileName);
        cameraFile.createNewFile();
        bitmap = RotateImage.getRotateBitmap(cameraFile.getAbsolutePath(), bitmap);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        //write the bytes in file
        FileOutputStream fo = new FileOutputStream(cameraFile);
        fo.write(bytes.toByteArray());
        // remember close de FileOutput
        fo.flush();
        fo.close();
        System.out.println(cameraFile.getAbsolutePath());
        return bitmap;
    }
}
