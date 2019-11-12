package com.example.csc372_mikeskwierawski_a3;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class StockDownloaderAsyncTask extends AsyncTask<String, Void, String> {
    private boolean status;
    private static final String API_SECRET = "quote?token=sk_5093c1b39d324d89b996f353b11d471d";
    public String nameURLApi = "https://cloud.iexapis.com/stable/stock";
    private JSONArray stockReturnedResults = new JSONArray();
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    public StockDownloaderAsyncTask(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPostExecute(String str){
        ArrayList<Stock> dataList = new ArrayList<>();
        try{
            JSONArray jsonArray = new JSONArray(str);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String sym = jsonObject.getString("symbol");
                String name = jsonObject.getString("companyName");
                double price;
                double changePoint = 0.0;
                double changePercent = 0.0;
                if (!jsonObject.isNull("latestPrice")) {
                    price = jsonObject.getDouble("latestPrice");
                }
                else{
                    price = 0.0;
                }
                if (!jsonObject.isNull("change")) {
                    price = jsonObject.getDouble("change");
                }
                else{
                    changePoint = 0.0;
                }
                if (!jsonObject.isNull("changePercent")) {
                    price = jsonObject.getDouble("changePercent");
                }
                else{
                    changePercent = 0.0;
                }
                Stock stock = new Stock(sym, name, price, changePoint, changePercent);
                dataList.add(stock);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (status == true){
            mainActivity.stockResult(dataList.get(0));
        }
        else{
            mainActivity.addStock(dataList.get(0));
        }

    }

    @Override
    protected String doInBackground(String... strings) {
        // build url for individual stock
        Uri.Builder stockURL = Uri.parse(nameURLApi).buildUpon();
        stockURL.appendEncodedPath(strings[0]).appendEncodedPath(API_SECRET);
        String urlFinal = stockURL.build().toString();
        StringBuilder sb = new StringBuilder();
        while(stockURL != null){
            try{
                URL url = new URL(urlFinal);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
                    return null;
                }
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while((line = reader.readLine()) != null){
                    sb.append('\n');
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
        return sb.toString();
    }
}
