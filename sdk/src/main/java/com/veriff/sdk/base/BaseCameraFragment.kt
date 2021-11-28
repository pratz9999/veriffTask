package com.veriff.sdk.base

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.veriff.sdk.R
import com.veriff.sdk.utility.Utils.generateCacheFile
import com.veriff.sdk.utility.Utils.openAppSettings
import com.veriff.sdk.utility.Utils.showConfirmationDialog
import com.markodevcic.peko.PermissionResult
import com.markodevcic.peko.requestPermissionsAsync
import kotlinx.coroutines.launch

abstract class BaseCameraFragment(private val cameraSelector: CameraSelector) :
    Fragment() {

    private val imageCapture = ImageCapture.Builder().build()
    private val preview = Preview.Builder().build()

    internal abstract fun onSDKError(error: SdkError)
    internal abstract fun onSDKURIResult(uriInString: String)
    internal abstract fun onSDKResult(data: String)

    protected fun initialize(previewView: PreviewView) {
        if (CameraHelper.supportsCamera(requireContext(), cameraSelector)) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    initWithPermissions(previewView)
                }
            }
        } else {
            showConfirmationDialog(
                getString(R.string.dialog_error_no_camera_title),
                getString(R.string.dialog_error_no_camera_message),
                getString(R.string.dialog_error_no_camera_positive),
                { setError(SdkError.CAMERA_UNSUPPORTED) },
                cancelable = false
            )
        }
    }


    private suspend fun initWithPermissions(
        previewView: PreviewView
    ) {
        when (requestPermissionsAsync(REQUIRED_PERMISSION)) {
            is PermissionResult.Granted -> initCamera(previewView)
            is PermissionResult.Denied.DeniedPermanently -> {
                showConfirmationDialog(
                    getString(R.string.dialog_allow_camera_access_title),
                    getString(R.string.dialog_allow_camera_access_message),
                    getString(R.string.dialog_allow_camera_access_positive),
                    { openAppSettings() },
                    getString(R.string.dialog_allow_camera_access_negative),
                    { setError(SdkError.CAMERA_ACCESS_FORBIDDEN) }
                )
            }
            else -> initWithPermissions(previewView)
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    protected fun initCamera(
        previewView: PreviewView
    ) {
        lifecycleScope.launchWhenStarted {
            previewView.post {
                val rotation = previewView.display.rotation
                preview.targetRotation = rotation
                preview.setSurfaceProvider(previewView.surfaceProvider)
                imageCapture.targetRotation = rotation

                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture)
                    .setViewPort(previewView.viewPort!!)
                    .build()
                lifecycleScope.launch {
                    setLoading(true)
                    try {
                        CameraHelper.startCamera(
                            requireContext(),
                            viewLifecycleOwner,
                            cameraSelector,
                            useCaseGroup
                        )
                    } catch (e: Exception) {
                        setError(SdkError.CAMERA_FAILURE)
                    } finally {
                        setLoading(false)
                    }
                }
            }
        }
    }


    protected suspend fun takePhoto(): Uri? {
        var fileUri: Uri? = null
        try {
            fileUri = CameraHelper.takePhoto(
                imageCapture,
                generateCacheFile()
            )
        } catch (e: Exception) {
            setError(SdkError.UNKNOWN)
        }
        return fileUri
    }

    protected fun setError(error: SdkError) {
        Handler(Looper.getMainLooper()).post {
            onSDKError(error)
        }
    }

    protected fun setResult(result: Uri) {
        Handler(Looper.getMainLooper()).post {
            onSDKURIResult(result.toString())
        }
    }

    protected fun setResult(result: String) {
        Handler(Looper.getMainLooper()).post {
            onSDKResult(result)
        }
    }

    protected fun setLoading(loading: Boolean) {
        lifecycleScope.launchWhenStarted {
            onLoading(loading)
        }
    }

    abstract fun onLoading(loading: Boolean)

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }

}