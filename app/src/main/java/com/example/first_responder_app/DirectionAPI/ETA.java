package com.example.first_responder_app.DirectionAPI;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.util.Log;

import com.example.first_responder_app.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ETA extends AsyncTask<String, Void, String> {

    public interface ETAResult{
        void resultCallback(String s);
    }

    private ETAResult listener;

    public void setListener(ETAResult listener){
        this.listener = listener;
    }
    public void removeListener(){ this.listener = null; }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG, "onpost: " + strings[0] + " " + this);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        String res = "";

        if(!strings[0].equals("")) {
            Request request = new Request.Builder()
                    .url(strings[0] + "&key=" + BuildConfig.api_key)
                    .method("GET", null)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                JSONObject obj = new JSONObject(body);
                JSONArray arr = obj.getJSONArray("rows");
                arr = arr.getJSONObject(0).getJSONArray("elements");
                obj = arr.getJSONObject(0).getJSONObject("duration");
                res = obj.getString("text");

            } catch (IOException ioException) {
                Log.e("Error", "IOException " + ioException.getMessage());
                return "Error";
            } catch (JSONException jsonException) {
                Log.e("Error", "JSONException " + jsonException.getMessage());
                return "Error";
            }
        }
        return res;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: " + s);
        super.onPostExecute(s);
        if(listener != null){
            listener.resultCallback(s);
        }
    }
}

