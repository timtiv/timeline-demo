package com.tim.cubo.internet;

import android.app.Activity;
import android.util.Log;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tim.cubo.model.Alert;
import com.tim.cubo.Constant;
import java.io.IOException;
import java.util.List;

public class ApiRequest {

    private Activity parent;
    private ObjectMapper mapper = new ObjectMapper();


    public ApiRequest(Activity activity) {
        this.parent = activity;
    }

    public void getAlerts(final CuboCallback callback) {
        String url = "http://demo6998483.mockable.io/alerts";
        InternetRequest.getAlerts(url, new CuboResponseListener(parent) {
            @Override
            public void handleResponse(String response) {
                super.handleResponse(response);
                try {
                    final List<Alert> alerts = mapper
                        .readValue(response, new TypeReference<List<Alert>>() {
                        });

                    if (parent != null) {
                        parent.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.getAlerts(alerts);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Constant.TAG, error.getMessage(), error);
            }
        });
    }

}
