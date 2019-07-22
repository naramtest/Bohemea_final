package com.emargystudio.bohemea.MakeReservation;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.emargystudio.bohemea.Model.Reservation;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.CommonReservation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Objects;


public class DataFragment extends Fragment {

    //widget
    private EditText timeEdt,calendarEdt,chairEdt;
    private FloatingActionButton nextFAB;
    private TextInputLayout timeLayout,calendarLayout,chairLayout;


    //var
    private int chosenYear,chosenMonth,chosenDay;
    private double chosenStartHour , chosenEndHour;
    private int chosenChair;
    private Reservation reservation = new Reservation();
    private TableFragment tableFragment;


    public DataFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //use findViewById to init views
        initView(view);

        //get current date/time to use it in date and time picker
        Calendar calendar = Calendar.getInstance();
        final int currentYear = calendar.get(Calendar.YEAR);
        final int currentMonth = calendar.get(Calendar.MONTH);
        final int currentDay   = calendar.get(Calendar.DAY_OF_MONTH);
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        final int currentMinute = calendar.get(Calendar.MINUTE);


        tableFragment = new TableFragment();

        //edit text onclick and on textChang listener + FAB onClick listener
        listener(currentYear, currentMonth, currentDay, currentHour, currentMinute);

    }



    //init fragment view and set custom font on the text header
    private void initView(@NonNull View view) {
        //widget
        TextView headerTxt = view.findViewById(R.id.data_header);
        timeEdt        = view.findViewById(R.id.edt_time);
        calendarEdt    = view.findViewById(R.id.edt_calender);
        chairEdt       = view.findViewById(R.id.edt_chair_number);
        nextFAB        = view.findViewById(R.id.next_fab);
        timeLayout     = view.findViewById(R.id.time_Layout);
        calendarLayout = view.findViewById(R.id.calendar_layout);
        chairLayout    = view.findViewById(R.id.chair_layout);


        if (getActivity()!=null){
            Typeface face = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Kabrio_Regular.ttf");
            headerTxt.setTypeface(face);
        }
    }

    //open dialog with calendar to let user choose the date they want to make a reservation on it
    private void datePicker(int currentYear, int currentMonth, int currentDay){

        DatePickerDialog dialog = new DatePickerDialog(Objects.requireNonNull(getContext()), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                chosenYear = year;
                chosenMonth = month+1;
                chosenDay = dayOfMonth;
                calendarEdt.setText(chosenYear +"-"+chosenMonth+"-"+chosenDay);
                //remove error from editText if exists
                calendarLayout.setErrorEnabled(false);


            }
        }, currentYear, currentMonth, currentDay);
        dialog.show();

    }

    //open dialog with clock to let user choose the time they want to make a reservation on it
    private void timePicker(int currentHour, int currentMinute){

        TimePickerDialog timeDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                chosenStartHour = formatStartingHour(hourOfDay,minute);
                timeEdt.setText(CommonReservation.changeHourFormat(chosenStartHour));
                chosenEndHour = chosenStartHour + 2;
                timeLayout.setErrorEnabled(false);


            }
        },currentHour,currentMinute,false);
        timeDialog.show();
    }


    // format time to look like this HH:mm with only tow number after decimal point
    private double formatStartingHour(int hour, int minute){
        double d = minute /100.00;
        double h = hour+d;
        return round(h);
    }
    private static double round(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    // make reservation object with chosen values
    private void setupReservationModel(){
        reservation.setYear(chosenYear);
        reservation.setMonth(chosenMonth);
        reservation.setDay(chosenDay);
        reservation.setStartHour(chosenStartHour);
        reservation.setEnd_hour(chosenEndHour);
        reservation.setChairNumber(chosenChair);
    }


    //return view to its status after changing it (landscape or went to table fragment )
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        setupReservationModel();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.reservation_bundle), reservation);
        outState.putBundle("args",args);
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null){
            Bundle bundle = savedInstanceState.getBundle("args");
            if (bundle != null) {
                reservation = bundle.getParcelable(getString(R.string.reservation_bundle));
                if (reservation!=null) {
                    chosenYear = reservation.getYear();
                    chosenMonth = reservation.getMonth();
                    chosenDay = reservation.getDay();
                    chosenStartHour = reservation.getStartHour();
                    chosenEndHour = reservation.getEnd_hour();
                    chosenChair = reservation.getChairNumber();
                }
            }

        }
        super.onActivityCreated(savedInstanceState);
    }



    //check user input values validation and if every things ok go to tableFragment
    private void nextFragment(){
        if (chosenYear == 0 || chosenMonth == 0 || chosenDay == 0) {
            calendarLayout.setError(getString(R.string.phone_number_empty));

        }else if (chosenStartHour == 0 && chosenEndHour == 0 ) {
            timeLayout.setError(getString(R.string.phone_number_empty));
        }else if (chosenChair == 0){
            chairLayout.setError(getString(R.string.phone_number_empty));

        }else {

            setupReservationModel();
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.reservation_bundle), reservation);
            tableFragment.setArguments(args);
            if (getActivity()!=null) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.your_placeholder, tableFragment, "table");
                ft.addToBackStack("Table");
                ft.commit();
            }
        }
    }


    //edit text onclick and on textChang listener + FAB onClick listener
    private void listener(final int currentYear, final int currentMonth, final int currentDay, final int currentHour, final int currentMinute) {

        //call nextFragment method to check every things and then go to tableFragment
        nextFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextFragment();
            }
        });

        //open date dialog and set the text of editText to the chosen values
        calendarEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker(currentYear,currentMonth,currentDay);

            }
        });

        //open time dialog and set the text of editText to the chosen values
        timeEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker(currentHour,currentMinute);
            }
        });


        //set the value of chosenChair and remove editText error after user stop typing
        chairEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!chairEdt.getText().toString().isEmpty()) {
                    chosenChair = Integer.parseInt(chairEdt.getText().toString());
                    chairLayout.setErrorEnabled(false);


                }
            }
        });


        chairEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                 if(actionId== EditorInfo.IME_ACTION_DONE) {
                    nextFragment();
                    return true;
                }
                return false;
            }
        });


    }
}
