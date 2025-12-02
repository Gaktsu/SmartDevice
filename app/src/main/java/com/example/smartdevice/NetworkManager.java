package com.example.smartdevice;import android.util.Log;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {

    private static final String BASE_URL = "http://10.0.2.2/api/";
    private static Retrofit retrofit = null;

    public interface RankingCallback {
        void onRankingsReceived(List<Ranking> rankings);
        void onError(String message);
    }

    public static ApiService getApiService() {
        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    public void SaveRanking(String username, int score, String playTime) {
        ApiService api = getApiService();
        api.saveRanking(username, score, playTime).enqueue(new Callback<ApiService.SaveResponse>() {
            @Override
            public void onResponse(Call<ApiService.SaveResponse> call, Response<ApiService.SaveResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Log.d("APITest", "✅ 랭킹 저장 성공: " + response.body().message);
                } else {
                    String errorMsg = response.body() != null ? response.body().message : "응답 본문 없음";
                    Log.e("APITest", "❌ 랭킹 저장 실패, 서버 메시지: " + errorMsg + " (응답 코드: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<ApiService.SaveResponse> call, Throwable t) {
                Log.e("APITest", "❌ 랭킹 저장 중 네트워크 오류: " + t.getMessage());
            }
        });
    }

    public void GetRanking(RankingCallback callback) {
        ApiService api = getApiService();
        api.getRankings().enqueue(new Callback<List<Ranking>>() {
            @Override
            public void onResponse(Call<List<Ranking>> call, Response<List<Ranking>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    Log.d("APITest", "✅ 랭킹 조회 성공! 데이터 개수: " + response.body().size());
                    callback.onRankingsReceived(response.body());
                } else {

                    Log.e("APITest", "❌ 랭킹 조회 실패, 응답 코드: " + response.code());
                    callback.onError("랭킹 조회 실패, 응답 코드: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Ranking>> call, Throwable t) {

                Log.e("APITest", "❌ 랭킹 조회 완전 실패: " + t.getMessage());
                callback.onError("네트워크 오류: " + t.getMessage());
            }
        });
    }
}
