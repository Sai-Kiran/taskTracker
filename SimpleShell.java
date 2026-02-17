package com.example.demo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.valueOf;

public class SimpleShell {
    private static String filePath = "data.json";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Java Simple Shell. Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine();

            if (command.equalsIgnoreCase("exit")) {
                System.out.println("Exiting Simple Shell.");
                break;
            } else {
                String[] commandArgs = parseArgs(command);
                handleArgs(commandArgs);
            }
        }
        scanner.close();
    }

    private static String[] parseArgs(String command) {
        List<String> matchList = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
        while (m.find()) {
            String arg = m.group(1);
            if (arg.startsWith("\"") && arg.endsWith("\"")) {
                arg = arg.substring(1, arg.length() - 1);
            }
            matchList.add(arg);
        }
        return matchList.toArray(new String[0]);
    }

    public static void handleArgs(String[] args) {
        int argsLength = args.length;

        if (argsLength > 3) {
            System.out.println("Wrong arguments provided.");
        } else if (argsLength == 1) {
            String taskName = args[0];

            if (taskName.equalsIgnoreCase(valueOf(TaskNames.LIST))) {
                fetchTasks(TaskStatus.TODO);
                fetchTasks(TaskStatus.IN_PROGRESS);
                fetchTasks(TaskStatus.DONE);
            } else {
                System.out.println("Wrong task: " + taskName);
                System.exit(0);
            }
        } else if (argsLength == 2) {
            String taskName = args[0];

            TaskNames currTaskName = TaskNames.fromString(taskName);
            String secondArg = args[1];

            switch (currTaskName) {
                case LIST:
                    TaskStatus filterEnum = TaskStatus.fromString(secondArg);

                    switch (filterEnum) {
                        case DONE:
                            fetchTasks(TaskStatus.DONE);
                            break;
                        case IN_PROGRESS:
                            fetchTasks(TaskStatus.IN_PROGRESS);
                            break;
                        case TODO:
                            fetchTasks(TaskStatus.TODO);
                            break;
                        default:
                            System.out.println("Unknown filter: " + secondArg);
                            break;
                    }
                    break;
                case ADD:
                    addTask(secondArg);
                    break;
                case DELETE:
                    deleteTask(secondArg);
                    break;
                case MARK_DONE:
                    updateTaskStatus(secondArg, TaskStatus.DONE);
                    break;
                case MARK_IN_PROGRESS:
                    updateTaskStatus(secondArg, TaskStatus.IN_PROGRESS);
                    break;
                default:
                    System.out.println("Wrong task: " + taskName);
                    break;
            }
        } else {
            String taskName = args[0];

            if (taskName.equalsIgnoreCase(valueOf(TaskNames.UPDATE))) {
                String taskId = args[1];
                String newDetails = args[2];
                System.out.println("Updating task ID: " + taskId + " with new details: " + newDetails);
                updateTaskDescription(taskId, newDetails);
            } else {
                System.out.println("Wrong arguments provided for task: " + taskName);
            }
        }
    }

    private static void addTask(String taskDetails) {
        System.out.println("Task added: " + taskDetails);

        int newId = getNewId();

        TaskProperties taskProperties = new TaskProperties();
        taskProperties.setId(newId);
        taskProperties.setDescription(taskDetails);
        taskProperties.setStatus(TaskStatus.TODO);
        taskProperties.setCreatedAt(new Date(System.currentTimeMillis()));

        JSONObject taskJson = createTaskJson(taskProperties);
        // Check if the file exists and read existing tasks, then add the new task to the list and save it back to the file
        File file = new File(filePath);
        JSONArray tasksArray = new JSONArray();

        if (file.exists()) {
            try (Scanner fileScanner = new Scanner(file)) {
                StringBuilder jsonContent = new StringBuilder();
                while (fileScanner.hasNextLine()) {
                    jsonContent.append(fileScanner.nextLine());
                }
                tasksArray = new JSONArray(jsonContent.toString());
            } catch (Exception e) {
                System.out.println("Error reading tasks from file: " + e.getMessage());
            }
        }

        tasksArray.put(taskJson);
        writeToFile(tasksArray);
    }

    private static int getNewId() {
        JSONArray tasksArray = fetchFile();
        int maxId = 0;

        for (int i = 0; i < tasksArray.length(); i++) {
            JSONObject taskJson = tasksArray.getJSONObject(i);
            int id = taskJson.getInt("id");
            if (id > maxId) {
                maxId = id;
            }
        }

        return maxId + 1;
    }

    private static void deleteTask(String taskIdStr) {
        System.out.println("Deleting task..." + taskIdStr);
        JSONArray tasksArray = fetchFile();
        JSONArray updatedTasksArray = new JSONArray();

        for (int i = 0; i < tasksArray.length(); i++) {
            JSONObject taskJson = tasksArray.getJSONObject(i);
            if (taskJson.getInt("id") != Integer.parseInt(taskIdStr)) {
                updatedTasksArray.put(taskJson);
            }
        }

        writeToFile(updatedTasksArray);
    }

    private static void updateTaskStatus(String taskId, TaskStatus taskStatus) {
        System.out.println("Updating task status..." + taskId + " to " + taskStatus);
        JSONArray tasksArray = fetchFile();
        JSONArray updatedTasksArray = new JSONArray();

        for (int i = 0; i < tasksArray.length(); i++) {
            JSONObject taskJson = tasksArray.getJSONObject(i);
            if (taskJson.getInt("id") == Integer.parseInt(taskId)) {
                taskJson.put("status", taskStatus.toString());
            }
            updatedTasksArray.put(taskJson);
        }

        writeToFile(updatedTasksArray);
    }

    private static void updateTaskDescription(String taskId, String newDescription) {
        System.out.println("Updating task description of..." + taskId + " to " + newDescription);
        JSONArray tasksArray = fetchFile();
        JSONArray updatedTasksArray = new JSONArray();

        for (int i = 0; i < tasksArray.length(); i++) {
            JSONObject taskJson = tasksArray.getJSONObject(i);
            if (taskJson.getInt("id") == Integer.parseInt(taskId)) {
                taskJson.put("description", newDescription);
            }
            updatedTasksArray.put(taskJson);
        }

        writeToFile(updatedTasksArray);
    }

    private static JSONArray fetchFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No tasks found.");
            return new JSONArray();
        }

        try (Scanner fileScanner = new Scanner(file)) {
            StringBuilder jsonContent = new StringBuilder();
            while (fileScanner.hasNextLine()) {
                jsonContent.append(fileScanner.nextLine());
            }
            return new JSONArray(jsonContent.toString());
        } catch (Exception e) {
            System.out.println("Error reading tasks from file: " + e.getMessage());
        }
        return new JSONArray();
    }

    private static void fetchTasks(TaskStatus taskStatus) {
        System.out.println(taskStatus);
        Map<Integer, String> tasks = new HashMap<>();
        JSONArray tasksArray = fetchFile();

        for (int i = 0; i < tasksArray.length(); i++) {
            JSONObject taskJson = tasksArray.getJSONObject(i);
            if (TaskStatus.fromString(taskJson.getString("status")) == taskStatus) {
                tasks.put(taskJson.getInt("id"), taskJson.getString("description"));
            }
        }

        printTasks(tasks);
    }

    private static void printTasks(Map<Integer, String> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        tasks.forEach((id, description) -> System.out.println("ID: " + id + ", Description: " + description));
    }

    private static JSONObject createTaskJson(TaskProperties taskProperties) {
        JSONObject taskJson = new JSONObject();
        taskJson.put("id", taskProperties.getId());
        taskJson.put("description", taskProperties.getDescription());
        taskJson.put("status", taskProperties.getStatus().toString());
        taskJson.put("createdAt", taskProperties.getCreatedAt().toString());

        return taskJson;
    }

    private static void writeToFile(JSONArray tasksArray) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(tasksArray.toString());
            System.out.println("Tasks saved to file successfully.");
        } catch (Exception e) {
            System.out.println("Error writing tasks to file: " + e.getMessage());
        }
    }


}
