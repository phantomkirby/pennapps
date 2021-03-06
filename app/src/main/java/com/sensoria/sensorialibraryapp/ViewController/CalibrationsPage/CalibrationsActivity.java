package com.sensoria.sensorialibraryapp.ViewController.CalibrationsPage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseObject;
import com.sensoria.sensorialibrary.SAAnklet;
import com.sensoria.sensorialibrary.SAAnkletInterface;
import com.sensoria.sensorialibrary.SAFoundAnklet;
import com.sensoria.sensorialibraryapp.R;
import com.sensoria.sensorialibraryapp.ViewController.MainMenu.MenuActivity;

import java.util.ArrayList;

public class CalibrationsActivity extends ActionBarActivity implements SAAnkletInterface
{
    //The user should calibrate and pose with his/her feet in 4 different positions. This keeps
    //track which pose he/she has done.
    private static int calibrationPose = 0;
    //this will be used for each calibration pose
    int numberOfPointsRecorded = 0;
    PlaceholderFragment currFragment;
    protected boolean firstTime = false;
    protected SAAnklet anklet;
    boolean recordPoints = false;

    ArrayList<Integer> cal0_mtb1 = new ArrayList<Integer>();
    ArrayList<Integer> cal1_mtb1 = new ArrayList<Integer>();
    ArrayList<Integer> cal2_mtb1 = new ArrayList<Integer>();
    ArrayList<Integer> cal3_mtb1 = new ArrayList<Integer>();

    ArrayList<Integer> cal0_mtb5 = new ArrayList<Integer>();
    ArrayList<Integer> cal1_mtb5 = new ArrayList<Integer>();
    ArrayList<Integer> cal2_mtb5 = new ArrayList<Integer>();
    ArrayList<Integer> cal3_mtb5 = new ArrayList<Integer>();

