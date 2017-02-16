package tactimedical.com.mercury_clinical.app;

import android.app.Application;
import android.content.Context;

import tactimedical.com.mercury_clinical.data.DBHelper;
import tactimedical.com.mercury_clinical.data.DatabaseManager;


public class App extends Application {
    private static Context context;
    private static DBHelper dbHelper;

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this.getApplicationContext();
        dbHelper = new DBHelper();
        DatabaseManager.initializeInstance(dbHelper);

    }


    public static Context getContext(){
        return context;
    }

}

