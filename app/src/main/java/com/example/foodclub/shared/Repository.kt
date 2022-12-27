package com.example.foodclub.shared

import android.net.Uri
import com.example.foodclub.model.*
import com.example.foodclub.utils.Constants
import com.example.foodclub.utils.DataState
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


class Repository @Inject constructor(
    private val auth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun login(email: String, password: String) = callbackFlow<DataState<*>> {
        trySend(DataState.Loading)
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { signInTask ->
            when {
                signInTask.isSuccessful -> {
                    fireStore.collection(Constants.USERS_COLLECTION)
                        .document(auth.currentUser?.uid!!)
                        .get()
                        .addOnCompleteListener {
                            when {
                                it.isSuccessful -> {
                                    if (it.result.exists() && it.result.data != null) {
                                        val user = it.result.toObject(UserProfile::class.java)
                                        if (user != null) {
                                            trySend(DataState.Success(user))
                                        }
                                    }
                                }
                                it.isCanceled -> {
                                    trySend(DataState.Canceled)
                                    cancel()
                                }
                                it.exception != null -> {
                                    trySend(DataState.Error(it.exception!!))
                                    cancel("Error logging in", it.exception)
                                }
                            }
                        }
                }
                signInTask.isCanceled -> {
                    trySend(DataState.Canceled)
                    cancel()

                }
                signInTask.exception != null -> {
                    when (signInTask.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            trySend(DataState.Invalid(Constants.LOGIN_INVALID_CREDENTIALS))
                            cancel("Invalid Credentials", signInTask.exception)

                        }
                        is FirebaseAuthInvalidUserException -> {
                            trySend(DataState.Invalid(Constants.LOGIN_NO_USER))
                            cancel("Invalid User", signInTask.exception)

                        }
                        else -> {
                            trySend(DataState.Error(signInTask.exception!!))
                            cancel("Exception", signInTask.exception)
                        }
                    }
                }
            }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    suspend fun register(userProfile: UserProfile, password: String) =
        callbackFlow<DataState<UserProfile>?> {
            trySend(DataState.Loading)
            auth.createUserWithEmailAndPassword(userProfile.email, password)
                .addOnCompleteListener {
                    if (it.isComplete) {
                        when {
                            it.isSuccessful -> {
                                fireStore.collection(Constants.USERS_COLLECTION)
                                    .document(auth.currentUser?.uid!!)
                                    .set(userProfile)
                                    .addOnCompleteListener { docTask ->
                                        when {
                                            docTask.isSuccessful -> {
                                                trySend(DataState.Success(userProfile))
                                            }
                                            docTask.isCanceled -> {
                                                trySend(DataState.Canceled)
                                                cancel()
                                            }
                                            docTask.exception != null -> {
                                                trySend(DataState.Error(it.exception!!))
                                                cancel(
                                                    message = "Error adding user",
                                                    cause = docTask.exception
                                                )
                                            }
                                        }
                                    }
                            }
                            it.isCanceled -> {
                                trySend(DataState.Canceled)
                                cancel()
                            }
                            it.exception != null -> {
                                trySend(DataState.Error(it.exception!!))
                                cancel(
                                    message = "Error adding user",
                                    cause = it.exception
                                )
                            }
                        }
                    }
                }
            awaitClose()
        }.flowOn(Dispatchers.IO)

    suspend fun uploadProfilePicture(imageUri: Uri) = callbackFlow {
        trySend(DataState.Loading)
        if (auth.uid != null) {
            val doc = fireStore.collection(Constants.USERS_COLLECTION).document(auth.uid!!)
            doc.get().addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        if (it.result.exists()) {
                            val uploadTask = storage.getReference("profilePictures/")
                                .child(System.currentTimeMillis().toString())
                                .putFile(imageUri)
                            uploadTask.addOnCompleteListener { completionTask ->
                                if (completionTask.isComplete) {
                                    when {
                                        completionTask.isSuccessful -> {
                                            completionTask.result.storage.downloadUrl.addOnCompleteListener {
                                                val downloadUrl = it.result.toString()
                                                doc.update(
                                                    Constants.PROFILE_PICTURE_FIELD,
                                                    downloadUrl
                                                ).addOnCompleteListener {
                                                    when {
                                                        it.isSuccessful -> {
                                                            trySend(DataState.Success(0))
                                                        }
                                                        it.isCanceled -> {
                                                            trySend(DataState.Canceled)
                                                            cancel()
                                                        }
                                                        it.exception != null -> {
                                                            trySend(DataState.Error(completionTask.exception!!))
                                                            cancel(
                                                                "Error uploading Profile Picture",
                                                                completionTask.exception
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                        completionTask.isCanceled -> {
                                            trySend(DataState.Canceled)
                                            cancel()
                                        }
                                        completionTask.exception != null -> {
                                            trySend(DataState.Error(completionTask.exception!!))
                                            cancel(
                                                "Error uploading Profile Picture",
                                                completionTask.exception
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    it.isCanceled -> {
                        trySend(DataState.Canceled)
                    }
                    it.exception != null -> {
                        trySend(DataState.Error(it.exception!!))
                    }
                }
            }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun isUserSignedIn() = auth.currentUser != null

    suspend fun getCurrentUserProfile() = callbackFlow<DataState<UserProfile?>> {
        if (auth.uid != null) {
            trySend(DataState.Loading)
            fireStore.collection(Constants.USERS_COLLECTION)
                .document(auth.uid!!)
                .get()
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            if (it.result != null && it.result.exists()) {
                                val userProfile = it.result.toObject(UserProfile::class.java)!!
                                userProfile.uId = auth.uid!!
                                trySend(DataState.Success(userProfile))
                            }
                        }
                        it.isCanceled -> {
                            trySend(DataState.Canceled)
                            cancel()
                        }
                        it.exception != null -> {
                            trySend(DataState.Error(it.exception!!))
                            cancel(
                                CancellationException(
                                    "Error obtaining current user profile",
                                    it.exception!!
                                )
                            )
                        }
                    }
                }
        } else {
            trySend(DataState.Success(null))
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    suspend fun addToFavorite(restaurantId: String) = callbackFlow {
        trySend(DataState.Loading)
        fireStore.collection(Constants.USERS_COLLECTION)
            .document(auth.uid!!)
            .get()
            .addOnCompleteListener { userTask ->
                when {
                    userTask.isSuccessful -> {
                        val state =
                            if ((userTask.result[Constants.FAVORITES] as ArrayList<*>?)?.contains(
                                    restaurantId
                                )!!
                            ) {
                                userTask.result.reference.update(
                                    Constants.FAVORITES,
                                    FieldValue.arrayRemove(restaurantId)
                                )
                                Constants.FAVORITE_STATE_REMOVED
                            } else {
                                userTask.result.reference.update(
                                    Constants.FAVORITES,
                                    FieldValue.arrayUnion(restaurantId)
                                )
                                Constants.FAVORITE_STATE_ADDED
                            }
                        trySend(DataState.Success(state))
                        close()
                    }
                    userTask.isCanceled -> {
                        trySend(DataState.Canceled)
                        cancel()
                    }
                    userTask.exception != null -> {
                        trySend(DataState.Error(userTask.exception!!))
                        cancel("Error adding to favorites", userTask.exception)

                    }
                }
            }

        awaitClose()
    }.flowOn(Dispatchers.IO)


    fun getDefaultRestaurantsQuery(ordered: Boolean): Query {
        return when (ordered) {
            true -> {
                fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                    .orderBy(Constants.FIELD_NAME)
                    .limit(50)
            }
            false -> {
                fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                    .limit(50)
            }
        }
    }

    suspend fun defaultFavoritesQuery() = callbackFlow<DataState<ArrayList<Restaurant>>?> {
        if (auth.uid != null) {
            fireStore.collection(Constants.USERS_COLLECTION)
                .document(auth.uid!!)
                .get()
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            val currentUser = it.result?.toObject(UserProfile::class.java)
                            if (currentUser != null) {
                                val favoritesList = currentUser.favorites!!
                                if (favoritesList.size > 0) {
                                    fireStore.collection(Constants.RESTAURANTS_COLLECTION).whereIn(
                                        FieldPath.documentId(),
                                        favoritesList
                                    ).get().addOnCompleteListener { restaurantsSnapShot ->
                                        val list = ArrayList<Restaurant>()
                                        for (item in restaurantsSnapShot.result!!) {
                                            val restaurant = item.toObject(Restaurant::class.java)
                                            restaurant.id = item.id
                                            list.add(restaurant)
                                        }
                                        trySend(DataState.Success(list))
                                        close()

                                    }
                                }
                            }
                        }
                        it.exception != null -> {
                            trySend(DataState.Error(it.exception!!))
                            cancel(CancellationException("Error:", it.exception))
                        }
                    }
                }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    suspend fun removeRestaurant(id: String) = callbackFlow<DataState<Boolean>> {
        if (auth.uid != null) {
            fireStore.collection(Constants.USERS_COLLECTION)
                .document(auth.uid!!)
                .get()
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            val currentUser = it.result?.toObject(UserProfile::class.java)
                            if (currentUser != null) {
                                if (currentUser.favorites?.contains(id)!!) {
                                    it.result?.reference?.update(
                                        Constants.FAVORITES,
                                        FieldValue.arrayRemove(id)
                                    )
                                    trySend(DataState.Success(true))
                                    close()
                                }
                            }
                        }
                        it.exception != null -> {
                            trySend(DataState.Error(it.exception!!))
                            cancel("Remove Restaurant Exception: ", it.exception!!)

                        }
                    }
                }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun filterRestaurantsQuery(cuisineName: String): Query {
        return fireStore.collection(Constants.RESTAURANTS_COLLECTION)
            .whereArrayContains(Constants.FIELD_CUISINES, cuisineName)
    }

    suspend fun getRestaurantFavoriteState(restaurantId: String) = callbackFlow {
        if (auth.uid != null) {
            fireStore.collection(Constants.USERS_COLLECTION)
                .document(auth.uid!!)
                .get()
                .addOnCompleteListener { userTask ->
                    when {
                        userTask.isSuccessful -> {
                            val user = userTask.result.toObject(UserProfile::class.java)
                            if (user?.favorites != null) {
                                trySend(DataState.Success(user.favorites?.contains(restaurantId)!!))
                                close()
                            }
                        }
                        userTask.exception != null -> {
                            trySend(DataState.Error(userTask.exception!!))
                            cancel(
                                CancellationException(
                                    "Error Retrieving Restaurant FavoriteState: ",
                                    userTask.exception!!
                                )
                            )
                        }
                        userTask.isCanceled -> {
                            cancel(CancellationException("Restaurant FavoriteState Task Canceled"))
                        }
                    }
                }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)


    fun getFilteredQuery(checked: ArrayList<String>): Query {
        var query = fireStore.collection(Constants.RESTAURANTS_COLLECTION).limit(1000)
        for (item in checked) {
            when (item) {
                Constants.A_TO_Z -> {
                    query = query.orderBy(Constants.FIELD_NAME, Query.Direction.ASCENDING)
                }
                Constants.FIELD_NO_REVIEWS -> {
                    query = query.orderBy(Constants.FIELD_NO_REVIEWS, Query.Direction.DESCENDING)
                }
                Constants.FIELD_FREE_DELIVERY -> {
                    query = query.whereEqualTo(Constants.FIELD_FREE_DELIVERY, true)
                }
                Constants.FIELD_OPEN_NOW -> {
                    query = query.whereEqualTo(Constants.FIELD_OPEN_NOW, true)
                }
            }
        }
        return query
    }

    fun logOut() {
        auth.signOut()
    }

    suspend fun getRestaurantById(restaurantId: String) = callbackFlow<DataState<Restaurant>> {
        trySend(DataState.Loading)
        fireStore.collection(Constants.RESTAURANTS_COLLECTION)
            .document(restaurantId)
            .get()
            .addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        val restaurant = it.result.toObject(Restaurant::class.java)!!
                        trySend(DataState.Success(restaurant))
                        close()
                    }
                    it.isCanceled -> {
                        trySend(DataState.Canceled)
                        cancel()

                    }
                    it.exception != null -> {
                        trySend(DataState.Error(it.exception!!))
                        cancel(
                            CancellationException(
                                "Error retrieving restaurant info",
                                it.exception
                            )
                        )
                    }
                }
            }
        awaitClose()
    }.flowOn(Dispatchers.Main)

    suspend fun getMenuItemById(restaurantId: String, menuItemId: String) =
        callbackFlow<DataState<MenuItemUi>> {
            trySend(DataState.Loading)
            fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                .document(restaurantId)
                .collection(Constants.MENU_ITEMS_COLLECTION)
                .document(menuItemId)
                .get()
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            if (it.result.exists()) {
                                val menuItemUi = it.result.toObject(MenuItemUi::class.java)!!
                                trySend(DataState.Success(menuItemUi))
                                close()
                            }

                        }
                        it.isCanceled -> {
                            trySend(DataState.Canceled)
                            cancel(CancellationException())
                        }
                        it.exception != null -> {
                            trySend(DataState.Error(it.exception!!))
                            cancel(
                                CancellationException(
                                    "Error retrieving MenuItem info",
                                    it.exception
                                )
                            )
                        }
                    }

                }
            awaitClose()
        }.flowOn(Dispatchers.IO)

    suspend fun getCartQuery() = callbackFlow<DataState<CartUi>> {
        try {
            auth.uid?.let {
                trySend(DataState.Loading)
                fireStore.collection(Constants.USERS_COLLECTION)
                    .document(auth.uid!!)
                    .collection(Constants.CART_COLLECTION)
                    .document(Constants.CART_COLLECTION)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.exception != null) {
                            trySend(DataState.Error(task.exception!!))
                            close(task.exception!!)
                        } else {
                            val restaurantId = task.result["restaurantId"] as String?
                            if (restaurantId != null) {
                                task.result.reference.collection("cartItems")
                                    .get()
                                    .addOnCompleteListener { cartItems ->
                                        if (task.exception != null) {
                                            trySend(DataState.Error(task.exception!!))
                                            close(task.exception!!)
                                        } else {
                                            fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                                                .document(restaurantId).get()
                                                .addOnCompleteListener {
                                                    if (it.exception != null) {
                                                        trySend(DataState.Error(task.exception!!))
                                                        close(task.exception!!)
                                                    } else {
                                                        val restaurant =
                                                            it.result.toObject(Restaurant::class.java)
                                                        restaurant?.id = restaurantId
                                                        val cartItemsObjs =
                                                            cartItems.result.toObjects(CartItem::class.java) as ArrayList<CartItem>
                                                        val tasks =
                                                            ArrayList<Task<DocumentSnapshot>>()
                                                        for (c in cartItemsObjs) {
                                                            tasks.add(
                                                                fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                                                                    .document(restaurantId)
                                                                    .collection(Constants.MENU_ITEMS_COLLECTION)
                                                                    .document(c.mealId!!).get()
                                                            )
                                                        }
                                                        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                                                            .addOnCompleteListener {
                                                                val menuItems =
                                                                    ArrayList<CartItemUi>()
                                                                it.result.forEach {
                                                                    (it.toObject(MenuItem::class.java))
                                                                        ?.let { menuItem ->
                                                                            menuItem.id = it.id
                                                                            val exactItem =
                                                                                cartItemsObjs.filter { it.mealId == menuItem.id }[0]
                                                                            val cartItemUi =
                                                                                CartItemUi(
                                                                                    mealId = menuItem.id!!,
                                                                                    quantity = exactItem.quantity!!,
                                                                                    name = menuItem.name!!,
                                                                                    price = menuItem.price!!,
                                                                                )
                                                                            menuItems.add(cartItemUi)
                                                                        }
                                                                }
                                                                val cartUi =
                                                                    CartUi(restaurant, menuItems)
                                                                trySend(DataState.Success(cartUi))
                                                                close()
                                                            }

                                                    }
                                                }
                                        }
                                    }
                            } else {
                                // Cart is empty
                                trySend(DataState.Empty)
                                close()
                            }

                        }
                    }
            }
        } catch (e: Exception) {
            trySend(DataState.Error(e))
            close()
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    suspend fun removeCartItem(cartItemUi: CartItemUi) = callbackFlow<DataState<Int>> {
        auth.uid?.let {
            trySend(DataState.Loading)
            val collection = fireStore.collection(Constants.USERS_COLLECTION)
                .document(it)
                .collection(Constants.CART_COLLECTION)
                .document(Constants.CART_COLLECTION)
                .collection("cartItems")

            collection.get().addOnCompleteListener {
                if (it.exception != null) {
                    trySend(DataState.Error(it.exception!!))
                    close(it.exception!!)
                } else {
                    val docs = it.result.documents
                    var isEmpty = 0
                    if (docs.size <= 1) {
                        isEmpty = Constants.EMPTY_CART
                    }

                    collection.document(cartItemUi.mealId!!)
                        .delete()
                        .addOnCompleteListener { deletionTask ->
                            if (deletionTask.exception != null) {
                                trySend(DataState.Error(deletionTask.exception!!))
                                close(deletionTask.exception!!)
                            } else {
                                trySend(DataState.Success(isEmpty))
                                close()

                            }
                        }.addOnFailureListener {
                            trySend(DataState.Error(it))
                            close(it)
                        }

                }
            }


        }
        awaitClose()
    }.flowOn(Dispatchers.IO)


    suspend fun postReview(restaurantId: String, review: Review) = callbackFlow<DataState<Int>> {
        if (auth.uid != null) {
            trySend(DataState.Loading)
            fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                .document(restaurantId)
                .collection(Constants.REVIEWS_COLLECTION)
                .add(review)
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            trySend(DataState.Success(0))
                            close()
                        }
                        it.isCanceled -> {
                            trySend(DataState.Canceled)
                            close(CancellationException())
                        }
                        it.exception != null -> {
                            trySend(DataState.Canceled)
                            close(CancellationException("Error posting review: ", it.exception))
                        }
                    }
                }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun getReviewsQuery(restaurantId: String): Query {
        return fireStore.collection(Constants.RESTAURANTS_COLLECTION)
            .document(restaurantId)
            .collection(Constants.REVIEWS_COLLECTION)
            .orderBy(Constants.FIELD_TIMESTAMP, Query.Direction.DESCENDING)


    }

    suspend fun updateLocation(userLocation: UserLocation) = callbackFlow {
        auth.uid?.let {
            trySend(DataState.Loading)
            fireStore.collection(Constants.USERS_COLLECTION)
                .document(auth.uid!!)
                .update("location", userLocation)
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            trySend(DataState.Success(userLocation))
                            close()
                        }
                        it.exception != null -> {
                            trySend(DataState.Error(it.exception!!))
                            close(CancellationException("Error updating location", it.exception))
                        }
                    }
                }.addOnFailureListener {
                    trySend(DataState.Error(it))
                    close(CancellationException("Error updating location", it))
                }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)


    suspend fun addToCart(cartItem: CartItem) = callbackFlow {
        auth.uid?.let {
            trySend(DataState.Loading)
            val cartCollection = fireStore.collection(Constants.USERS_COLLECTION)
                .document(auth.uid!!)
                .collection(Constants.CART_COLLECTION)

            val cartDoc = cartCollection.document("cart")
            val cartItemsCollection = cartDoc.collection("cartItems")
            cartDoc.get().addOnCompleteListener {
                if (it.exception != null) {
                    trySend(DataState.Error(it.exception!!))
                    close()
                } else {
                    val docResId = it.result["restaurantId"] as String?
                    if (docResId == null) {
                        cartDoc.set(
                            hashMapOf("restaurantId" to cartItem.restaurantId),
                            SetOptions.merge()
                        ).addOnCompleteListener {
                            if (it.exception != null) {
                                trySend(DataState.Error(it.exception!!))
                                close()
                            } else {
                                // check presence of cart then insert
                                cartItemsCollection.get().addOnCompleteListener {
                                    if (it.exception != null) {
                                        trySend(DataState.Error(it.exception!!))
                                        close()
                                    } else {
                                        if (it.result.documents.isEmpty()) {
                                            // just insert
                                            cartItemsCollection.document(cartItem.mealId!!)
                                                .set(cartItem).addOnCompleteListener {
                                                    if (it.exception != null) {
                                                        trySend(DataState.Error(it.exception!!))
                                                        close()
                                                    } else {
                                                        trySend(DataState.Success(0))
                                                        close()
                                                    }
                                                }
                                        } else {
                                            // check and update existing / insert new
                                            cartItemsCollection.document(cartItem.mealId!!).get()
                                                .addOnCompleteListener {
                                                    if (it.exception != null) {
                                                        trySend(DataState.Error(it.exception!!))
                                                        close()
                                                    } else {
                                                        if (it.result.exists()) {
                                                            // exists, update qty
                                                            it.result.reference
                                                                .update(
                                                                    "quantity",
                                                                    cartItem.quantity!!
                                                                )
                                                                .addOnCompleteListener {
                                                                    if (it.exception != null) {
                                                                        trySend(DataState.Error(it.exception!!))
                                                                        close()
                                                                    } else {
                                                                        trySend(DataState.Success(0))
                                                                        close()
                                                                    }
                                                                }
                                                        } else {
                                                            // doesn't exist, add new.
                                                            cartItemsCollection.document(cartItem.mealId)
                                                                .set(cartItem)
                                                                .addOnCompleteListener {
                                                                    if (it.exception != null) {
                                                                        trySend(DataState.Error(it.exception!!))
                                                                        close()
                                                                    } else {
                                                                        trySend(DataState.Success(0))
                                                                        close()
                                                                    }
                                                                }

                                                        }
                                                    }
                                                }

                                        }
                                    }
                                }

                            }
                        }
                    } else {
                        // restaurant id not null
                        // compare ids then INSERT
                        if (docResId == cartItem.restaurantId!!) {
                            // same id, check and add
                            cartItemsCollection.document(cartItem.mealId!!).get()
                                .addOnCompleteListener {
                                    if (it.exception != null) {
                                        trySend(DataState.Error(it.exception!!))
                                        close()
                                    } else {
                                        if (it.result.exists()) {
                                            // exists, update
                                            it.result.reference.update(
                                                "quantity",
                                                cartItem.quantity!!
                                            )
                                                .addOnCompleteListener {
                                                    if (it.exception != null) {
                                                        trySend(DataState.Error(it.exception!!))
                                                        close()
                                                    } else {
                                                        trySend(DataState.Success(0))
                                                        close()
                                                    }
                                                }
                                        } else {
                                            // doesn't exist, add new.
                                            cartItemsCollection.document(cartItem.mealId)
                                                .set(cartItem).addOnCompleteListener {
                                                    if (it.exception != null) {
                                                        trySend(DataState.Error(it.exception!!))
                                                        close()
                                                    } else {
                                                        trySend(DataState.Success(0))
                                                        close()
                                                    }
                                                }

                                        }
                                    }
                                }

                        } else {
                            // different ids, cancel.
                            trySend(DataState.Invalid(Constants.RESTAURANT_MISMATCH))
                            close()
                        }
                    }
                }
            }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)


    suspend fun getMenu(restaurantId: String) = callbackFlow<DataState<List<MenuItem>>> {
        trySend(DataState.Loading)
        if (auth.uid != null) {
            fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                .document(restaurantId)
                .collection("menu")
                .get()
                .addOnCompleteListener {
                    if (it.exception != null) {
                        trySend(DataState.Error(it.exception!!))
                        close()
                    } else {
                        val res = ArrayList<MenuItem>()
                        for (doc in it.result.documents) {
                            val item = doc.toObject(MenuItem::class.java)
                            item?.id = doc.id
                            res.add(item!!)
                        }
                        trySend(DataState.Success(res))
                        close()
                    }
                }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    suspend fun addToFreshCart(cartItem: CartItem) = callbackFlow {
        trySend(DataState.Loading)
        if (auth.uid != null) {
            val cartCollection = fireStore.collection(Constants.USERS_COLLECTION)
                .document(auth.uid!!)
                .collection(Constants.CART_COLLECTION)
            val cartDoc = cartCollection.document("cart")
            val cartItemsCollection = cartDoc.collection("cartItems")
            cartItemsCollection.get().addOnCompleteListener {
                if (it.exception != null) {
                    trySend(DataState.Error(it.exception!!))
                    close()
                } else {
                    it.result.documents.forEach {
                        it.reference.delete()
                    }
                    cartCollection.get().addOnCompleteListener {
                        it.result.documents[0].reference.delete().addOnCompleteListener {
                            if (it.exception != null) {
                                trySend(DataState.Error(it.exception!!))
                                close()
                            } else {
                                cartDoc.set(
                                    hashMapOf("restaurantId" to cartItem.restaurantId),
                                    SetOptions.merge()
                                ).addOnCompleteListener {
                                    if (it.exception != null) {
                                        trySend(DataState.Error(it.exception!!))
                                        close()
                                    } else {
                                        cartItemsCollection.document(cartItem.mealId!!)
                                            .set(cartItem)
                                            .addOnCompleteListener {
                                                if (it.exception != null) {
                                                    trySend(DataState.Error(it.exception!!))
                                                    close()
                                                } else {
                                                    trySend(DataState.Success(0))
                                                    close()
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    }


                }
            }.addOnFailureListener {
                trySend(DataState.Error(it))
                cancel(it.message!!)
            }

        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    suspend fun removeCart() = callbackFlow {
        auth.uid?.let {
            fireStore.collection(Constants.USERS_COLLECTION)
                .document(auth.uid!!)
                .collection(Constants.CART_COLLECTION)
                .document(Constants.CART_COLLECTION)
                .delete()
                .addOnCompleteListener {
                    if (it.exception != null) {
                        trySend(DataState.Error(it.exception!!))
                        close(it.exception!!)
                    } else {
                        trySend(DataState.Success(0))
                        close()
                    }
                }.addOnFailureListener {
                    trySend(DataState.Error(it))
                    close(it)
                }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    suspend fun placeOrder(orderItem: OrderItem) = callbackFlow {
        auth.uid?.let {
            trySend(DataState.Loading)
            // Create two tasks, first to write to user, second to write to restaurant
            // make both of the documents carry the same id
            val uuid = UUID.randomUUID().toString()
            orderItem.userId = it
            val userTask = fireStore.collection(Constants.USERS_COLLECTION)
                .document(it)
                .collection(Constants.ORDERS_COLLECTION)
                .document(uuid)
                .set(orderItem)
            val restaurantTask = fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                .document(orderItem.restaurantId!!)
                .collection(Constants.ORDERS_COLLECTION)
                .document(uuid)
                .set(orderItem)
            val tasks = arrayListOf(userTask, restaurantTask)
            Tasks.whenAllComplete(tasks).addOnCompleteListener { tasks ->
                tasks.result.forEach { task ->
                    if (task.exception != null) {
                        trySend(DataState.Error(task.exception!!))
                        close(task.exception)
                    }
                }
                trySend(DataState.Success(0))
                close()
            }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    fun getOrders() = callbackFlow {
        auth.uid?.let {
            trySend(DataState.Loading)
            fireStore.collection(Constants.USERS_COLLECTION)
                .document(it)
                .collection(Constants.ORDERS_COLLECTION)
                .get()
                .addOnCompleteListener {
                    if (it.exception != null) {
                        trySend(DataState.Error(it.exception!!))
                        close()
                    } else {
                        val orderItems = ArrayList<OrderItem>()
                        it.result.forEach {
                            val item = it.toObject(OrderItem::class.java)
                            item.orderId = it.id
                            orderItems.add(item)
                        }

                        val tasks = ArrayList<Task<DocumentSnapshot>>()
                        orderItems.forEach {
                            it.items?.forEach {
                                val menuCollection =
                                    fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                                        .document(orderItems[0].restaurantId!!)
                                        .collection(Constants.MENU_ITEMS_COLLECTION)
                                tasks.add(menuCollection.document(it.mealId!!).get())


                            }
                        }
                        Tasks.whenAllComplete(tasks).addOnCompleteListener { docTasks ->
                            if (it.exception != null) {
                                trySend(DataState.Error(it.exception!!))
                                close()
                            } else {
                                fireStore.collection(Constants.RESTAURANTS_COLLECTION)
                                    .document(orderItems[0].restaurantId!!).get()
                                    .addOnCompleteListener {
                                        if (it.exception != null) {
                                            trySend(DataState.Error(it.exception!!))
                                            close()
                                        } else {
                                            val restaurant =
                                                it.result.toObject(Restaurant::class.java)
                                            restaurant?.id = it.result.id
                                            val items = ArrayList<OrderItemUi>()
                                            launch {
                                                docTasks.result.forEach {
                                                    val res = it.result as DocumentSnapshot
                                                    val menuItem =
                                                        res.toObject(MenuItem::class.java)
                                                    menuItem?.id = res.id
                                                    orderItems.forEach { orderItem ->
                                                        orderItem.items?.forEach { orderItemsDetails ->
                                                            if (orderItemsDetails.mealId == menuItem?.id) {
                                                                val orderItemUi = OrderItemUi(
                                                                    orderId = orderItem.orderId,
                                                                    restaurant = restaurant,
                                                                    mealId = orderItemsDetails.mealId,
                                                                    quantity = orderItemsDetails.quantity,
                                                                    name = menuItem?.name,
                                                                    imageUrl = menuItem?.imageUrl,
                                                                    price = orderItemsDetails.price,
                                                                    total = orderItemsDetails.total,
                                                                    timestamp = orderItem.timestamp,
                                                                )
                                                                items.add(orderItemUi)
                                                            }
                                                        }

                                                    }
                                                }
                                            }.invokeOnCompletion {
                                                trySend(DataState.Success(items))
                                                close()
                                            }
                                        }
                                    }


                            }

                        }


                    }
                }

        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    suspend fun changePassword(curPass: String, newPass: String) = callbackFlow {
        auth.currentUser?.uid?.let {
            trySend(DataState.Loading)
            val cred = EmailAuthProvider.getCredential(auth.currentUser?.email!!, curPass)
            auth.currentUser?.reauthenticate(cred)?.addOnCompleteListener {
                if (it.exception != null) {
                    trySend(DataState.Error(it.exception!!))
                    close(CancellationException("Failed to change password : ${it.exception!!}"))
                } else {
                    auth.currentUser?.updatePassword(newPass)?.addOnCompleteListener {
                        if (it.exception != null) {
                            trySend(DataState.Error(it.exception!!))
                            close(CancellationException("Failed to change password : ${it.exception!!}"))
                        } else {
                            trySend(DataState.Success(0))
                            close()
                        }
                    }
                }
            }

        }
        awaitClose()
    }.flowOn(Dispatchers.IO)

    suspend fun sendResetEmail(email: String) = callbackFlow {
        trySend(DataState.Loading)
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.exception != null) {
                trySend(DataState.Error(it.exception!!))
                close(CancellationException("Failed to reset password : ${it.exception!!}"))
            } else {
                trySend(DataState.Success(0))
                close()
            }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)


}
