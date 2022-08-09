package com.lsk.bigimageviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.service.controls.templates.RangeTemplate;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.wanjian.cockroach.Cockroach;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private EditText urlInput;
    private Button downloadButton;
    private int jobId;
    private File dataDir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cockroach.install((thread, throwable) -> {
            Toast.makeText(getApplicationContext(), "Thread: " + thread.getName() + " threw an uncaught exception: " + throwable.getMessage(), Toast.LENGTH_SHORT);
            Log.e("BigImageViewer:"+thread.getName(), "Caught exception: " + throwable.getClass().getCanonicalName() + ": " + throwable.getMessage(), throwable);
        });

        try {
            File sdcardDir = Environment.getExternalStorageDirectory();
            dataDir = new File(sdcardDir, "BigImageViewerDownload");
            if (!dataDir.exists()) {
                dataDir.mkdir();
            }
            Log.i("BigImageViewer:" + Thread.currentThread().getName(), "sdcard path: "+dataDir.getCanonicalPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        this.urlInput = findViewById(R.id.urlInput);
        this.downloadButton = findViewById(R.id.downloadBtn);
        this.downloadButton.setOnClickListener(view -> {
            String url = this.urlInput.getText().toString();
            if (url == null || url.equals("")) {
                Toast.makeText(getApplicationContext(), "URL is empty!", Toast.LENGTH_SHORT);
                return;
            }
            if (!url.matches("^((ht|f)tps?):\\/\\/([\\w-]+(\\.[\\w-]+)*\\/?)+(\\?([\\w\\-\\.,@?^=%&:\\/~\\+#]*)+)?$")) {
                Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_SHORT);
                return;
            }

            try {
                PRDownloader.download(url, dataDir.getCanonicalPath(), parseFilename(url));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    private String parseFilename(String url) {
        String[] parts = url.split("/"); // https://xxxx.xxx:xxx/a/b/c.jpg -> https:, xxx.xxx:xxx, a, b, c.jpg
        String lastPart = parts[parts.length - 1]; // c.jpg
        if (lastPart.indexOf(".") == -1) {
            // User didn't provide a file name, then ask for it.

        }
        return "";
    }
}