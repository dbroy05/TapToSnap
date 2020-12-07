package com.taptosnap.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.taptosnap.R
import com.taptosnap.model.SnapItem
import com.taptosnap.viewmodel.SnapViewModel
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_list_content.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import kotlinx.coroutines.NonCancellable.cancel
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var timer: CountDownTimer
    val TWO_MINUTES_IN_MILLIS: Long = 2 * 60 * 1000

    private var itemList: List<SnapItem>? = null
    private val mViewModel: SnapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Observes the ViewModel to get the list of items to be snapped
        mViewModel.getSpanItems().observe(this){
            itemList = it
            item_list.layoutManager = GridLayoutManager(this, 2)
            item_list.adapter = SimpleItemRecyclerViewAdapter(this, itemList!!)
        }

        //CountDown timer for the game
        timer = object : CountDownTimer(TWO_MINUTES_IN_MILLIS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counter.text = String.format("%d:%d:%d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))
            }

            override fun onFinish() {

                //When time is up, show the message
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle(R.string.timout_title).setMessage(R.string.timout_header)
                        .setPositiveButton("Ok",
                                DialogInterface.OnClickListener { dialog, id ->
                                    restartGame(dialog)
                                    timer.start()
                                })
                        .setNegativeButton("Cancel",
                                DialogInterface.OnClickListener { dialog, id ->
                                    restartGame(dialog)
                                    timer.cancel()
                                })
                // Create the AlertDialog object and return it
                builder.create().show()


            }
        }.start()
    }

    private fun restartGame(dialog: DialogInterface) {
        dialog.dismiss()
        itemList?.forEach {
            it.itemTapped = null
            it.itemMatched = null
        }
        item_list.removeAllViews()
        item_list.adapter?.notifyDataSetChanged()
    }

    /**
     * Receives the result back from CameraActivity once it captures the image of the item
     */
    val startForPhotoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val id = it.data?.getStringExtra("id")
            //Get itemLabel that matched the tapped item id
            var itemLabel = itemList?.filter { it.id == id}?.map { it.name }?.get(0)

            //Verifies if the item image matches based on API call, updates the item in callback
            mViewModel.checkImage(itemLabel!!, id!!) { matched ->
                itemList?.forEach { item -> if (item.id == id) item.itemMatched = matched}
                item_list.adapter?.notifyDataSetChanged()
            }

            //Update the state of the item tapped to 'Verifying'
            itemList?.forEach { item -> if (item.id == id) {
                item.itemTapped = true
                item.itemMatched = null
            }}
            item_list.adapter?.notifyDataSetChanged()
        }
    }

    /**
     * Adapter to list the items in Grid view
     */
    inner class SimpleItemRecyclerViewAdapter(
            private val parentActivity: MainActivity,
            private val values: List<SnapItem>
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        //Listener to handle when tile's tapped
        private val onClickListener = View.OnClickListener { v ->
            val item = v.tag as SnapItem
            //Launches the CameraActivity passing the item id
            val intent = Intent(v.context, CameraActivity::class.java)
            intent.putExtra("id", item.id)
            startForPhotoResult.launch(intent)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        //Gets called the the holder is bound to an item at the position
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.itemName.text = item.name

            //If the item's tapped, update the state to 'Verifying'
            checkIfTapped(item, holder)

            //If item is matched or not, update the background state accordingly
            item.itemMatched?.let {
                holder.progressBar.visibility = View.GONE
                when (it) {
                    true -> {
                        holder.holderView.background = ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.tile_success)
                        holder.tapToTryTxt.visibility = View.GONE
                    }
                    false -> {
                        val uri = getImgUri(item)
                        Glide.with(parentActivity).load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .apply(bitmapTransform(BlurTransformation(25, 1)))
                                .skipMemoryCache(true)
                                .into(holder.itemImage)
                        holder.holderView.background = ContextCompat.getDrawable(
                                applicationContext,
                                R.drawable.tile_incorrect)
                        holder.tapToTryTxt.visibility = View.VISIBLE
                    }
                }
            }

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        private fun checkIfTapped(item: SnapItem, holder: ViewHolder) {
            item.itemTapped?.let {
                if (it) {
                    val uri = getImgUri(item)
                    Glide.with(parentActivity).load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(holder.itemImage)

                    holder.tapToTryTxt.visibility = View.GONE
                    holder.progressBar.visibility = View.VISIBLE
                    holder.holderView.background = ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.tile_verify)
                }

            }
        }

        private fun getImgUri(item: SnapItem): Uri? {
            val imgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/${item.id}.jpg"
            val uri = Uri.fromFile(File(imgPath))
            return uri
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemName: TextView = view.item_name
            val tapToTryTxt: TextView = view.tap_to_try_txt
            val itemImage: ImageView = view.item_imageView
            val progressBar: ProgressBar = view.progressbar
            val holderView = view
        }
    }
}