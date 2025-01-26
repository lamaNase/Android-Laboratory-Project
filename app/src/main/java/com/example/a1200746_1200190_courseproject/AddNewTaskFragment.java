package com.example.a1200746_1200190_courseproject;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.DatePickerDialog;

import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddNewTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNewTaskFragment extends Fragment {
    private EditText title;
    private EditText description;
    private EditText dueDate;
    private EditText dueTime;
    private ChipGroup priorityLevel;
    private Chip lowPriority;
    private Chip mediumPriority;
    private Chip highPriority;
    private Button saveButton;
    private Switch reminderSwitch;
    private String selectedPriority = "Medium";
    private LinearLayout reminderOptionsLayout;
    private Spinner spinnerReminderOptions;
    public static Button importTask;

    // Remainder options
    private final String FIVE_DAYS = "5 Days Before";
    private final String DAY = "1 Day Before";
    private final String HOUR = "1 Hour Before";
    private final String THIRTY_MINUTES = "30 Minutes Before";

    private boolean reminderChecked;
    private DataBaseHelper dbHelper;
    private String selectedReminderOption = "";


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddNewTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddNewTaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddNewTaskFragment newInstance(String param1, String param2) {
        AddNewTaskFragment fragment = new AddNewTaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_new_task, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();
        title = getActivity().findViewById(R.id.taskTitle);
        description = getActivity().findViewById(R.id.taskDescription);
        dueDate = getActivity().findViewById(R.id.editTextDueDate);
        dueTime = getActivity().findViewById(R.id.editTextDueTime);
        priorityLevel = getActivity().findViewById(R.id.priorityLevelChipGroup);
        lowPriority = getActivity().findViewById(R.id.chipLow);
        mediumPriority = getActivity().findViewById(R.id.chipMedium);
        highPriority = getActivity().findViewById(R.id.chipHigh);
        reminderSwitch = getActivity().findViewById(R.id.switch1);
        reminderOptionsLayout = getActivity().findViewById(R.id.reminderOptionsLayout);
        saveButton = getActivity().findViewById(R.id.saveButton);
        dbHelper = new DataBaseHelper(getActivity());
        importTask=getActivity().findViewById(R.id.importTask);

        mediumPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.selected_chip_stroke_width));

        importTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectionAsyncTask connectionAsyncTask = new ConnectionAsyncTask(getActivity());
                connectionAsyncTask.execute("https://mocki.io/v1/03986100-7a12-42a9-a998-d87db54f8175");
            }
        });

        dueDate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                // Get the current date
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Open the DatePickerDialog

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getActivity(),  // Use getActivity() instead of MainActivity.this
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(android.widget.DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                                // Format the selected date and set it in the EditText
                                String selectedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                dueDate.setText(selectedDate);
                            }
                        },
                        year,
                        month,
                        day
                );
                // Prevent selecting past dates
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

                datePickerDialog.show();
            }

        });

        dueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current date and time
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                // Open the TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // Get the selected date from the dueDate EditText
                                String selectedDateText = dueDate.getText().toString();
                                Calendar selectedDate = Calendar.getInstance();
                                Calendar currentDate = Calendar.getInstance();

                                // Parse the selected date (assumes dd/MM/yyyy format in dueDate)
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("d/M/y", Locale.getDefault());
                                    selectedDate.setTime(sdf.parse(selectedDateText));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return; // If parsing fails, don't proceed
                                }

                                // Compare the selected date with the current date
                                if (selectedDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                                        selectedDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)) {
                                    // Today's date selected; ensure time is not in the past
                                    if (hourOfDay < currentHour || (hourOfDay == currentHour && minute < currentMinute)) {
                                        Toast.makeText(getActivity(), "Selected time cannot be in the past.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                // Explicitly format the selected time and set it in the EditText
                                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                                dueTime.setText(selectedTime);
                            }
                        },
                        currentHour,
                        currentMinute,
                        true // Set true for 24-hour format, false for 12-hour format
                );

                timePickerDialog.show();
            }
        });


        // Set individual chip listeners
        lowPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPriority = "Low";
                mediumPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.default_chip_stroke_width));
                highPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.default_chip_stroke_width));
                lowPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.selected_chip_stroke_width));
            }
        });
        mediumPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPriority = "Medium";
                lowPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.default_chip_stroke_width));
                highPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.default_chip_stroke_width));
                mediumPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.selected_chip_stroke_width));
            }
        });
        highPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPriority = "High";
                mediumPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.default_chip_stroke_width));
                lowPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.default_chip_stroke_width));
                highPriority.setChipStrokeWidth(getResources().getDimension(R.dimen.selected_chip_stroke_width));
            }
        });

        // Setup switch listener
        reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                reminderChecked = true;
                // Add spinner dynamically
                addSpinner();
            } else {
                reminderChecked = false;
                // Remove spinner
                removeSpinner();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleInput = title.getText().toString();
                String desStringInput = description.getText().toString();
                if(isEditTextsValid(titleInput, desStringInput)) {
                    String date = dueDate.getText().toString();
                    String time = dueTime.getText().toString();
                    if (reminderChecked) {
                        selectedReminderOption =
                                spinnerReminderOptions.getSelectedItem().toString();
                    }
                    // Create a confirmation dialog
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Confirm Addition")
                            .setMessage("Are you sure you want to add this task?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Task task = new Task(titleInput, desStringInput, date, time, selectedPriority, false, reminderChecked, selectedReminderOption);

                                    if (!dbHelper.insertTask(task))
                                        Toast.makeText(requireContext(), "Failed to add, task already exist", Toast.LENGTH_SHORT).show();
                                    else {
                                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                        transaction.replace(R.id.fragment_container, new AddNewTaskFragment());
                                        transaction.addToBackStack(null);
                                        transaction.commit();

                                        Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Dismiss the dialog without doing anything
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(true) // Allow the user to cancel the dialog by tapping outside
                            .show();  // Show the dialog
                }
            }
        });


    }

    private boolean isEditTextsValid(String titleInput, String descriptionInput) {
        if (TextUtils.isEmpty(titleInput)) {
            title.setError("Title is required");
            return false;
        }

        if (TextUtils.isEmpty(descriptionInput)) {
            description.setError("Title is required");
            return false;
        }

        String date = dueDate.getText().toString();
        String time = dueTime.getText().toString();

        if (TextUtils.isEmpty(date)) {
            dueDate.setError("Due sate is required");
            return false;
        }

        if (TextUtils.isEmpty(time)) {
            dueTime.setError("Sue time is required");
            return false;
        }

        return true;
    }

    // Method to hide the keyboard
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        if (imm != null && getActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addSpinner() {
        if (spinnerReminderOptions == null) { // Ensure we don't add multiple spinners
            spinnerReminderOptions = new Spinner(requireContext());

            // Create an adapter for the spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{FIVE_DAYS, DAY, HOUR, THIRTY_MINUTES}
            );
            spinnerReminderOptions.setAdapter(adapter);

            // Set layout parameters to control width and alignment
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // Full width
                    LinearLayout.LayoutParams.WRAP_CONTENT  // Height based on content
            );
            layoutParams.gravity = Gravity.CENTER; // Center the spinner

            spinnerReminderOptions.setLayoutParams(layoutParams);

            // Add the spinner to the layout
            reminderOptionsLayout.addView(spinnerReminderOptions);

            // Ensure the parent layout's gravity aligns the spinner correctly
            if (reminderOptionsLayout instanceof LinearLayout) {
                ((LinearLayout) reminderOptionsLayout).setGravity(Gravity.CENTER);
            }

        }
    }


    private void removeSpinner() {
        if (spinnerReminderOptions != null) {
            reminderOptionsLayout.removeView(spinnerReminderOptions);
            spinnerReminderOptions = null;
        }
    }

    public static void importTasks(List <Task> tasks ){
        DataBaseHelper dbHelper = new DataBaseHelper(importTask.getContext());
        for(Task task : tasks){
            dbHelper.insertTask(task);
        }
    }
}