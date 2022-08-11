package com.lsk.bigimageviewer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.service.controls.templates.RangeTemplate;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.downloader.request.DownloadRequestBuilder;
import com.wanjian.cockroach.Cockroach;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private EditText urlInput;
    private Button downloadButton;
    private File dataDir;
    private ProgressDialog downloadProgress;
    private Handler progressHandler;

    private static final int PROGRESS_DIALOG_SHOW = 0;
    private static final int PROGRESS_DIALOG_DISMISS = 1;
    private static final int PROGRESS_DIALOG_PROGRESS = 2;
    private static final int PROGRESS_DIALOG_FAILED = 3;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cockroach.install((thread, throwable) -> {
            Toast.makeText(getApplicationContext(), "Thread: " + thread.getName() + " threw an uncaught exception: " + throwable.getMessage(), Toast.LENGTH_SHORT);
            Log.e("BigImageViewer:"+thread.getName(), "Caught exception: " + throwable.getClass().getCanonicalName() + ": " + throwable.getMessage(), throwable);
        });

        this.downloadProgress = new ProgressDialog(this);
        this.downloadProgress.setTitle("Downloading...");
        this.downloadProgress.setMax(100);
        this.downloadProgress.setProgress(0);
        this.downloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progressHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                switch (message.what) {
                    case PROGRESS_DIALOG_SHOW:
                        downloadProgress.setTitle((String) message.obj);
                        downloadProgress.show();
                        break;
                    case PROGRESS_DIALOG_DISMISS:
                        downloadProgress.dismiss();
                        break;
                    case PROGRESS_DIALOG_PROGRESS:
                        downloadProgress.setProgress(message.arg1);
                        break;
                    case PROGRESS_DIALOG_FAILED:
                        downloadProgress.dismiss();
                        Toast.makeText(getApplicationContext(), (String) message.obj, Toast.LENGTH_SHORT);
                        break;
                }
                return true;
            }
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

            parseFilename(url, s -> {
                if (s.equals("")) {
                    return;
                } else {
                    try {
                        DownloadRequestBuilder download = PRDownloader.download(url, dataDir.getCanonicalPath(), s);
                        download.build()
                                .setOnStartOrResumeListener(() -> {
                                    Message message = Message.obtain(progressHandler);
                                    message.obj = s;
                                    message.what = PROGRESS_DIALOG_SHOW;
                                    message.sendToTarget();
                                })
                                .setOnProgressListener(progress -> {
                                    int realProg = (int) Math.floor(progress.currentBytes / progress.totalBytes);
                                    Message message = Message.obtain(progressHandler);
                                    message.arg1 = realProg;
                                    message.what = PROGRESS_DIALOG_PROGRESS;
                                    message.sendToTarget();
                                })
                                .start(new OnDownloadListener() {
                                    @Override
                                    public void onDownloadComplete() {
                                        try {
                                            Message message = Message.obtain(progressHandler);
                                            message.what = PROGRESS_DIALOG_DISMISS;
                                            message.sendToTarget();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("filename", s);
                                            bundle.putString("storagePath", dataDir.getCanonicalPath());
                                            Intent intent = new Intent(MainActivity.this, ShowActivity.class);
                                            intent.putExtras(bundle);
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                    @Override
                                    public void onError(Error error) {
                                        Message message = Message.obtain(progressHandler);
                                        message.what = PROGRESS_DIALOG_FAILED;
                                        message.obj = error.getResponseCode() + ": " + error.getServerErrorMessage();
                                        message.sendToTarget();
                                    }
                                });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void parseFilename(String url, Consumer<String> filename) {
        String[] parts = url.split("/"); // https://xxxx.xxx:xxx/a/b/c.jpg -> https:, xxx.xxx:xxx, a, b, c.jpg
        String lastPart = parts[parts.length - 1]; // c.jpg
        if (lastPart.indexOf(".") == -1) {
            // User didn't provide a file name, then ask for it.
            final AskForNameDialog askForNameDialog = new AskForNameDialog(getApplicationContext());
            askForNameDialog.setOnCancel(s -> {
                askForNameDialog.dismiss();
                filename.accept(s);
            });
            askForNameDialog.setOnCancel(s -> {
                askForNameDialog.dismiss();
                Toast.makeText(getApplicationContext(), "A filename should be provided!", Toast.LENGTH_SHORT);
                filename.accept("");
            });
            askForNameDialog.show();
        } else {
            filename.accept(lastPart);
        }
    }
}