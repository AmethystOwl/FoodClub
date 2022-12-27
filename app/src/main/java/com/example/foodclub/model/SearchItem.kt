package com.example.foodclub.model

import com.algolia.instantsearch.core.highlighting.HighlightedString
import com.algolia.instantsearch.highlighting.Highlightable
import com.algolia.search.model.Attribute
import com.algolia.search.model.ObjectID
import com.algolia.search.model.indexing.Indexable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SearchItem(
    var restaurantId: String,
    // mealId
    override val objectID: ObjectID,
    var name: String,
    var likes: Long,
    var imageUrl: String,
    var category: String,
    var restaurantName:String,
    var restaurantImageUrl: String,
    override val _highlightResult: JsonObject?,
    ) : Highlightable, Indexable {
    val highlightedTitle: HighlightedString?
        get() = getHighlight(Attribute("name"))
}


