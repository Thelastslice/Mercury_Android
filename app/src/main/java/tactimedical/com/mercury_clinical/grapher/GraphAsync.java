package tactimedical.com.mercury_clinical.grapher;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tactimedical.com.mercury_clinical.R;

/**
 * Created by Alex on 2017-02-15.
 */

public class GraphAsync extends AsyncTask<DataPoint[],Void,DataPoint[]> {
    Activity atv;
    int _ID;
    String patientCSV;
    AlertDialog.Builder alertDialog;
    GraphView mGraph;
    Date mDate;
    Button mCancel;

    public GraphAsync (Activity atv,int id,String patient) {
        this.atv = atv;
        this._ID = id;
        this.patientCSV = patient;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        alertDialog = new AlertDialog.Builder(atv);
        Log.d("TAG","OnPreExecute");
    }

    @Override
    protected DataPoint[] doInBackground(DataPoint[]... params) {
        Log.d("TAG","background");
        List<String[]> result = readCVSFromAssetFolder(patientCSV);
        DataPoint[] mDataPoints = new DataPoint[result.size() - 1];

        for (int i = 1; i < result.size(); i++) { //Skip first header row

            String[] rows = result.get(i);
            SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss:SS", Locale.US);
            try {
                mDate = format.parse(rows[0]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            mDataPoints[i - 1] = new DataPoint(mDate.getTime(), Double.parseDouble(rows[_ID]));
        }
        return mDataPoints;
    }

    @Override
    protected void onPostExecute(DataPoint[] result)  // contain according to regCheck.php
    {
        Log.d("TAG","OnPostExecute");
        View dialogView = LayoutInflater.from(atv).inflate(R.layout.fragment_graph, null);
        alertDialog.setTitle(R.string.graph_title);
        mGraph = (GraphView) dialogView.findViewById(R.id.sensorGraph);
        mCancel = (Button) dialogView.findViewById(R.id.graph_cancel);
        Viewport mGraphEdit = mGraph.getViewport();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(result);
        mGraph.addSeries(series);
        mGraphEdit.setScalable(true);
        mGraphEdit.setScrollable(true);
        mGraphEdit.setXAxisBoundsManual(true);
        mGraphEdit.setYAxisBoundsManual(true);
        mGraphEdit.setMinY(0);
        mGraphEdit.setMaxY(1.1 * mGraphEdit.getMaxY(true));
        mGraphEdit.setMinX(mGraphEdit.getMinX(true));
        mGraphEdit.setMaxX(mGraphEdit.getMaxX(true));
        mGraphEdit.scrollToEnd();
        GridLabelRenderer gridLabel = mGraph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time");
        gridLabel.setHorizontalAxisTitleTextSize(20);
        gridLabel.setVerticalAxisTitleTextSize(20);
        gridLabel.setVerticalAxisTitle("mmHg");
        gridLabel.setHorizontalLabelsAngle(15);
        gridLabel.setHumanRounding(false);
        gridLabel.setLabelFormatter(new DateAsXAxisLabelFormatter(atv) {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    Date d = new Date((long) (value));
                    SimpleDateFormat mDateFormat = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.US);
                    return mDateFormat.format(d);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        final AlertDialog dialog = alertDialog.setView(dialogView).create();
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.graph_cancel) {
                    dialog.cancel();
                }
            }
        });


        dialog.show();
        super.onPostExecute(result);
    }


    private List<String[]> readCVSFromAssetFolder(String patientFileString) {
        File file = new File(patientFileString);
        List<String[]> csvLine = new ArrayList<>();
        String[] content = null;
        try {
            FileInputStream fin = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fin));
            String line = "";
            while ((line = br.readLine()) != null) {
                content = line.split(",");
                csvLine.add(content);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvLine;
    }

}

