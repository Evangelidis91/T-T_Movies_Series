package com.evangelidis.t_tmoviesseries.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.evangelidis.t_tmoviesseries.R
import com.evangelidis.t_tmoviesseries.model.PersonCombinedResponse
import com.evangelidis.t_tmoviesseries.model.PersonDetailsResponse
import com.evangelidis.t_tmoviesseries.utils.Constants.ACTOR_IMAGE_URL
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
    private val sdf = SimpleDateFormat("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person)

        personId = intent.getIntExtra(PERSON_ID, personId)

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)

        viewModel.getPersonDetails(personId)
        viewModel.getPersonCombinedCredits(personId)

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
            knowingContainer.visibility = View.VISIBLE
        }

        data.profilePath?.let {
            Glide.with(this@PersonActivity)
                .load(ACTOR_IMAGE_URL + it)
                .apply(RequestOptions.placeholderOf(R.color.colorPrimary))
                .into(actorImage)
        }

        data.gender?.let {
            when(it){
                1 -> gender.text = "Gender: Female"
                2 -> gender.text = "Gender: Male"
                else -> gender.text = "Gender: Not specified"
            }
        }

        data.placeOfBirth?.let {
            placeOfBirth.text = it
        }

        data.biography?.let {
            biographyContent.text = it
        }

        setUpActorDates(data)
    }

    private fun setUpActorDates(data: PersonDetailsResponse) {
        val currentDate = Date()

        data.birthday?.let {
            if (data.deathday.isNullOrEmpty()){
                val date = sdf.parse(it)
                val age = getDiffYears(date, currentDate)
                born.text = "Born: " + it + " (age " + age + ")"
            } else{
                deathday.visibility = View.VISIBLE
                //born.text = response.birthday
                val dateBorn = sdf.parse(it)
                val dateDied = sdf.parse(data.deathday)
                val age = getDiffYears(dateBorn, dateDied)
                born.text = "Born: " + it
                deathday.text = "Died: " + data.deathday + " (age " + age + ")"
            }
        }

    }

    private fun getDiffYears(first: Date, last: Date): Int {
        val a = getCalendar(first)
        val b = getCalendar(last)
        var diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR)
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) || a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(
                Calendar.DATE
            ) > b.get(Calendar.DATE)) {
            diff--
        }
        return diff
    }

    private fun getCalendar(date: Date): Calendar {
        val cal = Calendar.getInstance(Locale.US)
        cal.time = date
        return cal
    }

    private fun setUpCombinedCreditsList(data: PersonCombinedResponse) {

        actorMovies.removeAllViews()
        data.cast?.let {
            filmographyContainer.visibility = View.VISIBLE

            for (result in it) {
                val parent =
                    layoutInflater.inflate(R.layout.thumbnail_actors_movie, actorMovies, false)
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
                } else if (result.mediaType == "tv"){
                    movieName.text = result.name
                    actorCharacter.visibility = View.GONE
                    parent.setOnClickListener {
                        val intent = Intent(this@PersonActivity, TvShowActivity::class.java)
                        intent.putExtra(TV_SHOW_ID, result.id)
                        startActivity(intent)
                    }
                }

                if (!(result.releaseDate.isNullOrEmpty())) {
                    movieYear.visibility = View.VISIBLE
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