    ArrayList<Integer> cal0_heel = new ArrayList<Integer>();
    ArrayList<Integer> cal1_heel = new ArrayList<Integer>();
    ArrayList<Integer> cal2_heel = new ArrayList<Integer>();
    ArrayList<Integer> cal3_heel = new ArrayList<Integer>();




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null)
        {
            PlaceholderFragment placeholderFragment = new PlaceholderFragment();
            currFragment = placeholderFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, placeholderFragment)
                    .commit();
        }

        setTitle("Calibrations");

        //sending calibration data to our backend and analyzing the metadata.
        Parse.initialize(this, "p6UR5hKjTNIa78j8HykqFR2zsvI5nbrwZAJvvWlC", "3dfrd51jLIiGjYTIirGKwl6GtQpfupZg30LaPzBI");

        anklet = new SAAnklet(this);
        onConnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        anklet.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        anklet.pause();

        //CALIBRATION STORAGE TO CLOUD
        //take range of heel, mtb1, mtb5
        //if range > 100... redo the whole calibration steps (the 4 steps)
        //take mean of heel, mtb1, mtb5, and half the range to person's google fit
        //use those numbers later

        SharedPreferences preferences = getSharedPreferences("temp", getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("cal0_mtb1_mean", calculateMean(cal0_mtb1));
        editor.putInt("cal0_mtb5_mean", calculateMean(cal0_mtb5));
        editor.putInt("cal0_heel_mean", calculateMean(cal0_heel));

        editor.putInt("cal1_mtb1_mean", calculateMean(cal1_mtb1));
        editor.putInt("cal1_mtb5_mean", calculateMean(cal1_mtb5));
        editor.putInt("cal1_heel_mean", calculateMean(cal1_heel));

        editor.putInt("cal2_mtb1_mean", calculateMean(cal2_mtb1));
        editor.putInt("cal2_mtb5_mean", calculateMean(cal2_mtb5));
        editor.putInt("cal2_heel_mean", calculateMean(cal2_heel));

        editor.putInt("cal3_mtb1_mean", calculateMean(cal3_mtb1));
        editor.putInt("cal3_mtb5_mean", calculateMean(cal3_mtb5));
        editor.putInt("cal3_heel_mean", calculateMean(cal3_heel));

        ///////////////////////////////

        editor.putInt("cal0_mtb1_range", calculateRange(cal0_mtb1));
        editor.putInt("cal0_mtb5_range", calculateRange(cal0_mtb5));
        editor.putInt("cal0_heel_range", calculateRange(cal0_heel));

        editor.putInt("cal1_mtb1_range", calculateRange(cal1_mtb1));
        editor.putInt("cal1_mtb5_range", calculateRange(cal1_mtb5));
        editor.putInt("cal1_heel_range", calculateRange(cal1_heel));

        editor.putInt("cal2_mtb1_range", calculateRange(cal2_mtb1));
        editor.putInt("cal2_mtb5_range", calculateRange(cal2_mtb5));
        editor.putInt("cal2_heel_range", calculateRange(cal2_heel));

        editor.putInt("cal3_mtb1_range", calculateRange(cal3_mtb1));
        editor.putInt("cal3_mtb5_range", calculateRange(cal3_mtb5));
        editor.putInt("cal3_heel_range", calculateRange(cal3_heel));

        editor.commit();
    }

    public int calculateMean(ArrayList<Integer> numbers)
    {
        int sum = 0;
        if (numbers != null && !numbers.isEmpty())
        {
            for (int i = 0; i < numbers.size(); i++)
            {
                sum += numbers.get(i);
            }
            return sum/numbers.size();
        }
        else
        {
            return 0;
        }
    }

    public int calculateRange(ArrayList<Integer> numbers)
    {
        if (numbers == null)
        {
            return 0;
        }

        int max = 0;
        int min = 9999;

        for (int i = 0; i < numbers.size(); i++)
        {
            if (numbers.get(i) > max)
            {
                max = numbers.get(i);
            }
            else if (numbers.get(i) < min)
            {
                min = numbers.get(i);
            }
        }

        return Math.abs(max-min);
    }

    public void storeRangeAndMeanOf(String valueToStore, int calibrationPoseNumber)
    {

    }

    @Override
    protected void onStop() {
        super.onStop();
        anklet.disconnect();
    }

    private String selectedCode;
    private String selectedMac;

    public void onStartScan(View view) {
        anklet.startScan();
        onConnect();
    }

    public void onStopScan(View view) {
        anklet.stopScan();
    }

    public void onConnect() {
        Log.w("SensoriaLibrary", "Connect to " + selectedCode + " " + selectedMac);
        anklet.deviceCode = selectedCode;
        anklet.deviceMac = selectedMac;
        anklet.connect();
    }

    @Override
    public void didDiscoverDevice() {

        Log.w("SensoriaLibrary", "Device Discovered!");

        Spinner s = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, anklet.deviceDiscoveredList);
        s.setAdapter(adapter);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                SAFoundAnklet deviceDiscovered = anklet.deviceDiscoveredList.get(position);
                selectedCode = deviceDiscovered.deviceCode;
                selectedMac = deviceDiscovered.deviceMac;

                Log.e("SensoriaLibrary", selectedCode + " " + selectedMac);

                if (!firstTime)
                {
                    firstTime = true;
                    Toast.makeText(CalibrationsActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    onConnect();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCode = null;

                if (!firstTime)
                {
                    firstTime = true;
                    Toast.makeText(CalibrationsActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    onConnect();
                }
            }
        });
    }

    @Override
    public void didConnect() {
        Log.w("SensoriaLibrary", "Device Connected!");
        if (!firstTime)
        {
            firstTime = true;
            Toast.makeText(CalibrationsActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            onConnect();
        }
    }

    public void didError(String message) {

        Log.e("SensoriaLibrary", message);
        if (!firstTime)
        {
            firstTime = true;
            Toast.makeText(CalibrationsActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            onConnect();
        }
    }

    @Override
    public void didUpdateData()
    {
        if (recordPoints)
        {
            switch(calibrationPose)
            {
                case 0:
                    if (anklet.tick%10 == 0)
                    {
                        ParseObject dataPoint = new ParseObject("Cal0");
                        dataPoint.put("mtb1", anklet.mtb1);
                        dataPoint.put("mtb5", anklet.mtb5);
                        dataPoint.put("heel", anklet.heel);
                        dataPoint.put("accz", anklet.accZ);
                        dataPoint.saveInBackground();

                        Log.e("cal 0 mtb1", ""+anklet.mtb1);
                        Log.e("cal 0 mtb5", ""+anklet.mtb5);
                        Log.e("cal 0 heel", ""+anklet.heel);

                        cal0_heel.add(anklet.heel);
                        cal0_mtb1.add(anklet.mtb1);
                        cal0_mtb5.add(anklet.mtb5);

                        numberOfPointsRecorded++;
                    }
                    break;
                case 1:
                    if (anklet.tick%10 == 0)
                    {
                        ParseObject dataPoint = new ParseObject("Cal1");
                        dataPoint.put("mtb1", anklet.mtb1);
                        dataPoint.put("mtb5", anklet.mtb5);
                        dataPoint.put("heel", anklet.heel);
                        dataPoint.put("accz", anklet.accZ);
                        dataPoint.saveInBackground();

                        cal1_heel.add(anklet.heel);
                        cal1_mtb1.add(anklet.mtb1);
                        cal1_mtb5.add(anklet.mtb5);

                        Log.e("cal 1 mtb1", ""+anklet.mtb1);
                        Log.e("cal 1 mtb5", ""+anklet.mtb5);
                        Log.e("cal 1 heel", ""+anklet.heel);

                        numberOfPointsRecorded++;
                    }
                    break;
                case 2:
                    if (anklet.tick%10 == 0)
                    {
                        ParseObject dataPoint = new ParseObject("Cal2");
                        dataPoint.put("mtb1", anklet.mtb1);
                        dataPoint.put("mtb5", anklet.mtb5);
                        dataPoint.put("heel", anklet.heel);
                        dataPoint.put("accz", anklet.accZ);
                        dataPoint.saveInBackground();

                        cal2_heel.add(anklet.heel);
                        cal2_mtb1.add(anklet.mtb1);
                        cal2_mtb5.add(anklet.mtb5);

                        Log.e("cal 2 mtb1", ""+anklet.mtb1);
                        Log.e("cal 2 mtb5", ""+anklet.mtb5);
                        Log.e("cal 2 heel", ""+anklet.heel);

                        numberOfPointsRecorded++;
                    }
                    break;
                case 3:
                    if (anklet.tick%10 == 0)
                    {
                        ParseObject dataPoint = new ParseObject("Cal3");
                        dataPoint.put("mtb1", anklet.mtb1);
                        dataPoint.put("mtb5", anklet.mtb5);
                        dataPoint.put("heel", anklet.heel);
                        dataPoint.put("accz", anklet.accZ);
                        dataPoint.saveInBackground();

                        cal3_heel.add(anklet.heel);
                        cal3_mtb1.add(anklet.mtb1);
                        cal3_mtb5.add(anklet.mtb5);

                        Log.e("cal 3 mtb1", ""+anklet.mtb1);
                        Log.e("cal 3 mtb5", ""+anklet.mtb5);
                        Log.e("cal 3 heel", ""+anklet.heel);

                        numberOfPointsRecorded++;
                    }
                    break;
            }
        }

        if (numberOfPointsRecorded == 10)
        {
            Log.e("","should go on to next pose");
            numberOfPointsRecorded = 0;
//            anklet.pause();
//            anklet.disconnect();
            calibrationPose++;
            if (calibrationPose == 4)
            {
                Intent intent = new Intent(CalibrationsActivity.this, MenuActivity.class);
                startActivity(intent);
            }
            else
            {
                recordPoints = false;
                getSupportFragmentManager().beginTransaction().remove(currFragment).commit();
                PlaceholderFragment newPlaceHolderFragment = new PlaceholderFragment();
                currFragment = newPlaceHolderFragment;
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, newPlaceHolderFragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.calibrations_fragment, container, false);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.imageViewFootPosition);
            TextView textView = (TextView) rootView.findViewById(R.id.textView11);

            switch(calibrationPose) {
                case 0:
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.foot1));
                    textView.setText("HEEL STRIKE");
                    break;
                case 1:
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.foot2));
                    textView.setText("FLAT FOOT");
                    break;
                case 2:
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.foot2));
                    textView.setText("LEAN FORWARD"); //same pic as last one because barely any difference
                    break;
                case 3:
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.foot4));
                    textView.setText("HEEL UP");
                    break;
                default: break;
            }

            final Button button = (Button) rootView.findViewById(R.id.button_calibrations);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ((CalibrationsActivity)getActivity()).recordPoints = true;
//                    ((CalibrationsActivity)getActivity()).firstTime = false;
//                    ((CalibrationsActivity)getActivity()).anklet.resume();
                    button.setText("Calibrating...");
                    button.setEnabled(false);
                }
            });
            return rootView;
        }
    }
}
