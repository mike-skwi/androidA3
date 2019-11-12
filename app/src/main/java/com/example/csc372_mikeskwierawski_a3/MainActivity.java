package com.example.csc372_mikeskwierawski_a3;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ArrayList<Stock> stockArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StockListAdapter stockListAdapter;
    private NameDownloaderAsyncTask ndat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Stock Watch");

        if (!networkCheck()){
//            networkError("AH");
            //TODO network error dialog shit
        }
        else{
            if (stockArrayList.size() == 0){
                doWrite(this);
            }

            new NameDownloaderAsyncTask(this).execute(doRead());
        }
        recyclerView = findViewById(R.id.recyclerView);
        stockListAdapter = new StockListAdapter(stockArrayList,this);
        recyclerView.setAdapter(stockListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private boolean networkCheck(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null){
            return false;
        }
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            return true;
        }
        else {
            return false;
        }
    }

    public void stockResult(Stock stock) {
        if (stock == null){
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
            return;
        }
        //TODO write to json
        stockArrayList.add(stock);
        stockListAdapter.notifyDataSetChanged();
    }

    public ArrayList<Stock> doRead() {

        try {
            InputStream inputStream = openFileInput("jsonData.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();

                String jsonText = stringBuilder.toString();

                try {
                    JSONArray jsonArray = new JSONArray(jsonText);
                    Log.d(TAG, "doRead: " + jsonArray.length());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String symbol = jsonObject.getString("symbol");
                        String companyName = jsonObject.getString("companyName");
                        Double price = jsonObject.getDouble("latestPrice");
                        Double priceChange = jsonObject.getDouble("change");
                        Double changePercentage = jsonObject.getDouble("changePercent");

                        Stock n = new Stock(symbol, companyName, price, priceChange, changePercentage);
                        stockArrayList.add(n);
                    }
                    Log.d(TAG, "doRead: " + stockArrayList);
                } catch (JSONException e) {
                    //TODO write a blank file?
                    e.printStackTrace();
                }
            }
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "doRead: File not found: \" + e.toString()");
        } catch (IOException e) {
            Log.d(TAG, "doRead: Can not read file: " + e.toString());
        }
        return stockArrayList;
    }

    public void doWrite(Context t){
        JSONArray jsonArray = new JSONArray();
        for (Stock ind : stockArrayList){
            try{
                JSONObject stockJson = new JSONObject();
                stockJson.put("symbol",ind.getStockSymbol());
                stockJson.put("companyName",ind.getCompanyName());
                stockJson.put("latestPrice",ind.getPrice());
                stockJson.put("priceChange",ind.getPriceChange());
                stockJson.put("changePercentage",ind.getChangePercentage());

                jsonArray.put(stockJson);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String jsonText = jsonArray.toString();

        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(
                            openFileOutput("jsonData.txt", Context.MODE_PRIVATE)
                    );

            outputStreamWriter.write(jsonText);
            outputStreamWriter.close();
        }
        catch (IOException e) {
        }
    }


    public void addStock(Stock stock) {
        // call async stock downloader
        if(networkCheck() != false) {
//            new NameDownloaderAsyncTask(this)
            new StockDownloaderAsyncTask(this).execute(stock.getStockSymbol());
            //TODO check to make sure that this isnt a duplicated stock
        }
        else{
            //TODO new dialog no network connection
        }
    }
}
