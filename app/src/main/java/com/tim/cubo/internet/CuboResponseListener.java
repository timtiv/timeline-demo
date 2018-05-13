package com.tim.cubo.internet;

import android.app.Activity;
import android.util.Log;
import com.android.volley.Response;

public class CuboResponseListener implements Response.Listener<String>, CuboResponse {

    private Activity parentActivity;

    public CuboResponseListener(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @Override
    public void onResponse(final String response) {
        Log.d("cubo", response);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (parentActivity != null) {
                    handleResponse(response);
                }
            }
        }).start();
    }

    @Override
    public void handleResponse(String response) {

    }
}
