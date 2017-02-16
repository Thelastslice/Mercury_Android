package tactimedical.com.mercury_clinical.data.model;

public class PatientPlacement {
    public static final String TAG = PatientPlacement.class.getSimpleName();
    public static final String TABLE = "PatientPlacement";

    // Labels Table Columns names
    public static final String KEY_RunningID = "RunningID";
    public static final String KEY_PatientID = "_id";
    public static final String KEY_PlacementID = "PlacementID";
    public static final String KEY_PatientPlacementSaveName = "PatientPlacementSaveName";

    public   String patientID;
    public   String placementID;
    public   String PatientPlacementSaveName;

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getPlacementID() {
        return placementID;
    }

    public void setPlacementID(String placementID) {
        this.placementID = placementID;
    }

    public String getPatientPlacementSaveName() {
        return PatientPlacementSaveName;
    }

    public void setPatientPlacementSaveName(String PatientPlacementSaveName) {
        this.PatientPlacementSaveName = PatientPlacementSaveName;
    }
}
