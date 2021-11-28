package com.veriff.sdk

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.UseCaseGroup
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.veriff.sdk.base.CameraHelper
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class CameraHelperTest {

    private var lifeCycleOwner: LifecycleOwner? = null
    private var registry: LifecycleRegistry? = null

    @Test
    fun testRecognizeText() {
        val text = runBlocking {
            CameraHelper.recognizeText(context, assetToUri("id_card_example.jpg"))
        }
        assertFalse(text?.text.isNullOrBlank())
        assertTrue(text?.text.orEmpty().contains("MARI-LIIS"))
        assertTrue(text?.text.orEmpty().contains("MÄNNIK"))
    }

    @Test
    fun testSingleFaceRecognition() {
        val faces = runBlocking {
            CameraHelper.recognizeFaces(context, assetToUri("single_face.jpg"))
        }
        assertTrue(faces.isNotEmpty())
        assertTrue(faces.size == 1)
    }

    @Test
    fun testMultiFaceRecognition() {
        val faces = runBlocking {
            CameraHelper.recognizeFaces(context, assetToUri("two_faces.jpg"))
        }
        assertTrue(faces.isNotEmpty())
        assertTrue(faces.size == 2)
    }

    @Test
    fun testStartCamera() {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(ImageCapture.Builder().build())
            .build()
        runBlocking {
            CameraHelper.startCamera(context, lifeCycleOwner!!, cameraSelector, useCaseGroup)
        }
    }

    @Test
    fun testTakePhoto() {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val imageCapture = ImageCapture.Builder()
            .build()
        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(imageCapture)
            .build()
        runBlocking {
            CameraHelper.startCamera(context, lifeCycleOwner!!, cameraSelector, useCaseGroup)
        }
        val outputFile = File(context.cacheDir, "photo.jpg")
        val uri = runBlocking {
            CameraHelper.takePhoto(imageCapture, outputFile)
        }
        assertTrue(uri != null)
    }

    @Before
    @UiThreadTest
    fun setup() {
        lifeCycleOwner = LifecycleOwner {
            registry!!
        }
        registry = LifecycleRegistry(lifeCycleOwner!!).also {
            it.currentState = Lifecycle.State.STARTED
        }
    }

    @After
    @UiThreadTest
    fun tearDown() {
        registry?.currentState = Lifecycle.State.DESTROYED
    }

    companion object {

        private lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            context = ApplicationProvider.getApplicationContext()
            val supportsCamera =
                CameraHelper.supportsCamera(context, CameraSelector.DEFAULT_BACK_CAMERA)
            assertTrue("Testing device does not support BACK camera", supportsCamera)
        }

        private fun assetToUri(filename: String): Uri {
            val outputFile = File(context.cacheDir, filename)
            outputFile.outputStream().use { output ->
                context.assets.open(filename).use { input ->
                    input.copyTo(output)
                }
            }
            return outputFile.toUri()
        }
    }

}