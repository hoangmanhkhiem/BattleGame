package github.com.youknow2509.battleship.utils.okhttp;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.List;


public class HttpClient {
    public interface HttpResponseCallback {
        void onSuccess(String response);
        void onFailure(IOException e);
    }

    public static void SendDataToServer(int[][] placeShip, List<Integer> getListShipNotSunk, HttpResponseCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        String jsonData = gson.toJson(placeShip);
        String jsonData2 = gson.toJson(getListShipNotSunk);
        System.out.println(jsonData);
        System.out.println(jsonData2);
        // Tao body request
        String json = "{\"placeShip\":" + jsonData + ",\"getListShipNotSunk\":" + jsonData2 + "}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://127.0.0.1:5000/ai-battleship")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Pass the failure to the callback
                callback.onFailure(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Pass the response to the callback
                    callback.onSuccess(response.body().string());
                } else {
                    // Handle unsuccessful response
                    callback.onFailure(new IOException("Request failed with code: " + response.code()));
                }
            }
        });
    }

    public static class ShipData {
        int[][] placeShip;
        List<Integer> getListShipNotSunk;

        public ShipData(int[][] placeShip, List<Integer> getListShipNotSunk) {
            this.placeShip = placeShip;
            this.getListShipNotSunk = getListShipNotSunk;
        }
    }
}
