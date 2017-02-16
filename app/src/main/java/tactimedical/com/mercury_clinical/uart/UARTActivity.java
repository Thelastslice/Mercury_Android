/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package tactimedical.com.mercury_clinical.uart;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import tactimedical.com.mercury_clinical.AppHelpFragment;
import tactimedical.com.mercury_clinical.PlacementSelect;
import tactimedical.com.mercury_clinical.R;
import tactimedical.com.mercury_clinical.grapher.GraphAsync;
import tactimedical.com.mercury_clinical.grapher.GraphFragment;
import tactimedical.com.mercury_clinical.profile.BleProfileService;
import tactimedical.com.mercury_clinical.profile.BleProfileServiceReadyActivity;

public class UARTActivity extends BleProfileServiceReadyActivity<UARTService.UARTBinder> implements UARTInterface,
		AdapterView.OnItemSelectedListener{

	private final static String TAG = "UARTActivity";
	public final static String KEY_FILE = "KEY_FILE";
	private SeekBar yellowBar; private TextView yellowBarValue;
	private SeekBar redBar; private TextView redBarValue;

	private static final int[] idArray = {R.id.top_center, R.id.center_right, R.id.top_right,
			R.id.top_left, R.id.center_left, R.id.bottom, R.id.center_center};
	private Context mContext;
	private Button[] buttList = new Button[idArray.length];
	private Button mThreshold;

	// Create file
	File patientFile;
	//Value holders for total presure times
	private double[] greenTime= new double[7];
	private double[] yellowTime= new double[7];
	private double[] redTime= new double[7];
	final String[] channelList= new String[]{"TC","CR","TR","TL","CL","BC","CC"};
	int yellowLevel =175;
	int redLevel = 300;

	private long thisTime;
	private long lastTime;
	private long timeDiff;

	private final static String PREFS_BUTTON_ENABLED = "prefs_uart_enabled_";
	private final static String PREFS_BUTTON_COMMAND = "prefs_uart_command_";
	private final static String PREFS_BUTTON_ICON = "prefs_uart_icon_";
	/** This preference keeps the ID of the selected configuration. */
	private final static String PREFS_CONFIGURATION = "configuration_id";
	/** This preference is set to true when initial data synchronization for wearables has been completed. */
	private final static String PREFS_WEAR_SYNCED = "prefs_uart_synced";
	private final static String SIS_EDIT_MODE = "sis_edit_mode";

	private final static int SELECT_FILE_REQ = 2678; // random
	private final static int PERMISSION_REQ = 24; // random, 8-bit


	/** The current configuration. */
	private SharedPreferences mPreferences;
	private UARTService.UARTBinder mServiceBinder;


	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return UARTService.class;
	}

	@Override
	protected void setDefaultUI() {
		// empty
	}

	@Override
	protected void onServiceBinded(final UARTService.UARTBinder binder) {
		mServiceBinder = binder;
	}

	@Override
	protected void onServiceUnbinded() {
		mServiceBinder = null;
	}

	@Override
	protected void onInitialize(final Bundle savedInstanceState) {
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	}


	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUARTBroadcastReceiver);
		super.onDestroy();
	}

	private final BroadcastReceiver mUARTBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String data = intent.getStringExtra(UARTService.EXTRA_DATA);
			Log.d(TAG,"Broadcast Received");
			runUIDataUpdater(data);

		}
	};

	private static IntentFilter makeUARTIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UARTService.BROADCAST_UART_RX);
		return intentFilter;
	}

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		setContentView(R.layout.main_display);
		LocalBroadcastManager.getInstance(this).registerReceiver(mUARTBroadcastReceiver, makeUARTIntentFilter());
		ButtonInitializer();

		patientFile = (File) getIntent().getExtras().get(PlacementSelect.KEY_FILE);
	}

	@Override
	protected void onViewCreated(final Bundle savedInstanceState) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowTitleEnabled(true);
		}

	}

	@Override
	protected void onRestoreInstanceState(final @NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
		Toast.makeText(getApplicationContext(),"Beginning Mercury Patch services.", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDeviceSelected(final BluetoothDevice device, final String name) {
		// The super method starts the service
		Toast.makeText(getApplicationContext(),"Connecting to Mercury Patch", Toast.LENGTH_LONG).show();
		super.onDeviceSelected(device, name);

	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.uart_default_name;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.uart_about_text;
	}

	@Override
	protected UUID getFilterUUID() {
		return null; // not used
	}

	@Override
	public void send(final String text) {
		if (mServiceBinder != null)
			mServiceBinder.send(text);
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.clear();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		switch (id) {
			case R.id.action_about:

				final AppHelpFragment fragment = AppHelpFragment.getInstance(R.string.about_text, true);
				fragment.show(getSupportFragmentManager(), "help_fragment");
				break;
			default:
				break;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case PERMISSION_REQ: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(this, R.string.no_required_permission, Toast.LENGTH_SHORT).show();

				}
				break;
			}
		}
	}

	@Override
	public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
		//null
	}

	@Override
	public void onNothingSelected(final AdapterView<?> parent) {
		// do nothing
	}


	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_CANCELED)
			return;

		switch (requestCode) {
			case SELECT_FILE_REQ: {
				// clear previous data

				break;
			}
		}
	}

	public void onFileWrite(String fileWriteString){
		String currentDateTimeString = new SimpleDateFormat("yy/MM/dd HH:mm:ss:SS", Locale.US).format(new Date());
		try {
			if(!patientFile.exists()){
				FileOutputStream fOut = new FileOutputStream(patientFile);
				//Log.d(TAG, "File write");
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				myOutWriter.write("Time, TL, TC, TR, CL, CC, CR, BT\n");
				myOutWriter.write(currentDateTimeString+","+fileWriteString+"\n");
				myOutWriter.close();
				fOut.close();
			}
			else {
				FileOutputStream fOut = new FileOutputStream(patientFile, true);
				//Log.d(TAG, "File write");
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				myOutWriter.write(currentDateTimeString + "," + fileWriteString + "\n");
				myOutWriter.close();
				fOut.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void ButtonInitializer(){

		mContext = this;
		mThreshold = (Button) findViewById(R.id.action_setcolor);
		mThreshold.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder builder = new AlertDialog.Builder(UARTActivity.this);
				LayoutInflater inflater = UARTActivity.this.getLayoutInflater();
				View dialogView = inflater.inflate(R.layout.color_dialog,null);

				builder.setView(dialogView)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Toast.makeText(getApplicationContext(), "Thresholds Changed", Toast.LENGTH_SHORT).show();
							yellowLevel = Integer.parseInt(yellowBarValue.getText().toString());
							redLevel = Integer.parseInt(redBarValue.getText().toString());
							Log.d(TAG,yellowBarValue.getText().toString());
							Log.d(TAG,redBarValue.getText().toString());
						}
					})
					.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User cancelled the dialog
						}
					})
					.setTitle(R.string.action_setcolor);

				redBarValue = (TextView) dialogView.findViewById(R.id.redBarValue);
				yellowBarValue = (TextView) dialogView.findViewById(R.id.yellowBarValue);

				redBarValue.setText(String.valueOf(redLevel));
				yellowBarValue.setText(String.valueOf(yellowLevel));
				redBar = (SeekBar) dialogView.findViewById(R.id.redBar);
				yellowBar = (SeekBar) dialogView.findViewById(R.id.yellowBar);

				redBar.setMax(1500);
				redBar.setProgress(redLevel);
				yellowBar.setMax(redLevel);
				yellowBar.setProgress(yellowLevel);

				yellowBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
												  boolean fromUser) {
						yellowBarValue.setText(String.valueOf(progress));
					}
				});

				redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
												  boolean fromUser) {
						redBarValue.setText(String.valueOf(progress));
						yellowBar.setMax(progress);
					}
				});

				builder.create();
				builder.show();
			}
		});

		for (int i=0; i<idArray.length; i++) {
			final int b = i;
			buttList[b] = (Button) findViewById(idArray[b]);
			buttList[b].setText(channelList[b]);
			buttList[b].getBackground().setColorFilter(Color.parseColor(parseResColor(R.color.colorGreen)), PorterDuff.Mode.SRC_ATOP);
			buttList[b].setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					GraphAsync test = new GraphAsync(UARTActivity.this,b+1,patientFile.toString());
					test.execute();

				}
			});

		}
	}

	public String parseResColor(int id){
		String colorString =  "#" + Integer.toHexString(this.getResources().getColor(id,null));
		return colorString;
	}

	//Rounding for decimal point selection and string output
	public double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}


	public void runUIDataUpdater(String data) {
		String[] tester = data.split("[\\p{Punct}&&[^.]]");
		int parseIndex = 0;

		for (String stringIndex : tester) {
			if (parseIndex < 8 && parseIndex >= 1) {

				Double uartData = Double.parseDouble(stringIndex);
				if (uartData == 0.0 && uartData < yellowLevel) {
					buttList[parseIndex-1].getBackground().setColorFilter(Color.parseColor(parseResColor(R.color.colorGreen)), PorterDuff.Mode.SRC_ATOP);
					greenTime[parseIndex-1] += (double) timeDiff / 60000.0;
				} else if (uartData >= yellowLevel && uartData < redLevel) {
					buttList[parseIndex-1].getBackground().setColorFilter(Color.parseColor(parseResColor(R.color.colorYellow)), PorterDuff.Mode.SRC_ATOP);
					yellowTime[parseIndex-1] += (double) timeDiff / 60000.0;
				} else if (uartData >= redLevel) {
					buttList[parseIndex-1].getBackground().setColorFilter(Color.parseColor(parseResColor(R.color.colorRed)), PorterDuff.Mode.SRC_ATOP);
					redTime[parseIndex-1] += (double) timeDiff / 60000.0;
				}

				buttList[parseIndex - 1].setText(stringIndex);


			}
			parseIndex++;
		}

		onFileWrite(data.replace("[", " ").replace("]", ""));
		lastTime = SystemClock.elapsedRealtime();
	}


 }
