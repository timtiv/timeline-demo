package com.tim.cubo.internet;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tim.cubo.Cubo;

public class InternetRequest {

    private static RequestQueue queue;

    private static class RequestTask {

        private Response.Listener<String> successListener;
        private Response.ErrorListener errorListener;

        RequestTask(Listener<String> successListener,
            ErrorListener errorListener) {
            this.successListener = successListener;
            this.errorListener = errorListener;
        }

        private void execute(String url) {
            StringRequest request = new StringRequest(Method.GET, url, successListener,
                errorListener);
            request.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy
                .DEFAULT_BACKOFF_MULT) {
            });
            getQueue().add(request);
        }
    }

    private static RequestQueue getQueue() {
        if (queue == null) {
            queue = Volley.newRequestQueue(Cubo.getAppContext());
        }
        return queue;
    }

    static void getAlerts(String url, CuboResponseListener successListener,
        ErrorListener errorListener) {
        new RequestTask(successListener, errorListener).execute(url);
    }
}
