package com.example.banknote_app;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "banknotes_app.MESSAGE";
	public final static String EXTRA_PHOTOPATH = "banknotes_app.PHOTOPATH";
	private final static int REQUEST_IMAGE_CAPTURE = 1;
	private String _currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
    		// Setting a simple bitmap on the main window using the taken photo.
//    		Bundle extras = data.getExtras();
//    		Bitmap imgBitmap = (Bitmap) extras.get("data");
//    		ImageView imgViewer = (ImageView) findViewById(R.id.photo_viewer);
//    		imgViewer.setImageBitmap(imgBitmap);
    		File imgFile = new File(_currentPhotoPath);
    		if (imgFile.exists()) {
    			Intent i = new Intent();
    			i.setAction(android.content.Intent.ACTION_VIEW);
    			i.setDataAndType(Uri.fromFile(imgFile), "image/jpg");
    			startActivity(i);
    		} else {
    			Log.d("CameraPhoto", "Error");
    		}

    		// Opening a new window to show the full size image.
//    		Intent intent = new Intent(this, DisplayImage.class);
//    		intent.putExtra(EXTRA_PHOTOPATH, _currentPhotoPath);
//    		startActivity(intent);
    	} else {
    		AlertDialog alert = new AlertDialog.Builder(this).create();
    		alert.setMessage("Image not taken");
    		alert.show();
    	}
    }
    
    public void sendMessage(View view) {
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }
    
    private File createImageFile() throws IOException {
    	// Image filename.
    	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    	String imgFilename = "JPG_" + timeStamp;
    	File storageDir = Environment.getExternalStoragePublicDirectory(
    			Environment.DIRECTORY_PICTURES);
    	if (!storageDir.exists())
    		if (!storageDir.mkdirs()) {
    			AlertDialog alert = new AlertDialog.Builder(this).create();
    			alert.setMessage("Directory was not created!");
    			alert.show();
    			return null;
    		}
    		
//    	File output = new File(storageDir.getPath(), imgFilename);
    	File output = File.createTempFile(
    			imgFilename, // prefix
    			".jpg", // suffix
    			storageDir // directory
    			);
    	// Save a file: path for use with ACTION_VIEW intents
//    	_currentPhotoPath = "file:" + output.getAbsolutePath();
    	_currentPhotoPath = output.getAbsolutePath();
    	return output;
    }
    
    public void takePhoto(View view) {
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	// Checks if there is an app to handle the intent.
    	if (intent.resolveActivity(getPackageManager()) != null) {
    		File photoFile = null;
    		try {
				photoFile = createImageFile();
			} catch (IOException e) {
				// TODO: handle exception
				// http://stackoverflow.com/questions/10954114/android-how-to-display-a-dialog-from-error-of-a-try-catch
				// http://stackoverflow.com/questions/5362929/runtimeexception-on-alertdialog-show
				AlertDialog alert = new AlertDialog.Builder(this).create();
				alert.setMessage("File not created!");
				alert.show();
				e.printStackTrace();
				return;
			}
    		
    		// The full size photo is saved with this intent.
    		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
    		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    	}
    }
    
}
