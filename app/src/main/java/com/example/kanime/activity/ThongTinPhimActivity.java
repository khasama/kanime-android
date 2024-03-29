package com.example.kanime.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.kanime.R;
import com.example.kanime.adapter.CommentAdapter;
import com.example.kanime.adapter.EpisodeAdapter;
import com.example.kanime.adapter.MenuLeftAdapter;
import com.example.kanime.adapter.PhimMoiAdapter;
import com.example.kanime.adapter.SeasonAdapter;
import com.example.kanime.model.Comment;
import com.example.kanime.model.Episode;
import com.example.kanime.model.MenuLeft;
import com.example.kanime.model.PhimMoi;
import com.example.kanime.model.Season;
import com.example.kanime.server.Url;
import com.example.kanime.ultil.CheckConnect;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThongTinPhimActivity extends AppCompatActivity {

    NavigationView nvMenu;
    ListView lvMenu;
    DrawerLayout drlayout;
    ImageButton btnopenmenu, btnsearch;
    ArrayList<MenuLeft> arrayMenulef;
    MenuLeftAdapter menuLeftAdapter;
    TextView tvTenPhim, tvTenKhac, tvTrangThai, tvTheLoai, tvNam, tvLuotXem;
    RecyclerView rvListEp, rvListSeason;
    ImageView ivAnhPhim;
    ArrayList<Episode> arrayEpisode;
    EpisodeAdapter episodeAdapter;
    ArrayList<Season> arraySeason;
    SeasonAdapter seasonAdapter;
    WebView wbNoiDung;

    LinearLayout llCmt;
    SharedPreferences sharedPreferences;

    ArrayList<Comment> arrayCmt;
    CommentAdapter commentAdapter;
    RecyclerView rvListCmt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_phim);
        initPreferences();
        anhXa();
        if(CheckConnect.haveNetworkConnection(getApplicationContext())){
            GetMenuLeft();
            GetInfo();
            MenuClick();
        }else{
            CheckConnect.Thongbao(getApplicationContext(), "Kiểm tra lại kết nối!!!");
            finish();
        }
    }

    private void checkLogin(String idPhim) {
        String savedData = sharedPreferences.getString("idUser", "");
        if(savedData.length() == 0 ){

        }else{
            addComment(idPhim, savedData);
        }
    }

    private void initPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void GetInfo() {
        Bundle bd = getIntent().getExtras();
        if( bd != null){
            String idPhim = bd.getString("idPhim");
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Url.urlPhim + idPhim, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    if(response != null){
                        int ID = 0;
                        String TenPhim = "";
                        String TenKhac = "";
                        String TrangThai = "";
                        String TheLoai = "";
                        String Nam = "";
                        String LuotXem = "";
                        String Anhbia = "";
                        String Server = "";
                        String Mota = "";
                        for(int i = 0; i < response.length(); i++){
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                ID = jsonObject.getInt("id");
                                TenPhim = jsonObject.getString("name");
                                TenKhac = jsonObject.getString("oname");
                                TheLoai = jsonObject.getString("theloai");
                                TrangThai = jsonObject.getString("trangthai");
                                Nam = jsonObject.getString("year");
                                LuotXem = jsonObject.getString("view");
                                Anhbia = jsonObject.getString("bia");
                                Server = jsonObject.getString("server");
                                Mota = jsonObject.getString("mota");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Picasso.get().load(Anhbia).into(ivAnhPhim);
                        tvTenPhim.setText(TenPhim);
                        tvTenKhac.setText(TenKhac);
                        tvTrangThai.setText(TrangThai);
                        tvTheLoai.setText(TheLoai);
                        tvNam.setText(Nam);
                        tvLuotXem.setText(LuotXem);
                        GetEpisode(idPhim, Server);
                        GetSeason(idPhim);
                        GetNoiDung(idPhim);
                        getComment(idPhim);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("aaa", error.toString());
                }
            });
            requestQueue.add(jsonArrayRequest);
            checkLogin(idPhim);
        }
    }

    private void GetEpisode(String idPhim, String idServer) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Url.GetAllEp(idPhim, idServer), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response != null){
                    String idEp = "";
                    String Episode = "";
                    for(int i = 0; i < response.length(); i++){
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            idEp = jsonObject.getString("idep");
                            Episode = jsonObject.getString("episode");
                            arrayEpisode.add(new Episode(idEp, Episode));
                            episodeAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("aaa", error.toString());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    private void GetSeason(String idPhim) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Url.urlSeason + idPhim, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response != null){
                    String idPhim = "";
                    String Season = "";
                    for(int i = 0; i < response.length(); i++){
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            idPhim = jsonObject.getString("id");
                            Season = jsonObject.getString("season");
                            arraySeason.add(new Season(idPhim, Season));
                            seasonAdapter.notifyDataSetChanged();
                            Log.e("eee", jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("aaa", error.toString());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    private void GetNoiDung(String idPhim) {
        WebSettings webSettings = wbNoiDung.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wbNoiDung.loadUrl(Url.urlMota + idPhim);
    }

    private void GetMenuLeft() {
        arrayMenulef.add(new MenuLeft("Trang Chủ", "https://img.icons8.com/ultraviolet/40/4a90e2/home.png"));
        arrayMenulef.add(new MenuLeft("Thể Loại", "https://img.icons8.com/ultraviolet/40/4a90e2/movie.png"));
        arrayMenulef.add(new MenuLeft("Trạng Thái", "https://img.icons8.com/ultraviolet/40/4a90e2/movie.png"));
        arrayMenulef.add(new MenuLeft("Năm Phát Hành", "https://img.icons8.com/ultraviolet/40/4a90e2/movie.png"));
        arrayMenulef.add(new MenuLeft("Thông Tin", "https://img.icons8.com/ultraviolet/40/4a90e2/gender-neutral-user.png"));
        Log.e("aaa", "3");
    }

    private void anhXa() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        lvMenu = findViewById(R.id.listviewMenu);
        nvMenu = findViewById(R.id.nvMenu);
        drlayout = findViewById(R.id.drawerlayout);
        btnopenmenu = findViewById(R.id.btnOpenMenu);
        btnsearch = findViewById(R.id.btnSearch);
        arrayMenulef = new ArrayList<>();
        menuLeftAdapter = new MenuLeftAdapter(arrayMenulef, getApplicationContext());
        lvMenu.setAdapter(menuLeftAdapter);
        tvTenPhim = findViewById(R.id.tvTenPhim);
        tvTenKhac = findViewById(R.id.tvTenKhac);
        tvTrangThai = findViewById(R.id.tvTrangThai);
        tvTheLoai = findViewById(R.id.tvTheLoai);
        tvNam = findViewById(R.id.tvNam);
        tvLuotXem = findViewById(R.id.tvLuotXem);
        rvListEp = findViewById(R.id.rvListEp);
        rvListSeason = findViewById(R.id.rvListSeason);
        ivAnhPhim = findViewById(R.id.ivAnhPhim);
        arrayEpisode = new ArrayList<>();
        episodeAdapter = new EpisodeAdapter(getApplicationContext(), arrayEpisode);
        rvListEp.setHasFixedSize(true);
        rvListEp.setAdapter(episodeAdapter);
        rvListEp.setLayoutManager(new GridLayoutManager(getApplicationContext(), 6));
        arraySeason = new ArrayList<>();
        seasonAdapter = new SeasonAdapter(getApplicationContext(), arraySeason);
        rvListSeason.setHasFixedSize(true);
        rvListSeason.setAdapter(seasonAdapter);
        rvListSeason.setLayoutManager(layoutManager);
        wbNoiDung = findViewById(R.id.wvNoiDung);

        llCmt = findViewById(R.id.llCmt);
        arrayCmt = new ArrayList<>();
        commentAdapter = new CommentAdapter(getApplicationContext(), arrayCmt);
        rvListCmt = findViewById(R.id.rvListCmt);
        rvListCmt.setHasFixedSize(true);
        rvListCmt.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
    }

    public void getComment(String idMovie){
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Url.urlShowCmt + idMovie, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response != null){
                    commentAdapter.clear();
                    String idCmt = "";
                    String userName = "";
                    String content = "";
                    String date = "";
                    String img = "";
                    for(int i = 0; i < response.length(); i++){
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            idCmt = jsonObject.getString("id-cmt");
                            userName = jsonObject.getString("user-name");
                            content = jsonObject.getString("content");
                            date = jsonObject.getString("date");
                            img = jsonObject.getString("img");
                            arrayCmt.add(new Comment(idCmt, userName, content, date, img));
                            commentAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    rvListCmt.setAdapter(commentAdapter);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("aaaaaa", error.toString());
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    public void openMenu(View view){
        drlayout.openDrawer(GravityCompat.START);
    }

    public void goHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivities(new Intent[]{intent});
    }

    public void Search(View view){
        Intent intent = new Intent(this, SearchActivity.class);
        startActivities(new Intent[]{intent});
    }

    public void addComment(String idPhim, String idUser) {
        String idMovie = idPhim;
        String idU = idUser;
        View view = getLayoutInflater().inflate(R.layout.component_cmt, null);

        EditText etCmt = view.findViewById(R.id.etCmt);
        ImageButton ibSendCmt = view.findViewById(R.id.ibSendCmt);

        ibSendCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmt = etCmt.getText().toString().trim();
                String idPhim = idMovie;
                String idUser = idU;
                if(!cmt.isEmpty()){
                    sendComment(cmt, idPhim, idUser);
                    etCmt.setText("");
                }else {
                    Toast.makeText(ThongTinPhimActivity.this, "Vui lòng nhập đầy đủ !!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        llCmt.addView(view);
    }

    public void sendComment(String cmt, String idPhim, String idUser) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Url.sendComment, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.contains("success")){
                    getComment(idPhim);
                }else{
                    Toast.makeText(getApplicationContext(), "Đã xảy ra lỗi vui lòng thử lại sau !!!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Đã có lỗi xảy ra !!!", Toast.LENGTH_SHORT).show();
            }
        }){
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> data = new HashMap<>();
                data.put("cmt", cmt);
                data.put("idUser", idUser);
                data.put("idPhim", idPhim);
                return data;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void MenuClick() {

        lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                switch (position){
                    case 0:
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivities(new Intent[]{intent});
                        break;
                    case 1:
                        drlayout.closeDrawer(GravityCompat.START);
                        break;
                    case 2:
                        drlayout.closeDrawer(GravityCompat.START);
                        break;
                    case 3:
                        drlayout.closeDrawer(GravityCompat.START);
                        break;
                    case 4:
                        String savedData = sharedPreferences.getString("idUser", "");
                        if(savedData.length() == 0 ){
                            intent = new Intent(getApplicationContext(), LogRegActivity.class);
                            startActivities(new Intent[]{intent});
                        }else{
                            intent = new Intent(getApplicationContext(), UserActivity.class);
                            intent.putExtra("idUser", savedData);
                            startActivities(new Intent[]{intent});
                        }
                        break;
                }
            }
        });
    }

}