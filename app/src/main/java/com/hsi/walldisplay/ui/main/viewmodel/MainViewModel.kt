package com.hsi.walldisplay.ui.main.viewmodel

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ImageView
import android.widget.Spinner
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.hsi.walldisplay.BR
import com.hsi.walldisplay.R
import com.hsi.walldisplay.databinding.DialogChangeNameBinding
import com.hsi.walldisplay.databinding.DialogFloorsBinding
import com.hsi.walldisplay.helper.BuildingClickListener
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.helper.SceneClickListener
import com.hsi.walldisplay.helper.SessionManager
import com.hsi.walldisplay.model.Building
import com.hsi.walldisplay.model.BuildingService
import com.hsi.walldisplay.model.DeviceType
import com.hsi.walldisplay.model.HomeScene
import com.hsi.walldisplay.model.ShowLayout
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.ui.main.adapter.BuildingAdapter
import com.hsi.walldisplay.ui.main.adapter.CityAdapter
import com.hsi.walldisplay.ui.main.adapter.CurtainAdapter
import com.hsi.walldisplay.ui.main.adapter.FontAdapter
import com.hsi.walldisplay.ui.main.adapter.LightAdapter
import com.hsi.walldisplay.ui.main.adapter.SceneAdapter
import com.hsi.walldisplay.ui.main.adapter.ThermostatAdapter
import com.hsi.walldisplay.view.ArcSeekBar
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

