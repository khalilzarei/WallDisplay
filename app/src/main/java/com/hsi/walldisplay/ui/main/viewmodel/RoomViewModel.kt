package com.hsi.walldisplay.ui.main.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.hsi.walldisplay.model.Building
import com.hsi.walldisplay.BR
import kotlin.properties.Delegates

class RoomViewModel(building: Building) : BaseObservable() {


    @get:Bindable
    var building: Building by Delegates.observable(
        building
    ) { _, _, _ ->
        notifyPropertyChanged(BR.building)
    }
}
