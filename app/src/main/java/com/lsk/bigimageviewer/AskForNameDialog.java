package com.lsk.bigimageviewer;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import java.util.function.Consumer;

public class AskForNameDialog extends Dialog {

    private Button okButton;
    private Button cancelButton;
    private EditText text;
    private Consumer<String> onOk;
    private Consumer<String> onCancel;

    public AskForNameDialog(Context context) {
        super(context);
    }

    public void setOnOk(Consumer<String> onOk) {
        this.onOk = onOk;
    }

    public void setOnCancel(Consumer<String> onCancel) {
        this.onCancel = onCancel;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ask_for_name_dialog);
        setCanceledOnTouchOutside(false);

        this.okButton = findViewById(R.id.asK_for_name_ok);
        this.cancelButton = findViewById(R.id.asK_for_name_cancel);
        this.text = findViewById(R.id.urlInput);

        this.okButton.setOnClickListener(view -> {
            if (this.onOk == null) {
                return;
            }
            this.onOk.accept(this.text.getText().toString());
        });

        this.cancelButton.setOnClickListener(view -> {
            if (this.onCancel == null) {
                return;
            }
            this.onCancel.accept(this.text.getText().toString());
        });
    }

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }
}
