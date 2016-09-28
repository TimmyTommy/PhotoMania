package de.tommy.photomania;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    static final int PICK_PHOTO_REQUEST = 1;
    private String PictureDir;
    private String ThumbnailDir;
    private String JsonFileDir;
    private File JsonFile;
    private ListView listView;
    private LocationManager locManager;
    private LocationListener locListener;
    //private static ImageItem tmpItem;
    //private static ImageItem longClickedItem;
    private ImageItem tmpItem;
    private ImageItem longClickedItem;
    private List<ImageItem> imageList = new ArrayList<ImageItem>();
    private CustomListAdapter adapter;
    private ProgressDialog pDialog;


    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }
    private void showPDialog() {
        if (pDialog == null) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addGPSTagAndSave(null, String.format(
                            "%s %s",
                            getResources().getString(R.string.str_SearchForGPSCancelled),
                            getResources().getString(R.string.str_noGPSTagAdded))
                    );
                }
            };
            pDialog = new ProgressDialog(this);
            pDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getResources().getString(R.string.str_Cancel),
                    dialogClickListener
            );
            pDialog.setCancelable(false);
            pDialog.setMessage(String.format(
                    "%s\n%s",
                    getResources().getString(R.string.str_SearchForGPS),
                    getResources().getString(R.string.str_PleaseWait))
            );
            pDialog.show();
        }
    }
    private void addGPSTagAndSave(LatLng pos, String message){
        hidePDialog();
        locManager.removeUpdates(locListener);
        tmpItem.setGpsPos(pos);
        imageList.add(tmpItem);
        if (!message.equals("")) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
        adapter.notifyDataSetChanged();
        saveImageListToJsonFile();
    }

    private void askForGPSTagDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        showPDialog();
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locListener);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        addGPSTagAndSave(null, getResources().getString(R.string.str_noGPSTagAdded));
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage( getResources().getString(R.string.str_AskAddGPSTag))
                .setPositiveButton( getResources().getString(R.string.str_Yes), dialogClickListener)
                .setNegativeButton( getResources().getString(R.string.str_No), dialogClickListener).show();
    }
    private void initLocListener(){
        locManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                addGPSTagAndSave(pos, "");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                addGPSTagAndSave(null, String.format("%s %s",
                        getResources().getString(R.string.str_GPS_IsOff),
                        getResources().getString(R.string.str_noGPSTagAdded))
                );
            }
        };
    }
    private void initListViewListener(){

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageItem item = adapter.getItem(i);
                String itemJson = item.toJsonObject().toString();
                Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                intent.putExtra("itemJson", itemJson);
                intent.putExtra("imageFilePath", new File(PictureDir, item.getName()).getAbsolutePath());
                intent.putExtra("jsonFileName", JsonFile.getAbsolutePath());
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                longClickedItem = adapter.getItem(i);
                return false;
            }
        });
    }

    private void initializeVars(){
        PictureDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        ThumbnailDir = getExternalFilesDir("Thumbnails/").getAbsolutePath();

        listView = (ListView) findViewById(R.id.listViewFiles);
        adapter = new CustomListAdapter(this, imageList);
        listView.setAdapter(adapter);

        JsonFileDir = getExternalFilesDir("Json/").getAbsolutePath();
        JsonFile = new File(JsonFileDir, "JsonImageList.json");

        registerForContextMenu(listView);
    }

    private ImageItem generateImageItem(Date date){
        ImageItem item = new ImageItem();

        String dateString = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault()).format(date);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
        String fileName = String.format("IMG_%s.jpg", timeStamp);

        item.setName(fileName);
        item.setDate(dateString);
        item.setThumbnailPath(new File(ThumbnailDir, fileName).getAbsolutePath());

        return item;
    }
    private void generateThumbnailFile(String fileName, int size) {
        String imagePath = new File(PictureDir, fileName).getAbsolutePath();
        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath), size, size);
        String thumbPath = new File(ThumbnailDir, fileName).getAbsolutePath();
        try {
            FileOutputStream fOut = new FileOutputStream(thumbPath);
            thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.tmpItem = generateImageItem(new Date());
        File photoFile = new File(PictureDir, tmpItem.getName());
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(takePictureIntent, PICK_PHOTO_REQUEST);
    }

    private void loadJsonFileToImageList(){
        if (JsonFile.exists()) {
            try {
                String jsonFileString = FileStringReader.getStringFromFile(JsonFile.getAbsolutePath());
                JSONArray json = new JSONArray(jsonFileString);
                int i, count;
                count = json.length();
                for (i = 0; i < count; i++) {
                    imageList.add(new ImageItem(json.getJSONObject(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
    }
    private void saveImageListToJsonFile(){
        JSONArray json = new JSONArray();
        for (ImageItem item : imageList){
            json.put(item.toJsonObject());
        }
        try {
            String test = json.toString(2);
            FileOutputStream fOut = new FileOutputStream(JsonFile);
            fOut.write(test.getBytes());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVars();
        initLocListener();
        initListViewListener();
        loadJsonFileToImageList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK){
            generateThumbnailFile(tmpItem.getName(), 240);
            Point dim = BitmapTools.getImageDimensions(new File(PictureDir, tmpItem.getName()).getAbsolutePath());
            tmpItem.setImageSize(String.format("%d x %d", dim.x, dim.y));
            askForGPSTagDialog();
        } else {
            new File(PictureDir, tmpItem.getName()).delete();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_photo) {
            dispatchTakePictureIntent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.listViewFiles) {
            menu.setHeaderTitle(getResources().getString(R.string.str_options));
            String[] menuItems = {  getResources().getString(R.string.str_delete),
                                    getResources().getString(R.string.str_share),
                                    getResources().getString(R.string.action_showPos)
            };
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemIndex = item.getItemId();
        switch (menuItemIndex){
            case 0:
                deleteItem(longClickedItem);
                break;
            case 1:
                shareItem(longClickedItem);
                break;
            case 2:
                showOnMap(longClickedItem);
                break;
        }
        return true;
    }

    private void shareItem(ImageItem item){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        File file = new File(PictureDir, item.getName());
        intent.putExtra(Intent.EXTRA_SUBJECT, item.getName());
        intent.putExtra(Intent.EXTRA_TEXT, "Schau dir dieses tolle Bild an!");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.str_shareVia)));
    }
    private void deleteItem(ImageItem item){
        new File(item.getThumbnailPath()).delete();
        new File(PictureDir, item.getName()).delete();
        imageList.remove(item);
        adapter.notifyDataSetChanged();
        saveImageListToJsonFile();
        Toast.makeText(getApplicationContext(), String.format("%s %s", longClickedItem.getName(), getResources().getString(R.string.str_deleted)), Toast.LENGTH_LONG).show();
    }
    private void showOnMap(ImageItem item) {
        if (item.getGpsPos() != null) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("item", item.toJsonObject().toString());
            intent.putExtra("jsonFileName", JsonFile.getAbsolutePath());
            startActivity(intent);
        } else {
            Toast.makeText(this.getApplicationContext(), getResources().getString(R.string.str_noGPSTagAvailable), Toast.LENGTH_LONG).show();
        }
    }
}
