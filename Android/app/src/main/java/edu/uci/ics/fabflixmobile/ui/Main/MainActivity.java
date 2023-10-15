package edu.uci.ics.fabflixmobile.ui.Main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityMainBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText searchT;
    private TextView message;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    private final String host = "54.193.183.233";
    private final String port = "8443";
    private final String domain = "s23-122b-cs122b-team-99";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        searchT = binding.searchbar;
        message = binding.message;
        final Button searchButton = binding.search;

        //assign a listener to call a function to handle the user request when clicking a button
        searchButton.setOnClickListener(view -> search());
    }

    @SuppressLint("SetTextI18n")
    public void search() {
        message.setText("Searching");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is Get
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?placeholder=&pageNum=10&fullTitle=" + Uri.encode(searchT.getText().toString()),
                response -> {
                    Log.d("login.success", response);
                    finish();
                    // initialize the activity(page)/destination
                    Intent MovieListPage = new Intent(MainActivity.this, MovieListActivity.class);
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
}