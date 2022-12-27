package com.example.foodclub.algolia

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.algolia.instantsearch.android.list.autoScrollToStart
import com.algolia.instantsearch.android.loading.LoadingViewSwipeRefreshLayout
import com.algolia.instantsearch.android.paging3.liveData
import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.searchbox.SearchBoxView
import com.algolia.instantsearch.loading.connectView
import com.algolia.instantsearch.searchbox.connectView
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.adapter.SearchAdapter
import com.example.foodclub.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = requireActivity() as MainActivity
        main.hideBottomNavigation()
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


        val searchBoxView =
            MySearchBoxView(binding.searchView, binding.hits, binding.searchAnimationLottie)

        val loadingView = LoadingViewSwipeRefreshLayout(binding.swipeRefreshLayout)
        searchViewModel.connection += searchViewModel.loading.connectView(loadingView)
        searchViewModel.connection += searchViewModel.searchBox.connectView(searchBoxView)
        binding.swipeRefreshLayout.setColorSchemeColors(Color.GREEN)
        showSoftKeyboard(requireContext(), binding.searchView)

        val adapterProduct = SearchAdapter(SearchAdapter.OnSearchItemClickListener {
            findNavController().navigate(
                SearchFragmentDirections.actionProductFragmentToMealFragment(
                    it.restaurantId, it.restaurantName, it.restaurantImageUrl, it.objectID.raw
                )
            )
        })
        searchViewModel.paginator.liveData.observe(viewLifecycleOwner) { pagingData ->
            adapterProduct.submitData(lifecycle, pagingData)
        }
        binding.hits.let {
            it.itemAnimator = null
            it.adapter = adapterProduct
            it.layoutManager = LinearLayoutManager(requireContext())
            it.autoScrollToStart(adapterProduct)
        }

        searchViewModel.searcher.searchAsync()

    }


    private fun showSoftKeyboard(context: Context, searchView: SearchView) {
        searchView.requestFocus()
        searchView.postDelayed(
            {
                val keyboard =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.showSoftInput(searchView, 0)
            }, 200
        )
    }


}


class MySearchBoxView(
    val searchView: SearchView,
    val rv: RecyclerView,
    val lottieAnimationView: LottieAnimationView,
) : SearchBoxView {

    override var onQueryChanged: Callback<String?>? = null
    override var onQuerySubmitted: Callback<String?>? = null

    init {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                onQuerySubmitted?.invoke(query)
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                onQueryChanged?.invoke(query)
                if (!(query.isNullOrEmpty() || query.isBlank())) {
                    lottieAnimationView.cancelAnimation()
                    lottieAnimationView.visibility = View.GONE
                    rv.visibility = View.VISIBLE
                } else {
                    lottieAnimationView.playAnimation()
                    lottieAnimationView.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                }
                return false
            }
        })
    }

    override fun setText(text: String?, submitQuery: Boolean) {
        searchView.setQuery(text, submitQuery)
    }
}