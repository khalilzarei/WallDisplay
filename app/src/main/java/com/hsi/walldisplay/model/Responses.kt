package com.hsi.walldisplay.model

import com.google.gson.annotations.SerializedName


data class ThermostatResponse(
        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var devicies: ArrayList<BuildingService> = arrayListOf(),
        @SerializedName("succeeded") var succeeded: Boolean? = null
)

data class SceneResponse(

        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var scenes: ArrayList<HomeScene> = arrayListOf(),
        @SerializedName("succeeded") var succeeded: Boolean? = null
)

data class JobResponse(

        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var jobs: ArrayList<Job> = arrayListOf(),
        @SerializedName("succeeded") var succeeded: Boolean? = null
)

data class StoreJobResponse(

        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var job: Job = Job(),
        @SerializedName("succeeded") var succeeded: Boolean? = null
)

data class SpecialServiceResponse(
        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var buildingService: BuildingService,
        @SerializedName("succeeded") var succeeded: Boolean? = null
)

data class ServiceResponse(
        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var buildingServices: ArrayList<BuildingService> = arrayListOf(),
        @SerializedName("succeeded") var succeeded: Boolean? = null
)

data class BuildingResponse(
        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var data: ArrayList<Building> = arrayListOf(),
        @SerializedName("succeeded") var succeeded: Boolean? = null
)


class LoginResponse(
        @SerializedName("message") var message: Any? = null,
        @SerializedName("data") var user: User? = User(),
        @SerializedName("succeeded") var succeeded: Boolean? = null
)


data class BuildingsToMood(
        @SerializedName("name") var name: String? = null,
        @SerializedName("services") var services: ArrayList<MoodBuildingService> = arrayListOf()

)


data class BuildingMoodResponse(
        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var data: ArrayList<MoodBuilding>? = arrayListOf(),
        @SerializedName("succeeded") var succeeded: Boolean? = null

)


data class MoodsResponse(

        @SerializedName("message") var message: String? = null,
        @SerializedName("data") var moodBuildings: ArrayList<MoodBuilding> = arrayListOf(),
        @SerializedName("succeeded") var succeeded: Boolean? = null

)