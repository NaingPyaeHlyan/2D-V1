package mm.com.fairway.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;
import mm.com.fairway.R;

public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private Runnable runnable;
    private TextView txtDate, txtSet, txtValue, txtLiveData;
    private String url = "https://marketdata.set.or.th/mkt/marketsummary.do?language=en&country=TH";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtDate = (TextView)findViewById(R.id.txtDate);
        txtSet = (TextView)findViewById(R.id.txtSet);
        txtValue = (TextView)findViewById(R.id.txtValue);
        txtLiveData = (TextView)findViewById(R.id.txtLiveData);
        String date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        txtDate.setText(date);

        checkDataEveryMinute();
    }
    public void checkDataEveryMinute() {
        handler.postDelayed(run, 2000);
    }
    Runnable run = new Runnable() {
        @Override
        public void run() {
            if (ConnectivityHelper.isConnectedToNetwork(getApplicationContext())){
                new Loading().execute(url);
                checkDataEveryMinute();
            }else {
                Toast.makeText(getApplicationContext(),"No Internet Connection.\nTry again!",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                finish();
            }
        }
    };

    class Loading extends AsyncTask<String, Void, ArrayList<String>>{
        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            ArrayList<String> arrayList = new ArrayList<>();
            System.out.println("Load");
            try {
                Document document = Jsoup.connect(strings[0]).get();
                Elements table = document.getElementsByClass("table-info");
                Elements tr  = table.get(0).select("tr");
                Elements td = tr.get(1).select("td");
                for (Element cell : td){
                    arrayList.add(cell.text());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return arrayList;
        }
        @Override
        protected void onPostExecute(ArrayList<String> arrayList) {
            // at 4:00 Pm getLas has change to "-"
            if (arrayList.get(1).equals("-"))return;

            super.onPostExecute(arrayList);
            txtSet.setText(arrayList.get(1));
            txtValue.setText(arrayList.get(7));
            txtLiveData.setText(getPrefix(arrayList.get(1)) + getLast(arrayList.get(7)));
        }
    }
    private String getLast(String prefix){
        String a = prefix.replaceAll(",","");
        int index = a.indexOf('.');
        int strLength = index - 1;
        return a.substring(strLength, index);
    }
    private String getPrefix(String last){
        String a = last.replaceAll(",","");
        int lastIndex = a.length() - 1;
        int strLength = a.length();
        return a.substring(lastIndex, strLength);
    }
    @Override
    public void onBackPressed(){
        moveTaskToBack(true);finish();
        handler.removeCallbacks(runnable);
    }
}