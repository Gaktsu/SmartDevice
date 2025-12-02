// ApiService.java
package com.example.smartdevice;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("get_ranking.php")
    Call<List<Ranking>> getRankings();

    @FormUrlEncoded
    @POST("save_ranking.php")
    Call<SaveResponse> saveRanking(
            @Field("username") String username,
            @Field("score") int score,
            @Field("play_time") String play_time
    );

    class SaveResponse {
        boolean success;
        String message;
    }
}
