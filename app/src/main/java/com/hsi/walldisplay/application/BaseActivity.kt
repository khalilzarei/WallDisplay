package com.hsi.walldisplay.application

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hsi.walldisplay.R
import com.hsi.walldisplay.db.AppDatabase
import com.hsi.walldisplay.db.DataBaseDao
import com.hsi.walldisplay.helper.Calligrapher
import com.hsi.walldisplay.helper.Constants.API_URL
import com.hsi.walldisplay.helper.SessionManager
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import java.io.File
import java.net.URL
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection


abstract class BaseActivity : AppCompatActivity() {

    var activity = this

    private var TAG: String? = null

    var view: View? = null


    //    lateinit var user: User
    val url: String by lazy { "${SessionManager.localJobIp}${API_URL}" }


    val sessionManager: SessionManager by lazy { SessionManager(this) }

    private val database: AppDatabase by lazy {
        AppDatabase(this)
    }

    val dataBaseDao: DataBaseDao by lazy {
        database.dataBaseDao()
    }
    val gson: Gson by lazy {
        GsonBuilder().setLenient()
            .create()
    }
    private val builder: AlertDialog.Builder by lazy { AlertDialog.Builder(activity) }

    private val alertDialog: AlertDialog by lazy {
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        builder.create()
    }

    fun showProgressDialog() {
//      if (!alertDialog.isShowing) alertDialog.show()
    }

    fun dismissProgressDialog() {
        if (alertDialog.isShowing) Handler(Looper.getMainLooper()).postDelayed({
            alertDialog.dismiss()
        }, 2000)
    }

    val calligrapher: Calligrapher by lazy { Calligrapher(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTag(BaseActivity::class.java.simpleName)

//        if (sessionManager.iconSize <= 50)
//        sessionManager.iconSize = dpToPx(50)
        val config = PRDownloaderConfig.newBuilder()
            .setReadTimeout(30000)
            .setConnectTimeout(30000)
            .build()
        PRDownloader.initialize(applicationContext, config)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        if (sessionManager.isNightModeOn) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        user = if (sessionManager.isLoggedIn) sessionManager.user
//        else User()

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setFontAndFontSize()

    }

    fun setFontAndFontSize() {
        calligrapher.setFont(this, "fonts/${SessionManager.font}", true)
        calligrapher.setFontSize(this, SessionManager.fontSize, true)
    }

    fun setFontAndFontSize(view: View) {
        calligrapher.setFont(view, "fonts/${SessionManager.font}")
        calligrapher.setFontSize(view)
    }

    fun downloadFile(
        url: String,
        fileName: String,
    ) {
        PRDownloader.download(
            url,
            baseContext.filesDir.absolutePath,
            fileName,
        )
            .build()
            .setOnProgressListener {}
            .start(
                object : OnDownloadListener {
                    override fun onDownloadComplete() {
                    }

                    override fun onError(error: com.downloader.Error?) {
                        Toast.makeText(
                            baseContext, "Failed to download the $url", Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                },
            )
    }

    fun projectDir(): String? {
        val folderName: String = activity.resources.getString(R.string.app_name)
        var dir: File?
        dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/" + folderName,
            )
        } else {
            File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/" + folderName,
            )
        }
        if (!dir.exists()) {
            val success = dir.mkdirs()
            if (!success) {
                dir = null
            }
        }
        return dir?.path
    }

//    override fun attachBaseContext(newBase: Context) {
//        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
//    }

    fun logE(message: String?) {
        Log.e(TAG, message!!)
    }

    fun logW(message: String?) {
        Log.w(TAG, message!!)
    }

    fun logI(message: String?) {
        Log.i(TAG, message!!)
    }

    fun logD(message: String?) {
        Log.d(TAG, message!!)
    }

