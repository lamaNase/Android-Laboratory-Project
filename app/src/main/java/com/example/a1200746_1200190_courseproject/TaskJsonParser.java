package com.example.a1200746_1200190_courseproject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
public class TaskJsonParser {
    public static List<Task> getObjectFromJson(String json) {
        List<Task> tasks;
        try {
            JSONArray jsonArray = new JSONArray(json);
            tasks = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject = (JSONObject) jsonArray.get(i);
                Task task = new Task();
                task.setId(jsonObject.getInt("id"));
                task.setTitle(jsonObject.getString("title"));
                task.setDescription(jsonObject.getString("description"));
                task.setDueDate(jsonObject.getString("dueDate"));
                task.setDueTime(jsonObject.getString("dueTime"));
                task.setPriorityLevel(jsonObject.getString("priorityLevel"));
                task.setCompletionStatus(jsonObject.getBoolean("completionStatus"));
                task.setReminderIcon(jsonObject.getBoolean("reminderIcon"));
                task.setSelectedRemainder(jsonObject.getString("selectedRemainder"));

                tasks.add(task);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return tasks;
    }
}