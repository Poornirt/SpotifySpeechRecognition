package httprequest;

import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class APICall {

    private static APICall apiCall;

    public String playNextAudio(String pAuthToken){
        URL url;
        HttpURLConnection httpURLConnection;
        String lResponse = null;
        try {
            url = new URL("https://api.spotify.com/v1/me/player/next");
            httpURLConnection= (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            httpURLConnection.setRequestProperty("Authorization","Bearer"+pAuthToken);
            httpURLConnection.connect();

            if(httpURLConnection.getResponseCode()==200){
                lResponse=httpURLConnection.getResponseMessage(); 
            }
            Log.d("APICall", "playNextAudio:"+httpURLConnection.getResponseMessage());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return lResponse;
    }

    public static APICall getInstance(){
        if(apiCall==null)
            apiCall=new APICall();
        return apiCall;
    }
}
