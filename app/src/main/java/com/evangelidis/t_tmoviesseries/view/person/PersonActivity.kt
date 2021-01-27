package com.evangelidis.t_tmoviesseries.view.person

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.databinding.ActivityPersonBinding
import com.evangelidis.t_tmoviesseries.databinding.ThumbnailActorsMovieBinding
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.extensions.updatePadding
import com.evangelidis.t_tmoviesseries.model.PersonCast
import com.evangelidis.t_tmoviesseries.model.PersonDetailsResponse
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.INPUT_DATE_FORMAT
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.changeDateFormat
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getImageTopRadius
import com.evangelidis.t_tmoviesseries.view.biography.BiographyActivity
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.t_tmoviesseries.view.movie.MovieActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.t_tmoviesseries.view.tvshow.TvShowActivity
import java.text.SimpleDateFormat
import java.util.*

class PersonActivity : AppCompatActivity() {

    companion object {
        const val PERSON_ID = "PERSON_ID"
        const val MEDIA_MOVIE = "movie"
        const val MEDIA_TV_SHOW = "tv"

        fun createIntent(context: Context, movieId: Int): Intent =
            Intent(context, PersonActivity::class.java)
                .putExtra(PERSON_ID, movieId)
    }

    private lateinit var viewModel: ViewModelPerson

    private val binding: ActivityPersonBinding by lazy { ActivityPersonBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val personId = intent.getIntExtra(PERSON_ID, 0)

        viewModel = ViewModelProviders.of(this).get(ViewModelPerson::class.java)
        viewModel.getPersonDetails(personId)
        viewModel.getPersonCombinedCredits(personId)

        setToolbar()
        observeViewModel()
    }

    private fun setToolbar() {
        binding.toolbar.apply {
            imageToMain.setOnClickListener {
                val intent = Intent(this@PersonActivity, MainActivity::class.java)
                startActivity(intent)
            }
            searchIcn.setOnClickListener {
                startActivity(SearchActivity.createIntent(this@PersonActivity))
            }
        }
    }

    private fun observeViewModel() {
        viewModel.personDetails.observe(this, Observer { data ->
            data?.let {
                setUpPersonInfoUI(data)
                setUpActorDates(data)
                binding.progressBar.gone()
            }
        })

        viewModel.personCombinedCredits.observe(this, Observer { data ->
            data.cast?.let {
                data.cast.sortByDescending { it.popularity }
                setUpCombinedCreditsList(data.cast)
            }
        })
    }

    private fun setUpPersonInfoUI(data: PersonDetailsResponse) {
        data.name?.let {
            binding.toolbar.toolbarTitle.text = it
            binding.actorName.text = it
        }

        if (!data.knownForDepartment.isNullOrEmpty()) {
            binding.knowingLabel.text = getString(R.string.known_for).replace("{x}", data.knownForDepartment)
            binding.knowingLabel.show()
        }

        data.profilePath?.let {
            getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(it), binding.actorImage)
        }

        data.gender?.let {
            when (it) {
                1 -> binding.gender.text = getString(R.string.gender_female)
                2 -> binding.gender.text = getString(R.string.gender_male)
                else -> binding.gender.text = getString(R.string.gender_no)
            }
        }

        data.placeOfBirth?.let {
            binding.placeOfBirth.text = it
        }

        if (!data.biography.isNullOrEmpty()) {
            binding.biographyContent.text = data.biography
            binding.biographyLayout.setOnClickListener {
                startActivity(BiographyActivity.createIntent(this, data.biography, data.name.orEmpty()))
            }
            binding.biographyLayout.show()
        }
    }

    private fun setUpActorDates(data: PersonDetailsResponse) {
        val currentDate = Date()
        val sdf = SimpleDateFormat(INPUT_DATE_FORMAT, Locale.UK)
        data.birthday?.let {
            if (data.deathday.isNullOrEmpty()) {
                val date = sdf.parse(it)
                val age = getDiffYears(date, currentDate)
                binding.born.text = getString(R.string.person_single_born)
                    .replace("{DATE}", changeDateFormat(it))
                    .replace("{AGE}", age.toString())
            } else {
                val dateBorn = sdf.parse(it)
                val dateDied = sdf.parse(data.deathday)
                val age = getDiffYears(dateBorn, dateDied)
                binding.born.text = getString(R.string.person_born).replace("{DATE}", it)
                binding.deathday.text = getString(R.string.person_death)
                    .replace("{DEATH_DATE}", changeDateFormat(data.deathday))
                    .replace("{AGE}", age.toString())
                binding.deathday.show()
            }
        }
    }

    private fun getDiffYears(first: Date, last: Date): Int {
        val a = getCalendar(first)
        val b = getCalendar(last)
        var diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR)
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) || a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE)) {
            diff--
        }
        return diff
    }

    private fun getCalendar(date: Date): Calendar {
        val cal = Calendar.getInstance(Locale.UK)
        cal.time = date
        return cal
    }

    private fun setUpCombinedCreditsList(data: MutableList<PersonCast>) {
        binding.actorMovies.removeAllViews()
        if (data.isNotEmpty()) {
            for (result in data) {
                val item = ThumbnailActorsMovieBinding.inflate(layoutInflater)
                getImageTopRadius(this, IMAGE_SMALL_BASE_URL.plus(result.posterPath), item.movieImg)
                item.actorCharacter.text = result.character
                item.movieName.text = result.title ?: result.name
                result.voteAverage?.let {
                    item.movieRate.text = it.toString()
                }
                if (result.mediaType == MEDIA_MOVIE) {
                    item.movieImg.setOnClickListener {
                        startActivity(MovieActivity.createIntent(this, result.id))
                    }
                } else if (result.mediaType == MEDIA_TV_SHOW) {
                    item.root.setOnClickListener {
                        startActivity(TvShowActivity.createIntent(this, result.id))
                    }
                }
                item.root.updatePadding(left = 20, right = 20, bottom = 20)
                binding.actorMovies.addView(item.root)
            }
            binding.filmographyContainer.show()
        }
    }
}
