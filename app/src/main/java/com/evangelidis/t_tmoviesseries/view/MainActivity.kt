package com.evangelidis.t_tmoviesseries.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.Genre
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: ListViewModel
    private val moviesListAdapter = MoviesListAdapter(arrayListOf())
    private var genresList :ArrayList<Genre> = arrayListOf()

    var currentPage = 1
    var listOfRetrievedPages = arrayListOf(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        viewModel.getGenres()
        viewModel.getMovies(currentPage)

        countriesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moviesListAdapter
        }

        viewModel.genresData.observe(this, Observer { data ->
            data?.let {
                moviesListAdapter.appendGenres(data.genres)
                genresList.addAll(data.genres)
            }
        })

        viewModel.moviesList.observe(this, Observer { data ->
            data?.let {
                moviesListAdapter.appendMovies(it.results)
            }
        })

        val manager = LinearLayoutManager(this)
        countriesList.layoutManager = manager

        countriesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = manager.itemCount
                val visibleItemCount = manager.childCount
                val firstVisibleItem = manager.findFirstVisibleItemPosition()
                if (firstVisibleItem + visibleItemCount >= totalItemCount / 2) {

                    listOfRetrievedPages.add(listOfRetrievedPages.last() + 1)
                    viewModel.fetchMovies(listOfRetrievedPages.last())
                }
            }
        })
    }
}
