package com.veriff.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * The ViewModel for Documents and Face.
 */
@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    val uri = MutableLiveData("")
    val result = MutableLiveData("")
    val error = MutableLiveData("")
}