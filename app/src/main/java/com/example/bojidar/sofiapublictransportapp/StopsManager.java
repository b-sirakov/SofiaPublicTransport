package com.example.bojidar.sofiapublictransportapp;


import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Bojidar on 5/26/2018.
 */

public class StopsManager {

    private static StopsManager myInstance;
    public static List<String> favStopsList = new LinkedList<>();

    private StopsManager(){

    }

    public static StopsManager getInstance(){
        if(myInstance==null){
            myInstance=new StopsManager();
        }
        return myInstance;
    }

    public List<String> getFavStops(){
        return Collections.unmodifiableList(favStopsList);
    }

    public void addStopInFav(String stopNameCode){
        if(favStopsList.contains(stopNameCode)){
            favStopsList.remove(stopNameCode);
        }
        favStopsList.add(0,stopNameCode);
    }

    public void removeStopFromFav(String stopNameCode){
        if(favStopsList.contains(stopNameCode)){
            favStopsList.remove(stopNameCode);
        }
    }

    public void removeStopFromFav(int position) {
        if(favStopsList.size()-1>=position){
            favStopsList.remove(position);
        }
    }

    public void initFavStopsListFromSharePrefs(Activity activity){
        SharedPreferences share= activity.getSharedPreferences("fav_stops_share_prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor =share.edit();

        if(share.getString("fav_stops", null)==null){
            Gson gson = new Gson();
            ArrayList<String> tempArrayList=new ArrayList<>();
            String json = gson.toJson(tempArrayList);
            editor.putString("fav_stops", json);
            editor.apply();
        }

        String json = share.getString("fav_stops", null);
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<String>>() {}.getType();

        favStopsList = gson.fromJson(json, type);
    }

    public void saveFavStopsInSharedPref(Activity activity) {
        SharedPreferences share= activity.getSharedPreferences("fav_stops_share_prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor =share.edit();
        Gson gson = new Gson();
        String json = gson.toJson(favStopsList);
        editor.putString("fav_stops", json);
        editor.apply();
    }

    public void clearFavList(){
        favStopsList.clear();
    }


}
