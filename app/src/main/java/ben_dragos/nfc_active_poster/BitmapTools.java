package ben_dragos.nfc_active_poster;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapTools {

    public static String bitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static String create1BitBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        String toReturn = "";
        //Bitmap toReturn = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        char sixteen_bits = (char)0x0000;

        // Iterate over height
        for (int y = 0; y < height; y++) {
            // Iterate over width
            for (int x = 0; x < width; x++) {
                int offset = ((x+(y*height))%16);
                int pixel = bitmap.getPixel(x, y);
                //int lowestBit = pixel & 0xff000000;
                if(pixel < 0) {
                    sixteen_bits = (char)(sixteen_bits | (0x1 << offset));
                    //toReturn.setPixel(x, y, Color.BLACK);
                }
                /*
                else {
                    //toReturn.setPixel(x, y, Color.WHITE);
                    // sixteen_bytes = (char)(sixteen_bytes & (0x0 << (height*width)));
                }
                */
                if ((offset == 15) /*&& !(x == 0 && y == 0)*/) {
                    toReturn = toReturn + sixteen_bits;
                    sixteen_bits = (char)0x0000;
                }
            }
        }
        toReturn = toReturn + sixteen_bits;

        return toReturn;
    }

    public static Bitmap get1BitBitmap(String bitmap) {
        int width = (int)Math.sqrt(bitmap.length()*16);
        int height = (int)Math.sqrt(bitmap.length()*16);
        Bitmap toReturn = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Iterate over height
        for (int y = 0; y < height; y++) {
            // Iterate over width
            for (int x = 0; x < width; x++) {
                int offset_major = ((x+(y*height))/16);
                int offset_minor = ((x+(y*height))%16);
                char pixel = bitmap.charAt(offset_major);
                if((pixel & (0x1 << offset_minor)) > 0x0) {
                    toReturn.setPixel(x, y, Color.BLACK);
                }
                else {
                    toReturn.setPixel(x, y, Color.WHITE);
                }
            }
        }

        return toReturn;
    }

    public static String getBase64Bitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encoded;
    }
}
