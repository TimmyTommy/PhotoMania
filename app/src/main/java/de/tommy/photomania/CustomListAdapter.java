package de.tommy.photomania;

/**
 * Created by Tommy on 19.10.2014.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ImageItem> imageItems;

    public CustomListAdapter(Activity activity, List<ImageItem> imageItems) {
        this.activity = activity;
        this.imageItems = imageItems;
    }

    @Override
    public int getCount() {
        return imageItems.size();
    }

    @Override
    public ImageItem getItem(int location) {
        return imageItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.listview_item_custom, null);

        ImageView thumbNail = (ImageView) convertView.findViewById(R.id.thumbnail);
        TextView fileName = (TextView) convertView.findViewById(R.id.fileName);
        TextView date = (TextView) convertView.findViewById(R.id.date);
        TextView gpsPos = (TextView) convertView.findViewById(R.id.gpsPos);
        TextView imageSize = (TextView) convertView.findViewById(R.id.imageSize);

        ImageItem m = imageItems.get(position);

        Bitmap ThumbImage = BitmapFactory.decodeFile(m.getThumbnailPath());
        thumbNail.setImageBitmap(ThumbImage);

        fileName.setText(m.getName());
        date.setText(String.format("%s: %s", "Datum", m.getDate()));
        String posStr;
        if (m.getGpsPos() != null) {
            posStr = String.format("Lat: %s° / Lon: %s°",
                    Location.convert(m.getGpsPos().latitude, Location.FORMAT_DEGREES),
                    Location.convert(m.getGpsPos().longitude, Location.FORMAT_DEGREES));
        } else {
            posStr = "Kein GPS-Tag";
        }
        gpsPos.setText(posStr);
        imageSize.setText(String.format("%s: %s", "Auflösung", m.getImageSize()));

        return convertView;
    }

}
