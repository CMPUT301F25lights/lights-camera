package com.example.lotterize;

import static org.junit.Assert.*;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;

@RunWith(AndroidJUnit4.class)
public class QRTest {

    @Test
    public void testGenerateBitmap_NotNull() {
        Bitmap bitmap = QR.generateBitmap("hello", 256);
        assertNotNull(bitmap);
    }

    @Test
    public void testGenerateBitmap_SizeCorrect() {
        int size = 300;
        Bitmap bitmap = QR.generateBitmap("test", size);

        assertNotNull(bitmap);
        assertEquals(size, bitmap.getWidth());
        assertEquals(size, bitmap.getHeight());
    }

    @Test
    public void testSaveBitmapToStream_WritesData() {
        Bitmap bitmap = QR.generateBitmap("test123", 256);
        assertNotNull(bitmap);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean result = QR.saveBitmapToStream(bitmap, outputStream);

        assertTrue(result);
        assertTrue(outputStream.size() > 0);
    }
}
