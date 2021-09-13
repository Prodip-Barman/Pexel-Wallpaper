package com.example.pexelwallpaper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    WallpaperAdapter wallpaperAdapter;
    List<WallpaperModel> wallpaperModelList;
    int pageNumber = 1;
    String url ="https://api.pexels.com/v1/curated/?page="+pageNumber+"&per_page=80";

    Boolean isScrolling = false;
    int currentItems, totalItems, scrollOutItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        wallpaperModelList = new ArrayList<>();
        wallpaperAdapter = new WallpaperAdapter(this, wallpaperModelList);

        recyclerView.setAdapter(wallpaperAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                currentItems = gridLayoutManager.getChildCount();
                totalItems = gridLayoutManager.getItemCount();
                scrollOutItems = gridLayoutManager.findFirstVisibleItemPosition();

                if (isScrolling && (currentItems + scrollOutItems == totalItems)) {
                    isScrolling = false;
                    fetchWallpaper();
                }
            }
        });

        fetchWallpaper();
    }

    public void fetchWallpaper() {

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        JSONArray jsonArray = jsonObject.getJSONArray("photos");
                        int length = jsonArray.length();

                        for (int i = 0; i < length; i++) {

                            JSONObject object = jsonArray.getJSONObject(i);

                            int id = object.getInt("id");

                            JSONObject objectImages = object.getJSONObject("src");

                            String originalUri = objectImages.getString("original");
                            String mediumUri = objectImages.getString("medium");

                            WallpaperModel wallpaperModel = new WallpaperModel(id, originalUri, mediumUri);
                            wallpaperModelList.add(wallpaperModel);
                        }

                        wallpaperAdapter.notifyDataSetChanged();
                        pageNumber++;

                    } catch (JSONException e) {
                        // Here we Write jsonException
                    }
                }, error -> {
                    // Here we write Volley error Message
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "563492ad6f917000010000011069c623c7ad4919b7f934771d8adbe9");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_search) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText editText = new EditText(this);
            editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            alert.setMessage("Enter Category e.g. Nature");
            alert.setTitle("Search Wallpaper");

            alert.setView(editText);

            alert.setPositiveButton("Yes", (dialog, which) -> {
                String query = editText.getText().toString().toLowerCase();

                url = "https://api.pexels.com/v1/search/?page="+pageNumber+"&per_page=80&query="+query;
                wallpaperModelList.clear();
                fetchWallpaper();
            });
            alert.setNegativeButton("No", (dialog, which) -> {

            });
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }
}