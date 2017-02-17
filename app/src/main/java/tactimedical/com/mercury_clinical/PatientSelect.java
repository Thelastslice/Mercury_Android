package tactimedical.com.mercury_clinical;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import tactimedical.com.mercury_clinical.data.DatabaseManager;
import tactimedical.com.mercury_clinical.model.PatientPlacementList;
import java.util.List;
import tactimedical.com.mercury_clinical.data.model.Patient;
import tactimedical.com.mercury_clinical.data.model.PatientPlacement;
import tactimedical.com.mercury_clinical.data.model.Placement;
import tactimedical.com.mercury_clinical.data.model.Room;
import tactimedical.com.mercury_clinical.data.repo.PatientPlacementRepo;
import tactimedical.com.mercury_clinical.data.repo.PatientRepo;
import tactimedical.com.mercury_clinical.data.repo.PlacementRepo;
import tactimedical.com.mercury_clinical.data.repo.RoomRepo;


import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.NFC;

public class PatientSelect extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {


    Button btnAddNew;
    private ListView listView;
    public final static String KEY_EXTRA_CONTACT_ID = "KEY_EXTRA_CONTACT_ID";
    private final String TAG = PatientSelect.class.getSimpleName().toString();

    private static final String[] permissions = {WRITE_EXTERNAL_STORAGE,BLUETOOTH,BLUETOOTH_ADMIN,ACCESS_COARSE_LOCATION,NFC};
    private static final int PERMISSION_REQUEST_CODE = 200;
    protected static final int REQUEST_CHECK_SETTINGS = 2;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 180000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";


    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation;
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_select);

        checkFirstRun();

        if(!checkAppPermissions()){
            requestAppPermissions();
        }

        mRequestingLocationUpdates = false;
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettings();

        btnAddNew = (Button) findViewById(R.id.addNew);
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PatientSelect.this, CreateOrSelectPatient.class);
                intent.putExtra(KEY_EXTRA_CONTACT_ID, 0);
                startActivity(intent);
            }
        });

        refreshPatientList();
    }

    private void failAllBUPatient(){
        PatientPlacementRepo patientPlacementRepo = new PatientPlacementRepo();
        patientPlacementRepo.failALLBUPatient();

    }

    private void deleteAllBUPatient(){
        PatientPlacementRepo patientPlacementRepo = new PatientPlacementRepo();
        patientPlacementRepo.deleteAllPatient();

    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        refreshPatientList();
        //Refresh your stuff here
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        refreshPatientList();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshPatientList(){
        PatientRepo patientRepo = new PatientRepo();
        PlacementRepo placementRepo = new PlacementRepo();
        RoomRepo roomRepo = new RoomRepo();
        PatientPlacementRepo patientPlacementRepo = new PatientPlacementRepo();

        Patient patient = new Patient();
        PatientPlacement patientPlacement = new PatientPlacement();

        String[] columns = new String[]{
                patientPlacement.KEY_PatientID,
                patient.KEY_Name,
                patient.KEY_RoomID
                
        };
        int[] widgets = new int[]{
                R.id.KEY_PATIENTID,
                R.id.KEY_NAME,
                R.id.KEY_ROOMID
        };

        SimpleCursorAdapter cursorAdapter1 = new SimpleCursorAdapter(this,R.layout.patient_info,patientPlacementRepo.getAllPatients(),columns,widgets,0);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(cursorAdapter1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                Cursor itemCursor = (Cursor) PatientSelect.this.listView.getItemAtPosition(position);
                int patientID = itemCursor.getInt(itemCursor.getColumnIndex(PatientPlacement.KEY_PatientID));
                Log.d(TAG,Integer.toString(patientID));
                Intent intent = new Intent(getApplicationContext(), PlacementSelect.class);
                intent.putExtra(KEY_EXTRA_CONTACT_ID, patientID);

               // itemCursor.close();
               // DatabaseManager.getInstance().closeDatabase();
                startActivity(intent);

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> listView, View view,
                                           int position, long id) {
                Cursor itemCursor = (Cursor) PatientSelect.this.listView.getItemAtPosition(position);
                int patientID = itemCursor.getInt(itemCursor.getColumnIndex(PatientPlacement.KEY_PatientID));
                Intent intent = new Intent(getApplicationContext(), CreateOrSelectPatient.class);
                intent.putExtra(KEY_EXTRA_CONTACT_ID, patientID);
             //   itemCursor.close();
             //   DatabaseManager.getInstance().closeDatabase();
                startActivity(intent);
                return true;
            }
        });



    }


    private void insertInitialData(){

        PatientRepo patientRepo = new PatientRepo();
        PlacementRepo placementRepo = new PlacementRepo();
        RoomRepo roomRepo = new RoomRepo();
        PatientPlacementRepo patientPlacementRepo = new PatientPlacementRepo();

        patientPlacementRepo.delete();
        roomRepo.delete();
        placementRepo.delete();
        patientRepo.delete();

        //Insert Sample data if the table is empty
        Placement placement = new Placement();

        placement.setName("Sacral");
        placement.setPlacementID("SA");
        placementRepo.insert(placement);

        placement.setName("HeelRight");
        placement.setPlacementID("HR");
        placementRepo.insert(placement);

        placement.setName("HeelLeft");
        placement.setPlacementID("HL");
        placementRepo.insert(placement);

        placement.setName("ThighRight");
        placement.setPlacementID("THR");
        placementRepo.insert(placement);

        placement.setName("ThighLeft");
        placement.setPlacementID("THL");
        placementRepo.insert(placement);

        placement.setName("Head");
        placement.setPlacementID("HD");
        placementRepo.insert(placement);

        Room room = new Room();

        room.setName("Zayed");
        room.setRoomID("Zayed");
        roomRepo.insert(room);

        room.setName("Carnegie");
        room.setRoomID("Carnegie");
        roomRepo.insert(room);

        room.setName("JHOC");
        room.setRoomID("JHOC");
        roomRepo.insert(room);

        Patient patient = new Patient();

        patient.setPatientID("1");
        patient.setName("John Doe");
        patient.setRoom("Carnegie");
        patientRepo.insert(patient);

        patient.setPatientID("2");
        patient.setName("Jane Doe");
        patient.setRoom("JHOC");
        patientRepo.insert(patient);

        PatientPlacement patientPlacement = new PatientPlacement();
        patientPlacement.setPatientID("1");
        patientPlacement.setPlacementID("SA");
        patientPlacement.setPatientPlacementSaveName("Path");
        patientPlacementRepo.insert(patientPlacement);

        patientPlacement.setPatientID("1");
        patientPlacement.setPlacementID("THR");
        patientPlacement.setPatientPlacementSaveName("Path");
        patientPlacementRepo.insert(patientPlacement);

        patientPlacement.setPatientID("2");
        patientPlacement.setPlacementID("HL");
        patientPlacement.setPatientPlacementSaveName("Path");
        patientPlacementRepo.insert(patientPlacement);
    }



    public static boolean areAllTrue(boolean[] array)
    {
        for(boolean b : array) if(!b) return false;
        return true;
    }

    private boolean checkAppPermissions() {

        int permissionChecker;
        boolean[] boolPermissions = new boolean[permissions.length];
        for (int pi = 0; pi<permissions.length;pi++) {
            permissionChecker = ContextCompat.checkSelfPermission(getApplicationContext(),permissions[pi]);
            if(permissionChecker == PackageManager.PERMISSION_GRANTED) {
                boolPermissions[pi] = true;
            }
            else{
                boolPermissions[pi] = false;
            }
        }

        return areAllTrue(boolPermissions);
    }

    private void requestAppPermissions() {

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, final String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {


                    boolean[] boolPermissions = new boolean[grantResults.length];
                    for (int pi = 0; pi<grantResults.length;pi++) {
                        if (grantResults[pi] == PackageManager.PERMISSION_GRANTED) {
                            boolPermissions[pi] = true;
                        } else {
                            boolPermissions[pi] = false;
                        }
                    }

                    if (areAllTrue(boolPermissions))
                        Snackbar.make(listView, "Proper permissions enabled.", Snackbar.LENGTH_LONG).show();
                    else {

                        Snackbar.make(listView, "Certain permissions are disabled.", Snackbar.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(permissions[0])) {
                                showMessageOKCancel("You need to allow access to multiple permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(permissions,
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(PatientSelect.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void checkFirstRun() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;


        // Get current version code
        int currentVersionCode = 0;
        try {
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            // handle exception
            e.printStackTrace();
            return;
        }

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {
            insertInitialData();

        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade

        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).commit();

    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(PatientSelect.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
}

