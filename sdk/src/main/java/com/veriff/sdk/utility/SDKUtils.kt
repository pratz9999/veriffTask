package com.veriff.sdk.utility

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.veriff.sdk.R
import java.io.File

typealias SimpleClickListener = () -> Unit

internal object Utils {

    fun Fragment.showConfirmationDialog(
        title: String?,
        message: String?,
        positiveButtonText: String? = getString(R.string.ok), positive: SimpleClickListener = {},
        negativeButtonText: String? = null, negative: SimpleClickListener = {},
        cancelable: Boolean = true,
    ): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
            .apply {
                title?.also { setTitle(it) }
                message?.also { setMessage(it) }
                positiveButtonText?.also {
                    setPositiveButton(it) { dialog, _ ->
                        dialog.dismiss()
                        positive()
                    }
                }
                negativeButtonText?.also {
                    setNegativeButton(negativeButtonText) { dialog, _ ->
                        dialog.dismiss()
                        negative()
                    }
                }
                setCancelable(cancelable)
                setOnCancelListener {
                    it.dismiss()
                }
            }.show()
    }

    fun Fragment.openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package", requireContext().packageName, null)
        }.also {
            startActivity(it)
        }
    }

    fun Fragment.generateCacheFile(ext: String = "jpg"): File {
        val path = File(requireContext().cacheDir, "sdk")
        path.mkdirs()
        return File(path, "${System.currentTimeMillis()}.$ext")
    }

    fun Fragment.showError(
        message: String,
        buttonText: String = getString(R.string.ok),
        onDismiss: () -> Unit = {}
    ) {
        showConfirmationDialog(
            getString(R.string.error),
            message,
            buttonText,
            { onDismiss() },
            cancelable = false
        )
    }
}