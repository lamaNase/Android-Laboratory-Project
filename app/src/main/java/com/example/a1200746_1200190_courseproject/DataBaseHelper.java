package com.example.a1200746_1200190_courseproject;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ParseException;
import android.net.Uri;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Task_Management_Database";
    private static final int DATABASE_VERSION = 1;
    public static User currentUser;
    private Context context;

    // Tables
    private static final String TABLE_USER = "user";
    private static final String TABLE_TASK = "task";
    // User table columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";
    private static final String COLUMN_PROFILE_PICTURE = "profile_picture";


    // Task table columns
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DUE_DATE = "dueDate";
    private static final String COLUMN_DUE_TIME = "dueTime";
    private static final String COLUMN_PRIORITY_LEVEL = "priorityLevel";
    private static final String COLUMN_COMPLETION_STATUS = "completionStatus";
    private static final String COLUMN_REMINDER_ICON = "reminderIcon";
    private static final String COLUMN_SELECTED_REMINDER_OPTION = "selectedReminderOption";
    private static final String COLUMN_USER_EMAIL = "user_email";



    // Global Variables
    private static String user_email = "";
    private boolean clearTableFlag = false;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER +
                "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_EMAIL + " TEXT UNIQUE," +
                COLUMN_FIRST_NAME + " TEXT," +
                COLUMN_LAST_NAME + " TEXT," +
                COLUMN_PASSWORD_HASH + " TEXT," +
                COLUMN_PROFILE_PICTURE + " TEXT" +
                ")";
        db.execSQL(CREATE_USER_TABLE);

        String CREATE_TASK_TABLE = "CREATE TABLE " + TABLE_TASK +
                "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TITLE + " TEXT," +
                COLUMN_DESCRIPTION + " TEXT ," +
                COLUMN_DUE_DATE + " TEXT," +
                COLUMN_DUE_TIME + " TEXT," +
                COLUMN_PRIORITY_LEVEL + " TEXT," +
                COLUMN_COMPLETION_STATUS + " INTEGER," +
                COLUMN_REMINDER_ICON + " INTEGER," +
                COLUMN_SELECTED_REMINDER_OPTION + " TEXT," +
                COLUMN_USER_EMAIL + " TEXT," +
                "FOREIGN KEY(" + COLUMN_USER_EMAIL + ") REFERENCES " + TABLE_USER + "(" + COLUMN_EMAIL + ")" +
                ")";
        db.execSQL(CREATE_TASK_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        onCreate(db);
    }

    // Insert user into the database
    public boolean insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_FIRST_NAME, user.getFirstName());
        values.put(COLUMN_LAST_NAME, user.getLastName());
        values.put(COLUMN_PASSWORD_HASH, encryptPassword(user.getPassword()));
        Uri profilePictureUri = user.getProfilePicture();
        if (profilePictureUri != null) {
            values.put(COLUMN_PROFILE_PICTURE, profilePictureUri.toString());
        } else {
            values.putNull(COLUMN_PROFILE_PICTURE);
        }
        long result = db.insert(TABLE_USER, null, values);
        db.close();
        return result != -1;
    }

    // Insert task into the database
    public boolean insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Query to check if a task for this user with the same title and description already exists
        String query = "SELECT * FROM " + TABLE_TASK + " WHERE " +
                COLUMN_TITLE + " = ? AND " +
                COLUMN_DESCRIPTION + " = ? AND " +
                COLUMN_USER_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{task.getTitle(), task.getDescription(), user_email});

        // If a matching task is found, close the cursor and database, and return false
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false; // Task already exists
        }

        // Proceed to insert the task if no duplicate is found
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());

        // Convert Date and LocalTime to String format to store them in the database
        values.put(COLUMN_DUE_DATE, task.getDueDate().toString());
        values.put(COLUMN_DUE_TIME, task.getDueTime().toString());

        values.put(COLUMN_PRIORITY_LEVEL, task.getPriorityLevel());
        values.put(COLUMN_COMPLETION_STATUS, task.isCompletionStatus() ? 1 : 0);
        values.put(COLUMN_REMINDER_ICON, task.isReminderIcon() ? 1 : 0);
        values.put(COLUMN_SELECTED_REMINDER_OPTION, task.getSelectedRemainder());

        // Set the user email for the task
        values.put(COLUMN_USER_EMAIL, user_email);

        // Insert the values into the task table
        long result = db.insert(TABLE_TASK, null, values);

        // Close cursor and database
        cursor.close();
        db.close();

        // Return true if the task was inserted successfully, otherwise false
        return result != -1;
    }

    // Delete task by ID
    public boolean deleteTaskById(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_TASK, COLUMN_ID + "=?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsAffected > 0;
    }

    // Update task information based on task ID
    public boolean updateTaskById(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a ContentValues object to hold the updated task information
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, task.getTitle());
        contentValues.put(COLUMN_DESCRIPTION, task.getDescription());
        contentValues.put(COLUMN_DUE_DATE, task.getDueDate());
        contentValues.put(COLUMN_DUE_TIME, task.getDueTime());
        contentValues.put(COLUMN_PRIORITY_LEVEL, task.getPriorityLevel());
        contentValues.put(COLUMN_COMPLETION_STATUS, task.isCompletionStatus());
        contentValues.put(COLUMN_REMINDER_ICON, task.isReminderIcon());
        contentValues.put(COLUMN_SELECTED_REMINDER_OPTION, task.getSelectedRemainder());

        // Update the task in the database using its ID
        int rowsAffected = db.update(TABLE_TASK, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        // Return true if a row was updated, false otherwise
        return rowsAffected > 0;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + "=?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = encryptPassword(password);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD_HASH + "=?", new String[]{email, hashedPassword});
        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    // to return user for specific email
    public User getUserByEmail(String user_email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + "=?", new String[]{user_email});

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            // Retrieve data from the cursor
            String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME));
            String passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH));
            String profilePicture = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PICTURE));

            // Create a User object
            user = new User( email, firstName, lastName, passwordHash, profilePicture != null ? Uri.parse(profilePicture) : null);
        }

        if (cursor != null) {
            cursor.close();
        }

        return user;
    }

    // Display all tasks sorted chronologically for the current user
    public List<Task> getTasksSortedChronologically() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch tasks for the current user
        String selectQuery = "SELECT * FROM " + TABLE_TASK + " WHERE " + COLUMN_USER_EMAIL + " = ?";

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, new String[]{user_email});

            if (cursor.moveToFirst()) {
                do {
                    // Extract task details from the cursor
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    task.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)));
                    task.setDueTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_TIME)));
                    task.setPriorityLevel(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY_LEVEL)));
                    task.setCompletionStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETION_STATUS)) != 0);
                    task.setReminderIcon(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ICON)) != 0);
                    task.setSelectedRemainder(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELECTED_REMINDER_OPTION)));

                    // Add task to the list
                    taskList.add(task);
                } while (cursor.moveToNext());
            }

            // Sort tasks chronologically by date and time
            Collections.sort(taskList, (task1, task2) -> {
                try {
                    // Parse the due date and time
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("d/M/y HH:mm", Locale.getDefault());
                    String dateTime1 = task1.getDueDate() + " " + task1.getDueTime();
                    String dateTime2 = task2.getDueDate() + " " + task2.getDueTime();

                    // Convert to Date objects
                    Date date1 = dateTimeFormat.parse(dateTime1);
                    Date date2 = dateTimeFormat.parse(dateTime2);

                    // Compare the Date objects
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    Log.e("SortingError", "Error parsing date/time for sorting", e);
                    return 0; // Treat as equal if parsing fails
                } catch (java.text.ParseException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (Exception e) {
            Log.e("DatabaseError", "Error while fetching tasks", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return taskList;
    }

    public boolean updateTaskDueTime(int taskId, String newDueTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_DUE_TIME, newDueTime);

        // Update the task's due time where the ID matches
        int rowsAffected = db.update(TABLE_TASK, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(taskId)});

        db.close();

        // Return true if the update was successful (rows affected > 0)
        return rowsAffected > 0;
    }

    // Display today's tasks sorted by time for the current user
    public List<Task> getTasksForTodaySortedByTime() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get the current date in the format used in the database
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        Log.d("SortingError", "Current date: " + currentDate);

        Cursor cursor = null;

        try {
            // Query to fetch tasks for today's date and current user
            String selectQuery = "SELECT * FROM " + TABLE_TASK + " WHERE " + COLUMN_DUE_DATE + " = ? AND " + COLUMN_USER_EMAIL + " = ?";
            cursor = db.rawQuery(selectQuery, new String[]{currentDate, user_email});

            if (cursor.moveToFirst()) {
                do {
                    // Extract task details from the cursor
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    task.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)));
                    task.setDueTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_TIME)));
                    task.setPriorityLevel(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY_LEVEL)));
                    task.setCompletionStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETION_STATUS)) != 0);
                    task.setReminderIcon(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ICON)) != 0);
                    task.setSelectedRemainder(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELECTED_REMINDER_OPTION)));

                    // Add task to the list
                    taskList.add(task);
                } while (cursor.moveToNext());
            }

            // Sort tasks by time
            Collections.sort(taskList, (task1, task2) -> {
                try {
                    // Parse the due time
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date time1 = timeFormat.parse(task1.getDueTime());
                    Date time2 = timeFormat.parse(task2.getDueTime());

                    // Compare the time objects
                    return time1.compareTo(time2);
                } catch (ParseException | java.text.ParseException e) {
                    Log.e("SortingError", "Error parsing time for sorting", e);
                    return 0; // Treat as equal if parsing fails
                }
            });

        } catch (Exception e) {
            Log.e("DatabaseError", "Error while fetching tasks", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return taskList;
    }

    // Display completed tasks sorted by time for the current user
    public List<Task> getCompletedTasksSortedByTime() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        try {
            // Query to fetch completed tasks for the current user
            String selectQuery = "SELECT * FROM " + TABLE_TASK + " WHERE " + COLUMN_COMPLETION_STATUS + " = ? AND " + COLUMN_USER_EMAIL + " = ?";
            cursor = db.rawQuery(selectQuery, new String[]{"1", user_email}); // "1" indicates completed tasks

            if (cursor.moveToFirst()) {
                do {
                    // Extract task details from the cursor
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    task.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)));
                    task.setDueTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_TIME)));
                    task.setPriorityLevel(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY_LEVEL)));
                    task.setCompletionStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETION_STATUS)) != 0);
                    task.setReminderIcon(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ICON)) != 0);
                    task.setSelectedRemainder(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELECTED_REMINDER_OPTION)));

                    // Add task to the list
                    taskList.add(task);
                } while (cursor.moveToNext());
            }

            // Sort tasks by date and time
            Collections.sort(taskList, (task1, task2) -> {
                try {
                    // Parse the due date and time
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("d/M/y HH:mm", Locale.getDefault());
                    String dateTime1 = task1.getDueDate() + " " + task1.getDueTime();
                    String dateTime2 = task2.getDueDate() + " " + task2.getDueTime();

                    // Convert to Date objects
                    Date date1 = dateTimeFormat.parse(dateTime1);
                    Date date2 = dateTimeFormat.parse(dateTime2);

                    // Compare the Date objects
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    Log.e("SortingError", "Error parsing date/time for sorting", e);
                    return 0; // Treat as equal if parsing fails
                } catch (java.text.ParseException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (Exception e) {
            Log.e("DatabaseError", "Error while fetching completed tasks", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return taskList;
    }

    public boolean updateUserProfilePicture(String email, String newProfilePicture) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_PICTURE, newProfilePicture);
        int rowsAffected = db.update(TABLE_USER, values, COLUMN_EMAIL + "=?", new String[]{email});
        return rowsAffected > 0;
    }

    public boolean updateUserPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD_HASH, encryptPassword(newPassword));
        int rowsAffected = db.update(TABLE_USER, values, COLUMN_EMAIL + "=?", new String[]{email});
        return rowsAffected > 0;
    }

    public boolean updateUserEmail(String oldEmail, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Put the new email value into the ContentValues
        values.put(COLUMN_EMAIL, newEmail);

        // Perform the update operation
        int rowsAffected = db.update(
                TABLE_USER,
                values,
                COLUMN_EMAIL + "=?",
                new String[]{oldEmail}
        );

        // Close the database
        db.close();

        // Return true if at least one row was updated
        return rowsAffected > 0;
    }


    // Retrieve the profile picture URI for a specific user email
    public Uri getProfilePictureByEmail( String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PROFILE_PICTURE + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + "=?", new String[]{email});

        Uri profilePictureUri = null;
        if (cursor != null && cursor.moveToFirst()) {
            String profilePicture = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PICTURE));
            if (profilePicture != null) {
                profilePictureUri = Uri.parse(profilePicture);
            }
            cursor.close();
        }

        // If no profile picture is set, return the URI of the default drawable
        if (profilePictureUri == null) {
            profilePictureUri = Uri.parse("android.resource://" + context.getPackageName() + "/drawable/updateprofile");
        }

        return profilePictureUri;
    }

    // Display tasks in a date range for the current user
    public List<Task> getTasksByDateRange(String startDate, String endDate) {
        // List to store tasks within the date range
        List<Task> tasks = new ArrayList<>();

        // Convert the start and end dates to the proper format for comparison
        String startDateFormatted = startDate.replace("/", "-");
        String endDateFormatted = endDate.replace("/", "-");

        // Open the database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the columns to be selected
        String[] columns = {
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_DESCRIPTION,
                COLUMN_DUE_DATE,
                COLUMN_DUE_TIME,
                COLUMN_PRIORITY_LEVEL,
                COLUMN_COMPLETION_STATUS,
                COLUMN_REMINDER_ICON,
                COLUMN_SELECTED_REMINDER_OPTION
        };

        // Define the selection criteria (WHERE clause) for filtering tasks within the date range and by user email
        String selection = COLUMN_DUE_DATE + " BETWEEN ? AND ? AND " + COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {startDateFormatted, endDateFormatted, user_email};

        // Query the database
        Cursor cursor = db.query(TABLE_TASK, columns, selection, selectionArgs, null, null, null);

        // Iterate through the results and add tasks to the list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Extract values from the cursor and create a Task object

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE));
                String dueTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_TIME));
                String priorityLevel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY_LEVEL));
                Boolean completionStatus = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETION_STATUS)) != 0;
                Boolean reminderIcon = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ICON)) != 0;
                String selectedReminderOption = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELECTED_REMINDER_OPTION));

                // Create Task object and add it to the list
                Task task = new Task(title, description, dueDate, dueTime, priorityLevel, completionStatus, reminderIcon, selectedReminderOption);
                task.setId(id);
                tasks.add(task);
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Return the list of tasks
        return tasks;
    }

    // Function to fetch tasks with a keyword in title or description for the current user
    public List<Task> searchTasksByKeyword(String keyword) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL query to search for the keyword in title or description and filter by user email
        String query = "SELECT * FROM " + TABLE_TASK +
                " WHERE (" + COLUMN_TITLE + " LIKE ? OR " + COLUMN_DESCRIPTION + " LIKE ?) AND " + COLUMN_USER_EMAIL + " = ?";

        // Wildcard keyword for partial match
        String searchKeyword = "%" + keyword + "%";

        Cursor cursor = db.rawQuery(query, new String[]{searchKeyword, searchKeyword, user_email});

        // Loop through the results and add to the list
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE));
                String dueTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_TIME));
                String priorityLevel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY_LEVEL));
                Boolean completionStatus = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETION_STATUS)) != 0;
                Boolean reminderIcon = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ICON)) != 0;
                String selectedReminderOption = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELECTED_REMINDER_OPTION));

                // Create Task object and add it to the list
                Task task = new Task(title, description, dueDate, dueTime, priorityLevel, completionStatus, reminderIcon, selectedReminderOption);
                task.setId(id);

                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    // Function to update the completion status of a task by its ID
    public boolean updateTaskCompletionStatus(int taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Prepare the new completion status value
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETION_STATUS, isCompleted ? 1 : 0); // 1 for true, 0 for false

        // Update the task in the database
        int rowsUpdated = db.update(
                TABLE_TASK,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );

        db.close(); // Close the database connection

        // Return true if at least one row was updated
        return rowsUpdated > 0;
    }







//    public String getTaskDueDateById(int taskId) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String dueDate = "no task ";
//
//        // Query to fetch the due date for the given task ID
//        String query = "SELECT " + COLUMN_DUE_DATE + " FROM " + TABLE_TASK + " WHERE " + COLUMN_ID + " = ?";
//        Cursor cursor = null;
//
//        try {
//            cursor = db.rawQuery(query, new String[]{String.valueOf(taskId)});
//            if (cursor.moveToFirst()) {
//                dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE));
//            }
//        } catch (Exception e) {
//            Log.e("DatabaseError", "Error fetching due date for task ID: " + taskId, e);
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//            db.close();
//        }
//
//        return dueDate;
//    }


    public void setUserEmail(String email) {
        user_email = email;
    }
}
