package com.evangelidis.t_tmoviesseries.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.extensions.gone
import com.evangelidis.t_tmoviesseries.extensions.show
import com.evangelidis.t_tmoviesseries.model.PersonCombinedResponse
import com.evangelidis.t_tmoviesseries.model.PersonDetailsResponse
import com.evangelidis.t_tmoviesseries.utils.Constants.ACTOR_IMAGE_URL
import com.evangelidis.t_tmoviesseries.utils.Constants.ACTOR_NAME
import com.evangelidis.t_tmoviesseries.utils.Constants.BIOGRAPHY_TEXT
import com.evangelidis.t_tmoviesseries.utils.Constants.INPUT_DATE_FORMAT
import com.evangelidis.t_tmoviesseries.utils.Constants.MOVIE_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.PERSON_ID
import com.evangelidis.t_tmoviesseries.utils.Constants.TV_SHOW_ID
import com.evangelidis.t_tmoviesseries.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.activity_person.*
import kotlinx.android.synthetic.main.main_toolbar.*
import java.text.SimpleDateFormat
import java.util.*

class PersonActivity : AppCompatActivity() {

    private var personId: Int = 0
    lateinit var viewModel: ListViewModel
    private val sdf = SimpleDateFormat(INPUT_DATE_FORMAT, Locale.UK)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person)

        personId = intent.getIntExtra(PERSON_ID, personId)

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)

        viewModel.getPersonDetails(personId)
        viewModel.getPersonCombinedCredits(personId)

        imageToMain.setOnClickListener {
            val intent = Intent(this@PersonActivity, MainActivity::class.java)
            startActivity(intent)
        }

        search_img.setOnClickListener {
            val intent = Intent(this@PersonActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.personDetails.observe(this, Observer { data ->
            data?.let {
                setUpPersonInfoUI(data)
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
            toolbar_title.text = it
            actorName.text = it
        }

        data.knownForDepartment?.let {
            knowingLabel.text = it
            knowingContainer.show()
        }

        data.profilePath?.let {
            Glide.with(this)
                .load(ACTOR_IMAGE_URL + it)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(actorImage)
        }

        data.gender?.let {
            when (it) {
                1 -> gender.text = getString(R.string.gender_female)
                2 -> gender.text = getString(R.string.gender_male)
                else -> gender.text = getString(R.string.gender_no)
            }
        }

        data.placeOfBirth?.let {
            placeOfBirth.text = it
        }

        data.biography?.let {
            biographyContent.text = it
            biographyLayout.setOnClickListener {
                val intent = Intent(this@PersonActivity, BiographyActivity::class.java)
                intent.putExtra(BIOGRAPHY_TEXT, data.biography)
                intent.putExtra(ACTOR_NAME, data.name)
                startActivity(intent)
            }
        }
        setUpActorDates(data)
    }

    private fun setUpActorDates(data: PersonDetailsResponse) {
        val currentDate = Date()

        data.birthday?.let {
            if (data.deathday.isNullOrEmpty()) {
                val date = sdf.parse(it)
                val age = getDiffYears(date, currentDate)
                born.text = getString(R.string.person_single_born)
                    .replace("{DATE}", it)
                    .replace("{AGE}", age.toString())
            } else {
                val dateBorn = sdf.parse(it)
                val dateDied = sdf.parse(data.deathday)
                val age = getDiffYears(dateBorn, dateDied)
                born.text = getString(R.string.person_born).replace("{DATE}", it)
                deathday.text = getString(R.string.person_death)
                    .replace("{DEATH_DATE}", data.deathday)
                    .replace("{AGE}", age.toString())
                deathday.show()
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
        actorMovies.removeAllViews()
        data.cast?.let {
            filmographyContainer.show()

            for (result in it) {
                val parent = layoutInflater.inflate(R.layout.thumbnail_actors_movie, actorMovies, false)
                val movieImg = parent.findViewById<ImageView>(R.id.movie_img)
                val movieName = parent.findViewById<TextView>(R.id.movie_name)
                val actorCharacter = parent.findViewById<TextView>(R.id.actor_character)
                val movieYear = parent.findViewById<TextView>(R.id.movie_year)

                if (result.mediaType == "movie") {
                    movieName.text = result.title
                    actorCharacter.text = result.character
                    movieImg.setOnClickListener {
                        val intent = Intent(this@PersonActivity, MovieActivity::class.java)
                        intent.putExtra(MOVIE_ID, result.id)
                        startActivity(intent)
                    }
                } else if (result.mediaType == "tv") {
                    movieName.text = result.name
                    actorCharacter.gone()
                    parent.setOnClickListener {
                        val intent = Intent(this@PersonActivity, TvShowActivity::class.java)
                        intent.putExtra(TV_SHOW_ID, result.id)
                        startActivity(intent)
                    }
                }

                if (!(result.releaseDate.isNullOrEmpty())) {
                    movieYear.show()
                    movieYear.text = result.releaseDate.substring(0, 4)
                }

                Glide.with(this@PersonActivity)
                    .load(ACTOR_IMAGE_URL + result.posterPath)
                    .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                    .into(movieImg)

                actorMovies.addView(parent)
            }
        }
    }
}