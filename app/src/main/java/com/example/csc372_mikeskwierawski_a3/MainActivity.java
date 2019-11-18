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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ArrayList<Stock> stockArrayList = new ArrayList<>();
    private Set<String> stockHashSet = new HashSet<>();
    private RecyclerView recyclerView;
    private StockListAdapter stockListAdapter;
    private String m_Text = "";
    private EditText input;
    private SwipeRefreshLayout srl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clearStockList();
        recyclerView = findViewById(R.id.recyclerView);
        stockListAdapter = new StockListAdapter(stockArrayList,this);
        recyclerView.setAdapter(stockListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Stock Watch");
        srl = findViewById(R.id.sr);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doSwipeRefresh();
            }
        });
        if (!networkCheck()){
//            networkError("AH");
            //TODO network error dialog shit
        }
        else {
            doRead();
            new NameDownloaderAsyncTask(MainActivity.this).execute();
        }
        clearStockList();
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

    private void doSwipeRefresh() {
        networkCheck();
        if (!networkCheck()) {
            //TODO no network
            srl.setRefreshing(false);
            return;
        }
        for (Stock s : stockArrayList) {
            //TODO run a method that runs the stockdownloader async task
            getStockData(s.getStockSymbol());
        }
        stockListAdapter.notifyDataSetChanged();
        srl.setRefreshing(false);
        Toast.makeText(this, "Stocks have been updated", Toast.LENGTH_SHORT).show();
    }

//    private void getStockData(String symbol, String name) {
//        Stock st = new Stock(symbol, name,null,null,null);
//        updateStockArrayList(st);
//    }

//    public void updateStockArrayList(Stock s) {
//        if (!stockHashSet.contains(s.getStockSymbol())) {
//            stockArrayList.add(s);
//            stockHashSet.add(s.getStockSymbol());
//        } else {
//            int i = stockArrayList.indexOf(s);
//            stockArrayList.set(i, s);
//        }
//        updateList();
//
//    }

//    private void updateList() {
//        stockArrayList.sort(new Comparator<Stock>() {
//            @Override
//            public int compare(Stock a, Stock b) {
//                return a.getStockSymbol().compareTo(b.getStockSymbol());
//            }
//        });
//        stockListAdapter.notifyDataSetChanged();
//        try {
//            saveStocksIntoJson();
//        } catch (IOException | JSONException e) {
//            Toast.makeText(this, "Not saved", Toast.LENGTH_SHORT).show();
//        }
//
//    }

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

    private void getStockData(String symbol) {
        new StockDownloaderAsyncTask(MainActivity.this).execute(symbol);
    }



    public void saveStocksIntoJson() throws IOException, JSONException {
        FileOutputStream fos = getApplicationContext().openFileOutput("SavedStocks.json", Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        for (Stock s : stockArrayList) {
            JSONObject stockJSON = new JSONObject();
            stockJSON.put("symbol", s.getStockSymbol());
            stockJSON.put("name", s.getCompanyName());
            jsonArray.put(stockJSON);
        }
        String jsonText = jsonArray.toString();
        fos.write(jsonText.getBytes());
        fos.close();



    }

    public void doWrite(Context ma){
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
                input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setSingleLine();
                builder.setView(input);
                builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        getStockData(m_Text);
//                        stockArrayList = ndat.addStockToList(m_Text);
//                        ndat.execute(stockArrayList);
//                        new NameDownloaderAsyncTask(MainActivity.this).execute();
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

    public void clearStockList(){
        stockArrayList.clear();
        stockListAdapter.notifyDataSetChanged();
    }

    public void addStockToArrayList(Stock stock) {
        // call async stock downloader
        if(networkCheck() != false) {
            stockArrayList.add(stock);
            stockListAdapter.notifyDataSetChanged();
//            updateStockArrayList(stock);

            //TODO check to make sure that this isnt a duplicated stock
        }
        else{
            //TODO new dialog no network connection
        }
    }
}
