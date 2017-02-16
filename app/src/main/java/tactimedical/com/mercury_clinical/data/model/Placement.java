package tactimedical.com.mercury_clinical.data.model;

public class Placement {

    public static final String TAG = Placement.class.getSimpleName();
    public static final String TABLE = "Placement";
    // Labels Table Columns names
    public static final String KEY_PlacementID = "PlacementID";
    public static final String KEY_Name = "Name";

    private String placementID;
    private String name;


    public String getPlacementID() {
        return placementID;
    }

    public void setPlacementID(String placementID) {
        this.placementID = placementID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



}
