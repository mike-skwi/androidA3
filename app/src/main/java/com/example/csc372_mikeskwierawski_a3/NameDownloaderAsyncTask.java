package com.example.csc372_mikeskwierawski_a3;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static android.widget.Toast.LENGTH_SHORT;

public class NameDownloaderAsyncTask extends AsyncTask<String,Integer,String> {
    private static HashMap<String,String> symName;
    private static final String symAPIURL = "https://api.iextrading.com/1.0/ref-data/symbols";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private ArrayList<Stock> stockList;

    public NameDownloaderAsyncTask(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        this.symName = new HashMap<>();
    }

    public ArrayList<String> addStockToList(String symbol) {
        ArrayList<String> result = new ArrayList<>();
        for (String sym : symName.keySet()){
            String temp = symName.get(sym);
            if(sym.startsWith(symbol) || temp.toUpperCase().startsWith(symbol)){
                result.add(sym+"."+symName.get(sym));
            }
        }
        if (result.isEmpty()){
            result.add("Stock not found.");
        }
        Toast.makeText(this.mainActivity,symbol, LENGTH_SHORT).show();
        return result;
    }

    @Override
    protected String doInBackground(String... params) {
        Uri symbolUri = Uri.parse(symAPIURL);
        String urlToUse = symbolUri.toString();
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
                Log.d("AAA","Does it get here");

            }
        } catch (Exception e) {
            return null;
        }
        return sb.toString();
    }



//    @Override
//    protected String doInBackground(String... symbol){
//        android.os.Debug.waitForDebugger();
////        stockList = symbol[0];
//        StringBuilder sb = new StringBuilder();
//        Log.d("AAA","Does it get here");
//        Uri.Builder stockURL = Uri.parse(symAPIURL).buildUpon();
//        String urlFinal = stockURL.build().toString();
//        while(stockURL != null){
//            try{
//                URL url = new URL(urlFinal);
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
//                    return null;
//                }
//                conn.setRequestMethod("GET");
//                InputStream is = conn.getInputStream();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//                String line;
//                while((line = reader.readLine()) != null){
//                    sb.append(line).append('\n');
//                }
//                Log.d("SB",sb.toString());
//
//                return sb.toString();
//
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                return null;
//            } catch (ProtocolException e) {
//                e.printStackTrace();
//                return null;
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//        return sb.toString();
//    }

    @Override
    protected void onPostExecute(String str){
        parseJSON(str);
//        for(int i = 0; i<stockList.size();i++){
//            new StockDownloaderAsyncTask().execute(stockList.get(i).getStockSymbol());
//        }
    }

    private void parseJSON(String s) {
        try {
            JSONArray jObjMain = new JSONArray(s);
            for(int i=0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);
                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name");
                if(!name.isEmpty()) {
                    this.symName.put(symbol, name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    protected void parseJSON(String str){
//        symName.clear();
//        try{
//            JSONArray jsonArray = new JSONArray(str);
//            for(int i = 0; i< jsonArray.length();i++){
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                String sym = jsonObject.getString("symbol");
//                String company = jsonObject.getString("companyName");
//                symName.put(sym,company);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

}
