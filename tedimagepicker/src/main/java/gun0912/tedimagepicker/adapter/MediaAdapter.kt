package gun0912.tedimagepicker.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bumptech.glide.Glide
import gun0912.tedimagepicker.R
import gun0912.tedimagepicker.base.BaseSimpleHeaderAdapter
import gun0912.tedimagepicker.base.BaseViewHolder
import gun0912.tedimagepicker.builder.TedImagePickerBaseBuilder
import gun0912.tedimagepicker.builder.type.MediaType
import gun0912.tedimagepicker.databinding.ItemGalleryCameraBinding
import gun0912.tedimagepicker.databinding.ItemGalleryMediaBinding
import gun0912.tedimagepicker.model.Media
import gun0912.tedimagepicker.util.ToastUtil
import gun0912.tedimagepicker.zoom.TedImageZoomActivity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class MediaAdapter(
    private val activity: Activity,
    private val builder: TedImagePickerBaseBuilder<*>
) : BaseSimpleHeaderAdapter<Media>(if (builder.showCameraTile) 1 else 0) {

    internal val selectedUriList: MutableList<Uri> = mutableListOf()
    var onMediaAddListener: (() -> Unit)? = null

    fun setSquareSize(size: Int) {
        squareSize = size
    }
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    override fun getHeaderViewHolder(parent: ViewGroup) = CameraViewHolder(parent)
    override fun getItemViewHolder(parent: ViewGroup) = ImageViewHolder(parent)

    fun toggleMediaSelect(uri: Uri) {
        if (selectedUriList.contains(uri)) {
            removeMedia(uri)
        } else {
            addMedia(uri)
        }
    }

    private fun addMedia(uri: Uri) {
        if (selectedUriList.size == builder.maxCount) {
            val message =
                builder.maxCountMessage ?: activity.getString(builder.maxCountMessageResId)
            ToastUtil.showToast(message)
        } else {
            selectedUriList.add(uri)
            onMediaAddListener?.invoke()
            refreshSelectedView()
        }
    }

    private fun getViewPosition(it: Uri): Int =
        items.indexOfFirst { media -> media.uri == it } + headerCount

    private fun removeMedia(uri: Uri) {
        val position = getViewPosition(uri)
        selectedUriList.remove(uri)
        notifyItemChanged(position)
        refreshSelectedView()
    }

    private fun refreshSelectedView() {
        selectedUriList.forEach {
            val position: Int = getViewPosition(it)
            notifyItemChanged(position)
        }
    }

    inner class ImageViewHolder(parent: ViewGroup) :
        BaseViewHolder<ItemGalleryMediaBinding, Media>(parent, R.layout.item_gallery_media) {

        var thisData: Media? = null

        init {
            itemView.layoutParams.width = squareSize
            itemView.layoutParams.height = squareSize

            binding.run {
                selectType = builder.selectType
                isSelected = false
                viewZoomOut.setOnClickListener {
                    val item = getItem(adapterPosition.takeIf { it != NO_POSITION }
                        ?: return@setOnClickListener)
                    startZoomActivity(item)
                }
                showZoom = false
                showDuration = false
            }

        }

        override fun bind(data: Media) {
            binding.run {
                media = data
                isSelected = selectedUriList.contains(data.uri)
                if (isSelected) {
                    selectedNumber = selectedUriList.indexOf(data.uri) + 1
                }

                itemView.warning_view.setOnClickListener {
                    Toast.makeText(context, "Error with ${data.uri}", Toast.LENGTH_LONG).show()
                }

                showDuration = builder.mediaType == MediaType.VIDEO
                isVideo = showDuration

                if (!isVideo) {
                    selectedBorder.bottomThickness = -1
                }

                selectedBorder.borderBackgroundColor = ContextCompat.getColor(context, builder.selectedBackgroundColor)
                ivSelectedNumber.background = ColorDrawable(ContextCompat.getColor(context, builder.selectedBackgroundColor))

                val textColor = ContextCompat.getColor(context, builder.selectedTextColor)
                ivSelectedNumber.setTextColor(textColor)
                videoDurationTextviewSelected.setTextColor(textColor)
                ivVideoIconSelected.setColorFilter(textColor)

                try {
                    if (showDuration) {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(context, data.uri)

                        var length: Long = 0
                        try {
                            length =
                                (Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000).toLong()
                        } catch (e: Exception) {
                        }
                        itemView.video_duration_textview.text = "${DateUtils.formatElapsedTime(length)}"
                        itemView.video_duration_textview_selected.text = "${DateUtils.formatElapsedTime(length)}"

                        if (builder.typeface != null) {
                            itemView.video_duration_textview.typeface =
                                ResourcesCompat.getFont(context, builder.typeface!!)
                            itemView.video_duration_textview_selected.typeface =
                                ResourcesCompat.getFont(context, builder.typeface!!)
                            itemView.iv_selectedNumber.typeface = ResourcesCompat.getFont(context, builder.typeface!!)
                        }
                    }
                } catch (e: Exception) {
                    Log.i("FAIL", "Failed for ${data.uri}")
                    itemView.item_preview.visibility = View.GONE
                    itemView.warning_view.visibility = View.VISIBLE
                }

                showZoom =
                    !isSelected && (builder.mediaType == MediaType.IMAGE) && builder.showZoomIndicator

                if (builder.mediaType == MediaType.VIDEO && builder.showVideoDuration) {
                    setVideoDuration(data.uri)
                }
            }
        }

        private fun setVideoDuration(uri: Uri) = executorService.execute {
            val durationMills = uri.getVideoDuration() ?: return@execute
            val hours = TimeUnit.MILLISECONDS.toHours(durationMills)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMills)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMills)
            binding.duration =
                if (hours > 0) {
                    String.format("%d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%02d:%02d", minutes, seconds)
                }
        }

        private fun Uri.getVideoDuration(): Long? {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, this)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            return time?.toLongOrNull()
        }

        override fun recycled() {
            if (activity.isDestroyed) {
                return
            }
            Glide.with(activity).clear(binding.ivImage)
        }

        private fun startZoomActivity(media: Media) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                binding.ivImage,
                media.uri.toString()
            ).toBundle()

            activity.startActivity(TedImageZoomActivity.getIntent(activity, media.uri), options)
        }
    }

    inner class CameraViewHolder(parent: ViewGroup) : HeaderViewHolder<ItemGalleryCameraBinding>(
        parent, R.layout.item_gallery_camera
    ) {

        init {
            itemView.layoutParams.width = squareSize
            itemView.layoutParams.height = squareSize
            binding.ivImage.setImageResource(builder.cameraTileImageResId)
            itemView.setBackgroundResource(builder.cameraTileBackgroundResId)
        }
    }
}
