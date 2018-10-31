package ben_dragos.nfc_active_poster;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileTools {

    public static void writeStringAsFile(final String fileContents, String fileName) {
        Context context = App.getContext();
        try {
            FileWriter out = new FileWriter(new File(context.getFilesDir(), fileName));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
            //Logger.logError(TAG, e);
        }
    }

    public static String readFileAsString(String fileName) {
        Context context = App.getContext();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(context.getFilesDir(), fileName)));
            while ((line = in.readLine()) != null) stringBuilder.append(line);

        } catch (FileNotFoundException e) {
            //Logger.logError(TAG, e);
        } catch (IOException e) {
            //Logger.logError(TAG, e);
        }

        return stringBuilder.toString();
    }

    public static String readAssetFileAsString(String fileName) {
        StringBuilder DataString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        Context context = App.getContext();
        try {
            fIn = context.getAssets().open(fileName);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                DataString.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }

        return DataString.toString();
    }

    public static boolean fileExists(String filename) {
        Context context = App.getContext();
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }
}
