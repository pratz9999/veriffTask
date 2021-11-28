package com.veriff.sdk.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.veriff.sdk.R
import com.veriff.sdk.databinding.ActivitySdkBinding
import com.veriff.sdk.utility.BundleConstants
import com.veriff.sdk.utility.Constants
import com.veriff.sdk.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SDKActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySdkBinding
    private lateinit var navController: NavController

    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySdkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        intent?.let {
            launchFragment(it.getStringExtra(BundleConstants.ACTION) ?: Constants.DOCUMENT)
        }
        initObservers()
    }

    private fun launchFragment(action: String) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController
        navController.navInflater.inflate(R.navigation.nav_graph).apply {
            startDestination = navigationByAction(action)
            navController.setGraph(this, intent.extras)
        }
    }

    private fun initObservers() {
        val intent = Intent()
        viewModel.uri.observe(this) {
            if (it != Constants.EMPTY_STRING) {
                intent.putExtra(BundleConstants.ERROR, viewModel.error.value)
                intent.putExtra(BundleConstants.RESULT, viewModel.result.value)
                intent.putExtra(BundleConstants.RESULT_URI, it)
                returnResult(intent)
            }
        }
    }

    private fun returnResult(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun navigationByAction(action: String): Int {
        return when (action) {
            Constants.FACE -> R.id.faceFragment
            else -> R.id.documentFragment
        }
    }
}