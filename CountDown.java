package imran.app.timesup;

import android.app.AlertDialog;
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

/*
 * Created by Imran on 8/30/2015.
 */
public class CountDown extends Fragment {
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
       final View CountDown_fragment = inflater.inflate(R.layout.countdown_layout, container, false);

        start = (Button) CountDown_fragment.findViewById(R.id.button_start);
        pause = (Button) CountDown_fragment.findViewById(R.id.button_pause);
        reset = (Button) CountDown_fragment.findViewById(R.id.button_reset);

        timer = (Chronometer) CountDown_fragment.findViewById(R.id.chronometer);

        SetTime_text = (TextView) CountDown_fragment.findViewById(R.id.textView_SetTimeText);
        SetTime_text.setText("00:00");

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_start:
                        CountDown_fragment.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                        timer.setBase(SystemClock.elapsedRealtime() + time);
                        timer.start();
                        //write("1");
                        break;
                    case R.id.button_pause:
                        CountDown_fragment.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        time = timer.getBase() - SystemClock.elapsedRealtime();
                        timer.stop();
                        //write("2");
                        break;
                }
            }
        };

        int[] ids = {R.id.button_start, R.id.button_pause};

        for (int i : ids) CountDown_fragment.findViewById(i).setOnClickListener(listener);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.stop();
                time = 0;
                CountDown_fragment.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
                timer.setText("00:00");
                timer.setBase(SystemClock.elapsedRealtime());
                flag = false;
            }
        });

        return CountDown_fragment;
    }

}
