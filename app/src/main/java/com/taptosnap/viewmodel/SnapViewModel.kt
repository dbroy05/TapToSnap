package com.taptosnap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.taptosnap.api.TapToSnapRepository
import com.taptosnap.model.SnapItem

class SnapViewModel : ViewModel() {
    private lateinit var snapItems : LiveData<List<SnapItem>>
    val service = TapToSnapRepository()
    init {
        snapItems = service.getAllSnapItems()
    }

    fun getSpanItems(): LiveData<List<SnapItem>> {
        return snapItems
    }

    fun checkImage(imgLabel: String, id : String, callback : (Boolean) -> Unit) {
        service.checkImage(imgLabel,id, callback)
    }
}