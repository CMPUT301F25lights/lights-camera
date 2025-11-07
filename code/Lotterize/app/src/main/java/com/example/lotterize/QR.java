package com.example.lotterize;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class QR {
    /**
     * Generates a random UUID string to be encoded to a QR code.
     * @return The generated code string.
     */
    public static String generateCode(){
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a QR code bitmap from a given string.
     * The generated bitmap an be displayed with an ImageView using imageView.setImageBitmap(bitmap);
     * <p>
     * Example usage:
     * <pre>{@code
     * Bitmap bitmap = QR.generateBitmap("test", 512);
     * imageView.setImageBitmap(bitmap);
     * }</pre>
     * @param code String to be encoded in the QR code
     * @param size Size of the QR code
     * @return Bitmap of the QR code
     */
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
            Log.e("QRCodeGenerator", "Failed to generate QR bitmap", e);
            return null;
        }
    }

    /**
     * Saves a bitmap as PNG to the provided OutputStream.
     * Used with Storage Access Framework to save to user-selected location.
     *
     * @param bitmap The bitmap to save
     * @param outputStream The output stream to write to
     * @return true if successful, false otherwise
     */
    public static boolean saveBitmapToStream(Bitmap bitmap, OutputStream outputStream) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            Log.e("QRCodeGenerator", "Failed to save bitmap", e);
            return false;
        }
    }

    /**
     * Saves the QR code for the current event to the URI selected by the user.
     *
     * @param context The application context
     * @param code The code to be saved
     * @param uri The URI where the QR code should be saved
     * @return String status of the operation
     */
    public static String saveQrCodeToUri(Context context, Uri uri, String code) {

        try {
            // Generate QR code bitmap from the stored qrCode string
            Bitmap qrBitmap = QR.generateBitmap(code, 1024);

            if (qrBitmap == null) {
                return "Failed to generate QR code";
            }

            // Open output stream and save
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                return "Failed to open file";
            }

            boolean success = QR.saveBitmapToStream(qrBitmap, outputStream);

            if (success) {
                return "QR code saved successfully";
            } else {
                return "Failed to save QR code";
            }

        } catch (Exception e) {
            return "Error saving QR code";
        }
    }

//---------------- Testing, not used yet --------------------------------
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


}
