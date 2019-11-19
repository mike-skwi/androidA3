package com.example.csc372_mikeskwierawski_a3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    ArrayList<Stock> stockArrayList = new ArrayList<>();
    private Set<String> stockHashSet = new HashSet<>();
    private RecyclerView recyclerView;
    private StockListAdapter stockListAdapter;
    private String m_Text = "";
    private EditText input;
    private SwipeRefreshLayout srl;
    private static final String MARKET_WATCH_LINK = "https://www.marketwatch.com/investing/stock/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        clearStockList();
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
            networkErrorDialog();
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

            srl.setRefreshing(false);
            networkErrorDialog();
            return;

        }
        if (stockArrayList.size() != 0){
            for (Stock s : stockArrayList) {
                //TODO run a method that runs the stockdownloader async task
                getStockData(s.getStockSymbol());
            }
        }
        stockListAdapter.notifyDataSetChanged();
        srl.setRefreshing(false);
        Toast.makeText(this, "Stocks have been updated", Toast.LENGTH_SHORT).show();
    }

//    private void getStockData(String symbol, String name) {
//        Stock st = new Stock(symbol, name,null,null,null);
//        updateStockArrayList(st);
//    }

    @Override
    public void onClick(View view) {
//        Toast.makeText(view.getContext(), "position = " + recyclerView.getChildLayoutPosition(view), Toast.LENGTH_SHORT).show();
        int positionClicked = recyclerView.getChildLayoutPosition(view);
        Stock st = stockArrayList.get(positionClicked);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String stockUrl = MARKET_WATCH_LINK + st.getStockSymbol();
        intent.setData(Uri.parse(stockUrl));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View view) {
//        Toast.makeText(view.getContext(), "long click. position = " + recyclerView.getChildLayoutPosition(view), Toast.LENGTH_SHORT).show();
        final int deletePosition = recyclerView.getChildLayoutPosition(view);


        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Delete");
        builder.setMessage("Are you sure you want to delete this stock?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                MainActivity.this.deleteFromList(deletePosition);
                deleteFromList(deletePosition);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        return true;


    }

    private void symNotFoundError(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(symbol + " was not found.");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void updateStockArrayList(Stock s) {
        if (!stockHashSet.contains(s.getStockSymbol())) {
            stockArrayList.add(s);
            stockHashSet.add(s.getStockSymbol());
        } else {
            if (stockArrayList.contains(s)){
                int i = stockArrayList.indexOf(s);
                stockArrayList.set(i, s);
            }
        }
        updateList();

    }

    private void updateList() {
        stockArrayList.sort(new Comparator<Stock>() {
            //sort is underlined but still works
            @Override
            public int compare(Stock prev, Stock next) {
                return prev.getStockSymbol().compareTo(next.getStockSymbol());
            }
        });

        stockListAdapter.notifyDataSetChanged();
        try {
            saveStocksIntoJson();
//            clearStockList();
        } catch (IOException | JSONException e) {
            Toast.makeText(this, "Not saved", Toast.LENGTH_SHORT).show();
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

    public void networkErrorDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Internet Error");
        AlertDialog dialog = builder.create();
        dialog.show();
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
                        m_Text = input.getText().toString().toUpperCase();
//                        getStockData(m_Text);
//                        stockArrayList = ndat.addStockToList(m_Text);
//                        ndat.execute(stockArrayList);
//                        new NameDownloaderAsyncTask(MainActivity.this).execute();
                        if (!m_Text.isEmpty()) {
                            Map<String, String> closelySpelledKeys = NameDownloaderAsyncTask.mapLookUp(m_Text);
                            if (closelySpelledKeys.size() >= 1) {
                                if (!stockHashSet.contains(m_Text)) {
                                    getStockData(m_Text);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Duplicate Stock Detected");
                                    builder.setMessage(m_Text + " is already in the list");
                                    AlertDialog dialog2 = builder.create();
                                    dialog2.show();
                                }
                            }
//                            else if (closelySpelledKeys.size() > 1){
                                // This is where a multi dialog thing would be
//                            }
                            else {
                                Toast.makeText(MainActivity.this,"Couldn't find stock",LENGTH_SHORT).show();
                            }
                        }
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
//        stockListAdapter.notifyDataSetChanged();
    }

    public void deleteFromList(int stockPos){
        String symbol = stockArrayList.get(stockPos).getStockSymbol();
        stockHashSet.remove(symbol);
        stockArrayList.remove(stockPos);
        updateList();
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
