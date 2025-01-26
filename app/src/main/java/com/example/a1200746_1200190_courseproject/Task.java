package com.example.a1200746_1200190_courseproject;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Task {
    private int id;
    private String title;
    private String description;
    private Date dueDate;
    public LocalTime dueTime;
    private String priorityLevel;
    private boolean completionStatus;
    private boolean reminderIcon;
    private String selectedRemainder;
    public static int countTasks=0;

    private final String FIVE_DAYS = "5 Days Before";
    private final String DAY = "1 Day Before";
    private final String HOUR = "1 Hour Before";
    private final String THIRTY_MINUTES = "30 Minutes Before";


    public Task(){

    }

    public Task(String title, String description, String dueDate, String dueTime, String priorityLevel, boolean completionStatus, boolean reminderIcon, String selectedRemainder ) {
        this.id = countTasks;
        this.title = title;
        this.description = description;
        setDueDate(dueDate);
        setDueTime(dueTime);
        this.priorityLevel = priorityLevel;
        this.completionStatus = completionStatus;
        this.reminderIcon = reminderIcon;
        setSelectedRemainder(selectedRemainder);
        countTasks++;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        SimpleDateFormat formatter= new SimpleDateFormat("d/mm/yyyy");
        return formatter.format(dueDate);
    }

    public String getSelectedRemainder() {
        return selectedRemainder;
    }

    public void setSelectedRemainder(String selectedRemainder) {
        if(reminderIcon) {
            //Log.i(TAG, selectedRemainder);
            this.selectedRemainder = selectedRemainder;
        }
    }

    public void setDueDate(String dueDateString) {
        // Define the date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/mm/yyyy");
        dateFormat.setLenient(false); // Ensure strict parsing of the date

        try {
            // Convert the string to a Date object
            Date dueDate = dateFormat.parse(dueDateString);
            this.dueDate = dueDate; // Assign the converted date to the variable
        } catch (ParseException e) {
            e.printStackTrace(); // Handle the parsing error
            throw new IllegalArgumentException("Invalid date format. Please use d/m/y.");
        }
    }

    public String getDueTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dueTime.format(formatter);
    }

    public void setDueTime(String dueTimeString) {
        // Define the time format
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            // Parse the string to a LocalTime object
            LocalTime dueTime = LocalTime.parse(dueTimeString, timeFormatter);
            this.dueTime = dueTime; // Assign the parsed time to the variable
        } catch (DateTimeParseException e) {
            e.printStackTrace(); // Handle the parsing error
            throw new IllegalArgumentException("Invalid time format. Please use HH:mm.");
        }
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public boolean isCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(boolean completionStatus) {
        this.completionStatus = completionStatus;
    }

    public boolean isReminderIcon() {
        return reminderIcon;
    }

    public void setReminderIcon(boolean reminderIcon) {
        this.reminderIcon = reminderIcon;
    }


    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", dueTime=" + dueTime +
                ", priorityLevel='" + priorityLevel + '\'' +
                ", completionStatus=" + completionStatus +
                ", reminderIcon=" + reminderIcon +
                ", selectedRemainder='" + selectedRemainder + '\'' +
                '}';
    }

    public void addFiveMinutesToDueTime() {
        if (this.dueTime != null) {
            // Add 5 minutes to the dueTime
            this.dueTime = this.dueTime.plusMinutes(5);
            Log.i(TAG, "new due time after snoze = " + getDueTime());
        } else {
            throw new IllegalStateException("Due time is not set.");
        }
    }

    public String checkReminder() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        LocalTime currentTime = LocalTime.now();

        if(selectedRemainder == null || isCompletionStatus()){
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = currentTime.format(formatter);
        if(getDueDate().equals(currentDate) && getDueTime().equals(time))
            return "It's time for " + getTitle() + " task";

        switch (selectedRemainder.toLowerCase()) {
            case "5 days before":
                formatter = DateTimeFormatter.ofPattern("HH:mm");
                time = currentTime.format(formatter);
                if(dueTime != null && getDueTime().equals(time)) {
                    String newDate = addDaysToDate(currentDate, 5);
                    if (dueDate != null && getDueDate().equals(newDate)) {
                        return "Five days left for " + getTitle() + " task";
                    }
                }
                break;

            case "1 day before":
                formatter = DateTimeFormatter.ofPattern("HH:mm");
                time = currentTime.format(formatter);
                if(dueTime != null && getDueTime().equals(time)) {
                    String newDate = addDaysToDate(currentDate, 1);
                    if (dueDate != null && getDueDate().equals(newDate)) {
                        return "One day left for " + getTitle() + " task";
                    }
                }
                break;

            case "1 hour before":
                // Check if the due date is today and the due time is 1 hour ahead of the current time
                if (dueDate != null && getDueDate().equals(currentDate)) {
                    currentTime = currentTime.plusHours(1);
                    formatter = DateTimeFormatter.ofPattern("HH:mm");
                    time = currentTime.format(formatter);
                    if (dueTime != null && getDueTime().equals(time)) {
                        return "One hour left for " + getTitle() + " task";
                    }
                }
                break;

            case "30 minutes before":
                // Check if the due date is today and the due time is 30 minutes ahead of the current time
                if (dueDate != null && getDueDate().equals(currentDate)) {
                    currentTime = currentTime.plusMinutes(30);
                    formatter = DateTimeFormatter.ofPattern("HH:mm");
                    time = currentTime.format(formatter);
                    if (dueTime != null && getDueTime().equals(time)) {
                        return "Thirty minutes left for " + getTitle() + " task";
                    }
                }
                break;
        }

        return "";
    }

    public String addDaysToDate(String oldDate, int daysToAdd) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/y", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            // Parse the input date
            Date date = dateFormat.parse(oldDate);
            if (date != null) {
                calendar.setTime(date);
            }
        } catch (Exception e) {
            return null; // Return null if the input date format is invalid
        }

        // Add the specified number of days
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);

        // Format the new date
        return dateFormat.format(calendar.getTime());
    }

}
