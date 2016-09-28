package de.tommy.photomania;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONObject;

/**
 * Created by Tommy on 23.10.2014.
 */
public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
    private LayoutInflater inflater;
    private ImageItem imageItem;

    public CustomInfoWindowAdapter(Context context, ImageItem imageItem){
        this.inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.imageItem = imageItem;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    // Defines the contents of the InfoWindow
    @Override
    public View getInfoContents(Marker arg0) {

        // Getting view from the layout file info_window_layout
        View v = inflater.inflate(R.layout.info_window_layout, null);

        // Getting the position from the marker

        TextView textFileName = (TextView) v.findViewById(R.id.fileNameIW);
        TextView textDate = (TextView) v.findViewById(R.id.dateIW);
        TextView textPos = (TextView) v.findViewById(R.id.gpsPosIW);

        textFileName.setText(imageItem.getName());
        textDate.setText(imageItem.getDate());
        textPos.setText(String.format("Lat: %s° / Lon: %s°",
                Location.convert(imageItem.getGpsPos().latitude, Location.FORMAT_DEGREES),
                Location.convert(imageItem.getGpsPos().longitude, Location.FORMAT_DEGREES))
        );

        ImageView thumbnail = (ImageView) v.findViewById(R.id.thumbnailIW);
        thumbnail.setImageBitmap(BitmapFactory.decodeFile(imageItem.getThumbnailPath()));

        return v;

    }
}
