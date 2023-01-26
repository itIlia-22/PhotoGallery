package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bignerdranch.android.photogallery.model.GalleryItem
import com.bignerdranch.android.photogallery.view.VisibleFragment
import com.bignerdranch.android.photogallery.viewmodel.PhotoGalleryViewModel
import com.bignerdranch.android.photogallery.viewweb.PhotoPageActivity
import com.bignerdranch.android.photogallery.work.PollWork
import java.util.concurrent.TimeUnit


private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

/**@author
 * Чтобы запланировать выполнение Worker, нам нужен запрос
WorkRequest. Сам класс WorkRequest является абстрактным,
поэтому нам придется использовать один из его подклассов в
зависимости от типа работы, которую вам нужно выполнить.
Если у вас есть что-то, что нужно выполнить только один раз,
используйте OneTimeWorkRequest. Если ваша работа должна
выполняться периодически, используйте
PeriodicWorkRequest.

 */

class PhotoGalleryFragment : VisibleFragment() {
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
        setHasOptionsMenu(true)

        val responseHandler = Handler()
        thumbnailDownloader =
            ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
                val drawable = BitmapDrawable(resources, bitmap)
                photoHolder.bindDrawable(drawable)
            }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

        /*
        /**
         * В OneTimeWorkRequest используется конструктор для
        создания экземпляра. Мы передаем класс Worker конструктору,
        который будет запущен в рабочем запросе. Как только ваш
        рабочий запрос будет готов, вам нужно запланировать его с
        помощью класса WorkManager. Мы вызываем функцию
        getInstance() для доступа к WorkManager, затем функцию
        enqueue(...) с рабочим запросом в качестве параметра. Это
        запланирует выполнение вашего рабочего запроса с учетом
        типа запроса и любых ограничений, которые вы в него
        добавляете.
         */
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        val workRequest = OneTimeWorkRequest
            .Builder(PollWork::class.java)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance()
            .enqueue(workRequest)
         */

    }

    override fun onOptionsItemSelected(
        item:
        MenuItem
    ): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                viewModel.fetchPhotos("")
                true
            }
            /**
             * обратите внимание на блок else, который вы добавили
            здесь. Если работник в данный момент не запущен, то мы
            назначим новый запрос на работу с WorkManager. В этом
            случае вы используете PeriodicWorkRequest, чтобы
            заставить вашего работника самого запланировать себя через
            некоторый интервал. В запросе используется конструктор,
            такой как OneTimeWorkRequest, который вы использовали
            ранее. Конструктору нужен класс Worker, а также интервал
            выполнения.
            Если вы считаете, что 15 минут — это слишком долго, то вы
            правы. Однако если вы установите меньшее значение
            интервала, то обнаружите, что работник все равно выполняется
            с 15-минутным интервалом. Это минимальный интервал,
            допустимый для PeriodicWorkRequest, чтобы система не
            была постоянно привязана к выполнению одного и того же
            рабочего запроса. Это экономит системные ресурсы и ресурс
            батареи.
            Конструктор PeriodicWorkRequest принимает
            ограничения, как и одноразовый запрос, поэтому вы можете
            добавить ограничение на работу с сетью с измерением
            трафика. Когда вы хотите спланировать запрос на работу, вы
            используете класс WorkManager, но на этот раз вы используете
            функцию enqueueUniquePeriodicWork(...). Эта функция
            принимает имя типа String, политику и ваш рабочий запрос.
            Имя позволяет однозначно идентифицировать запрос, что
            полезно, если вы захотите его отменить.
            Политика работы подсказывает менеджеру, что делать, если
            вы уже запланировали запрос на работу с определенным
            именем. В этом случае вы используете опцию KEEP, которая
            отказывается от нового запроса в пользу уже существующего.
            Другая опция — REPLACE, которая, как следует из названия,
            заменяет существующий запрос на новый.
            Если работник уже запущен, то вам необходимо сообщить
            WorkManager об отмене запроса на работу. В этом случае для
            удаления периодического запроса на работу вызывается
            функция cancelUniqueWork(...) с именем POLL_WORK.

             */
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(), false)
                } else {
                    val constrains = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWork::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constrains)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(
                        POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicRequest
                    )
                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }


            else ->
                super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        viewModel.fetchPhotos(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d(TAG, "QueryTextChange: $newText")
                    return false

                }

            })

            setOnClickListener {
                searchView.setQuery(viewModel.searchTerm, false)
            }
        }

        val toggleItem =
            menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling =
            QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)

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

    /**
     * Сначала мы откроем страницу по URL-адресу при помощи
    старого знакомого — неявного интента. Этот интент запустит
    браузер с URL-адресом страницы фотографии.
    Для начала нужно организовать прослушивание нажатий на
    элементах представления RecyclerView. Измените код
    реализации PhotoHolder из PhotoGalleryFragment и
    включите в нее слушателя кликов, который будет выдавать
    неявный интент.

     */
    private inner class PhotoHolder(itemImageView: ImageView) :
        RecyclerView.ViewHolder(itemImageView),
        View.OnClickListener {
        private lateinit var galleryItem: GalleryItem

        init {
            itemImageView.setOnClickListener(this)
        }

        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

        fun bindGalleryItem(item: GalleryItem) {
            galleryItem = item
        }


        override fun onClick(v: View?) {
            //val intent = Intent(Intent.ACTION_VIEW, galleryItem.photoPageUri)
            val intent = PhotoPageActivity.newIntent(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
        }

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
            holder.bindGalleryItem(galleryItem)
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