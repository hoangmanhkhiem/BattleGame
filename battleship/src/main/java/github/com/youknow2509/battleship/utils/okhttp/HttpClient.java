package github.com.youknow2509.battleship.utils.okhttp;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class HttpClient {
    private static final String BASE_URL = "http://127.0.0.1:5000";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public interface HttpResponseCallback {
        void onSuccess(String response);
        void onFailure(IOException e);
    }

    // Gửi yêu cầu POST tới /fire
    public static void fire(int x, int y, HttpResponseCallback callback) {
        String url = BASE_URL + "/fire";
        String json = gson.toJson(new FireRequest(x, y));

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure(new IOException("Request failed with code: " + response.code()));
                }
            }
        });
    }

    // Gửi yêu cầu POST tới /ai-move
    public static void aiMove(HttpResponseCallback callback) {
        String url = BASE_URL + "/ai-move";

        Request request = new Request.Builder().url(url).post(RequestBody.create("", null)).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure(new IOException("Request failed with code: " + response.code()));
                }
            }
        });
    }

    // Gửi yêu cầu GET tới /board
    public static void getBoard(HttpResponseCallback callback) {
        String url = BASE_URL + "/board";

        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure(new IOException("Request failed with code: " + response.code()));
                }
            }
        });
    }

    // Gửi yêu cầu POST tới /reset
    public static void reset(HttpResponseCallback callback) {
        String url = BASE_URL + "/reset";

        Request request = new Request.Builder().url(url).post(RequestBody.create("", null)).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure(new IOException("Request failed with code: " + response.code()));
                }
            }
        });
    }

    // Class đại diện cho dữ liệu gửi trong yêu cầu /fire
    private static class FireRequest {
        int x;
        int y;

        FireRequest(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
