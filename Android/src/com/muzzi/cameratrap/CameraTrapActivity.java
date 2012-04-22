package com.muzzi.cameratrap;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Set;
 
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.widget.ArrayAdapter;
import android.widget.TextView;
 
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

//added imports from CameraIntent example
import android.graphics.Bitmap;
import android.widget.ImageView;
import java.io.File;

//imports from Camera class example
import android.hardware.Camera;
import android.content.ContentValues;
import android.provider.MediaStore.Images.Media;
import java.io.FileNotFoundException;

//added 3/19
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.content.res.Configuration;
 
//added onClickListener and SurfaceHolder.Callback on 3/19
public class CameraTrapActivity extends Activity implements OnClickListener, SurfaceHolder.Callback, Runnable, Camera.PictureCallback {
 
	private TextView mResponseField;
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private FileInputStream mInputStream;
	private FileOutputStream mOutputStream;
	boolean ready = false;
	
	//added variables from CameraIntent example
	final static int CAMERA_RESULT = 0;
	ImageView imv;
	String imageFilePath;
	
	//added variable from Camera class example
	Camera camera;
	
	//added 3/19
	SurfaceView cameraView;
	SurfaceHolder surfaceHolder;
	
	BluetoothAdapter mBluetoothAdapter;
	boolean bluetoothEnabled = false;
	ArrayAdapter mArrayAdapter;
	
 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mResponseField = (TextView)findViewById(R.id.arduinoresponse);
		setupAccessory();
		
		//added 3/19
		cameraView = (SurfaceView) this.findViewById(R.id.CameraView);
		surfaceHolder = cameraView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);
        cameraView.setFocusable(true);
        cameraView.setFocusableInTouchMode(true);
        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //mArrayAdapter = new ArrayAdapter();
        /*mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        	//sorry bra, no bluetooth here
        }

        if (!mBluetoothAdapter.isEnabled()) {*/
        /*Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST-ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);*/
        /*Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 3);
        }*/
		
	}
	
	//added 3/19
	public void onClick(View v)
    {
    	//camera.takePicture(null,  null, this);
		enableRemoteTrigger();
    }// end onClick 
	
	public void enableRemoteTrigger()
	{
		byte[] buffer = new byte[1];
		buffer[0]=(byte)1;
		
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Toast t = Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT);
	    		t.show();
			}
		}
		
	}// enableRemoteTrigger
	
	
	//added function from Camera class example
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
		
		camera.startPreview();
	}// end onPictureTaken
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {	
		/////////////////////// attempt to solve sleep problem
		
		/*try
    	{
    		camera.setPreviewDisplay(holder);
    		Camera.Parameters parameters = camera.getParameters();
    		
    		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
    		{
    			parameters.set("orientation", "portrait");
    			camera.setDisplayOrientation(90);
    		}
    		
    		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
    		
    		camera.setParameters(parameters);
    	}
    	catch (IOException exception)
    	{
    		camera.release();
    	}*/
		
		//////////////////////
		
		
		
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
    		
    		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
    		
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
 
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mAccessory != null) {
			return mAccessory;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}
 
	@Override
	public void onResume() {
		super.onResume();
		
		///////////////////////// attempt to solve sleep problem
		/*camera = Camera.open();
		cameraView = (SurfaceView)this.findViewById(R.id.CameraView);
        surfaceHolder = cameraView.getHolder(); 
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        //surfaceHolder.setSizeFromLayout();
        surfaceHolder.addCallback(this);
        cameraView.setFocusable(true);
        cameraView.setFocusableInTouchMode(true);
        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);*/
		
		/////////////////////////
 
		if (mInputStream != null && mOutputStream != null) {
			//streams were not null");
			return;
		}
		//streams were null");
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory, mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
			
			////////////////////////////// attempt to solve sleep problem
			
			/*camera = Camera.open();
	    	try
	    	{
	    		camera.setPreviewDisplay(holder);*/
	    		/*Camera.Parameters parameters = camera.getParameters();
	    		
	    		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
	    		{
	    			parameters.set("orientation", "portrait");
	    			camera.setDisplayOrientation(90);
	    		}
	    		
	    		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
	    		
	    		camera.setParameters(parameters);*/
	    	/*}
	    	catch (IOException exception)
	    	{
	    		camera.release();
	    	}*/
			
			
			///////////////////////////////
			
		} else {
			// null accessory
		}
	}
 
	@Override
	public void onPause() {
		
		/////////////////////// attempt to solve sleep problem
		
		/*camera.stopPreview();
		surfaceHolder.removeCallback(this);
		camera.release();*/
		
		///////////////////////
		
		super.onPause();
	}
 
	@Override
	public void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}
 
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ValueMsg t = (ValueMsg) msg.obj;
			// this is where you handle the data you sent. You get it by calling the getReading() function
			mResponseField.setText("Flag: "+t.getFlag()+"; Reading: "+t.getReading()+"; Date: "+(new Date().toString()));
		}
	};
 
	private void setupAccessory() {
		mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent =PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}
	}
 
	private void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Thread thread = new Thread(null, this, "OpenAccessoryTest");
			thread.start();
			//Accessory opened
		} else {
			// failed to open accessory
		}
	}
 
	private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}
	}
	
	public void enableBluetooth()
	{
		//mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        	//sorry bra, no bluetooth here
        }

        if (!mBluetoothAdapter.isEnabled()) {
        /*Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST-ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);*/
        //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //startActivityForResult(enableBtIntent, 3);
        	/*if (bluetoothEnabled == false)
        	{*/
        		mBluetoothAdapter.enable();
        		Intent discoverableIntent = new
        				Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        				startActivity(discoverableIntent);
        				
        				/*Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        				// If there are paired devices
        				if (pairedDevices.size() > 0) {
        				    // Loop through paired devices
        				    for (BluetoothDevice device : pairedDevices) {
        				        // Add the name and address to an array adapter to show in a ListView
        				        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        				        mBluetoothAdapter.
        				    }
        				}*/
        		//mBluetoothAdapter.startDiscovery();
        		//bluetoothEnabled = true;
        	//}	
        }
        else
        {
        	//mBluetoothAdapter.startDiscovery();
        	mBluetoothAdapter.disable();
        	//bluetoothEnabled = false;
        }
        
	}
 
	public void run() {
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;
 
		while (true) { // read data
			try {
					ret = mInputStream.read(buffer);
					
			} catch (IOException e) {
				break;
			}
 
			i = 0;
			while (i < ret) {
				int len = ret - i;
				if (len >= 1) {
					Message m = Message.obtain(mHandler);
					int value = (int)buffer[i];
					//mike's if statement
					
						/*if (value == 1)
						{
							if (ready == false)
							{
								ready = true;
							}
							else
							{
								camera.takePicture(null, null, this);
							}
							
						}*/
						if (value == 1)
						{
							camera.takePicture(null, null, this);
						}
						else if (value == 2)
						{
							enableBluetooth();
						}
						// 'f' is the flag, use for your own logic
						// value is the value from the arduino
						m.obj = new ValueMsg('f', value);
						mHandler.sendMessage(m);
					
					
				}
				i += 1; // number of bytes sent from arduino
			}
 
		}
	}
 
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						// USB permission denied
					}
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					//accessory detached
					closeAccessory();
				}
			}
		}
	};
 
}