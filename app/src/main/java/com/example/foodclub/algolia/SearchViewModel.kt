package com.example.foodclub.algolia

import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import com.algolia.instantsearch.android.paging3.Paginator
import com.algolia.instantsearch.android.paging3.searchbox.connectPaginator
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.loading.LoadingConnector
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.instantsearch.searcher.hits.SearchForQuery
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.example.foodclub.BuildConfig
import com.example.foodclub.model.SearchItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    val searcher = HitsSearcher(
        applicationID = ApplicationID(BuildConfig.algoliaAppId),
        apiKey = APIKey(BuildConfig.algoliaKey),
        indexName = IndexName("menu_items"),
        triggerSearchFor = SearchForQuery.lengthAtLeast(1)
    )
    val paginator = Paginator(
        searcher = searcher,
        pagingConfig = PagingConfig(pageSize = 50, enablePlaceholders = false),
        transformer = { hit -> hit.deserialize(SearchItem.serializer()) }
    )

    val searchBox = SearchBoxConnector(searcher)
    val loading = LoadingConnector(searcher)
    val connection = ConnectionHandler(searchBox, loading)

    init {
        connection += searchBox.connectPaginator(paginator)
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
        connection.disconnect()
    }
}