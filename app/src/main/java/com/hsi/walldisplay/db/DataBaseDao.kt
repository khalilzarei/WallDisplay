package com.hsi.walldisplay.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hsi.walldisplay.helper.Constants
import  com.hsi.walldisplay.model.*
import java.lang.reflect.Type
import java.util.Collections


@Dao
interface DataBaseDao {

    //    @get:Query("SELECT * FROM $TABLE_NAME_TEXT")
//    val textItems : List<TextItem>
//
//    @get:Query("SELECT * FROM $TABLE_NAME_VIDEO")
//    val videoItems : List<VideoItem>
//


    @Query(
        "SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE} WHERE buildingId= :buildingId AND type = :deviceType"
    )
    fun getBuildingDevices(buildingId: Int?, deviceType: String): List<BuildingService>

    @Query(
        "SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE} WHERE buildingId= :value AND type != '${
            DeviceType.CURTAIN
        }'"
    )
    fun getDaliLightsOfBuilding(value: Int?): List<BuildingService>

    @Query(
        "SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE} WHERE buildingId= :value AND type = '${
            DeviceType.THERMOSTAT
        }'"
    )
    fun getThermostatsOfBuilding(value: Int?): List<BuildingService>

    @Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE} WHERE buildingId= :value ")
    fun getDevicesOfBuilding(value: Int?): List<BuildingService>

    @Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE} WHERE type= :type ")
    fun getDevicesOfBuildingWithType(type: String?): List<BuildingService>

    /*
    //region Thermostat
    @Insert
    fun insertThermostat(thermostat: Thermostat)

    @Update
    fun updateThermostat(thermostat: Thermostat)

    //endregion

    */
    //region Building
    @Insert
    fun insertBuilding(building: Building)

    @Update
    fun updateBuilding(building: Building)

    @Query("DELETE FROM ${Constants.TABLE_NAME_BUILDING}")
    fun deleteBuilding()

    @get:Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING}")
    val buildingItems: List<Building>

    @Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING} WHERE id= :buildingId")
    fun getBuilding(buildingId: Int?): Building

    //endregion

    //region Service

    @Insert
    fun insertBuildingService(buildingService: BuildingService)

    @Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE} WHERE serviceId= :serviceId AND masterId= :masterId ")
    fun getBuildingService(
        serviceId: Int?,
        masterId: Int?
    ): BuildingService

    @Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE} WHERE serviceId= :serviceId AND type='Thermostat' ")
    fun getThermostat(serviceId: String?): BuildingService

    @Update
    fun updateBuildingService(buildingService: BuildingService)

    @Query("UPDATE ${Constants.TABLE_NAME_BUILDING_SERVICE} SET value = :value WHERE id = :id")
    fun updateBuildingServiceValue(
        id: Int,
        value: String?
    ): Int

    @Query("DELETE FROM ${Constants.TABLE_NAME_BUILDING_SERVICE}")
    fun deleteBuildingService()


    @get:Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE}")
    val buildingServiceItems: List<BuildingService>

    @Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE} WHERE buildingId= :value")
    fun getBuildingServices(value: Int?): List<BuildingService>

    @get:Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE}")
    val serviceItems: List<BuildingService>


    //endregion

    //region Scene

    @Insert
    fun insertScene(scene: HomeScene)

    @Update
    fun updateScene(scene: HomeScene)

    @Query("DELETE FROM ${Constants.TABLE_NAME_SCENE}")
    fun deleteScene()

    @Query("SELECT * FROM ${Constants.TABLE_NAME_SCENE} WHERE buildingId= :value")
    fun getBuildingScene(value: Int?): List<HomeScene>

    @Query("SELECT * FROM ${Constants.TABLE_NAME_SCENE} WHERE name= :value")
    fun getBuildingSceneWithName(value: String?): List<HomeScene>

    @Query("SELECT * FROM ${Constants.TABLE_NAME_SCENE} WHERE name= :value")
    fun getSceneWithName(value: String?): HomeScene

    @Query("SELECT * FROM ${Constants.TABLE_NAME_SCENE} WHERE sceneId= :sceneId AND masterId= :masterId ")
    fun getSceneWithIdAndMaster(
        sceneId: Int?,
        masterId: Int?
    ): HomeScene

    @Query("SELECT name FROM ${Constants.TABLE_NAME_SCENE} WHERE id= :value")
    fun getSceneWithId(value: Int?): String


    //endregion

    //region Curtain

    @Insert
    fun insertCurtain(curtain: Curtain)

    @Update
    fun updateCurtain(curtain: Curtain)

    @Query("DELETE FROM ${Constants.TABLE_NAME_CURTAIN}")
    fun deleteCurtain()

    @Query("SELECT * FROM ${Constants.TABLE_NAME_CURTAIN} WHERE buildingId= :value")
    fun getBuildingCurtain(value: Int?): List<Curtain>


    //endregion

    //region JOB

    @Query("SELECT * FROM ${Constants.TABLE_NAME_JOB}")
    fun getJobs(): List<Job>

    @Query("SELECT * FROM ${Constants.TABLE_NAME_JOB} WHERE buildingId= :buildingId ")
    fun getRoomJobs(buildingId: Int?): List<Job>

    @Insert
    fun insertJob(job: Job)

    @Delete
    fun deleteJob(job: Job)

    @Query("DELETE FROM ${Constants.TABLE_NAME_JOB}")
    fun deleteJob()

    @Query("DELETE FROM ${Constants.TABLE_NAME_JOB}")
    fun deleteAllJob()


    //endregion

    //region Topic
    @Insert
    fun insertTopic(topic: Topic)

    @Query("SELECT * FROM ${Constants.TABLE_NAME_TOPIC} WHERE topic= :value")
    fun getTopic(value: String?): Topic


    @get:Query("SELECT * FROM ${Constants.TABLE_NAME_TOPIC}")
    val topicItems: List<Topic>


    @Query("DELETE FROM ${Constants.TABLE_NAME_TOPIC}")
    fun deleteTopic()

    //endregion

    @Query("SELECT * FROM ${Constants.TABLE_NAME_BUILDING_SERVICE} WHERE groupId= :groupId AND masterId= :masterId ")
    fun getBuildingGroup(
        groupId: String?,
        masterId: Int?
    ): BuildingService


