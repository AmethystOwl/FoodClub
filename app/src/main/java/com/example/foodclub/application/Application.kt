package com.example.foodclub.application

import com.zeugmasolutions.localehelper.LocaleAwareApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : LocaleAwareApplication() {
    override fun onCreate() {
        super.onCreate()
      /*  MapboxSearchSdk.initialize(
            application = this,
            accessToken = getString(R.string.mapbox_access_token),
            locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        )*/
        /* val client = ClientSearch(
             ApplicationID("5J6686IE08"),
             APIKey("52f093c565f9d0255354548421c84f4f")
         )
         val index = client.initIndex(IndexName("menu_items"))
         FirebaseFirestore.getInstance().collection(Constants.RESTAURANTS_COLLECTION).get()
             .addOnCompleteListener {
                 it.result.documents.forEach { restaurantSnapShot ->
                     val res = restaurantSnapShot.toObject(Restaurant::class.java)!!
                     res.id = restaurantSnapShot.id
                     restaurantSnapShot.reference.collection(Constants.MENU_ITEMS_COLLECTION)
                         .get().addOnCompleteListener {
                             it.result.documents.forEach { mealSnapShot ->
                                 val meal = mealSnapShot.toObject(MenuItem::class.java)!!
                                 meal.id = mealSnapShot.id


                                 val searchable = Product(
                                     res.id!!,
                                     ObjectID(meal.id!!),
                                     meal.name!!,
                                     meal.likes!!,
                                     meal.imageUrl!!,
                                     meal.category!!,
                                     restaurantName = res.name!!,
                                     restaurantImageUrl = res.iconImageUrl!!
                                 )
                                 GlobalScope.launch {
                                     index.saveObject(Product.serializer(), searchable)
                                 }
                             }
                         }
                 }
             }*/


    }

}
