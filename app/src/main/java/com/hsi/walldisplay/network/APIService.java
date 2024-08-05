package com.hsi.walldisplay.network;



import com.hsi.walldisplay.model.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {


    //region ProjectInfo

//    @FormUrlEncoded
//    @POST("getProjectInfo.php")
//    Call<ProjectInfo> getProjectInfo(@Field("project_id") int projectId);


    //endregion


    //region delete Job

    @DELETE("scenes/jobs/{jobId}")
    Call<JobResponse> deleteJob(@Header("Authorization") String authorization, @Path("jobId") int jobId);

    //endregion

    //region login

    @FormUrlEncoded
    @POST("oauth/login")
    Call<LoginResponse> login(@Field("email") String email, @Field("password") String password, @Field("sync") int sync);


    //endregion

    //region Get Buildings

    @GET("buildings/get")
    Call<BuildingResponse> getBuildings(@Header("Authorization") String authorization);

    //endregion


    //region Get Thermostat

    @GET("thermostats/get")
    Call<ServiceResponse> getThermostats(@Header("Authorization") String authorization);

    //endregion

    //region Get Special Service

    @GET("services/get/{deviceId}")
    Call<SpecialServiceResponse> getSpecialService(@Header("Authorization") String authorization,
                                                   @Path("deviceId") int roomId);

    //endregion


    //region Get Building Services

    @GET("services/get/building/{roomId}")
    Call<ServiceResponse> getBuildingServices(@Header("Authorization") String authorization,
                                              @Path("roomId") int roomId);

    //endregion

    //region Get Services

    @GET("services/get")
    Call<ServiceResponse> getServices(@Header("Authorization") String authorization);

    //endregion


    //region Get Scenes

    @GET("scenes/get")
    Call<SceneResponse> getScenes(@Header("Authorization") String authorization);

    //endregion


    //region Get Moods

    @GET("/moods/get/array")
    Call<MoodsResponse> getMoods(@Header("Authorization") String authorization);

    //endregion


    //region store Mood

    @POST("/moods/{building_id}")
    Call<BuildingMoodResponse> storeMood(@Body BuildingsToMood buildingsToMood, @Path("building_id") int buildingId);

    //endregion


    //region store Job

    @POST("scenes/jobs/save")
    Call<StoreJobResponse> storeJob(@Header("Authorization") String authorization, @Body JobRaw job);

    //endregion


    //region get Job

    @GET("scenes/jobs/get")
    Call<JobResponse> getJobs(@Header("Authorization") String authorization);

    //endregion

}
