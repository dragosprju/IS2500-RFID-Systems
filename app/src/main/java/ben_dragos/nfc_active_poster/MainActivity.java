package ben_dragos.nfc_active_poster;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends Activity {

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;

    WebView webNFCContent;
    TextView tvNFCContent;
    TextView message;
    Button btnWrite;
    Button btnDraw;
    String htmlString;
    long startTime;
    long differenceTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        App.setContext(this);

        webNFCContent = (WebView)findViewById(R.id.nfc_contents_web);
        tvNFCContent = (TextView) findViewById(R.id.nfc_contents);
        message = (TextView) findViewById(R.id.edit_message);
        btnWrite = (Button) findViewById(R.id.button);
        btnDraw = (Button)findViewById(R.id.drawButton);

        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DrawActivity.class));
            }
        });

        webNFCContent.getSettings().setJavaScriptEnabled(true);
        htmlString = RecordStorage.getHTML();

        /*
        if (!fileExists("nfc_active_poster.html")) {
            writeStringAsFile(htmlString, "nfc_active_poster.html");
        }
        */

        //String content = readFileAsString("nfc_active_poster.html");
        webNFCContent.loadDataWithBaseURL("", htmlString, "text/html", "UTF-8", "");

        btnWrite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                try {
                    if(myTag ==null) {
                        Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                    } else {

                        if (message.getText().toString().trim().length() != 0) {
                            RecordStorage.save(message.getText().toString(), "text/plain");
                        }
                        startTime = SystemClock.elapsedRealtime();
                        write(myTag);
                        differenceTime = SystemClock.elapsedRealtime() - startTime;
                        Toast.makeText(context, WRITE_SUCCESS + "Time passed: " + String.valueOf(differenceTime) + "ms", Toast.LENGTH_LONG ).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                }
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }




    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        NdefRecord[] records = msgs[0].getRecords();

        int fewestNoRecords = RecordStorage.noRecords;
        if (records.length < RecordStorage.noRecords) {
            fewestNoRecords = records.length;
        }

        for (Integer i = 0; i < fewestNoRecords; i++) {
            String text = "";
            String type = new String(msgs[0].getRecords()[i].getType());
            byte[] payload = msgs[0].getRecords()[i].getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
//          int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
//          String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

            try {
                // Get the Text
                text = new String(payload, 0, payload.length, textEncoding);
                RecordStorage.updateRecord(text, type, i);
            } catch (UnsupportedEncodingException e) {
                Log.e("UnsupportedEncoding", e.toString());
            }
        }


        //tvNFCContent.setText("NFC Content: " + text);

        // Dragos: Here I update the webkit
        //String content = readFileAsString("nfc_active_poster.html");
        //String newString = htmlString.replace("Hi!", text);
        //writeStringAsFile(newString, "nfc_active_poster.html");

        String newString = RecordStorage.getHTML();
        webNFCContent.loadDataWithBaseURL("", newString, "text/html", "UTF-8", "");

    }


    /******************************************************************************
     **********************************Write to NFC Tag****************************
     ******************************************************************************/
    private void write(Tag tag) throws IOException, FormatException {
        NdefRecord[] records = RecordStorage.getRecords();
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }

    public static NdefRecord createRecord(String text, String type) {
        //byte[] typeBytes  = type.getBytes("US-ASCII");
        byte[] textBytes  = text.getBytes();

        //int    typeLength = typeBytes.length;
        int    textLength = textBytes.length;

        byte[] payload    = new byte[1 + textLength];

        // set status byte (see NDEF spec for actual bits)
        //payload[0] = (byte) textLength;

        // copy langbytes and textbytes into payload
        //System.arraycopy(typeBytes, 0, payload, 1, typeLength)
        System.arraycopy(textBytes, 0, payload, 0, textLength);


        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,  type.getBytes(),  new byte[0], payload);

        return recordNFC;
    }



    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }



    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
}
