package edu.uci.ics.fabflixmobile.ui.singleMovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;
import edu.uci.ics.fabflixmobile.ui.Main.MainActivity;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;

public class SingleMovieActivity extends AppCompatActivity {

    private final String host = "54.193.183.233";
    private final String port = "8443";
    private final String domain = "s23-122b-cs122b-team-99";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    private TextView ui_title;
    private TextView ui_year;
    private TextView ui_dir;
    private TextView ui_genre;
    private TextView ui_star;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Button mainButton = binding.movielist;
        mainButton.setOnClickListener(view -> toList());

        ui_title = binding.Title;
        ui_year = binding.year;
        ui_dir = binding.director;
        ui_genre = binding.genres;
        ui_star = binding.stars;


        Intent moviedata = getIntent();
        String myString = moviedata.getStringExtra("moviedata");
        final ArrayList<Movie> movies = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(myString);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String title = jsonObject.getString("movie_title");
            String director = jsonObject.getString("movie_director");
            String year = jsonObject.getString("movie_year");
            String genre = jsonObject.getString("movie_genres");
            String stars = parseName(jsonObject.getString("movie_stars"));

            ui_title.setText(title);
            ui_year.setText("Year: "+year);
            ui_dir.setText("Director: "+director);
            ui_genre.setText("Genre: "+genre);
            ui_star.setText("Stars: "+stars);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void toList() {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is Get
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?placeholder=",
                response -> {
                    Log.d("login.success", response);
                    finish();
                    // initialize the activity(page)/destination
                    Intent MovieListPage = new Intent(SingleMovieActivity.this, MovieListActivity.class);
                    MovieListPage.putExtra("moviedata", response);
                    // activate the list page.
                    startActivity(MovieListPage);
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
        };
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);
    }

    String parseName(String names){
        String[] splitName = names.split(",");
        String result = "";
        int n = splitName.length;
        for(int i = 0; i < n; i++){
            result += splitName[i].substring(0,splitName[i].length()-10);
            if(i<(n-1)){
                result+=", ";
            }
        }
        return result;
    }
}