package com.example.banknote_app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "banknotes_app.MESSAGE";
	private final static int REQUEST_IMAGE_CAPTURE = 1;

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
    		Bundle extras = data.getExtras();
    		Bitmap imgBitmap = (Bitmap) extras.get("data");
    		ImageView imgViewer = (ImageView) findViewById(R.id.photo_viewer);
    		imgViewer.setImageBitmap(imgBitmap);
    	}
    }
    
    public void sendMessage(View view) {
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }
    
    public void takePhoto(View view) {
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	// Checks if there is an app to handle the intent.
    	if (intent.resolveActivity(getPackageManager()) != null)
    		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }
    
}
