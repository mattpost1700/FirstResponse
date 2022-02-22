package com.example.first_responder_app;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.first_responder_app.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationService {
    String serverKey = BuildConfig.SERVER_KEY;

    public void notifyPostReq(Context context, String topic, String notifTitle, String notifBody) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url ="https://fcm.googleapis.com/fcm/send";
        String to = "/topics/" + topic;

        JSONObject notif = new JSONObject();
        notif.put("title", notifTitle);
        notif.put("body", notifBody);
        JSONObject json = new JSONObject();
        json.put("to", to);
        json.put("restricted_package_name", "com.example.first_responder_app");
        json.put("notification", notif);

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST,
                url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VOLLEY", "Success");
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VOLLEY", "Failure: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String key = "key=" + serverKey;
                headers.put("Authorization", key);
                return headers;
            }

        };
        queue.add(request_json);
    }
}
