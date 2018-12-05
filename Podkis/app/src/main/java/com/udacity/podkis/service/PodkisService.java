package com.udacity.podkis.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PodkisService {

    @GET("/{podcast}")
    Call<RssWrapper> getPodcastRss(@Path("podcast") String podcast);
}
