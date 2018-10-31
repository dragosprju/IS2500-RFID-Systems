package ben_dragos.nfc_active_poster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class DrawActivity extends Activity {

    DrawingView drawingView;
    ImageView img64View;
    Context context;
    Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_draw);

        drawingView = (DrawingView) findViewById(R.id.draw);
        img64View = (ImageView) findViewById(R.id.imgView);
        sendBtn = (Button) findViewById(R.id.buttonSendDraw);

        context = this;
        App.setContext(this);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap original512Bitmap = drawingView.getBitmap();
                Bitmap original64Bitmap = Bitmap.createScaledBitmap(original512Bitmap, 64, 64, false);

                String toDrawBitmap_string = BitmapTools.create1BitBitmap(original64Bitmap);
                Bitmap toDrawBitmap = BitmapTools.get1BitBitmap(toDrawBitmap_string);

                /*
                String picture = BitmapTools.bitmapToString(toDrawBitmap);
                byte[] decodedString = Base64.decode(picture, Base64.NO_WRAP);
                InputStream input = new ByteArrayInputStream(decodedString);
                Bitmap ext_pic = BitmapFactory.decodeStream(input);
                */

                img64View.setImageBitmap(toDrawBitmap);

                RecordStorage.save(toDrawBitmap_string, "text/1bitpic");

                Toast.makeText(context, "Drawing saved! Now go back and press 'Write' to send it to the NFC tag!", Toast.LENGTH_LONG ).show();
            }
        });


    }
}