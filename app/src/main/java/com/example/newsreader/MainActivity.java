package com.example.newsreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    static List<String> titleList= new ArrayList<>();
    static List<String> urlList = new ArrayList<>();
    JSONArray jsonArray = null;
    ListView listView;
    static ArrayAdapter<String> arrayAdapter;
    String idJson;
    static int index=0;
    static Boolean isFirstTime=true,flag_loading=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titleList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    LayoutParams params = view.getLayoutParams();
                    params.height = 170;
                    view.setLayoutParams(params);
                    return view;
                }
            };
        listView.setAdapter(arrayAdapter);
        if(isFirstTime) {
            try {
                new GetIDData().execute().get();
                jsonArray = new JSONArray(idJson.substring(idJson.indexOf('['), idJson.lastIndexOf(']') + 1));
                new GetListData().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("isFirstTime is useless I guess "+isFirstTime);
            isFirstTime=false;
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("Position",position);
                startActivity(intent);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                {
                    if(flag_loading == false)
                    {
                        flag_loading = true;
                        Toast.makeText(MainActivity.this, "Loading more articles", Toast.LENGTH_SHORT).show();
                        new GetListData().execute();
                    }
                }
            }
        });
    }

    class GetIDData extends AsyncTask {

        @SuppressLint("WrongThread")
        @Override
        protected Object doInBackground(Object... objects) {
            try {
                idJson = Jsoup.connect("https://hacker-news.firebaseio.com/v0/beststories.json?print=pretty").ignoreContentType(true).get().html();
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    class GetListData extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            for(int i=index;i<index+10;i++) {
                try {
                    String id = jsonArray.getString(i);
                    String html = Jsoup.connect("https://hacker-news.firebaseio.com/v0/item/"+id+".json?print=pretty").ignoreContentType(true).get().html();
                    final JSONObject jsonObject = new JSONObject(html.substring(html.indexOf('{'),html.lastIndexOf('}')+1));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                titleList.add(jsonObject.getString("title"));
                                urlList.add(jsonObject.getString("url"));
                                arrayAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //index+=10;
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            flag_loading=false;
            index+=10;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