    fun toast(message: String?) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT)
            .show()
    }

    fun log(message: String?) {
        Log.e(TAG, message!!)
    }

    fun setTag(TAG: String?) {
        this.TAG = TAG
    }

    fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    fun pxToDp(px: Int): Int = (px / resources.displayMetrics.density).toInt()

    fun isEmailValid(email: String?): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    //region UI Util
    fun vibration() {
        //        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        //        if (SessionManager.getVibration()) {
        //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        //            } else {
        //                v.vibrate(500);
        //            }
        //        }
    }

    fun playSound() {
        //        MediaPlayer mp = MediaPlayer.create(activity, R.raw.lamp_sound);
        //        if (SessionManager.getPlaySound()) {
        //            mp.start();
        //        }
    }

    fun getRealLeft(left: Int): Int { //        return pxToDp((int) (((getWidthSize() * left) / 100)));
        return (widthSize * left / 100)
    }

    fun getRealTop(top: Int): Int { //        return pxToDp((int) (((getHeightSize() * top) / 100)));
        return (heightSize * top / 100) //        return (int) top;
    }

    val heightSize: Int
        get() {
            val displayMetrics = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }
    val widthSize: Int
        get() {
            val displayMetrics = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }

    //endregion
    fun showErrorSnackBar(message: String?) {
        Snackbar.make(view!!, message!!, Snackbar.LENGTH_INDEFINITE)
            .setBackgroundTint(Color.RED)
            .setAction("Ok", null)
            .setTextColor(Color.YELLOW)
            .show()
    }

    fun checkForInternet(): Boolean {
        // register activity with the connectivity manager service
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection

        // Returns a Network object corresponding to
        // the currently active default data network.
        val network = connectivityManager.activeNetwork ?: return false

        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    }

    fun isDeviceOnline(context: Context): Boolean {
        val connManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connManager.getNetworkCapabilities(connManager.activeNetwork)
        if (networkCapabilities == null) {
            log("Device Offline")
            return false
        } else {
            log("Device Online")
            return if (networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED
                )
            ) {
                log("Connected to Internet")
                true
            } else {
                log("Not connected to Internet")
                false
            }
        }
    }

    fun isInternetAvailable(): Boolean {
        val urlConnection = URL("https://google.com/").openConnection() as HttpsURLConnection
        try {
            urlConnection.setRequestProperty("User-Agent", "Android")
            urlConnection.setRequestProperty("Connection", "close")
            urlConnection.connectTimeout = 1000
            urlConnection.connect()
            return urlConnection.responseCode == 204
        } catch (e: Exception) {
            return false
        }
    }

    fun clearAllData() {
//        dataBaseDao.deleteBuilding()
//        dataBaseDao.deleteBuildingService()
//        dataBaseDao.deleteJob()
//        dataBaseDao.deleteScene()
//        dataBaseDao.deleteCurtain()
//        dataBaseDao.deleteTopic()
    }

    fun showSuccessSnackBar(message: String?) {
        Snackbar.make(view!!, message!!, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.GREEN)
            .setTextColor(Color.BLACK)
            .show()
    }

    fun showSuccessDialog(
        title: String?,
        message: String?,
    ) {
        AestheticDialog.Builder(this, DialogStyle.EMOTION, DialogType.SUCCESS)
            .setTitle(title!!)
            .setDuration(1000)
            .setMessage(message!!)
            .show()
    }

    @SuppressLint("MissingInflatedId")
    fun showErrorDialog(
        title: String?,
        message: String?,
    ) {
//        AestheticDialog.Builder(this, DialogStyle.EMOTION, DialogType.ERROR)
//            .setTitle(title!!)
//            .setDuration(2000)
//            .setMessage(message!!)
//            .show()

//        val builder = AlertDialog.Builder(activity)
//        val viewGroup: ViewGroup = activity.findViewById(android.R.id.content)
//        val dialogView: View = LayoutInflater.from(activity)
//            .inflate(R.layout.dialog_warning, viewGroup, false)
//        builder.setView(dialogView)
//        val alertDialog = builder.create()
//        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        alertDialog.show()
//
//        dialogView.findViewById<TextView>(R.id.tvMessage).text = message
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            alertDialog.dismiss()
//        }, 1000)
    }

    companion object {

        fun showErrorSnackBar(
            view: View?,
            message: String?,
        ) {
            Snackbar.make(view!!, message!!, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.RED)
                .setTextColor(Color.YELLOW)
                .show()
        }

        fun showSuccessSnackBar(
            view: View?,
            message: String?,
        ) {
            Snackbar.make(view!!, message!!, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.GREEN)
                .setTextColor(Color.BLACK)
                .show()
        }
    }
}