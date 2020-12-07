package com.taptosnap.model

import com.google.gson.annotations.SerializedName

class SnapItem {

    @SerializedName("id")
    var id: String? = null

    @SerializedName("name")
    var name: String? = null

    var itemMatched : Boolean? = null

    var itemTapped : Boolean? = null

}