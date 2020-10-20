package com.bignerdranch.android.photogallery

import android.app.Application
import androidx.lifecycle.*

//sends first request for photos, preists through landscape changes
class PhotoGalleryViewModel(private val app: Application) :
    AndroidViewModel(app) {

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    //allow storing most recent query
    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()
    val searchTerm: String get() = mutableSearchTerm.value ?: ""


    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)

        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) {
                searchTerm ->
            if (searchTerm.isBlank()) {
                flickrFetchr.fetchPhotos()
            } else {
                flickrFetchr.searchPhotos(searchTerm)
            }
        }

    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }

}
