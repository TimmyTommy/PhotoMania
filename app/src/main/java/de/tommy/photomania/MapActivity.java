package de.tommy.photomania;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MapActivity extends Activity {
    private GoogleMap mMap;
    private ImageItem imageItem;
    private String jsonFileName;
    private HashMap<String, ImageItem> markerImageItemMap;
    private Marker mainMarker;
    private ArrayList<Marker> markerArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        markerImageItemMap = new HashMap<String, ImageItem>();
        markerArrayList = new ArrayList<Marker>();
        jsonFileName = getIntent().getStringExtra("jsonFileName");
        try {
            imageItem = new ImageItem(new JSONObject(getIntent().getStringExtra("item")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.getActionBar().setTitle(imageItem.getName());

        setUpMapIfNeeded();
        setMarkerInfoWindow();
        setMarkersForAllImages(false);
        setNewMarker(imageItem, true, true, true, true);
        initMarkerClickListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_gotoMainMarker) {
            gotoMarker(mainMarker);
        } else if (id == R.id.action_changeMapType) {
            showDialogChangeMapType();
        } else if (id == R.id.action_markerOptions) {
            showDialogMarkerOptions();
        }
        return super.onOptionsItemSelected(item);
    }

    private void gotoMarker(Marker marker){
        CameraUpdate cameraUpdateZoom = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15);
        mMap.animateCamera(cameraUpdateZoom);
        marker.showInfoWindow();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
    }

    private void setMarkerInfoWindow(){
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getApplicationContext() , imageItem));
    }

    private void setNewMarker(ImageItem item, Boolean showInfo, Boolean moveTo, Boolean visible, Boolean mainMarker){
        if (item.getGpsPos() != null) {
            LatLng pos = new LatLng(item.getGpsPos().latitude, item.getGpsPos().longitude);
            if (moveTo) {
                CameraUpdate cameraUpdateBegin = CameraUpdateFactory.newLatLngZoom(pos, 9);
                mMap.moveCamera(cameraUpdateBegin);
                CameraUpdate cameraUpdateZoom = CameraUpdateFactory.newLatLngZoom(pos, 15);
                mMap.animateCamera(cameraUpdateZoom);
            }

            MarkerOptions markerOpt = new MarkerOptions().position(pos).visible(visible);
            Marker marker = mMap.addMarker(markerOpt);
            if (mainMarker){
                this.mainMarker = marker;
            } else {
                markerArrayList.add(marker);
            }
            markerImageItemMap.put(marker.getId(), item);
            if (showInfo) {
                marker.showInfoWindow();
            }
        }
    }

    private void setMarkersForAllImages(Boolean visible){
        try {
            String jsonFileString = FileStringReader.getStringFromFile(jsonFileName);
            JSONArray json = new JSONArray(jsonFileString);
            markerImageItemMap.clear();
            for (int i=0; i<json.length(); i++){
                ImageItem item = new ImageItem(json.getJSONObject(i));
                setNewMarker(item, false, false, visible, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMarkerClickListener(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.setInfoWindowAdapter(
                        new CustomInfoWindowAdapter(
                                getApplicationContext(),
                                markerImageItemMap.get(marker.getId())
                        )
                );
                return false;
            }
        });
    }

    private void showDialogChangeMapType () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.action_changeMapType)
            .setItems(R.array.menu_mapTypes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            break;
                        case 1:
                            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            break;
                        case 2:
                            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            break;
                        case 3:
                            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                            break;
                    }
                }
            });
        builder.show();
    }

    private void showDialogMarkerOptions () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.str_markerOptions)
                .setItems(R.array.menu_markerOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                showAllMarkers();
                                break;
                            case 1:
                                showMainMarkerOnly();
                                break;
                        }
                    }
                });
        builder.show();
    }

    private void showMainMarkerOnly(){
        for (Marker m : markerArrayList){
            m.setVisible(false);
        }
    }

    private void showAllMarkers(){
        for (Marker m : markerArrayList){
            m.setVisible(true);
        }
    }


}
