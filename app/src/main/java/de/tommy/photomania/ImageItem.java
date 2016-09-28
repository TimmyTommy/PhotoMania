package de.tommy.photomania;

/**
 * Created by Tommy on 19.10.2014.
 */
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class ImageItem {
    private String name, thumbnailPath;
    private LatLng gpsPos;
    private String date;
    private String imageSize;

    public ImageItem() {
    }

    public ImageItem(JSONObject json) {
        try {
            this.name = json.getString("name");
            this.thumbnailPath = json.getString("thumbnailPath");
            this.date = json.getString("date");
            this.imageSize = json.getString("imageSize");
            this.gpsPos = new LatLng(json.getDouble("latitude"), json.getDouble("longitude"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJsonObject(){
        JSONObject json = new JSONObject();
        try {
            json.put("name", this.name);
            json.put("thumbnailPath", this.thumbnailPath);
            json.put("date", this.date);
            json.put("imageSize", this.imageSize);
            if (this.gpsPos != null) {
                json.put("latitude", this.gpsPos.latitude);
                json.put("longitude", this.gpsPos.longitude);
            } else {
                json.put("latitude", null);
                json.put("longitude", null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public LatLng getGpsPos() {
        return gpsPos;
    }

    public void setGpsPos(LatLng gpsPos) {
        this.gpsPos = gpsPos;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

