package com.example.a1200746_1200190_courseproject;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.a1200746_1200190_courseproject.R;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    public static Task currentTask;
    public static String fromFragmentToEdit = "";
    private DataBaseHelper dbHelper;
    public static View headerView;
    public static ImageView profilePic;
    public static TextView navEmail;

    private Handler handler = new Handler();
    private Runnable taskCheckerRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DataBaseHelper(HomeActivity.this);
        setContentView(R.layout.activity_home);
        // Hide the keyboard at the start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize the Toolbar and set it as the ActionBar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setBackgroundColor(Color.BLUE);
        setSupportActionBar(toolbar);

        // Initialize the DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Select the "Home" item by default
        navigationView.setCheckedItem(R.id.nav_today);

        navigationView.setItemIconTintList(null);

        navigationView.setItemIconSize(100);
        navigationView.setItemTextAppearanceActiveBoldEnabled(true);

        Menu menu = navigationView.getMenu();

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            // Get the View associated with the MenuItem
            View view = navigationView.getMenu().findItem(item.getItemId()).getActionView();

            if (view != null) {
                // Add padding (in pixels)
                view.setPadding(0, 20, 0, 20); // top, bottom (left & right can be adjusted)
            }

            // Increase text size
            SpannableString spanString = new SpannableString(item.getTitle());
            spanString.setSpan(new AbsoluteSizeSpan(20, true), 0, spanString.length(), 0); // Set text size in sp
            item.setTitle(spanString);
        }

        headerView = navigationView.getHeaderView(0);

        profilePic = (ImageView) headerView.findViewById(R.id.imgProfile);
        TextView navUsername = (TextView) headerView.findViewById(R.id.view_name);
        navEmail = (TextView) headerView.findViewById(R.id.view_email);
        profilePic.setImageURI(DataBaseHelper.currentUser.getProfilePicture());
        navUsername.setText(DataBaseHelper.currentUser.getFirstName() + " " + DataBaseHelper.currentUser.getLastName());
        navEmail.setText(DataBaseHelper.currentUser.getEmail());


        // Set up the ActionBarDrawerToggle
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Set the hamburger icon color to black
//        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.black));

        toolbar.setNavigationIcon(R.drawable.hamburger_icon);

        // Ensure ActionBar is not null before calling its methods
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Today");
        }

        // Load the TodayFragment by default
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new TodayTaskFragment()).commit();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here
        if (item.getItemId() == R.id.nav_today) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new TodayTaskFragment()).commit();
            getSupportActionBar().setTitle("Today");
        }
        else if (item.getItemId() == R.id.nav_add_task) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new AddNewTaskFragment()).commit();
            getSupportActionBar().setTitle("Add Task");
        }

        else if (item.getItemId() == R.id.nav_all_task) {
            //DisplayAllTaskFragment displayAllTaskFragment = new DisplayAllTaskFragment();
            DisplayAllTaskFragment.tasks =new ArrayList<>();
            DisplayAllTaskFragment.groupDays = new ArrayList<>();
            DisplayAllTaskFragment.isSearched=false;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new DisplayAllTaskFragment()).commit();
            getSupportActionBar().setTitle("All Task ");
        }

        else if (item.getItemId() == R.id.nav_completed_task) {
            CompletedTaskFragment.tasks=new ArrayList<>();
            CompletedTaskFragment.isSearched = false;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new CompletedTaskFragment()).commit();
            getSupportActionBar().setTitle("Completed Task");
        }

        else if (item.getItemId() == R.id.nav_search) {
            SearchFragment.tasks=new ArrayList<>();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new SearchFragment()).commit();
            getSupportActionBar().setTitle("Search For Task ");
        }

        else if (item.getItemId() == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new UserProfileFragment()).commit();
            getSupportActionBar().setTitle("Profile");
        }

        else if (item.getItemId() == R.id.nav_logout) {
            // Show a confirmation dialog before logging out
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Proceed with logout
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(getApplicationContext(), "Logout successful", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss the dialog and stay on the current screen
                        dialog.dismiss();
                    })
                    .setCancelable(false) // Prevent dismissal by tapping outside the dialog
                    .show();
        }


        // Close the drawer after item selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize and start the periodic task checking
        taskCheckerRunnable = new Runnable() {
            @Override
            public void run() {
                checkedTaskTime();
                handler.postDelayed(this, 60000); // Run every minute
            }
        };
        handler.post(taskCheckerRunnable); // Start the Runnable
    }

//    private String getCurrentDate(){
//        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        return formatter.format(new Date());
//    }
//
//    private String getCurrentTime(){
//        DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("HH:mm",Locale.getDefault());
//        return formatter.format(new Tim);
//    }

    // Helper method to check if two dates are on the same day
    private boolean isSameDay(String taskDate, Date currentDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date taskDueDate = dateFormat.parse(taskDate);
            return dateFormat.format(taskDueDate).equals(dateFormat.format(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
    }


    public void showAlarmDialog(Task task, String alertMessage) {
    // Play music when the alarm dialog appears
    MediaPlayer mediaPlayer = MediaPlayer.create(HomeActivity.this, R.raw.iphone_alarm); // Replace 'alarm_sound' with your actual music file name
    mediaPlayer.start();

    // Inflate the custom layout
    LayoutInflater inflater = getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.custom_alarm_dialog, null);

    // Create the AlertDialog with the custom view
    new AlertDialog.Builder(this)
            .setTitle("Task Reminder")
            .setMessage(alertMessage)
            .setView(dialogView) // Set the custom layout
            .setCancelable(false)
            .setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Stop the media player
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                    Toast.makeText(HomeActivity.this, "Snoozed for 5 minutes", Toast.LENGTH_SHORT).show();
                    task.addFiveMinutesToDueTime();
                    dbHelper.updateTaskDueTime(task.getId(), task.getDueTime());
                }
            })
            .setNegativeButton("Stop", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Stop the media player
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                    Toast.makeText(HomeActivity.this, "Alarm stopped", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            })
            .show();
	}


    private void checkedTaskTime(){
        List<Task> tasks = dbHelper.getTasksSortedChronologically();
        for(Task task : tasks){
            if(!task.checkReminder().equals("")){
                Log.i(TAG, "return " + task.checkReminder());
                showAlarmDialog(task,task.checkReminder());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the periodic checking when the activity is paused
        handler.removeCallbacks(taskCheckerRunnable);
    }
}