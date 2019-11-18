package com.example.csc372_mikeskwierawski_a3;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static android.widget.Toast.LENGTH_SHORT;

public class NameDownloaderAsyncTask extends AsyncTask<ArrayList<Stock>,Integer,String> {
    private static HashMap<String,String> symName;
    private static final String symAPIURL = "https://cloud.iexapis.com/stable/stock/";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private ArrayList<Stock> stockList;

    public NameDownloaderAsyncTask(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        symName = new HashMap<>();
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
    protected String doInBackground(ArrayList<Stock>... symbol) {
        stockList = symbol[0];
        Uri.Builder stockURL = Uri.parse(symAPIURL).buildUpon();
        String urlFinal = stockURL.build().toString();
        while(stockURL != null){
            StringBuilder sb = new StringBuilder();
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
                return sb.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (ProtocolException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String str){
//        parseJSON(str);
        for(int i = 0; i<stockList.size();i++){

            new StockDownloaderAsyncTask(mainActivity).execute(stockList.get(i).getStockSymbol());
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
