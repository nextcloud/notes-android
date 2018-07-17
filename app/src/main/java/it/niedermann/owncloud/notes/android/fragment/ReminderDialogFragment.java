package it.niedermann.owncloud.notes.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import it.niedermann.owncloud.notes.R;

/**
 * This fragment allows to set a reminder for a given note. It is launched from BaseNoteFragment.java
 */
public class ReminderDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    private TextView dateText, timeText;
    private Calendar calendar;
    private ReminderDialogListener listener;

    public interface ReminderDialogListener {
        void onDateTimeSet(Calendar calendar);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ReminderDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement ReminderDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Set a different style to avoid dialogs button to be shown with white text on white background
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_set_reminder, null);

        builder.setView(dialogView)
                .setTitle(R.string.dialog_reminder_title)
                .setPositiveButton(R.string.dialog_reminder_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDateTimeSet(calendar);
                    }
                })
                .setNeutralButton(R.string.dialog_reminder_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ReminderDialogFragment.this.dismiss();
                    }
                });

        dateText = dialogView.findViewById(R.id.dateText);
        timeText = dialogView.findViewById(R.id.timeText);
        calendar = Calendar.getInstance();

        updateTimeTextView(calendar);
        updateDateTextView(calendar);

        setListenersOnDateTimeButtons(dialogView);
        return builder.create();
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        calendar.set(year, month, day);
        updateDateTextView(calendar);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        updateTimeTextView(calendar);
    }

    private void setListenersOnDateTimeButtons(View view) {
        ImageButton changeTime = view.findViewById(R.id.timeButton);
        ImageButton changeDate = view.findViewById(R.id.dateButton);

        changeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // Create a new instance of TimePickerDialog and return it
                new TimePickerDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog_Alert, ReminderDialogFragment.this, hour, minute,
                        DateFormat.is24HourFormat(getActivity())).show();
            }
        });

        changeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the current date as the default date in the picker
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog_Alert, ReminderDialogFragment.this, year, month, day).show();
            }
        });
    }

    private void updateTimeTextView(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
        timeText.setText(sdf.format(calendar.getTime()));
    }

    private void updateDateTextView(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        dateText.setText(sdf.format(calendar.getTime()));
    }

}

