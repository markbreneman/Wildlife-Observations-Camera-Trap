package com.muzzi.cameratrap;

import java.io.FileNotFoundException;

//added upload stuff
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class takePicture extends Activity implements OnClickListener, SurfaceHolder.Callback, Camera.PictureCallback
{
	SurfaceView cameraView;
	SurfaceHolder surfaceHolder;
	Camera camera;
	
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      //setContentView(R.layout.main);
      setContentView(R.layout.photo);
      
      cameraView = (SurfaceView) this.findViewById(R.id.CameraView);
      
      surfaceHolder = cameraView.getHolder();
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
      surfaceHolder.addCallback(this);
      
      cameraView.setFocusable(true);
      cameraView.setFocusableInTouchMode(true);
      cameraView.setClickable(true);
      
      cameraView.setOnClickListener(this);       
  }// end onCreate
  
  public void onClick(View v)
  {
  	camera.takePicture(null,  null, this);
  }// end onClick                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
  
  public void onPictureTaken(byte[] data, Camera camera)
  {
  	Uri imageFileUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());
  	
  	try
  	{
  		OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);
  		imageFileOS.write(data);
  		imageFileOS.flush();
  		imageFileOS.close();
  	}
  	catch (FileNotFoundException e)
  	{
  		Toast t = Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT);
  		t.show();
  	}
  	catch (IOException e)
  	{
  		Toast t = Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT);
  		t.show();
  	}
  	
  	HttpURLConnection conn = null;
	    DataOutputStream dos = null;
	    DataInputStream inStream = null;

	    String lineEnd = "\r\n";
	    String twoHyphens = "--";
	    String boundary = "-----------------------------29772313742745";

	    try {
	      // ------------------ CLIENT REQUEST

	      URL url = new URL("http://itp.nyu.edu/~dbo3/up.php");
	      // Open a HTTP connection to the URL
	      conn = (HttpURLConnection) url.openConnection();

	      // Allow Inputs
	      conn.setDoInput(true);
	      // Allow Outputs
	      conn.setDoOutput(true);
	      // Don't use a cached copy.
	      conn.setUseCaches(false);
	      // Use a post method.
	      conn.setRequestMethod("POST");
	      conn.setRequestProperty("Connection", "Keep-Alive");
	      // conn.setRequestProperty("Cookie", "JSESSIONID="+PlayList.getSessionId());
	      conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

	      dos = new DataOutputStream(conn.getOutputStream());

	      dos.writeBytes(twoHyphens + boundary + lineEnd);
	      // dos.writeBytes("Content-Disposition: form-data; name=\"fileNameOnServer\""+lineEnd+URLEncoder.encode(fileNameOnServer,"UTF-8") + lineEnd);
	      dos.writeBytes(("Content-Disposition: form-data; name=\"data_file\"; filename=\"" + lineEnd + URLEncoder.encode("muzzitest.jpg", "UTF-8") + "\"\r\n"));
	      dos.writeBytes("Content-Type: " + "image/jpg" + " \r\n");
	      dos.writeBytes(lineEnd);

	      // create a buffer of maximum size
	      dos.write(data);

	      dos.writeBytes(lineEnd);
	      dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


	      dos.flush();
	      dos.close();
	    } 
	    catch (MalformedURLException ex) {
	      //println( "error: " + ex.getMessage());
	    }

	    catch (Exception ioe) {
	      //println("error: " + ioe.getMessage());
	    }

	    // ------------------ read the SERVER RESPONSE

	    try {
	      inStream = new DataInputStream(conn.getInputStream());
	      String str;

	      while ( (str = inStream.readLine ()) != null) {
	        //println("Server Response" +str );
	      }
	      //parent.finishedUpload();
	      //parent.finishedUpload();
	      inStream.close();
	    } 
	    catch (Exception ioex) {
	      //println("error: " + ioex.getMessage());
	    }
  	
  	
  	camera.startPreview();
  }// end onPictureTaken
  
  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
  {
  	camera.startPreview();
  }// end surfaceChanged
  
  public void surfaceCreated(SurfaceHolder holder)
  {
  	camera = Camera.open();
  	try
  	{
  		camera.setPreviewDisplay(holder);
  		Camera.Parameters parameters = camera.getParameters();
  		
  		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
  		{
  			parameters.set("orientation", "portrait");
  			camera.setDisplayOrientation(90);
  		}
  		
  		camera.setParameters(parameters);
  	}
  	catch (IOException exception)
  	{
  		camera.release();
  	}
  }// end surfaceCreated
  
  public void surfaceDestroyed(SurfaceHolder holder)
  {
  	camera.stopPreview();
  	camera.release();
  }// end surfaceDestroyed
  
}// end takePicture class
