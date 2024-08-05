package com.hsi.walldisplay.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.model.*


@Database(
    entities = [
        Building::class,
        BuildingService::class,
        Job::class,
        HomeScene::class,
        Curtain::class,
        Topic::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataBaseDao(): DataBaseDao


    companion object {


        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java, Constants.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    /*
        @PrimaryKey(autoGenerate = true) var idThermostat: Int = 0,
    override var id: Int? = null,
    override var buildingId: Int? = null,
    override var location: List<Double> = arrayListOf(),
    override var serviceId: String? = null,
    override var groupId: String? = null,
    override var value: String? = null,
    override var type: String? = null,
    override var defaultValue: Int = -1,

    @SerializedName("level") var masterId: Int? = null,
    @SerializedName("off") var off: Int? = null
     */

}