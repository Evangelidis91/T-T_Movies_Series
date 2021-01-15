package com.evangelidis.t_tmoviesseries.view.person

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
import com.evangelidis.t_tmoviesseries.model.PersonCombinedResponse
import com.evangelidis.t_tmoviesseries.model.PersonDetailsResponse
import com.evangelidis.t_tmoviesseries.utils.Constants.ACTOR_NAME
import com.evangelidis.t_tmoviesseries.utils.Constants.BIOGRAPHY_TEXT
import com.evangelidis.t_tmoviesseries.utils.Constants.IMAGE_SMALL_BASE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.INPUT_DATE_FORMAT
import com.evangelidis.t_tmoviesseries.utils.Constants.MOVIE_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.PERSON_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_ID
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.changeDateFormat
import com.evangelidis.t_tmoviesseries.utils.ItemsManager.getGlideImage
import com.evangelidis.t_tmoviesseries.view.biography.BiographyActivity
import com.evangelidis.t_tmoviesseries.view.main.MainActivity
import com.evangelidis.t_tmoviesseries.view.movie.MovieActivity
import com.evangelidis.t_tmoviesseries.view.search.SearchActivity
import com.evangelidis.t_tmoviesseries.view.tvshow.TvShowActivity
import java.text.SimpleDateFormat
import java.util.*

class PersonActivity : AppCompatActivity() {

    private var personId: Int = 0
    private lateinit var viewModel: ViewModelPerson

    private val binding: ActivityPersonBinding by lazy { ActivityPersonBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        personId = intent.getIntExtra(PERSON_ID, personId)

        viewModel = ViewModelProviders.of(this).get(ViewModelPerson::class.java)

        viewModel.getPersonDetails(personId)
        viewModel.getPersonCombinedCredits(personId)

        binding.toolbar.imageToMain.setOnClickListener {
            val intent = Intent(this@PersonActivity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.toolbar.searchIcn.setOnClickListener {
            val intent = Intent(this@PersonActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.personDetails.observe(this, Observer { data ->
            data?.let {
                setUpPersonInfoUI(data)
                binding.progressBar.gone()
            }
        })

        viewModel.personCombinedCredits.observe(this, Observer { data ->
            data?.let {
                setUpCombinedCreditsList(data)
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
                val intent = Intent(this@PersonActivity, BiographyActivity::class.java)
                intent.putExtra(BIOGRAPHY_TEXT, data.biography)
                intent.putExtra(ACTOR_NAME, data.name)
                startActivity(intent)
            }
            binding.biographyLayout.show()
        }
        setUpActorDates(data)
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

    private fun setUpCombinedCreditsList(data: PersonCombinedResponse) {
        binding.actorMovies.removeAllViews()
        data.cast?.let {
            if (it.isNotEmpty()) {
                for (result in it) {
                    val item = ThumbnailActorsMovieBinding.inflate(layoutInflater)
                    if (result.mediaType == "movie") {
                        item.movieName.text = result.title
                        item.actorCharacter.text = result.character
                        item.movieImg.setOnClickListener {
                            val intent = Intent(this@PersonActivity, MovieActivity::class.java)
                            intent.putExtra(MOVIE_ID, result.id)
                            startActivity(intent)
                        }
                    } else if (result.mediaType == "tv") {
                        item.movieName.text = result.name
                        item.actorCharacter.gone()
                        item.root.setOnClickListener {
                            val intent = Intent(this@PersonActivity, TvShowActivity::class.java)
                            intent.putExtra(TV_SHOW_ID, result.id)
                            startActivity(intent)
                        }
                    }

                    if (!(result.releaseDate.isNullOrEmpty())) {
                        item.movieYear.show()
                        item.movieYear.text = result.releaseDate.substring(0, 4)
                    }

                    getGlideImage(this, IMAGE_SMALL_BASE_URL.plus(result.posterPath), item.movieImg)

                    binding.actorMovies.addView(item.root)
                }
                binding.filmographyContainer.show()
            }
        }
    }
}
