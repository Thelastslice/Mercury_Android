package tactimedical.com.mercury_clinical.grapher;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.DefaultLabelFormatter;
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

public class GraphFragment extends DialogFragment {
    private final static String TAG = "GraphFragment";
    private final static String PARAM_FILE = "patientCSV";
    private final static String PARAM_ID = "mButtonID";
    private Date mDate;
    private DataPoint[] mDataPoints;
    private final Handler mHandler = new Handler();
    private String patientCSV;
    private int mButtonID;
    private GraphView mGraph;
    private Button mCancel;


    public static GraphFragment getInstance( int mButtonID, String patientCSV) {
        final GraphFragment fragment = new GraphFragment();

        final Bundle args = new Bundle();
        args.putInt(PARAM_ID,mButtonID);
        args.putString(PARAM_FILE,patientCSV);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();

        if (args.containsKey(PARAM_FILE)) {
            patientCSV = args.getString(PARAM_FILE);
        }
        if (args.containsKey(PARAM_ID)) {
            mButtonID = args.getInt(PARAM_ID);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_graph, null);

        mGraph = (GraphView) dialogView.findViewById(R.id.sensorGraph);
        Viewport mGraphEdit = mGraph.getViewport();
        mCancel = (Button) dialogView.findViewById(R.id.graph_cancel);
        builder.setTitle(R.string.graph_title);

        final AlertDialog dialog = builder.setView(dialogView).create();

        List<String[]> result = readCVSFromAssetFolder(patientCSV);
        DataPoint[] mDataPoints = new DataPoint[result.size()-1];

        for (int i = 1; i < result.size(); i++) { //Skip first header row

            String[] rows = result.get(i);
            SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss:SS", Locale.US);
            try {
                mDate = format.parse(rows[0]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            mDataPoints[i-1] = new DataPoint(mDate.getTime(), Double.parseDouble(rows[mButtonID]));
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(mDataPoints);
        mGraph.addSeries(series);
        series.setColor(getResources().getColor(R.color.colorAccent,null));

        mGraphEdit.setScalable(true);
        mGraphEdit.setScrollable(true);
        mGraphEdit.setXAxisBoundsManual(true);
        mGraphEdit.setYAxisBoundsManual(true);
        mGraphEdit.setMinY(0);
        mGraphEdit.setMaxY(1.1*mGraphEdit.getMaxY(true));
        mGraphEdit.setMinX(mGraphEdit.getMinX(true));
        mGraphEdit.setMaxX(mGraphEdit.getMaxX(true));

        mGraphEdit.scrollToEnd();
        GridLabelRenderer gridLabel = mGraph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle(getResources().getString(R.string.xaxis));
        gridLabel.setHorizontalAxisTitleTextSize(20);
        gridLabel.setVerticalAxisTitleTextSize(20);
        gridLabel.setVerticalAxisTitle(getResources().getString(R.string.yaxis));
        gridLabel.setHorizontalLabelsAngle(15);
        gridLabel.setHumanRounding(false);
        gridLabel.setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()) {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    Date d = new Date((long) (value));
                    SimpleDateFormat mDateFormat = new SimpleDateFormat("yy/MM/dd HH:mm",Locale.US);
                    return mDateFormat.format(d);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.graph_cancel) {
                    dialog.cancel();
                }
            }
        });

        return dialog;
    }

    private List<String[]> readCVSFromAssetFolder(String patientFileString){
        File file = new File(patientFileString);
        List<String[]> csvLine = new ArrayList<>();
        String[] content = null;
        try {
            FileInputStream fin = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fin));
            String line = "";
            while((line = br.readLine()) != null){
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
