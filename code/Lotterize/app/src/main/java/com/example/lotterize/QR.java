package com.example.lotterize;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class QR {
    public static void generatePNG(String code, String path) {

        try {
            // Generate bitmap
            Bitmap bitmap = generateBitmap(code, 512);

            if (bitmap == null) return;

            // Save to file
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// testing

    // In MainActivity.java

    //        // test qr code
//        String path = getExternalFilesDir(null) + "/qr_code.png";
//        QR.generatePNG("test", path);
    // path in android file system
    // /storage/emulated/0/Android/data/com.example.lotterize/files/

    public static Bitmap generateBitmap(String code, int size){
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(code, BarcodeFormat.QR_CODE, size, size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Testing

    // In activity_main.xml

//    <ImageView
//    android:id="@+id/qrImageView"
//    android:layout_width="300dp"
//    android:layout_height="300dp"
//    android:layout_gravity="center"
//    android:contentDescription="QR Code"
//    app:layout_constraintTop_toTopOf="@+id/LoginText"
//    tools:layout_editor_absoluteX="32dp" />

    // In MainActivity.java

//    // Display QR code
//    ImageView imageView = findViewById(R.id.qrImageView);
//    String code = "https://myapp.com/event/EVENT123";
//    Bitmap qr = QR.generateBitmap(code, 512);
//        imageView.setImageBitmap(qr);


}
