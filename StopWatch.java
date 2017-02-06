package imran.app.timesup;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Created by Imran on 8/30/2015.
 */
public class StopWatch extends Fragment {
    Boolean flag;
    Button start, pause, reset;
    Chronometer timer;
    TextView SetTime_text;
    long time = 0, time_elapsed = 0;

    //Set Time Dialog declarations
    NumberPicker npMinutes, npSeconds;
    AlertDialog alertDw;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View Stopwatch_fragment = inflater.inflate(R.layout.stopwatch_layout, container, false);

        start = (Button) Stopwatch_fragment.findViewById(R.id.button_start);
        pause = (Button) Stopwatch_fragment.findViewById(R.id.button_pause);
        reset = (Button) Stopwatch_fragment.findViewById(R.id.button_reset);

        timer = (Chronometer) Stopwatch_fragment.findViewById(R.id.chronometer);

        SetTime_text = (TextView) Stopwatch_fragment.findViewById(R.id.textView_SetTimeText);
        SetTime_text.setText("00:00");

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_start:
                        Stopwatch_fragment.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                        timer.setBase(SystemClock.elapsedRealtime() + time);
                        timer.start();
                        //write("1");
                        break;
                    case R.id.button_pause:
                        Stopwatch_fragment.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        time = timer.getBase() - SystemClock.elapsedRealtime();
                        timer.stop();
                        //write("2");
                        break;
                }
            }
        };

        int[] ids = {R.id.button_start, R.id.button_pause};

        for (int i : ids) Stopwatch_fragment.findViewById(i).setOnClickListener(listener);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.stop();
                time = 0;
                Stopwatch_fragment.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
                timer.setText("00:00");
                timer.setBase(SystemClock.elapsedRealtime());
                flag = false;
            }
        });

        return Stopwatch_fragment;
    }


    //***************************Number Picker Dialog*****************************//
    public void showPickerDialog(View view) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog, null);

        npMinutes = (NumberPicker) v.findViewById(R.id.numberPickerMinutes);
        npSeconds = (NumberPicker) v.findViewById(R.id.numberPickerSeconds);

        npMinutes.setMaxValue(59);
        npMinutes.setMinValue(0);

        npSeconds.setMaxValue(59);
        npSeconds.setMinValue(0);

        npMinutes.setWrapSelectorWheel(true);
        npSeconds.setWrapSelectorWheel(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setView(v);
        builder.setTitle("Set Time:");
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast_Text = npMinutes.getValue() + ":" + npSeconds.getValue();
                Toast.makeText(getBaseContext(), Toast_Text, Toast.LENGTH_SHORT).show();


                SetTime_text.setText(new StringBuilder().append(pad(npMinutes.getValue()))
                        .append(":").append(pad(npSeconds.getValue())));
            }

            private String pad(int c) {
                if (c >= 10)
                    return String.valueOf(c);

                else
                    return "0" + String.valueOf(c);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDw.dismiss();
            }
        });
        alertDw = builder.create();
        alertDw.show();
    }


}

