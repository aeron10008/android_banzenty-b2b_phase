package com.restart.banzenty.base

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.RequestManager
import com.restart.banzenty.BuildConfig
import com.restart.banzenty.R
import com.restart.banzenty.network.DataState
import com.restart.banzenty.utils.*
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity(), DataStateChangeListener,
    UICommunicationListener {
    private val TAG = "BaseActivity"

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var requestManager: RequestManager
    var file: File? = null
    var selectedOption: Int = 0

    var imageCroppedInterface: ImageCroppedInterface? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUserSettings()
    }

    private fun initUserSettings() {
        AppCompatDelegate.setDefaultNightMode(
            sessionManager.getNightMode()
        )
        MainUtils.initializeSelectedLanguage(
            this,
            sessionManager.getLanguage()
        )
    }

    override fun onErrorStateChange(stateError: StateError) {
        GlobalScope.launch(Dispatchers.Main) {
            handleStateErrorEvent(stateError.response)
        }
    }

    private fun handleStateErrorEvent(errorResponse: Response) {
        when (errorResponse.responseType) {
            is ResponseType.Dialog -> {
                errorResponse.message?.let { message -> displayErrorDialog(message) }
                if (errorResponse.localizedMessage != null && errorResponse.localizedMessage != 0)
                    displayErrorDialog(errorResponse.localizedMessage)
            }
            is ResponseType.Toast -> {
                errorResponse.message?.let { message -> displayToast(message) }
            }
            is ResponseType.None -> {
                Log.e(TAG, "handleStateErrorEvent: ${errorResponse.message}")
            }
        }
    }

    override fun onDataStateChange(dataState: DataState<*>?) {
        Log.d(TAG, "onDataStateChange: ")
        dataState?.let {
            GlobalScope.launch(Dispatchers.Main) {
                showProgressBar(it.loading.isLoading)
                it.error?.let { errorEvent ->
                    Log.d(TAG, "onDataStateChange: Error")
                    handleStateErrorEvent(errorEvent)
                }
                it.data?.let {
                    it.response?.let { responseEvent ->
                        Log.d(TAG, "onDataStateChange: Success")
                        handleStateResponse(responseEvent)
                    }
                }
            }
        }
    }

    private fun handleStateResponse(event: Event<Response>) {
        event.getContentIfNotHandled()?.let {
            when (it.responseType) {
                is ResponseType.Dialog -> {
                    it.message?.let { message -> displaySuccessDialog(message) }
                    it.localizedMessage?.let { message -> displaySuccessDialog(message) }
                }
                is ResponseType.Toast -> {
                    it.message?.let { message -> displayToast(message) }
                }
                is ResponseType.None -> {
                    Log.d(TAG, "handleStateErrorEvent: ${it.message}")
                }
            }

        }
    }

    private fun handleStateErrorEvent(errorEvent: Event<StateError>) {
        errorEvent.getContentIfNotHandled()?.let {
            when (it.response.responseType) {
                is ResponseType.Dialog -> {
                    it.response.message?.let { message -> displayErrorDialog(message) }
                    it.response.localizedMessage?.let { message -> displayErrorDialog(message) }
                }
                is ResponseType.Toast -> {
                    it.response.message?.let { message -> displayToast(message) }
                }
                is ResponseType.None -> {
                    Log.e(TAG, "handleStateErrorEvent: ${it.response.message}")
                }


            }

        }
    }

    abstract fun showProgressBar(show: Boolean)

    abstract fun showErrorUI(
        show: Boolean,
        image: Int? = 0,
        title: String? = "",
        desc: String? = null,
        showButton: Boolean? = false
    )

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    override fun showSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(currentFocus, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onUIListOptionsReceived(uiListMultiSelection: UIListMultiSelection) {
        when (uiListMultiSelection.uiMessageType) {
            is UIMessageType.MultiChoiceDialog -> {
                multipleChoiceDialog(
                    uiListMultiSelection.title,
                    uiListMultiSelection.list,
                    uiListMultiSelection.indices,
                    uiListMultiSelection.uiMessageType.callback
                )
            }
            else -> {}
        }
    }

    override fun onUIListOptionResReceived(uiListSingleResSelection: UIListSingleResSelection) {
        when (uiListSingleResSelection.uiMessageType) {
            is UIMessageType.SingleChoiceDialog -> {
                singleChoiceResDialog(
                    uiListSingleResSelection.title,
                    uiListSingleResSelection.list,
                    uiListSingleResSelection.pos,
                    uiListSingleResSelection.uiMessageType.callback
                )
            }
            else -> {}
        }
    }

    override fun onUIListOptionStringReceived(uiListSingleStringSelection: UIListSingleStringSelection) {
        when (uiListSingleStringSelection.uiMessageType) {
            is UIMessageType.SingleChoiceDialog -> {
                singleChoiceStringDialog(
                    uiListSingleStringSelection.title,
                    uiListSingleStringSelection.list,
                    uiListSingleStringSelection.pos,
                    uiListSingleStringSelection.uiMessageType.callback
                )
            }
            else -> {}
        }
    }

    override fun onUIListOptionObjectReceived(uiListSingleObjectSelection: UIListSingleObjectSelection) {
        singleChoiceObjectDialog(
            uiListSingleObjectSelection.title,
            uiListSingleObjectSelection.adapter,
            uiListSingleObjectSelection.callback
        )
    }

    override fun onUIDateTimeReceived(uiDateTimePicker: UIDateTimePicker) {
        when (uiDateTimePicker.uiMessageType) {
            is UIMessageType.DateTimePickerDialog -> {
                dateTimePickerDialog(
                    uiDateTimePicker.needTime,
                    uiDateTimePicker.currentDateTime,
                    uiDateTimePicker.uiMessageType.callback
                )
            }
            else -> {}
        }
    }

    override fun onUIMessageReceived(uiMessage: UIMessage) {
        when (uiMessage.uiMessageType) {
            is UIMessageType.AreYouSureDialog -> {
                areYouSureDialog(uiMessage.message, uiMessage.uiMessageType.callback)
            }

            is UIMessageType.Toast -> {
                displayToast(uiMessage.message)
            }
            is UIMessageType.Dialog -> {
                displayInfoDialog(uiMessage.message)
            }
            is UIMessageType.None -> {
                Log.d(TAG, "onUIMessageReceived: ${uiMessage.message}")
            }
            else -> {}
        }
    }


    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                if (selectedOption == 0)
                    chooseUserPhotoFromCamera()
                else chooseUserPhotoFromGallery()

            }
        }

    fun choosePhoto() {
        val picOption = arrayOf(getString(R.string.camera), getString(R.string.gallery))
        singleChoiceStringDialog(
            getString(R.string.upload_image),
            picOption.toList(),
            0,
            object : SingleChoiceCallback {
                override fun proceed(pos: Int) {
                    selectedOption = pos
                    if (selectedOption == 0) chooseUserPhotoFromCamera() else chooseUserPhotoFromGallery()
                }

            })
    }

    fun chooseUserPhotoFromGallery() {
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            } else {
                val pickPhoto =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                cropImageActivityLauncher.launch(pickPhoto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val cropImageActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK && it.data != null) {
                    val data: Intent = it.data!!
                    cropImage(data.data!!)
                }
            }
        }

    private val croppedImageActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK && it.data != null) {
                    val resultUri: Uri? = UCrop.getOutput(it.data!!);
                    file = resultUri?.path?.let { it1 -> File(it1) };
                    val bm = BitmapFactory.decodeFile(resultUri!!.path)
                    val baos = ByteArrayOutputStream()
                    bm.compress(Bitmap.CompressFormat.JPEG, 30, baos)
//                    requestManager.load(bm)
                    imageCroppedInterface?.onImagePicked(file, bm)
                }
            }
        }

    fun chooseUserPhotoFromCamera() {
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            } else {
                val imageDirectory: File? = externalCacheDir
                if (imageDirectory != null) startCameraIntent(imageDirectory.getAbsolutePath())
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun startCameraIntent(imageDirectory: String) {
        file = createImageFile(imageDirectory)
        cameraActivityLauncher.launch(getCameraIntent())
        Log.d(TAG, "startCameraIntent: ")
    }

    private val cameraActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            run {
                if (it.resultCode == RESULT_OK) {
                    cropImage(Uri.fromFile(file))
                    revokeUriPermission(
                        FileProvider.getUriForFile(
                            this,
                            BuildConfig.APPLICATION_ID + ".provider", file!!
                        ), Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    revokeUriPermission(
                        FileProvider.getUriForFile(
                            this,
                            BuildConfig.APPLICATION_ID + ".provider", file!!
                        ), Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
        }


    private fun getCameraIntent(): Intent {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Put the uri of the image file as intent extra
        val imageUri: Uri =
            FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                file!!
            )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        // Get a list of all the camera apps
        val resolvedIntentActivities: List<ResolveInfo> = getPackageManager()
            .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY)

        // Grant uri read/write permissions to the camera apps
        for (resolvedIntentInfo in resolvedIntentActivities) {
            val packageName = resolvedIntentInfo.activityInfo.packageName
            grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return cameraIntent
    }

    private fun createImageFile(directory: String): File? {
        var imageFile: File? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val storageDir = File(directory)
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    return null
                }
            }
            val imageFileName: String =
                getString(R.string.app_name) + System.currentTimeMillis() + "_"
            try {
                imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return imageFile
    }


    private fun cropImage(photoUri: Uri) {
        val destinationFileName: String =
            getString(R.string.app_name) + System.currentTimeMillis() + ".jpg"
        val destinationUri: Uri = Uri.fromFile(File(cacheDir, destinationFileName))
        val options = UCrop.Options()
        options.setCompressionQuality(50)
        options.setCircleDimmedLayer(true)
        options.setShowCropFrame(false)
        options.setShowCropGrid(false)
        options.setToolbarTitle(getString(R.string.crop_image_text))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.red_orange))
        options.setToolbarColor(ContextCompat.getColor(this, R.color.flamingo))
        croppedImageActivityLauncher.launch(
            UCrop.of(photoUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(1f, 1f)
                .getIntent(this)
        )
//            .start(this)
    }

    interface ImageCroppedInterface {
        fun onImagePicked(uri: File?, bm: Bitmap)
    }
}