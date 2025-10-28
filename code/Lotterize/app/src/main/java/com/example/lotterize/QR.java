package com.example.lotterize;

import android.graphics.Bitmap;
import android.graphics.Color;
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
//        // test qr code
//        String path = getExternalFilesDir(null) + "/qr_code.png";
//        QR.generatePNG("test", path);
        //   /storage/emulated/0/Android/data/com.example.lotterize/files/
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

    public static Bitmap generateBitmap(String code, int size){
        // usage:
//        // Display QR code
//        Bitmap qr = QR.generateBitmap(code, 512);
//        imageView.setImageBitmap(qr);
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


}
