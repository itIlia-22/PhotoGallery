package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.photogallery.model.GalleryItem
import com.bignerdranch.android.photogallery.viewmodel.PhotoGalleryViewModel


private const val TAG = "PhotoGalleryFragment"


class PhotoGalleryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: PhotoGalleryViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    @SuppressLint("FragmentLiveDataObserve")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this)[PhotoGalleryViewModel::class.java]
        recyclerView = view.findViewById(R.id.photo_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 3)




        viewModel.galleryLiveData.observe(this, Observer { galleryItems ->
            recyclerView.adapter = Adapter(galleryItems)

        })


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // save fragment.Лучше не использовать так как диприкейт
        @Suppress("DEPRECATION")
        retainInstance = true

        val responseHandler = Handler()
        thumbnailDownloader =
            ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
                val drawable = BitmapDrawable(resources, bitmap)
                photoHolder.bindDrawable(drawable)
            }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)

    }
    override fun onDestroyView() {
        super.onDestroyView()
        thumbnailDownloader.clearQueue()
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
        return inflater.inflate(R.layout.fragment_photo_gallery, container, false)
    }


    private class PhotoHolder(itemImageView: ImageView) : RecyclerView.ViewHolder(itemImageView) {
        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

    }

    private inner class Adapter(private val galleryItem: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoHolder(view)

        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItem[position]
            val placeholder: Drawable =
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_launcher_foreground
                ) ?: ColorDrawable()
            holder.bindDrawable(placeholder)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }

        override fun getItemCount(): Int = galleryItem.size
    }

    companion object {

        @JvmStatic
        fun newInstance() = PhotoGalleryFragment()
    }
}