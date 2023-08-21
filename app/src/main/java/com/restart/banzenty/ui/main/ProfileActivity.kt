package com.restart.banzenty.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.restart.banzenty.R
import com.restart.banzenty.base.BaseActivity
import com.restart.banzenty.databinding.ActivityProfileBinding
import com.restart.banzenty.ui.auth.VerificationActivity
import com.restart.banzenty.utils.Validation
import com.restart.banzenty.utils.toArabicNumbers
import com.restart.banzenty.viewModels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class ProfileActivity : BaseActivity(), BaseActivity.ImageCroppedInterface {
    private val TAG = "ProfileActivity"
    private var isUpdateProfile = false
    private var userChangedPhoto = false
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    var profileImageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initViews()
        setProfileDetails()
        getProfileDetails()
    }

    private fun initToolbar() {
        binding.toolbar.root.setNavigationIcon(R.drawable.back_ic)
        binding.toolbar.root.setNavigationOnClickListener { finish() }
        binding.toolbar.textViewToolbarTitle.text = getString(R.string.profile)
    }

    private fun updateProfile() {
        isUpdateProfile = true
        Validation.validatePhoneEditText(binding.etPhone)
        viewModel.updateProfile(
            binding.etName.text.toString(),
            binding.ccp.fullNumberWithPlus.toString().trim(),
            binding.etEmail.text.toString(),
            profileImageFile,
            binding.editTextCarNumbers.text.toString().trim(),
            "${binding.etAlp1.text.toString().trim()}${
                binding.etAlp2.text.toString().trim()
            }${binding.etAlp3.text.toString().trim()}",
        ).observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { errorState ->
                    onErrorStateChange(errorState)
                    setProfileDetails()
                }

                showProgressBar(it.loading.isLoading)

                it.data?.data?.getContentIfNotHandled()?.let { userDataModel ->
                    if (userDataModel.isVerified) {
                        showUpdatedProfileSuccessDialog()
                        binding.tvUserName.text = binding.etName.text.toString()
                        binding.buttonSaveChanges.isEnabled = false
                        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                            this@ProfileActivity,
                            R.drawable.custom_button_french_gray_r14
                        )
                    } else {
                        val intent = Intent(this, VerificationActivity::class.java)
                        intent.putExtra("change_phone", true)
                        intent.putExtra(
                            "phone_number",
                            binding.ccp.fullNumberWithPlus.toString().trim()
                        )
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun getProfileDetails() {
        viewModel.getProfileDetails().observe(this) {
            if (it != null) {
                it.error?.getContentIfNotHandled()?.let { stateError ->
//                    onErrorStateChange(stateError)
                }
                it.data?.data?.getContentIfNotHandled()?.let { userDataModel ->
                    viewModel.isProfileDataLoaded = true
                    sessionManager.saveUser(userDataModel, null)
                    setProfileDetails()
                }
            }
        }
    }

    private fun setProfileDetails() {
        binding.tvUserName.text = sessionManager.getUserName()
        binding.etName.setText(sessionManager.getUserName())
        binding.etPhone.setText(sessionManager.getUserPhone().substring(3))
        binding.etEmail.setText(sessionManager.getUserEmail())
        binding.editTextCarNumbers.setText(sessionManager.getCarPlateDigits().toArabicNumbers())
        val carPlateChars = sessionManager.getCarPlateCharacters()
        for (i in carPlateChars.indices) {
            when (i) {
                0 -> binding.etAlp1.setText(carPlateChars[i].toString())
                1 -> binding.etAlp2.setText(carPlateChars[i].toString())
                2 -> binding.etAlp3.setText(carPlateChars[i].toString())
            }
        }
        Log.d(TAG, "userChangedPhoto: $userChangedPhoto")
        if (!userChangedPhoto) {
            Log.d(TAG, "setProfileDetails: changed and cancelled")
            requestManager
                .load(sessionManager.getUserImage())
                .transform(CenterCrop(), CircleCrop())
                .into(binding.ivProfilePic)
            binding.buttonSaveChanges.isEnabled = false
            binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                this@ProfileActivity,
                R.drawable.custom_button_french_gray_r14
            )
        } else {
            binding.buttonSaveChanges.isEnabled = true
            binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                this@ProfileActivity,
                R.drawable.custom_button_red_orange_r14
            )
        }
    }

    private fun initViews() {
        binding.ccp.registerPhoneNumberTextView(binding.etPhone)

        binding.ivProfilePic.setOnClickListener {
            //show dialog
            imageCroppedInterface = this
            choosePhoto()
        }

        binding.ivChangePic.setOnClickListener {
            //show dialog
            imageCroppedInterface = this
            choosePhoto()
        }

        binding.ivEditName.setOnClickListener {
            Log.i("TAG", "onClick: editing name")
            binding.etName.isEnabled = true
            binding.etName.requestFocus()
            binding.etName.setSelection(binding.etName.length())
            showSoftKeyboard()
            binding.etName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != sessionManager.getUserName() && Validation.isValidStringInput(
                            s.toString()
                        )
                    ) {
                        binding.buttonSaveChanges.isEnabled = true
                        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                            this@ProfileActivity,
                            R.drawable.custom_button_red_orange_r14
                        )
                        binding.etName.error = null
                    } else {
                        binding.buttonSaveChanges.isEnabled = false
                        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                            this@ProfileActivity,
                            R.drawable.custom_button_french_gray_r14
                        )
                        binding.etName.error = getString(R.string.invalid_name)
                    }
                }
            })
        }

        binding.etName.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etName.isEnabled = false
                hideSoftKeyboard()
                true
            }
            false
        }

        binding.ivEditPhone.setOnClickListener {
            Log.i("TAG", "onClick: editing phone")
            binding.etPhone.isEnabled = true
            binding.etPhone.requestFocus()
            binding.etPhone.setSelection(binding.etPhone.length())
            showSoftKeyboard()
            binding.etPhone.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if (binding.ccp.isValid && s.toString() != sessionManager.getUserPhone()
                            .substring(3)
                    ) {
                        binding.buttonSaveChanges.isEnabled = true
                        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                            this@ProfileActivity,
                            R.drawable.custom_button_red_orange_r14
                        )
                        binding.etPhone.error = null
                    } else {
                        binding.buttonSaveChanges.isEnabled = false
                        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                            this@ProfileActivity,
                            R.drawable.custom_button_french_gray_r14
                        )
                        binding.etPhone.error = getString(R.string.invalid_phone)
                    }
                }
            })
        }

        binding.etPhone.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etPhone.isEnabled = false
                hideSoftKeyboard()
                true
            }
            false
        }

        binding.ivEditEmail.setOnClickListener {
            Log.i("TAG", "onClick: editing email")
            binding.etEmail.isEnabled = true
            binding.etEmail.requestFocus()
            binding.etEmail.setSelection(binding.etEmail.length())
            showSoftKeyboard()
            binding.etEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != sessionManager.getUserEmail() && Validation.isValidEmail(s.toString())) {
                        binding.buttonSaveChanges.isEnabled = true
                        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                            this@ProfileActivity,
                            R.drawable.custom_button_red_orange_r14
                        )
                    } else {
                        binding.buttonSaveChanges.isEnabled = false
                        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                            this@ProfileActivity,
                            R.drawable.custom_button_french_gray_r14
                        )
                    }
                }
            })
        }

        binding.etEmail.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etEmail.isEnabled = false
                hideSoftKeyboard()
                true
            }
            false
        }
        binding.ivEditCarPlate.setOnClickListener {
            Log.i("TAG", "onClick: editing car plate")
            binding.editTextCarNumbers.isEnabled = true
            binding.etAlp1.isEnabled = true
            binding.etAlp2.isEnabled = true
            binding.etAlp3.isEnabled = true
            binding.etAlp1.requestFocus()
            binding.etAlp1.setSelection(binding.etAlp1.length())
            showSoftKeyboard()

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val carPlateDigits = binding.editTextCarNumbers.text.toString()
                    val carPlateChars = "${binding.etAlp1.text.toString().trim()}${
                        binding.etAlp2.text.toString().trim()
                    }${binding.etAlp3.text.toString().trim()}"
                    if ((carPlateChars != sessionManager.getCarPlateCharacters() || carPlateDigits != sessionManager.getCarPlateDigits().toArabicNumbers()) && (carPlateDigits.isNotEmpty() && carPlateChars.isNotEmpty())) {
                        binding.buttonSaveChanges.isEnabled = true
                        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                            this@ProfileActivity,
                            R.drawable.custom_button_red_orange_r14
                        )
                        binding.editTextCarNumbers.error = null
                    }
                    else
                    {
                        binding.buttonSaveChanges.isEnabled = false
                        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
                            this@ProfileActivity,
                            R.drawable.custom_button_french_gray_r14
                        )
                        binding.editTextCarNumbers.error = getString(R.string.invalid_car_plates)
                    }
                }
            }
            binding.etAlp1.addTextChangedListener(textWatcher)
            binding.etAlp2.addTextChangedListener(textWatcher)
            binding.etAlp3.addTextChangedListener(textWatcher)
            binding.editTextCarNumbers.addTextChangedListener(textWatcher)
        }

        binding.editTextCarNumbers.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideSoftKeyboard()
                true
            }
            false
        }

        binding.buttonSaveChanges.setOnClickListener {
            updateProfile()
        }
