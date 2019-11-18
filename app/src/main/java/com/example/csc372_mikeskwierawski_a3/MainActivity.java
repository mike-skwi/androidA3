package com.example.csc372_mikeskwierawski_a3;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ArrayList<Stock> stockArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StockListAdapter stockListAdapter;
    public NameDownloaderAsyncTask ndat = new NameDownloaderAsyncTask(MainActivity.this);
//  new NameDownloaderAsyncTask(this).execute(doRead());
    private String m_Text = "";

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
                doRead();
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
            Toast.makeText(this,"Error", LENGTH_SHORT).show();
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
            doWrite(this);

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.addButton:
                Toast.makeText(this,"ayyy",LENGTH_SHORT).show();
                //TODO add what happens when you press the add button
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Stock Selection");
                builder.setMessage("Please enter a Stock Symbol");
                // Set up the input
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
//                        stockArrayList = ndat.addStockToList(m_Text);
                        ndat.execute(stockArrayList);
//                        new NameDownloaderAsyncTask(MainActivity.this).execute(stockArrayList);
                    }
                });

                builder.setNegativeButton("Return", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

        }
        return super.onOptionsItemSelected(item);
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
