package com.lsk.bigimageviewer;

import android.app.Dialog;
import android.content.Context;

public class AskForNameDialog extends Dialog {

    private String name;

    public AskForNameDialog(Context context, String name) {
        super(context);
        this.name = name;
    }
}