//   todo when update profile returns code indicate that the phone has been changed
/*        val verificationIntent = Intent(requireActivity(), VerificationActivity::class.java)
        verificationIntent.putExtra("change_phone", true)
        openVerificationActivity.launch(verificationIntent)*/

      binding.editTextCarNumbers.doOnTextChanged { text, _, _, _ ->
            if (text!!.isNotEmpty()) {
                val lastChar: Char = text.get(text.length - 1)
                if (lastChar == '0' ||
                    lastChar == '1' ||
                    lastChar == '2' ||
                    lastChar == '3' ||
                    lastChar == '4' ||
                    lastChar == '5' ||
                    lastChar == '6' ||
                    lastChar == '7' ||
                    lastChar == '8' ||
                    lastChar == '9'
                ) {
                    val changeChar = when (lastChar) {
                        '1' -> {
                            '۱'
                        }
                        '2' -> {
                            '۲'
                        }
                        '3' -> {
                            '۳'
                        }
                        '4' -> {
                            '٤'
                        }
                        '5' -> {
                            '٥'
                        }
                        '6' -> {
                            '٦'
                        }
                        '7' -> {
                            '٧'
                        }
                        '8' -> {
                            '۸'
                        }
                        '9' -> {
                            '۹'
                        }
                        else -> {
                            '۰'
                        }
                    }
                    val builder = java.lang.StringBuilder()
                    text.forEachIndexed { i, c ->
                        if (i != text.length - 1) {
                            builder.append(c)
                        }
                    }
                    builder.append(changeChar)
                    val result = builder.toString()
                    binding.editTextCarNumbers.setText(result)
                    binding.editTextCarNumbers.setSelection(result.length)
                }
            }
        }

    }

    private fun showUpdatedProfileSuccessDialog() {
        //show Profile updated dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        val viewGroup = findViewById<ViewGroup>(android.R.id.content)

        val dialogView: View = LayoutInflater.from(this)
            .inflate(R.layout.custom_dialog_view, viewGroup, false)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.textViewDialogTitle)
        dialogTitle.text = getString(R.string.profile_updated)

        val dialogContent = dialogView.findViewById<TextView>(R.id.textViewDialogContent)
        dialogContent.text = getString(R.string.profile_update_msg)

        builder.setView(dialogView)

        val alertDialog: AlertDialog = builder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()

        val doneButton = dialogView.findViewById<Button>(R.id.buttonDone)

        doneButton.setOnClickListener {
            alertDialog.dismiss()
            binding.etName.isEnabled = false
            binding.etPhone.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.etAlp1.isEnabled = false
            binding.etAlp2.isEnabled = false
            binding.etAlp3.isEnabled = false
            binding.editTextCarNumbers.isEnabled = false
            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
            finish()
        }
    }

    override fun showProgressBar(show: Boolean) {
        if (show) {
            showErrorUI(false)
            if (isUpdateProfile) {
                binding.buttonSaveChanges.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.shimmer.visibility = View.VISIBLE
                binding.ivProfilePic.visibility = View.GONE
                binding.ivChangePic.visibility = View.GONE
                binding.tvUserName.visibility = View.GONE
                binding.relativeLayoutNameView.visibility = View.GONE
                binding.relativeLayoutEditingPhoneNum.visibility = View.GONE
                binding.relativeLayoutEditingEmail.visibility = View.GONE
            }
        } else {
            if (isUpdateProfile) {
                binding.buttonSaveChanges.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.shimmer.visibility = View.GONE
                binding.ivProfilePic.visibility = View.VISIBLE
                binding.ivChangePic.visibility = View.VISIBLE
                binding.tvUserName.visibility = View.VISIBLE
                binding.relativeLayoutNameView.visibility = View.VISIBLE
                binding.relativeLayoutEditingPhoneNum.visibility = View.VISIBLE
                binding.relativeLayoutEditingEmail.visibility = View.VISIBLE
            }
        }
    }

    override fun showErrorUI(
        show: Boolean, image: Int?, title: String?, desc: String?,
        showButton: Boolean?
    ) {
        if (show) {
            binding.errorLayout.tvDesc.visibility = View.GONE
            binding.errorLayout.root.visibility = View.VISIBLE
            binding.errorLayout.tvTitle.text = title
            binding.errorLayout.ivPic.setBackgroundResource(image!!)
            desc?.let {
                binding.errorLayout.tvDesc.visibility = View.VISIBLE
                binding.errorLayout.tvDesc.text = it
            }
            binding.errorLayout.tvTitle.text = title
            if (showButton == true) {
                binding.errorLayout.btnRetry.visibility = View.VISIBLE
                binding.errorLayout.btnRetry.setOnClickListener { getProfileDetails() }
            } else binding.errorLayout.btnRetry.visibility = View.GONE
        } else {
            binding.errorLayout.root.visibility = View.GONE
        }
    }

    override fun onImagePicked(uri: File?, bm: Bitmap) {
        Log.d(TAG, "onImagePicked:")
        profileImageFile = uri
        requestManager
            .load(bm)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transform(CenterCrop(), CircleCrop())
            .into(binding.ivProfilePic)
        userChangedPhoto = true
        binding.buttonSaveChanges.isEnabled = true
        binding.buttonSaveChanges.background = ContextCompat.getDrawable(
            this@ProfileActivity,
            R.drawable.custom_button_red_orange_r14
        )
    }

}