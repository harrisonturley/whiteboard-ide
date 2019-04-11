package com.project.harrisonturley.whiteboardide;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpRequestClient {

    interface HttpResponseListener {
        void onImageProcessingResponse(JSONObject response);
    }

    private static final String HEADER_KEY = "Ocp-Apim-Subscription-Key";
    private static final String DATA_TYPE_KEY = "Content-Type";
    private static final String DATA_TYPE_VALUE = "application/octet-stream";

    private OkHttpClient mClient = new OkHttpClient();
    private HttpResponseListener httpResponseListener;
    private String imageProcessingKey;

    public HttpRequestClient(String imageProcessingKey, HttpResponseListener httpResponseListener) {
        this.imageProcessingKey = imageProcessingKey;
        this.httpResponseListener = httpResponseListener;
    }

    public Object postWriting(String url, Map<String, Object> data) {
        String json;
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody requestBody = RequestBody.create(mediaType,(byte[]) data.get("data"));

        Request request = new Request.Builder()
                .addHeader(HEADER_KEY, imageProcessingKey)
                .addHeader(DATA_TYPE_KEY, DATA_TYPE_VALUE)
                .url(url)
                .post(requestBody)
                .build();

        try {
            Call call = mClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    getResult(response);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getUrl(String path, Map<String, Object> params) {
        StringBuffer url = new StringBuffer(path);

        boolean start = true;
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (start) {
                url.append("?");
                start = false;
            } else {
                url.append("&");
            }

            try {
                url.append(param.getKey());
                url.append("=");
                url.append(URLEncoder.encode(param.getValue().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return url.toString();
    }

    private void getResult(Response response) {
        Headers headers = response.headers();
        String url = headers.get("Operation-Location");

        try {
            Thread.sleep(10000);

            Request request = new Request.Builder()
                    .addHeader(HEADER_KEY, imageProcessingKey)
                    .url(url)
                    .build();

            Response requestResponse = mClient.newCall(request).execute();
            String jsonString = requestResponse.body().string();
            JSONObject json = new JSONObject(jsonString);
            httpResponseListener.onImageProcessingResponse(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readInput(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer json = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            json.append(line);
        }

        return json.toString();
    }
}
