// ApiService.java
package com.example.smartdevice;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    // 랭킹 조회 API
    // 최종 조합 URL: http://10.0.2.2/api/ + get_ranking.php
    @GET("get_ranking.php") // ★★★ 파일 이름만 지정 ★★★
    Call<List<Ranking>> getRankings();

    // 랭킹 저장 API
    // 최종 조합 URL: http://10.0.2.2/api/ + save_ranking.php
    @FormUrlEncoded
    @POST("save_ranking.php") // ★★★ 파일 이름만 지정 ★★★
    Call<SaveResponse> saveRanking(
            @Field("username") String username,
            @Field("score") int score,
            @Field("play_time") String play_time
    );

    // 랭킹 저장 후 응답을 받을 클래스 (성공/실패 여부)
    class SaveResponse {
        boolean success;
        String message;
    }
}