class MainViewModel(val activity: MainActivity) : BaseObservable(),
    SceneClickListener,
    BuildingClickListener {

    //region companion object

    companion object {
        private const val TAG = "MainViewModel"

        @JvmStatic
        @BindingAdapter("setImageUrl")
        fun setImageUrl(imageView: ImageView, imageUrl: String) {
            val options: RequestOptions =
                RequestOptions().centerCrop().placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)

            if (imageUrl.isNotEmpty() && imageUrl.contains("/"))
                Glide.with(imageView)
                    .load(imageUrl)
                    .apply(options)
                    .into(imageView)
        }

        @JvmStatic
        @BindingAdapter("setTextVisibility")
        fun setTextVisibility(view: View, visibility: Boolean) {
            view.visibility = if (visibility) View.VISIBLE else View.GONE
        }

        @JvmStatic
        @BindingAdapter("setYoYoAnimation")
        fun setYoYoAnimation(view: View, animation: Techniques) {
            YoYo.with(animation)
                .duration(700)
                .repeat(5)
                .playOn(view);
        }

        @JvmStatic
        @BindingAdapter("setChecked")
        fun setChecked(toggleButton: ToggleButton, device: BuildingService) {
            toggleButton.isChecked = device.value!!.split(",")[0].toInt() > 0
        }

        @JvmStatic
        @BindingAdapter("setCircleImageUrl")
        fun setCircleImageUrl(imageView: CircleImageView, imageUrl: String) {
            if (imageUrl.isNotEmpty() && imageUrl.contains("/"))
                Glide.with(imageView)
                    .load(imageUrl)
                    .into(imageView)

        }


        @JvmStatic
        @BindingAdapter("setCurtainAdapter")
        fun setCurtainAdapter(
            recyclerView: RecyclerView,
            adapter: CurtainAdapter?
        ) {
            recyclerView.layoutManager = LinearLayoutManager(
                recyclerView.context, RecyclerView.VERTICAL, false
            )
            recyclerView.adapter = adapter

        }


        @JvmStatic
        @BindingAdapter("setThermostatProgress")
        fun setThermostatProgress(
            seekBar: ArcSeekBar,
            progress: Int
        ) {
            seekBar.progress = progress
        }

        @JvmStatic
        @BindingAdapter("setThermostatAdapter")
        fun setThermostatAdapter(
            recyclerView: RecyclerView,
            adapter: ThermostatAdapter?
        ) {
            val layoutManager = object : LinearLayoutManager(recyclerView.context) {
                override fun canScrollVertically() = false// adapter.viewModel.layoutSeekBarVisibility
            }

            recyclerView.layoutManager = layoutManager// LinearLayoutManager(recyclerView.context)
            recyclerView.adapter = adapter
        }

        @JvmStatic
        @BindingAdapter("setSceneAdapter")
        fun setSceneAdapter(
            recyclerView: RecyclerView,
            adapter: SceneAdapter?
        ) {
            recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 2)
            recyclerView.adapter = adapter

        }

        @JvmStatic
        @BindingAdapter("setLightAdapter")
        fun setLightAdapter(
            recyclerView: RecyclerView,
            adapter: LightAdapter?
        ) {
            Log.d(TAG, "setLightAdapter: ${adapter!!.itemCount}")
            recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 1)
            recyclerView.adapter = adapter

        }

        @JvmStatic
        @BindingAdapter("setFontAdapter")
        fun setFontAdapter(
            spinner: Spinner,
            adapter: FontAdapter?
        ) {
            val activity = spinner.context as MainActivity
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val item = adapter!!.getItem(position)
                    SessionManager.font = item!!.font!!
                    activity.setTypeFaces()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }


        }

        @JvmStatic
        @BindingAdapter("setCityAdapter")
        fun setCityAdapter(
            spinner: Spinner,
            adapter: CityAdapter?
        ) {
            val activity = spinner.context as MainActivity
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val item = adapter!!.getItem(position)
                    SessionManager.city = "${item.name},${item.country}"
                    activity.viewModel.cityName = "${item.name},${item.country}"
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        }

    }

    //endregion

    //region Variable

    @get:Bindable
    var curtainTextVisibility: Boolean by Delegates.observable(true) { _, _, _ ->
        notifyPropertyChanged(BR.curtainTextVisibility)
    }

    @get:Bindable
    var acTextVisibility: Boolean by Delegates.observable(true) { _, _, _ ->
        notifyPropertyChanged(BR.acTextVisibility)
    }

    @get:Bindable
    var lightTextVisibility: Boolean by Delegates.observable(true) { _, _, _ ->
        notifyPropertyChanged(BR.lightTextVisibility)
    }

    @get:Bindable
    var building: Building by Delegates.observable(
        if (activity.sessionManager.selectedBuildingId != -1)
            activity.dataBaseDao.getBuilding(activity.sessionManager.selectedBuildingId)
        else
            activity.dataBaseDao.buildingItems[0]
    ) { _, _, _ ->
        notifyPropertyChanged(BR.building)
    }

    @get:Bindable
    var curtainAdapter: CurtainAdapter by Delegates.observable(
        CurtainAdapter(
            activity,
            if (activity.dataBaseDao.getBuildingCurtain(building.id).isNotEmpty())
                activity.dataBaseDao.getBuildingCurtain(building.id) as ArrayList
            else arrayListOf()
        )
    ) { _, _, _ ->
        notifyPropertyChanged(BR.curtainAdapter)
    }

    @get:Bindable
    var fontAdapter: FontAdapter by Delegates.observable(
        FontAdapter(
            activity,
            activity.getFontList()
        )
    ) { _, _, _ ->
        notifyPropertyChanged(BR.fontAdapter)
    }

    @get:Bindable
    var cityAdapter: CityAdapter by Delegates.observable(
        CityAdapter(
            activity,
            activity.getCityList()
        )
    ) { _, _, _ ->
        notifyPropertyChanged(BR.cityAdapter)
    }

    @get:Bindable
    var sceneAdapter: SceneAdapter by Delegates.observable(
        SceneAdapter(
            activity,
            this,
            if (activity.dataBaseDao.getBuildingScene(building.id).isNotEmpty())
                activity.dataBaseDao.getBuildingScene(building.id) as ArrayList
            else arrayListOf()
        )
    ) { _, _, _ ->
        notifyPropertyChanged(BR.sceneAdapter)
    }

    @get:Bindable
    var lightAdapter: LightAdapter by Delegates.observable(
        LightAdapter(
            activity,
            if (activity.dataBaseDao.getDaliLightsOfBuilding(building.id).isNotEmpty())
                activity.dataBaseDao.getDaliLightsOfBuilding(building.id) as ArrayList
            else arrayListOf()
        )
    ) { _, _, _ ->
        notifyPropertyChanged(BR.lightAdapter)
    }

    @get:Bindable
    var scene: HomeScene by Delegates.observable(
        if (activity.dataBaseDao.getBuildingScene(building.id).isNotEmpty())
            activity.dataBaseDao.getBuildingScene(building.id)[0]
        else HomeScene()
    ) { _, _, _ ->
        notifyPropertyChanged(BR.scene)
    }

    @get:Bindable
    var thermostatAdapter: ThermostatAdapter by Delegates.observable(
        ThermostatAdapter(
            activity,
            if (activity.dataBaseDao.getBuildingDevices(building.id, DeviceType.THERMOSTAT).isNotEmpty())
                activity.dataBaseDao.getBuildingDevices(building.id, DeviceType.THERMOSTAT) as ArrayList
            else arrayListOf()
        )
    ) { _, _, _ ->
        notifyPropertyChanged(BR.thermostatAdapter)
    }

    @get:Bindable
    var dateString: String by Delegates.observable(
        SimpleDateFormat("EEE , d MMM , yyyy").format(Date())
    ) { _, _, _ ->
        notifyPropertyChanged(BR.dateString)
    }

    @get:Bindable
    var cityName: String by Delegates.observable(
        SessionManager.city
    ) { _, _, _ ->
        notifyPropertyChanged(BR.cityName)
    }

    @get:Bindable
    var showLayout: ShowLayout by Delegates.observable(
        ShowLayout.DEFAULT
    ) { _, _, _ ->
        notifyPropertyChanged(BR.showLayout)
    }
    val topic = if (lightAdapter.getItems().size > 0) {
        "${activity.sessionManager.user.projectId}/${lightAdapter.getItems()[0].masterId}/${Constants.DALI_IN}"
    } else {
        null
    }
    //endregion

    //region Clicked


    private var count = 0
    fun showRoomListDialog(view: View) {
        count++
        if (count >= 10) {
            activity.toast("Show Room List")
            count = 0
            showFloorDialog()
        }
    }

    fun broadCastOn(view: View) {
        if (activity.dataBaseDao.getDaliLightsOfBuilding(building.id).isNotEmpty()) {
            if (topic != null) {
                val message = "{\"DAPC\":\"254\",\"BROADCAST\":\"ALL \"}"
                activity.publishMessage(topic, message)
            }
        } else activity.toast("Dali Lights not found")

        lightTextVisibility = !lightTextVisibility
    }

    fun broadCastOff(view: View) {
        if (activity.dataBaseDao.getDaliLightsOfBuilding(building.id).isNotEmpty()) {
            if (topic != null) {
                val message = "{\"DAPC\":\"0\",\"BROADCAST\":\"ALL \"}"
                activity.publishMessage(topic, message)
            }
        } else activity.toast("Dali Lights not found")

        lightTextVisibility = !lightTextVisibility
    }

    fun thermostatsOff(view: View) {
        if (activity.dataBaseDao.getBuildingDevices(building.id, DeviceType.THERMOSTAT).isNotEmpty()) {
            for (thermostat in thermostatAdapter.getItems()) {
                val topic = "${activity.sessionManager.user.projectId}/${Constants.THERMOSTAT_IN}"
                val message = "{\"id\":\"${thermostat.serviceId}\",\"command\":\"off\"}"
                activity.publishMessage(topic, message)
                Thread.sleep(300)
            }
        } else
            activity.toast("Air Condition found")
        acTextVisibility = !acTextVisibility
    }

    fun thermostatsOn(view: View) {
        if (activity.dataBaseDao.getBuildingDevices(building.id, DeviceType.THERMOSTAT).isNotEmpty()) {
            for (thermostat in thermostatAdapter.getItems()) {
                val topic = "${activity.sessionManager.user.projectId}/${Constants.THERMOSTAT_IN}"
                val message = "{\"id\":\"${thermostat.serviceId}\",\"command\":\"on\"}"
                activity.publishMessage(topic, message)
                Thread.sleep(300)
            }

        } else
            activity.toast("Air Condition found")

        acTextVisibility = !acTextVisibility
    }

    fun curtainsOff(view: View) {
        if (activity.dataBaseDao.getBuildingCurtain(building.id).isNotEmpty()) {
            for (curtain in curtainAdapter.getCurtains()) {
                val topic = "${activity.sessionManager.user.projectId}/${Constants.CURTAIN_IN}"
                val message = "{\"id\":\"${curtain.serviceId}\",\"command\":\"close\"}"
                activity.publishMessage(topic, message)
                Thread.sleep(300)
            }
        } else
            activity.toast("Curtain not found")


        curtainTextVisibility = !curtainTextVisibility
    }

    fun curtainsOn(view: View) {
        if (activity.dataBaseDao.getBuildingCurtain(building.id).isNotEmpty()) {
            for (curtain in curtainAdapter.getCurtains()) {
                val topic = "${activity.sessionManager.user.projectId}/${Constants.CURTAIN_IN}"
                val message = "{\"id\":\"${curtain.serviceId}\",\"command\":\"open\"}"
                activity.publishMessage(topic, message)
                Thread.sleep(300)
            }
        } else
            activity.toast("Curtain not found")

        curtainTextVisibility = !curtainTextVisibility
    }


    private fun showFloorDialog() {
        val dialogFloors: DialogFloorsBinding by lazy { DialogFloorsBinding.inflate(activity.layoutInflater) }
        val builder = AlertDialog.Builder(activity, R.style.WideDialog)
        builder.setCancelable(false)
        builder.setView(dialogFloors.root)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        dialogFloors.btnClose.setOnClickListener { alertDialog.dismiss() }

        activity.setFontAndFontSize(dialogFloors.rvRooms)

        val roomMoodAdapter = BuildingAdapter(
            activity,
            this,
            activity.dataBaseDao.buildingItems as ArrayList<Building>
        )
        dialogFloors.rvRooms.adapter = roomMoodAdapter
        dialogFloors.rvRooms.layoutManager = GridLayoutManager(activity, 2)

    }

    fun onLightListClicked(view: View) {
        if (activity.dataBaseDao.getDaliLightsOfBuilding(building.id).isNotEmpty()) {
            showLayout = ShowLayout.DALIS_LIST
            lightAdapter = LightAdapter(activity, arrayListOf())
            this.lightAdapter = LightAdapter(activity, activity.dataBaseDao.getDaliLightsOfBuilding(building.id) as ArrayList)
            lightAdapter.notifyDataSetChanged()
        } else activity.toast("Dali Lights not found")
    }

    fun onSceneListClicked(view: View) {
        activity.toast("onSceneListClicked")
        if (activity.dataBaseDao.getBuildingScene(building.id).isNotEmpty())
            showLayout = ShowLayout.SCENE
        else activity.toast("Scene not found")
    }

    fun onLightsClicked(view: View) {
        showLayout = ShowLayout.DALIS
    }

    fun onAirConditionClicked(view: View) {
        if (activity.dataBaseDao.getBuildingDevices(building.id, DeviceType.THERMOSTAT).isNotEmpty())
            showLayout = ShowLayout.AIR_CONDITION
        else
            activity.toast("Air Condition found")
    }

    fun onCurtainClicked(view: View) {
        if (activity.dataBaseDao.getBuildingCurtain(building.id).isNotEmpty())
            showLayout = ShowLayout.CURTAIN
        else
            activity.toast("Curtain not found")
    }

    fun onCurtainTextClicked(view: View) {
        activity.toast("Curtain not found")
        curtainTextVisibility = !curtainTextVisibility
    }

    fun onLightTextClicked(view: View) {
        activity.toast("Curtain not found")
        lightTextVisibility = !lightTextVisibility
    }

    fun onACTextClicked(view: View) {
        activity.toast("Curtain not found")
        acTextVisibility = !acTextVisibility
    }

    fun onBackClicked(view: View) {
        showLayout = ShowLayout.DEFAULT
    }

    fun onHomeClicked(view: View) {
        showLayout = ShowLayout.DEFAULT
    }

    fun onSettingsClicked(view: View) {
        showLayout = ShowLayout.SETTINGS

    }

    fun onLogOutClicked(view: View) {

    }

    //endregion

    //region SceneClickListener

    override fun onSceneClickListener(scene: HomeScene?) {
        val masters = scene?.masterId!!.split(",")
        activity.log("onSceneClickListener $masters")
        if (!masters.isNullOrEmpty())
            for (master in masters) {
                if (master.toInt() >= 0) {
                    val topic = "${activity.sessionManager.user.projectId}/$master/${Constants.DALI_IN}"
                    val msg = "{\"CMD\r\n\":\"GO_TO_SCENE_${scene.sceneId}\r\n\",\"BROADCAST\r\n\":\"ALL\r\n\"}"
                    activity.publishMessage(topic, msg)
                }
            }
    }

    override fun onSceneTitleLongClickListener(homeScene: HomeScene?) {
        val dialogChangeName: DialogChangeNameBinding by lazy { DialogChangeNameBinding.inflate(activity.layoutInflater) }
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogChangeName.root)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()

        activity.setFontAndFontSize(dialogChangeName.root)

        dialogChangeName.tvTitle.text = "Change title of ${homeScene!!.name}"
        val etBuildingName = dialogChangeName.etBuildingName
        etBuildingName.setText(homeScene.name)
        dialogChangeName.btnClose.setOnClickListener { alertDialog.dismiss() }

        dialogChangeName.btnSave.setOnClickListener {
            val buildingName: String = etBuildingName.text.toString()
            if (buildingName.isEmpty()) {
                activity.toast("Please Enter Scene name")
                return@setOnClickListener
            }
            homeScene.name = buildingName
            activity.dataBaseDao.updateScene(homeScene)

            sceneAdapter.setData(activity.dataBaseDao.getBuildingScene(building.id))
            alertDialog.dismiss()
        }
    }

    override fun onSceneImageLongClickListener(homeScene: HomeScene?) {
        scene = homeScene!!

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private var resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            if (data != null) {
                val selectedImageUri: Uri? = data.data
                scene.imagePath = selectedImageUri.toString()
                activity.dataBaseDao.updateScene(scene)
                sceneAdapter.setData(activity.dataBaseDao.getBuildingScene(building.id))
            }

        }
    }

    override fun onBuildingClickListener(building: Building?) {
        this.building = building!!
        activity.sessionManager.selectedBuildingId = building.id!!
        this.sceneAdapter = SceneAdapter(
            activity,
            this,
            if (activity.dataBaseDao.getBuildingScene(building.id).isNotEmpty())
                activity.dataBaseDao.getBuildingScene(building.id) as ArrayList
            else arrayListOf()
        )

        if (activity.dataBaseDao.getBuildingScene(building.id).isNotEmpty())
            scene = activity.dataBaseDao.getBuildingScene(building.id)[0]

        this.curtainAdapter = CurtainAdapter(
            activity,
            if (activity.dataBaseDao.getBuildingCurtain(building.id).isNotEmpty())
                activity.dataBaseDao.getBuildingCurtain(building.id) as ArrayList
            else arrayListOf()
        )
        lightAdapter = LightAdapter(activity, arrayListOf())
        this.lightAdapter = LightAdapter(activity, activity.dataBaseDao.getDaliLightsOfBuilding(building.id) as ArrayList)
        lightAdapter.notifyDataSetChanged()
        this.thermostatAdapter = ThermostatAdapter(
            activity,
            if (activity.dataBaseDao.getBuildingDevices(building.id, DeviceType.THERMOSTAT).isNotEmpty())
                activity.dataBaseDao.getBuildingDevices(building.id, DeviceType.THERMOSTAT) as ArrayList
            else arrayListOf()
        )
    }

    override fun onBuildingTitleClickListener(building: Building?) {
    }
//endregion
}