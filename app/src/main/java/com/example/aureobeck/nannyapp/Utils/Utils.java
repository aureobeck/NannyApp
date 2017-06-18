package com.example.aureobeck.nannyapp.Utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by moblee on 5/15/17.
 */

public class Utils {
    public Context ctx;

    public Utils(Context context){
        super();
        ctx = context;
    }

    public void showToast(String message, Integer length) {
        final Toast toast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, length);
    }

}
