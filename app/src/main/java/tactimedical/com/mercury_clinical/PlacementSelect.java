package tactimedical.com.mercury_clinical;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import tactimedical.com.mercury_clinical.data.FileHelper;
import tactimedical.com.mercury_clinical.data.model.Patient;
import tactimedical.com.mercury_clinical.data.model.PatientPlacement;
import tactimedical.com.mercury_clinical.data.repo.PatientPlacementRepo;
import tactimedical.com.mercury_clinical.model.PatientPlacementList;
import tactimedical.com.mercury_clinical.uart.UARTActivity;

public class PlacementSelect extends AppCompatActivity implements View.OnClickListener  {

    public static final String TAG = PlacementSelect.class.getSimpleName();
    int patientID;
    Button sacral_Button, thigh_right_Button, thigh_left_Button, heel_right_Button, heel_left_Button, head_Button;
    public final static String KEY_FILE = "KEY_FILE";
    public final static String KEY_ACTIVITY_ID = "KEY_ACTIVITY_ID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"Entered Placement");
        String previousActivity= getIntent().getStringExtra(KEY_ACTIVITY_ID);
        if(previousActivity.equals("PS")){
            patientID = getIntent().getIntExtra(PatientSelect.KEY_EXTRA_CONTACT_ID,0);
        }
        else{
            patientID = getIntent().getIntExtra(CreateOrSelectPatient.KEY_EXTRA_CONTACT_ID,0);
        }
        Log.d(TAG,Integer.toString(patientID));

        setContentView(R.layout.placement_select);
        ButtonInitializer();


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sacral_button:
                placementSelected(R.string.SA,"SA");
                return;

            case R.id.thigh_left_button:
                placementSelected(R.string.THL,"THL");
                return;

            case R.id.thigh_right_button:
                placementSelected(R.string.THR,"THR");
                return;

            case R.id.heel_left_button:
                placementSelected(R.string.HL,"HL");
                return;

            case R.id.heel_right_button:
                placementSelected(R.string.HR,"HR");
                return;
            case R.id.head_button:
                placementSelected(R.string.HD,"HD");
        }
    }

    public void placementSelected(int placeID, final String placementString){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(placeID)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PatientPlacementRepo patientPlacementRepo = new PatientPlacementRepo();
                        PatientPlacement patientPlacement = new PatientPlacement();
                        Patient patient = new Patient();

                        if (patientPlacementRepo.placementExists(patientID,placementString)){
                            List<PatientPlacementList> patientPlacementValues= patientPlacementRepo.getPatientPlacement(patientID,placementString);

                            if(patientPlacementValues.size()>1){
                                Toast.makeText(getApplicationContext(),"Database Error: multiple entries for same patient-placement combo, please edit patient.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), PatientSelect.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                            else{
                                /*Log.d(TAG, patientPlacementValues.get(0).getPatientName());
                                Log.d(TAG, patientPlacementValues.get(0).getPlacementID());*/

                                File dataOutput = FileHelper.createCSVFile(patientPlacementValues.get(0).getPatientName(),
                                        patientPlacementValues.get(0).getPlacementID());
                                Toast toast = Toast.makeText(getApplicationContext(), "Patient: " +
                                        patientPlacementValues.get(0).getPatientName()+
                                        " Location: "+
                                        patientPlacementValues.get(0).getPlacementID()+
                                        " Selected.", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP,0,100);
                                toast.show();
                                /*Log.d(TAG, dataOutput.getName());
                                Log.d(TAG, dataOutput.getPath());*/
                                Intent intent = new Intent(getApplicationContext(), UARTActivity.class);
                                intent.putExtra(KEY_FILE, dataOutput);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }

                        }
                        else{
                            patientPlacement.setPatientID(Integer.toString(patientID));
                            patientPlacement.setPlacementID(placementString);
                            patientPlacement.setPatientPlacementSaveName("Path");
                            patientPlacementRepo.insert(patientPlacement);

                            List<PatientPlacementList> patientPlacementValues= patientPlacementRepo.getPatientPlacement(patientID,placementString);

                            if(patientPlacementValues.size()>1){
                                Toast.makeText(getApplicationContext(),"Database Error: multiple entries for same patient-placement combo, please edit patient.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), PatientSelect.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                            else{/*
                                Log.d(TAG, patientPlacementValues.get(0).getPatientName());
                                Log.d(TAG, patientPlacementValues.get(0).getPlacementID());*/

                                File dataOutput = FileHelper.createCSVFile(patientPlacementValues.get(0).getPatientName(),
                                        patientPlacementValues.get(0).getPlacementID());

                                Toast toast = Toast.makeText(getApplicationContext(), "Patient: " +
                                        patientPlacementValues.get(0).getPatientName()+
                                        " Location: "+
                                        patientPlacementValues.get(0).getPlacementID()+
                                        " Selected.", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.TOP,0,100);
                                        toast.show();

                                /*Log.d(TAG, dataOutput.getName());
                                Log.d(TAG, dataOutput.getPath());*/
                                Intent intent = new Intent(getApplicationContext(), UARTActivity.class);
                                intent.putExtra(KEY_FILE, dataOutput);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }


                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog d = builder.create();
        d.setTitle("Placement Selected.");
        d.show();




    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(new Intent(PlacementSelect.this, PatientSelect.class));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    public void ButtonInitializer(){
        sacral_Button = (Button) findViewById(R.id.sacral_button);
        sacral_Button.setOnClickListener(this);
        thigh_right_Button = (Button) findViewById(R.id.thigh_right_button);
        thigh_right_Button.setOnClickListener(this);
        thigh_left_Button = (Button) findViewById(R.id.thigh_left_button);
        thigh_left_Button.setOnClickListener(this);
        heel_right_Button = (Button) findViewById(R.id.heel_right_button);
        heel_right_Button.setOnClickListener(this);
        heel_left_Button = (Button) findViewById(R.id.heel_left_button);
        heel_left_Button.setOnClickListener(this);
        head_Button = (Button) findViewById(R.id.head_button);
        head_Button.setOnClickListener(this);
    }

}


