package com.hsi.walldisplay.helper

import com.hsi.walldisplay.model.*


interface CurtainClicked {
    fun onCurtainClicked(item: Curtain)
}


interface BuildingClickListener {
    fun onBuildingClickListener(building: Building?)
    fun onBuildingTitleClickListener(building: Building?)
}


interface SceneClickListener {
    fun onSceneClickListener(homeScene: HomeScene?)
    fun onSceneTitleLongClickListener(homeScene: HomeScene?)

    fun onSceneImageLongClickListener(homeScene: HomeScene?)

}

//interface ThermostatClicked {
//
//   fun onThermostatClicked(item: Thermostat)
//   fun onThermostatDeleteClicked(item: Thermostat)
//}