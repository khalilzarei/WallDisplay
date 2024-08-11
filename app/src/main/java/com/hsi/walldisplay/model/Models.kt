package com.hsi.walldisplay.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.hsi.walldisplay.helper.Constants
import java.io.Serializable


data class SceneMessage(

    @SerializedName("CMD") var cmd: String? = null, @SerializedName(
        "BROADCAST"
    ) var BROADCAST: String? = null

)

data class Light(
    @SerializedName(
        "QUERY_ACTUAL_LEVEL"
    ) var queryActualLevel: String, @SerializedName(
        "SHORT_ADDRESS"
    ) var shortAddress: String
)


@Entity(tableName = Constants.TABLE_NAME_JOB)
data class Job(
    @PrimaryKey @SerializedName("id") var id: Int? = null,
    @SerializedName("building_id") var buildingId: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("scene_id") var sceneId: Int? = null,
    @SerializedName("master_id") var masterId: String? = null,
    @SerializedName("curtains") var curtains: String? = null,
    @SerializedName("run_at") var runAt: String? = null,
)


data class JobRaw(
    @SerializedName("scene_id") var scene_id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("curtains") var curtains: String? = null,
    @SerializedName("run_at") var run_at: String? = null,
)

@Entity(tableName = Constants.TABLE_NAME_CURTAIN)
data class Curtain(

    @PrimaryKey(autoGenerate = true) @SerializedName("id") var id: Int? = null,
    @SerializedName("service_id") var serviceId: String? = null,
    @SerializedName("value") var value: Int? = null,
    @SerializedName("master_id") var masterId: Int? = null,
    @SerializedName("building_id") var buildingId: Int? = null,
    @SerializedName("name") var name: String? = null,

    ) : Serializable

data class DimMessage(

    @SerializedName("DAPC") var dapc: String? = null, @SerializedName(
        "SHORT_ADDRESS"
    ) var shortAddress: String? = null

)


data class ThermostatValueMessage(
    @SerializedName("id") var id: String? = null, @SerializedName(
        "command"
    ) var command: String? = null, @SerializedName("value") var value: String? = null
)

data class GroupMessage(
    @SerializedName("DAPC") var dapc: String? = null, @SerializedName(
        "GROUP_ADDRESS"
    ) var groupAddress: String? = null

)

data class OnOffMessage(

    @SerializedName("CMD") var cmd: String? = null, @SerializedName(
        "SHORT_ADDRESS"
    ) var shortAddress: String? = null

)

@Entity(tableName = Constants.TABLE_NAME_BUILDING_SERVICE)
class BuildingService(
    @PrimaryKey(autoGenerate = true) var idBuildingService: Int = 0,
    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("building_id") var buildingId: Int? = null,
    @SerializedName("location") var location: List<Double> = arrayListOf(),
    @SerializedName("service_id") var serviceId: String? = null,
    @SerializedName("group_id") var groupId: String? = null,
    @SerializedName("value") var value: String? = "",
    @SerializedName("type") var type: String? = null,
    @SerializedName("default_value") var defaultValue: Int = -1,
    @SerializedName("to_value") var toValue: Int = -1,
    @SerializedName("level") var level: String? = "",
    @SerializedName("off") var off: Int? = -1,
    @SerializedName("master_id") var masterId: Int? = -1,
) : Serializable


@Entity(tableName = Constants.TABLE_NAME_BUILDING)
data class Building(
    @PrimaryKey(autoGenerate = true) var idBuilding: Int = 0, @SerializedName(
        "id"
    ) var id: Int? = null, @SerializedName(
        "name"
    ) var name: String? = null, @SerializedName(
        "type"
    ) var type: String? = null, @SerializedName(
        "area"
    ) var area: Int? = null, @SerializedName(
        "image"
    ) var image: String? = null, @SerializedName(
        "haveAccess"
    ) var haveAccess: Boolean = false
) : Serializable


//@Entity(tableName = Const.TABLE_NAME_MOOD_BUILDING_SERVICE)
data class MoodBuildingService(
//    @PrimaryKey(autoGenerate = true) var id : Int = 0,
    @SerializedName("service_id") var serviceId: Int? = null, @SerializedName(
        "dim"
    ) var dim: List<Int> = arrayListOf()
)

@Entity(tableName = Constants.TABLE_NAME_TOPIC)
data class Topic(
    @SerializedName("topic") var topic: String? = null,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}


//@Entity(tableName = Const.TABLE_NAME_MOOD_BUILDING)
data class MoodBuilding(

//    @PrimaryKey(autoGenerate = true) var idMoodBuilding : Int = 0,
    @SerializedName("id") var id: Int? = null, @SerializedName(
        "building_id"
    ) var buildingId: Int? = null, @SerializedName(
        "name"
    ) var name: String? = null, @SerializedName(
        "services"
    ) var moodBuildingServices: ArrayList<MoodBuildingService> = arrayListOf(), @SerializedName(
        "created_at"
    ) var createdAt: String? = null, @SerializedName(
        "updated_at"
    ) var updatedAt: String? = null, @SerializedName(
        "deleted_at"
    ) var deletedAt: String? = null


)


@Entity(tableName = Constants.TABLE_NAME_SCENE)
data class HomeScene(
    @PrimaryKey(autoGenerate = true) var idScene: Int = 0,
    @SerializedName("id") var id: Int? = -1,
    @SerializedName("building_id") var buildingId: Int? = -1,
    @SerializedName("master_id") var masterId: String? = "",
    @SerializedName("scene_id") var sceneId: Int? = -1,
    @SerializedName("name") var name: String? = "",
    @SerializedName("image") var imagePath: String? = "",
    @SerializedName("scene_service") val sceneService: List<SceneService>? = arrayListOf(),
)

data class SceneService(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("user_id") var userId: Int? = null,
    @SerializedName("scene_id") var sceneId: Int? = null,
    @SerializedName("service_id") var serviceId: Int? = null,
    @SerializedName("value") val value: String? = null,
)

class User(
    @SerializedName("name") var name: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("project_id") var projectId: Int? = 0,
    @SerializedName("accessToken") var accessToken: String? = null,
)


enum class SettingLayout {
    DEFAULT, CONNECTION, INTERACTION, ICONS, ADVANCE_SETTING, LOAD_MOOD, SAVE_MOOD
}

enum class VisibilityRecycler {
    NONE, MOODS, FLOOR, SCENE
}

enum class DeviceClicked {
    ON, OFF, FLOOR, SCENE
}

enum class ShowLayout {
    DEFAULT, DALIS, DALIS_LIST, AIR_CONDITION, CURTAIN, SCENE
}

//'dali_light','rgb','cct','curtain','relay'
object DeviceType {
    const val DALI_LIGHT = "dali_light"
    const val RGB_DT6 = "rgb.dt6"
    const val RGB_DT8 = "rgb.dt8"
    const val CCT_DT6 = "cct.dt6"
    const val CCT_DT8 = "cct.dt8"
    const val CURTAIN = "curtain"
    const val GROUP = "group"
    const val THERMOSTAT = "Thermostat"
    const val TUNABLE = "tunable"
    const val RELAY = "relay"
}

//region FanSpeed

object FanSpeed {

    const val LOW = "low"
    const val MEDIUM = "medium"
    const val HIGH = "high"
    const val AUTO = "auto"
}

//endregion

enum class Connected {
    FALSE, PENDING, TRUE
}