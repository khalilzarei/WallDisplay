package com.hsi.walldisplay.ui.main.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.hsi.walldisplay.BR
import com.hsi.walldisplay.model.HomeScene
import com.hsi.walldisplay.model.ShowLayout
import kotlin.properties.Delegates

class SceneViewModel(homeScene: HomeScene) : BaseObservable() {

    @get:Bindable
    var homeScene: HomeScene by Delegates.observable(
        homeScene
    ) { _, _, _ ->
        notifyPropertyChanged(BR.showLayout)
    }


}
