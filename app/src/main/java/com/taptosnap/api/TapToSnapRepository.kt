package com.taptosnap.api

import android.os.Environment
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.taptosnap.model.SnapItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import kotlin.random.Random


/**
 * Repository to abstract the API call to endpoint client.
 */
class TapToSnapRepository {
    val apiClient = SnapApiClient()

    /**
     * Gets all snap items available
     */
    fun getAllSnapItems() :  LiveData<List<SnapItem>> {
        val itemList = MutableLiveData<List<SnapItem>>()

        apiClient.getAllLaunches().enqueue(object : Callback<List<SnapItem>> {
            override fun onResponse(call: Call<List<SnapItem>>, response: Response<List<SnapItem>>) {
                itemList.value = response.body()
            }

            override fun onFailure(call: Call<List<SnapItem>>, t: Throwable) {
                println(t)
            }
        })

        return itemList
    }

    /**
     * Checks if there's a match for the image
     * params: imgLabel, itemId and callback
     */
    fun checkImage(imgLabel: String, id : String, callback : (Boolean) -> Unit) {
        try {

            val imgEncoded = encodeImage(id)

            apiClient.checkImage(imgLabel, imgEncoded!!, callback).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    //TODO: Actually this should be called, but the POST method is not working,
                    // so per requirement just returning the Random boolean
                    //callback(response.body()!!)

                    //TODO: For now random boolean is returned
                    callback(Random.nextBoolean())
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    callback(Random.nextBoolean())
                }

            })

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    /**
     * This method Base64 encodes the image file
     */
    private fun encodeImage(id: String): String? {
        val imgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/${id}.jpg"
        val inputStream: InputStream = FileInputStream(imgPath)
        val bytes: ByteArray
        val buffer = ByteArray(8192)
        var bytesRead: Int
        val byteArrayOutputStream = ByteArrayOutputStream()
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        bytes = byteArrayOutputStream.toByteArray()
        val imgEncoded = Base64.encodeToString(bytes, Base64.DEFAULT)
        return imgEncoded
    }

}