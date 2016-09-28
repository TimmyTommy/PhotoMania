package de.tommy.photomania;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class PhotoActivity extends Activity {
    private ImageView imageView;
    private ImageItem item;
    private String imageItemString;
    private String imageFilePath;
    private ShareActionProvider mShareActionProvider;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageFilePath = getIntent().getStringExtra("imageFilePath");
        try {
            imageItemString = getIntent().getStringExtra("itemJson");
            item = new ImageItem(new JSONObject(imageItemString));
            int size = getDisplayImageSize();
            bitmap = BitmapTools.decodeSampledBitmapFromResource(imageFilePath, size, size);
            imageView.setImageBitmap(bitmap);
            this.getActionBar().setTitle(item.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getDisplayImageSize() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        if (size.x>size.y)
            return size.y;
        else
            return size.x;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mShareActionProvider.setShareIntent(getShareIntent());
        return true;
    }

    private Intent getShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        File file = new File(imageFilePath);
        intent.putExtra(Intent.EXTRA_SUBJECT, file.getName());
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.str_watchThisGreatPicture));
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_showPos) {
            showOnMap();
            return true;
        } else if (id == R.id.action_copyToGallery) {
            copyToGallery(imageFilePath, this.item.getName());
            return true;
        } else if (id == R.id.action_turnLeft) {
            rotateImage(270);
            return true;
        } else if (id == R.id.action_turnRight) {
            rotateImage(90);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showOnMap(){
        if (this.item.getGpsPos() != null) {
            Intent intent = new Intent(PhotoActivity.this, MapActivity.class);
            intent.putExtra("item", imageItemString);
            intent.putExtra("jsonFileName", getIntent().getStringExtra("jsonFileName"));
            startActivity(intent);
        } else {
            Toast.makeText(this.getApplicationContext(),
                    getResources().getString(R.string.str_noGPSTagAvailable),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void rotateImage(float angle) {
        bitmap = BitmapTools.RotateBitmap(bitmap, angle);
        imageView.setImageBitmap(bitmap);
    }

    private void copyToGallery(String filePath, String fileName){
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, fileName);
        Toast.makeText(this.getApplicationContext(),
                getResources().getString(R.string.str_ImageWasSuccessfullyCopied),
                Toast.LENGTH_LONG
        ).show();
    }
}
