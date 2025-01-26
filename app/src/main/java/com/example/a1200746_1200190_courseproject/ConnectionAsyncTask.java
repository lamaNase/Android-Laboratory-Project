package com.example.a1200746_1200190_courseproject;

import android.app.Activity;
import android.os.AsyncTask;
import java.util.List;
public class ConnectionAsyncTask extends AsyncTask<String, String, String> {
    Activity activity;
    public ConnectionAsyncTask(Activity activity) {
        this.activity = activity;
    }
    @Override
    protected void onPreExecute() {
        (AddNewTaskFragment.importTask).setText("Connecting");
        super.onPreExecute();
       // ((MainActivity) activity).setProgress(true);
    }
    @Override
    protected String doInBackground(String... params) {
        String data = HttpManager.getData(params[0]);
        return data;
    }
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        (AddNewTaskFragment.importTask).setText("Connected");
        List<Task> tasks = TaskJsonParser.getObjectFromJson(s);
        AddNewTaskFragment.importTasks(tasks);
        //((MainActivity) activity).fillStudents(tasks);

    }
}