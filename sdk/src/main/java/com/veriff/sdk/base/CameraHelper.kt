package com.veriff.sdk.base

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object CameraHelper {

    fun supportsCamera(context: Context, cameraSelector: CameraSelector): Boolean {
        val provider = ProcessCameraProvider.getInstance(context).get()
        return provider.hasCamera(cameraSelector)
    }

    suspend fun takePhoto(imageCapture: ImageCapture, outputFile: File): Uri? =
        withContext(Dispatchers.Main) {
            suspendCoroutine { continuation ->
                val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile)
                    .build()
                imageCapture.takePicture(outputOptions, Executors.newSingleThreadExecutor(),
                    object : ImageCapture.OnImageSavedCallback {
                        @SuppressLint("UnsafeOptInUsageError")
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            continuation.resume(outputFile.toUri())
                        }

                        override fun onError(exception: ImageCaptureException) {
                            continuation.resumeWithException(exception)
                        }
                    })
            }
        }

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun recognizeText(context: Context, imageUri: Uri): Text? {
        val inputImage = withContext(Dispatchers.IO) {
            InputImage.fromFilePath(context, imageUri)
        }
        return withContext(Dispatchers.Default) {
            suspendCoroutine { continuation ->
                val textRecognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { continuation.resumeWithException(it) }
                    .addOnCanceledListener { continuation.resume(null) }
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun recognizeFaces(
        context: Context,
        imageUri: Uri,
        opts: FaceDetectorOptions.Builder.() -> Unit = {}
    ): List<Face> {
        val inputImage = withContext(Dispatchers.IO) {
            InputImage.fromFilePath(context, imageUri)
        }

        val optBuilder = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)

        opts(optBuilder)

        return withContext(Dispatchers.Default) {
            suspendCoroutine { continuation ->
                val faceDetection = FaceDetection.getClient(optBuilder.build())
                faceDetection.process(inputImage)
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { continuation.resumeWithException(it) }
                    .addOnCanceledListener { continuation.resume(listOf()) }
            }
        }
    }


    @SuppressLint("UnsafeOptInUsageError")
    suspend fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        useCaseGroup: UseCaseGroup
    ): Camera {
        return withContext(Dispatchers.Main) {
            return@withContext suspendCoroutine { continuation ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    try {
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
                            .also {
                                continuation.resume(it)
                            }
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        }
    }

    suspend fun stopCamera(context: Context): Boolean {
        return withContext(Dispatchers.Main) {
            return@withContext suspendCoroutine { continuation ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    continuation.resume(true)
                }, ContextCompat.getMainExecutor(context))
            }
        }
    }
}