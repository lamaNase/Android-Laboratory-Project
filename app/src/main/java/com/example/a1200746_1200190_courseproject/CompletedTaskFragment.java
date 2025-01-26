package com.example.a1200746_1200190_courseproject;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CompletedTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompletedTaskFragment extends Fragment {

    private LinearLayout taskContainer;
    private LinearLayout noTasksLayout;
    private  DataBaseHelper dpHelper;
    static EditText searchEditText;
    static Boolean isSearched = false;
    static ArrayList<ArrayList<Task>> tasks;

    // Group Days By Date
    private ArrayList <String> groupDays;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CompletedTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CompletedTaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompletedTaskFragment newInstance(String param1, String param2) {
        CompletedTaskFragment fragment = new CompletedTaskFragment();
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
        return inflater.inflate(R.layout.fragment_completed_task, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();
        noTasksLayout = getActivity().findViewById(R.id.noTasksLayout);
        dpHelper = new DataBaseHelper(getActivity());
        taskContainer = getActivity().findViewById(R.id.taskContainer);


        searchEditText = getActivity().findViewById(R.id.search_edit_text);


        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
                    hideKeyboard(getView());

                    String keyword = searchEditText.getText().toString();
                    //searchEditText.setText(keyword);
                    tasks = groupDayTask(dpHelper.searchTasksByKeyword(keyword));
//                if(!tasks.isEmpty())
//                    resetTaskContainer();

                    isSearched = true;
                    //Log.i(TAG, "enterrrrrrrrrrrrred");

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, new CompletedTaskFragment()); // Replace with your fragment's name
                    transaction.addToBackStack(null); // Optional: add to back stack if you want to allow back navigation
                    transaction.commit();
                    return true;
                }

                return false;
            }
        });


        // Clear the task container to avoid duplicates
        //taskContainer.removeAllViews();
        if (isSearched == false)
            tasks = groupDayTask(dpHelper.getCompletedTasksSortedByTime());
        if (tasks == null)
            noTasksLayout.setVisibility(View.VISIBLE);
        else if(tasks.isEmpty())
            noTasksLayout.setVisibility(View.VISIBLE);

        if(groupDays!=null){
            for (int i = 0; i < groupDays.size(); i++) {

                // Style The text view
                TextView groupDate = new TextView(getActivity());
                groupDate.setText(formatDate(groupDays.get(i)));
                groupDate.setTextSize(24);

                // Set text style to bold
                groupDate.setTypeface(null, android.graphics.Typeface.BOLD); // Set text style to bold

                // Add a shadow effect
                groupDate.setShadowLayer(4, 2, 2, Color.GRAY);

                groupDate.setPadding(0, 20, 0, 20);


                taskContainer.addView(groupDate);
                for (int j = 0; j < tasks.get(i).size(); j++) {
                    Task task = tasks.get(i).get(j);

                    //Log.i(TAG, task.getSelectedRemainder());
                    View taskCard =
                            LayoutInflater.from(getActivity()).inflate(R.layout.taskcard, taskContainer, false);
                    TextView title = taskCard.findViewById(R.id.taskTitle);
                    title.setText(task.getTitle());

                    Chip completedStatus = taskCard.findViewById(R.id.completedStatus);
                    if (task.isCompletionStatus()) {
                        completedStatus.setText("Completed");
                        // Get the color dynamically
                        int color = ContextCompat.getColor(getActivity(), android.R.color.holo_green_light);
                        // Set the color as the background color for the chip
                        completedStatus.setChipBackgroundColor(ColorStateList.valueOf(color));
                    } else {
                        completedStatus.setText("Not Completed");
                        // Get the color dynamically
                        int color = ContextCompat.getColor(getActivity(), android.R.color.holo_orange_light);
                        // Set the color as the background color for the chip
                        completedStatus.setChipBackgroundColor(ColorStateList.valueOf(color));
                    }
                    setCompleted(completedStatus,task);
                    Chip priority = taskCard.findViewById(R.id.priority);
                    setPriority(priority,task);


                    TextView dateAndTime = taskCard.findViewById(R.id.taskDateTime);
                    dateAndTime.setText(task.getDueDate() + " " + task.getDueTime());

                    LinearLayout descriptionAndButtonContainer = taskCard.findViewById(R.id.descriptionAndButtonContainer);

                    TextView taskDescription = taskCard.findViewById(R.id.taskDescrption);
                    taskDescription.setText(task.getDescription());

                    Button editTask = taskCard.findViewById(R.id.editButton);
                    Button removeTask = taskCard.findViewById(R.id.deleteButton);
                    Button sharedTask = taskCard.findViewById(R.id.shareButton);


                    // Set click listener on the card
                    taskCard.setOnClickListener(v -> {
                        // Toggle visibility of the button container
                        if (descriptionAndButtonContainer.getVisibility() == View.GONE) {
                            descriptionAndButtonContainer.setVisibility(View.VISIBLE);
                        } else {
                            descriptionAndButtonContainer.setVisibility(View.GONE);
                        }

                        // Play the sound
                        MediaPlayer mediaPlayer = MediaPlayer.create(v.getContext(), R.raw.taskcard_sound); // Replace 'your_sound_file' with the actual sound file name
                        mediaPlayer.start();

                        // Release the MediaPlayer resources after the sound finishes
                        mediaPlayer.setOnCompletionListener(mp -> {
                            mp.release();
                        });
                    });

                    // edit task
                    editTask.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i(TAG, "edite button");
                            HomeActivity.currentTask = task;
                            HomeActivity.fromFragmentToEdit = "Completed";
                            isSearched=false;
                            resetTaskContainer();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, new EditTaskFragment()); // Replace with your fragment's name
                            transaction.addToBackStack(null); // Optional: add to back stack if you want to allow back navigation
                            transaction.commit();

                        }
                    });

                    removeTask.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i(TAG, "Remove button clicked");

                            // Create a confirmation dialog
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Confirm Deletion")
                                    .setMessage("Are you sure you want to delete this task?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Logic to delete the task
                                            //Log.i(TAG, "Task deleted");
                                            dpHelper.deleteTaskById(task.getId());
                                            isSearched=false;
                                            resetTaskContainer();
                                            // After deletion, recreate the fragment to refresh the UI
                                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                            transaction.replace(R.id.fragment_container, new TodayTaskFragment()); // Replace with your fragment's name
                                            transaction.addToBackStack(null); // Optional: add to back stack if you want to allow back navigation
                                            transaction.commit();

                                            Log.i(TAG, "Task deleted and fragment recreated");


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
                    });


                    sharedTask.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Get the task details
                            String taskDetails = "Task Title: " + task.getTitle() + "\n" +
                                    "Description: " + task.getDescription() + "\n" +
                                    "Due Date: " + task.getDueDate() + "\n" +
                                    "Due Time: " + task.getDueTime() + "\n" +
                                    "Priority: " + task.getPriorityLevel() + "\n" +
                                    "Completion Status: " + (task.isCompletionStatus() ? "Completed" : "Pending");

                            // Open email client with task details
                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
                            emailIntent.setType("message/rfc822"); // MIME type for email
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Task Details: " + task.getTitle());
                            emailIntent.putExtra(Intent.EXTRA_TEXT, taskDetails);

                            try {
                                // Launch email client
                                startActivity(Intent.createChooser(emailIntent, "Send Task via Email"));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(getActivity(), "No email client installed on your device.", Toast.LENGTH_SHORT).show();
                            }
                            isSearched=false;
                            resetTaskContainer();
                        }
                    });

                    taskContainer.addView(taskCard);

                }
        }// end for
    }
  }


    private ArrayList<ArrayList<Task>> groupDayTask(List <Task> tasks){
        ArrayList <ArrayList <Task>> groupDaysTasks = new ArrayList<>() ;

        groupDays = new ArrayList<>();
        for (Task task : tasks){
            String day = task.getDueDate();

            // check if this day(date) already exists
            if(groupDays.contains(day)){
                // determine the day (date)  index in the array
                int index = groupDays.indexOf(day);
                groupDaysTasks.get(index).add(task);

            }
            else {
                groupDays.add(day);
                int index = groupDays.indexOf(day);
                groupDaysTasks.add(new ArrayList<>());
                groupDaysTasks.get(index).add(task);
            }
        }
        return groupDaysTasks;
    }


    private String formatDate(String dateString) {

        // Formatter for the input date
        SimpleDateFormat inputFormatter = new SimpleDateFormat("d/M/yyyy");

        // Formatter for the output date
        SimpleDateFormat outputFormatter = new SimpleDateFormat("MMM d, yyyy");

        try {
            // Parse the input date string
            Date date = inputFormatter.parse(dateString);

            // Format the date to the desired output
            String formattedDate = outputFormatter.format(date);
            return formattedDate;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    // Method to hide the keyboard
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        if (imm != null && getActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void resetTaskContainer() {
        // Remove all child views from taskContainer
        taskContainer.removeAllViews();

        // Reinflate only the original child views of taskContainer
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        inflater.inflate(R.layout.fragment_completed_task, taskContainer, true);
    }

    private void setPriority(Chip chip, Task task){
        int color;
        if(task.getPriorityLevel().equals("Low"))
            color = ContextCompat.getColor(getActivity(), R.color.lightLowPriority);
        else if(task.getPriorityLevel().equals("Medium"))
            color = ContextCompat.getColor(getActivity(), R.color.lightMediumPriority);
        else
            color = ContextCompat.getColor(getActivity(), R.color.lightHighPriority);

        chip.setChipBackgroundColor(ColorStateList.valueOf(color));
        chip.setText(task.getPriorityLevel());
    }

    private void setCompleted(Chip completionStatus, Task task){
        completionStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!task.isCompletionStatus()){
                    completionStatus.setText("Completed");

                    int color = ContextCompat.getColor(getActivity(), android.R.color.holo_green_light);
                    // Set the color as the background color for the chip
                    completionStatus.setChipBackgroundColor(ColorStateList.valueOf(color));
                    task.setCompletionStatus(true);
                }
                else{
                    completionStatus.setText("Not Completed");

                    int color = ContextCompat.getColor(getActivity(), android.R.color.holo_orange_light);
                    // Set the color as the background color for the chip
                    completionStatus.setChipBackgroundColor(ColorStateList.valueOf(color));
                    task.setCompletionStatus(false);

                }
                dpHelper.updateTaskCompletionStatus(task.getId(),task.isCompletionStatus());

            }
        });

    }


}