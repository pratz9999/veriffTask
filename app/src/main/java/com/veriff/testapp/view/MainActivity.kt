package com.veriff.testapp.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.veriff.sdk.utility.BundleConstants
import com.veriff.sdk.utility.Constants
import com.veriff.sdk.view.SDKActivity
import com.veriff.testapp.R
import com.veriff.testapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                when {
                    data?.extras?.containsKey("result_uri") == true -> {
                        data.extras?.getString("result")?.let { updateData(it) }
                        data.extras?.getString("result_uri")?.let { updateImage(it) }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initListeners()
    }

    private fun initListeners() {
        binding.btnReadText.setOnClickListener(this)
        binding.btnDetectFace.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_read_text -> {
                launchSDKActivity(Constants.DOCUMENT)
            }
            R.id.btn_detect_face -> {
                launchSDKActivity(Constants.FACE)
            }
        }
    }


    private fun updateData(msg: String) {
        binding.txtResult.text = msg
    }

    private fun updateImage(uri: String) {
        val uriData = Uri.parse(uri)

        Glide.with(this)
            .load(uriData)
            .apply(RequestOptions().centerCrop())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imageFace)
    }

    private fun launchSDKActivity(action: String) {

        resultLauncher.launch(Intent(this, SDKActivity::class.java).apply {
            putExtra(BundleConstants.ACTION, action)
        })
    }

}
