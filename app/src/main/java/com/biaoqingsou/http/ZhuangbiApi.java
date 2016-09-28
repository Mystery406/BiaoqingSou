package com.biaoqingsou.http;

import com.biaoqingsou.data.ZhuangbiData;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Mystery406.
 */

public interface ZhuangbiApi {
    @GET("search")
    Observable<List<ZhuangbiData>> search(
            @Query("q") String query);
}
