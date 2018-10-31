package ben_dragos.nfc_active_poster;

import android.graphics.Bitmap;
import android.nfc.NdefRecord;

public class RecordStorage {

    static int noRecords = 2;
    static int recordStart = 1;

    static String recordToWrite = new String();
    static String recordTypeToWrite = new String();

    static String[] records = new String[noRecords];
    static String[] recordTypes = new String[noRecords];
    static boolean initialized = false;

    public static String getHTML() {

        String htmlString = FileTools.readAssetFileAsString("main.html");

        if (!initialized) {
            initialized = true;

            for (Integer i = 0; i < noRecords; i++) {
                records[i] = "[Empty]";
                recordTypes[i] = "text/plain";
            }

            htmlString = htmlString.replace("%views%", "0000");
        }

        for (Integer i = 0; i < noRecords; i++) {
            Integer x = i + 1;
            String toReplace = "%record" + x.toString() + "%";

//            if (recordTypes[i].equals("text/plain")) {
//                htmlString = htmlString.replace(toReplace, records[i]);
//            }
//            else
            if (recordTypes[i].equals("text/1bitpic")) {
                Bitmap toDrawBitmap = BitmapTools.get1BitBitmap(records[i]);
                String picture = BitmapTools.bitmapToString(toDrawBitmap);

                String replaceWith = "<img src = \"data:image/png;base64, " + picture + "\" width=432px height=432px>";
                htmlString = htmlString.replace(toReplace, replaceWith);
            }
            else {
                if (records[i].contains("?views=")) {
                    int index = records[i].indexOf("?views=") + "?views=".length();
                    String views = records[i].substring(index);

                    htmlString = htmlString.replace("%views%", views);
                }
                else if (records[i].startsWith("\u0002en")) {
                    htmlString = htmlString.replace(toReplace, records[i].substring(3));
                }
                else {
                    htmlString = htmlString.replace(toReplace, records[i]);
                }

            }

        }

        return htmlString;
    }

    public static void clearRecords() {
        for (Integer i = (noRecords - 1); i > 0; i--) {

            records[i] = "[Empty]";
            recordTypes[i] = "text/plain";

        }
    }

    public static void save(String text, String type) {

        recordToWrite = text;
        recordTypeToWrite = type;

    }

    public static void updateRecord(String text, String type, Integer index) {
        records[index] = text;
        recordTypes[index] = type;
    }

    public static NdefRecord[] getRecords() {
        NdefRecord[] records_ = new NdefRecord[noRecords];

        for (Integer i = noRecords - 1; i > recordStart; i--) {

            records[i] = records[i-1];
            recordTypes[i] = recordTypes[i-1];

        }

        records[recordStart] = recordToWrite;
        recordTypes[recordStart] = recordTypeToWrite;

        for (Integer i = 0; i < noRecords; i++) {
            records_[i] = MainActivity.createRecord(records[i], recordTypes[i]);
        }

        return records_;
    }


}
