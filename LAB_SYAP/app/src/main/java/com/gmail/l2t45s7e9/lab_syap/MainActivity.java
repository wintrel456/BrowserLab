package com.gmail.l2t45s7e9.lab_syap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> arrayList = new ArrayList<String>();
    private WebView webView;
    private LinearLayout linearLayout;
    private ListView listView;
    private Button goForward;
    private Button reload;
    private Button search;
    private Button settings;
    private Button home;
    private Button setHomePageButton;
    private Button setBookmark;
    private Button showBookmarks;
    private EditText urlRequest;
    private ProgressBar progressBar;

    private View.OnClickListener goForwardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            goForward();
        }
    };

    private View.OnClickListener reloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            reload();
        }
    };

    private View.OnClickListener settingsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (linearLayout.getVisibility() == View.INVISIBLE) {
                linearLayout.setVisibility(View.VISIBLE);
                search.setVisibility(View.INVISIBLE);
                urlRequest.setVisibility(View.INVISIBLE);
                home.setVisibility(View.INVISIBLE);
            } else {
                linearLayout.setVisibility(View.INVISIBLE);
                search.setVisibility(View.VISIBLE);
                urlRequest.setVisibility(View.VISIBLE);
                urlRequest.setText(webView.getUrl());
                home.setVisibility(View.VISIBLE);
            }

        }
    };

    private View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            urlRequest.selectAll();
            String url = urlRequest.getText().toString();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            webView.loadUrl(url);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(webView.getWindowToken(), 0);
        }
    };

    private View.OnClickListener setHomePageListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SQLiteDatabase db = getBaseContext().openOrCreateDatabase("homePage.db", MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS homePage (name TEXT)");
            ContentValues cv = new ContentValues();
            try (Cursor query = db.rawQuery("SELECT name FROM homePage;", null)) {
                query.moveToFirst();
                while (!query.isAfterLast()) {
                    if (query.getColumnCount() > 0) {
                        cv.put("name", webView.getOriginalUrl());
                        db.update("homePage", cv, null, null);
                    } else {
                        cv.put("name", webView.getOriginalUrl());
                        db.insert("homePage", null, cv);
                    }
                    query.moveToNext();
                }
            }
            db.close();
            Toast.makeText(MainActivity.this, "Даный сайт установлен в качестве домашней страницы", Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener loadHomeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            loadHomePage();
        }
    };

    private View.OnClickListener setBookmarkListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SQLiteDatabase bmDb = getBaseContext().openOrCreateDatabase("bmDb.db", MODE_PRIVATE, null);
            bmDb.execSQL("CREATE TABLE IF NOT EXISTS bookmarks (name TEXT)");
            ContentValues cv = new ContentValues();
            cv.put("name", webView.getOriginalUrl());
            bmDb.insert("bookmarks", null, cv);
            bmDb.close();
            Toast.makeText(MainActivity.this, "Даный сайт добавлен в закладки", Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener showBookmarksListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (listView.getVisibility() == View.INVISIBLE) {
                listView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.INVISIBLE);
                getBookmarksList();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                            long id) {
                        goToPageFromBookmarksList((TextView) itemClicked);
                    }
                });
            } else {
                listView.setVisibility(View.INVISIBLE);
                webView.setVisibility(View.VISIBLE);
                arrayList.clear();
            }
        }
    };

    public void initializeVariables() {
        webView = findViewById(R.id.webView);
        goForward = findViewById(R.id.goForward);
        reload = findViewById(R.id.reloadButton);
        search = findViewById(R.id.searchButton);
        urlRequest = findViewById(R.id.editSearchRequest);
        progressBar = findViewById(R.id.progressBar);
        linearLayout = findViewById(R.id.settingsView);
        settings = findViewById(R.id.settings);
        home = findViewById(R.id.homePage);
        setHomePageButton = findViewById(R.id.setHomePage);
        setBookmark = findViewById(R.id.setBookmark);
        showBookmarks = findViewById(R.id.showBookmarks);
        listView = findViewById(R.id.listView);
    }

    public void initializeListeners() {
        goForward.setOnClickListener(goForwardListener);
        reload.setOnClickListener(reloadListener);
        search.setOnClickListener(searchListener);
        settings.setOnClickListener(settingsListener);
        setHomePageButton.setOnClickListener(setHomePageListener);
        home.setOnClickListener(loadHomeListener);
        showBookmarks.setOnClickListener(showBookmarksListener);
        setBookmark.setOnClickListener(setBookmarkListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_frame);
        initializeVariables();
        initializeListeners();
        setProgressBar();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        firstLaunch();
        webView.setWebViewClient(new Web(urlRequest));
    }

    public void setProgressBar() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100)
                    progressBar.setVisibility(View.GONE);
                else
                    progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        } else {
            listView.setVisibility(View.INVISIBLE);
            webView.setVisibility(View.VISIBLE);
            arrayList.clear();
        }
        urlRequest.setText(webView.getOriginalUrl());
    }

    public void goForward() {
        webView.goForward();
        urlRequest.setText(webView.getOriginalUrl());
    }

    public void reload() {
        webView.reload();
    }

    public void loadHomePage() {
        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("homePage.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS homePage (name TEXT)");
        Cursor query = db.rawQuery("SELECT name FROM homePage;", null);
        if (query.moveToFirst()) {
            do {
                if (query.getColumnCount() > 0) {
                    webView.loadUrl(query.getString(0));
                }
            }
            while (query.moveToNext());
        }
        query.close();
        db.close();
        urlRequest.setText(webView.getUrl());
    }

    public void firstLaunch() {
        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("homePage.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS homePage (name TEXT)");
        Cursor query = db.rawQuery("SELECT name FROM homePage;", null);
        if (query.moveToFirst()) {
            do {
                if (query.getColumnCount() > 0) {
                    webView.loadUrl(query.getString(0));
                } else {
                    db.execSQL("INSERT INTO homePage VALUES ('http://www.google.com');");
                    webView.loadUrl("http://www.google.com");
                }
            }
            while (query.moveToNext());
        }
        query.close();
        db.close();
        urlRequest.setText(webView.getUrl());
    }

    public void getBookmarksList() {
        SQLiteDatabase bmDb = getBaseContext().openOrCreateDatabase("bmDb.db", MODE_PRIVATE, null);
        bmDb.execSQL("CREATE TABLE IF NOT EXISTS bookmarks (name TEXT)");
        Cursor query = bmDb.rawQuery("SELECT name FROM bookmarks;", null);
        if (query.moveToFirst()) {
            do {
                arrayList.add(query.getString(0));
            }
            while (query.moveToNext());
        }
        query.close();
        bmDb.close();
    }

    public void goToPageFromBookmarksList(TextView itemClicked) {
        listView.setVisibility(View.INVISIBLE);
        webView.setVisibility(View.VISIBLE);
        arrayList.clear();
        webView.loadUrl((String) itemClicked.getText());
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        linearLayout = null;
        listView = null;
        goForward = null;
        reload = null;
        search = null;
        settings = null;
        home = null;
        setHomePageButton = null;
        setBookmark = null;
        showBookmarks = null;
        urlRequest = null;
        progressBar = null;
        super.onDestroy();
    }

}