//
//    @Insert
//    fun insertMoodBuilding(item : MoodBuilding)

//
//    @Update
//    fun updateNote(noteItem : NoteItem)
//
//    @Delete
//    fun deleteNote(noteItem : NoteItem)
//
//    @Query("SELECT * FROM $TABLE_NAME_NOTE WHERE TextId = :textId")
//    fun getNotesWithTextId(textId : Int) : List<NoteItem>

}


class Converters {
    var gson = Gson()

    @TypeConverter
    fun stringToDoubleList(data: String?): List<Double> {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Double?>?>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun doubleListToString(someObjects: List<Double?>?): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToIntList(data: String?): List<Int> {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Int?>?>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun intListToString(someObjects: List<Int?>?): String {
        return gson.toJson(someObjects)
    }


    @TypeConverter
    fun stringToIntArrayList(data: String?): ArrayList<Int> {
        if (data == null) {
            return ArrayList<Int>()
        }
        val listType: Type = object : TypeToken<ArrayList<Int?>?>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun intArrayListToString(someObjects: ArrayList<Int?>?): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToStringList(data: String?): List<String> {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<Int?>?>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun stringListToString(someObjects: List<String?>?): String {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun fromSceneServiceList(sceneServiceList: List<SceneService>?): String? {
        if (sceneServiceList == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<SceneService>>() {}.type
        return gson.toJson(sceneServiceList, type)
    }

    @TypeConverter
    fun toSceneServiceList(sceneServiceString: String?): List<SceneService>? {
        if (sceneServiceString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<SceneService>>() {}.type
        return gson.fromJson(sceneServiceString, type)
    }

}
