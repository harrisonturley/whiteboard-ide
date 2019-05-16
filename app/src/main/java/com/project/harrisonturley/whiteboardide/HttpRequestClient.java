package com.project.harrisonturley.whiteboardide;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequestClient {

    interface HttpResponseListener {
        void onImageProcessingResponse(JSONObject response);
    }

    private static final String IMAGE_HEADER_KEY = "Ocp-Apim-Subscription-Key";
    private static final String IMAGE_DATA_TYPE_KEY = "Content-Type";
    private static final String IMAGE_DATA_TYPE_VALUE = "application/octet-stream";
    private static final String CODE_EXECUTE_CLIENT_ID = "clientId";
    private static final String CODE_EXECUTE_CLIENT_SECRET = "clientSecret";
    private static final String CODE_EXECUTE_TEXT = "script";
    private static final String CODE_EXECUTE_LANGUAGE = "language";
    private static final String CODE_EXECUTE_VERSION_INDEX = "versionIndex";

    private OkHttpClient mClient = new OkHttpClient();
    private HttpResponseListener httpResponseListener;
    private String imageProcessingKey;
    private String codeExecuteClientId;
    private String codeExecuteClientSecret;

    public HttpRequestClient(String imageProcessingKey, String codeExecuteClientId, String codeExecuteClientSecret, HttpResponseListener httpResponseListener) {
        this.imageProcessingKey = imageProcessingKey;
        this.codeExecuteClientId = codeExecuteClientId;
        this.codeExecuteClientSecret = codeExecuteClientSecret;
        this.httpResponseListener = httpResponseListener;
    }

    public Object postImage(String url, Map<String, Object> data) {
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody requestBody = RequestBody.create(mediaType,(byte[]) data.get("data"));

        Request request = new Request.Builder()
                .addHeader(IMAGE_HEADER_KEY, imageProcessingKey)
                .addHeader(IMAGE_DATA_TYPE_KEY, IMAGE_DATA_TYPE_VALUE)
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
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    getImageResult(response);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return request;
    }

    public Object postCode(String url, String code) {
        MediaType mediaType = MediaType.parse("application/json");

        try {
            JSONObject json = createCodeTextJSON(code);
            Log.e("Test", json.toString());
            RequestBody requestBody = RequestBody.create(mediaType, json.toString());
            //RequestBody requestBody = new FormBody.Builder()
                    /*.add(CODE_EXECUTE_CLIENT_ID, codeExecuteClientId)
                    .add(CODE_EXECUTE_CLIENT_SECRET, codeExecuteClientSecret)
                    .add(CODE_EXECUTE_TEXT, code)
                    .add(CODE_EXECUTE_LANGUAGE, "java")
                    .add(CODE_EXECUTE_VERSION_INDEX, "" + 2)
                    .build();*/
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Call call = mClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("Callback", "Fail");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("Callback", response.body().string());
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

    private void getImageResult(Response response) {
        Headers headers = response.headers();
        String url = headers.get("Operation-Location");

        try {
            Thread.sleep(10000);

            Request request = new Request.Builder()
                    .addHeader(IMAGE_HEADER_KEY, imageProcessingKey)
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

    private JSONObject createCodeTextJSON(String code) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(CODE_EXECUTE_CLIENT_ID, codeExecuteClientId);
        json.put(CODE_EXECUTE_CLIENT_SECRET, codeExecuteClientSecret);
        json.put(CODE_EXECUTE_TEXT, code);
        json.put(CODE_EXECUTE_LANGUAGE, "java");
        json.put(CODE_EXECUTE_VERSION_INDEX, "" + 2);

        return json;
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
