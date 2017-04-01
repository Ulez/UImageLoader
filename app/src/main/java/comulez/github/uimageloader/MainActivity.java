package comulez.github.uimageloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView gridView;
    private ArrayList<String> urls = new ArrayList<>();
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (ListView) findViewById(R.id.gv);
        Gson gson = new Gson();
        Bean bean = gson.fromJson(Testjson.json, Bean.class);
        int i = 0;
        for (Bean.ResultsBean resultsBean : bean.getResults()) {
            urls.add(resultsBean.getUrl());
//            Log.e("lcy", i + ":" + resultsBean.getUrl());
            i++;
        }
        adapter = new MyAdapter(this, urls);
        gridView.setAdapter(adapter);
    }
}
