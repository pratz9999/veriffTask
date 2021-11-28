package com.veriff.sdk.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.veriff.sdk.R
import com.veriff.sdk.base.BaseCameraFragment
import com.veriff.sdk.base.CameraHelper
import com.veriff.sdk.base.SdkError
import com.veriff.sdk.databinding.FaceFragmentBinding
import com.veriff.sdk.utility.Utils.showError
import com.veriff.sdk.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * [FaceFragment]
 */
@AndroidEntryPoint
class FaceFragment : BaseCameraFragment(CameraSelector.DEFAULT_FRONT_CAMERA), View.OnClickListener {

    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FaceFragmentBinding
    private lateinit var navController: NavController

    override fun onSDKError(error: SdkError) {
        viewModel.error.postValue(error.name)
    }

    override fun onSDKURIResult(uriInString: String) {
        viewModel.uri.postValue(uriInString)
    }

    override fun onSDKResult(data: String) {
        viewModel.result.value = data
    }

    override fun onLoading(loading: Boolean) {
        binding.btnCapture.isEnabled = !loading
        binding.progressView.isVisible = loading
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FaceFragmentBinding.inflate(inflater, container, false)
        context ?: return binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        initListeners()
        initialize(binding.previewView)

    }

    private fun initListeners() {
        binding.btnCapture.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_capture -> {
                onTakePhotoClicked()
            }
        }
    }

    private fun onTakePhotoClicked() {
        lifecycleScope.launch {
            setLoading(true)

            takePhoto()?.also { uri ->
                CameraHelper.stopCamera(requireContext())
                val faces = CameraHelper.recognizeFaces(requireContext(), uri)
                when {
                    faces.isEmpty() -> {
                        showError(getString(R.string.face_detect_no_face_error)) {
                            initCamera(binding.previewView)
                        }
                    }
                    faces.size > 1 -> {
                        showError(getString(R.string.face_detect_multiple_faces_error)) {
                            initCamera(binding.previewView)
                        }
                    }
                    else -> {
                        setResult(uri)
                        setResult(getString(R.string.success))
                    }
                }
            } ?: setError(SdkError.UNKNOWN)

            setLoading(false)
        }
    }

}