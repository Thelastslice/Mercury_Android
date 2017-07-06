package tactimedical.com.mercury_clinical;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.util.Log;

import java.util.List;

import tactimedical.com.mercury_clinical.PatientSelect;
import tactimedical.com.mercury_clinical.data.model.Patient;
import tactimedical.com.mercury_clinical.data.model.PatientPlacement;
import tactimedical.com.mercury_clinical.data.model.Room;
import tactimedical.com.mercury_clinical.data.repo.PatientPlacementRepo;
import tactimedical.com.mercury_clinical.data.repo.PatientRepo;
import tactimedical.com.mercury_clinical.data.repo.PlacementRepo;
import tactimedical.com.mercury_clinical.data.repo.RoomRepo;
import tactimedical.com.mercury_clinical.model.PatientPlacementList;


public class CreateOrSelectPatient extends AppCompatActivity  implements View.OnClickListener {
    private final String TAG = CreateOrSelectPatient.class.getSimpleName().toString();

    PatientRepo patientRepo = new PatientRepo();
    PlacementRepo placementRepo = new PlacementRepo();
    RoomRepo roomRepo = new RoomRepo();
    PatientPlacementRepo patientPlacementRepo = new PatientPlacementRepo();

    public final static String KEY_EXTRA_CONTACT_ID = "KEY_EXTRA_CONTACT_ID";
    public final static String KEY_ACTIVITY_ID = "KEY_ACTIVITY_ID";
    EditText nameEditText;
    EditText idEditText;
    EditText roomEditText;

    Button saveButton, editButton, deleteButton;
    LinearLayout buttonLayout;

    int patientID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        patientID = getIntent().getIntExtra(PatientSelect.KEY_EXTRA_CONTACT_ID, 0);

        Log.d(TAG,Integer.toString(patientID));
        setContentView(R.layout.patient_edit);
        nameEditText = (EditText) findViewById(R.id.editTextName);
        idEditText = (EditText) findViewById(R.id.editTextID);
        roomEditText = (EditText) findViewById(R.id.editTextRoom);

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
        buttonLayout = (LinearLayout) findViewById(R.id.buttonLayout);
        editButton = (Button) findViewById(R.id.editButton);
        editButton.setOnClickListener(this);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);

        if(patientID > 0) {
            saveButton.setVisibility(View.GONE);
            buttonLayout.setVisibility(View.VISIBLE);

            Cursor rs = patientPlacementRepo.getPatient(patientID);
            rs.moveToFirst();
            String patientName = rs.getString(rs.getColumnIndex(Patient.KEY_Name));
            String patientIDText = Integer.toString(rs.getInt(rs.getColumnIndex(PatientPlacement.KEY_PatientID)));
            String patientRoom = rs.getString(rs.getColumnIndex(Patient.KEY_RoomID));

            if (!rs.isClosed()) {
                rs.close();
            }

            nameEditText.setText(patientName);
            nameEditText.setFocusable(false);
            nameEditText.setClickable(false);

            idEditText.setText(patientIDText);
            idEditText.setFocusable(false);
            idEditText.setClickable(false);

            roomEditText.setText(patientRoom);
            roomEditText.setFocusable(false);
            roomEditText.setClickable(false);

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveButton:
                if (!nameEditText.getText().toString().matches("^[-\\w.]+")||
                        !idEditText.getText().toString().matches("^[-\\w.]+") ||
                        !roomEditText.getText().toString().matches("^[-\\w.]+")){

                    Toast.makeText(getApplicationContext(), "Some fields have not been filled.", Toast.LENGTH_SHORT).show();
                }
                else{
                    persistPatient();
                }
                return;

            case R.id.editButton:
                saveButton.setVisibility(View.VISIBLE);
                buttonLayout.setVisibility(View.GONE);

                nameEditText.setEnabled(true);
                nameEditText.setFocusableInTouchMode(true);
                nameEditText.setClickable(true);

                idEditText.setEnabled(true);
                idEditText.setFocusableInTouchMode(true);
                idEditText.setClickable(true);

                roomEditText.setEnabled(true);
                roomEditText.setFocusableInTouchMode(true);
                roomEditText.setClickable(true);

                return;

            case R.id.deleteButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.deletePatient)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                patientPlacementRepo.deletePatient(patientID);

                                Toast.makeText(getApplicationContext(), patientID+": Deleted", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), PatientSelect.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                AlertDialog d = builder.create();
                d.setTitle("Delete Patient?");
                d.show();
                return;
        }
    }

    public void persistPatient() {

        if(patientID > 0) {

            if(!patientPlacementRepo.roomExists(roomEditText.getText().toString())){
                Room room = new Room();
                room.setName(roomEditText.getText().toString());
                room.setRoomID(roomEditText.getText().toString());
                roomRepo.insert(room);
            }
            Patient patient = new Patient();
            patient.setPatientID(idEditText.getText().toString());
            patient.setName(nameEditText.getText().toString());
            patient.setRoom(roomEditText.getText().toString());

            if(patientRepo.updatePatient(patient)){
                Toast.makeText(getApplicationContext(), "Patient Update Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), PatientSelect.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            else {
                Toast.makeText(getApplicationContext(), "Patient Update Failed", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if(!patientPlacementRepo.roomExists(roomEditText.getText().toString())){
                Room room = new Room();
                room.setName(roomEditText.getText().toString());
                room.setRoomID(roomEditText.getText().toString());
                roomRepo.insert(room);
            }

            final Patient patient = new Patient();
            patient.setPatientID(idEditText.getText().toString());
            patient.setName(nameEditText.getText().toString());
            patient.setRoom(roomEditText.getText().toString());

            if(patientRepo.patientExists(idEditText.getText().toString())){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.existsPatient)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                patientRepo.updatePatient(patient);
                                Toast.makeText(getApplicationContext(), "Patient Update Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), PatientSelect.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                AlertDialog d = builder.create();
                d.setTitle("Patient Already Exists.");
                d.show();
                return;
            }

            else{
                if(patientRepo.insert(patient)) {
                        Toast.makeText(getApplicationContext(), "Patient Inserted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), PlacementSelect.class);
                        intent.putExtra(KEY_EXTRA_CONTACT_ID, Integer.parseInt(idEditText.getText().toString()));
                        intent.putExtra(KEY_ACTIVITY_ID,"CEP");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);


                }
                else{
                    Toast.makeText(getApplicationContext(), "Could not Insert patient", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), PatientSelect.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }


            }

        }
    }
}
