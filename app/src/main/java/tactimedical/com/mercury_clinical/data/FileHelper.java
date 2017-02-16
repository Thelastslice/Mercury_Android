package tactimedical.com.mercury_clinical.data;


import android.os.Environment;

import java.io.File;


public class FileHelper {

    final static File root = new File(Environment.getExternalStorageDirectory(), "Mercury Patch");

    public static void checkDocRootExists(){
        if (!root.exists()) {
            root.mkdir();
        }
    }

    public static File patientFilePath(String patientID){
        final File patientDir = new File(root, patientID);
        if (!patientDir.exists()) {
            patientDir.mkdir();
        }
        return patientDir;

    }

    public static File createCSVFile(String patientName, String placementID){
        checkDocRootExists();
        File patientDir = patientFilePath(patientName);
        File patientCSV = new File(patientDir, patientName+" "+placementID+".csv");

        return patientCSV;
    }

}



