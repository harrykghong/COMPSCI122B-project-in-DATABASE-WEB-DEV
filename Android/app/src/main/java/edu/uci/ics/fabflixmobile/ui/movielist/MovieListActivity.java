package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivityMainBinding;
import edu.uci.ics.fabflixmobile.ui.Main.MainActivity;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.singleMovie.SingleMovieActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;

public class MovieListActivity extends AppCompatActivity {

    private final String host = "54.193.183.233";
    private final String port = "8443";
    private final String domain = "s23-122b-cs122b-team-99";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Button mainButton = binding.main;
        mainButton.setOnClickListener(view -> toMain());
        final Button prevButton = binding.prev;
        prevButton.setOnClickListener(view -> prevP());
        final Button nextButton = binding.next;
        nextButton.setOnClickListener(view -> nextP());

        Intent moviedata = getIntent();
        String myString = moviedata.getStringExtra("moviedata");
        final ArrayList<Movie> movies = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(myString);
            JSONObject pageInfo = jsonArray.getJSONObject(0);
            int currentPage = pageInfo.getInt("page");
            int maxPage = pageInfo.getInt("maxPage");
            if(currentPage<=1){
                prevButton.setEnabled(false);
            }else{
                prevButton.setEnabled(true);
            }
            if(currentPage>=maxPage){
                nextButton.setEnabled(false);
            }else{
                nextButton.setEnabled(true);
            }

            for (int i = 1; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("movieId");
                String title = jsonObject.getString("movieName");
                String director = jsonObject.getString("movieDir");
                String year = jsonObject.getString("movieYear");
                String genre = jsonObject.getString("movieGen");
                String stars = parseName(jsonObject.getString("movieStar"));
//
                movies.add(new Movie(id,title,Short.parseShort(year),director,genre,stars));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            singleMovie(movie.getId());
        });
    }

    public void singleMovie(String id) {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is Get
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id=" + id,
                response -> {
                    Log.d("login.success", response);
                    finish();
                    // initialize the activity(page)/destination
                    Intent MovieListPage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
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

    void toMain(){
        finish();
        Intent MainPage = new Intent(MovieListActivity.this, MainActivity.class);
        startActivity(MainPage);
    }

    public void prevP() {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is Get
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?placeholder=&changePage=previous",
                response -> {
                    Log.d("login.success", response);
                    finish();
                    // initialize the activity(page)/destination
                    Intent MovieListPage = new Intent(MovieListActivity.this, MovieListActivity.class);
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

    public void nextP() {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is Get
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?placeholder=&changePage=next",
                response -> {
                    Log.d("login.success", response);
                    finish();
                    // initialize the activity(page)/destination
                    Intent MovieListPage = new Intent(MovieListActivity.this, MovieListActivity.class);
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
        int n = 0;
        if(splitName.length>=3){
            n = 3;
        }else{
            n = splitName.length;
        }
        for(int i = 0; i < n; i++){
            result += splitName[i].substring(0,splitName[i].length()-10);
            if(i<(n-1)){
                result+=", ";
            }
        }
        return result;
    }
}