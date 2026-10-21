package ru.gitverse.bizzareowl.lab2;

import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
@Repository
public class TaskRepository {
    private List<Task> taskList = new ArrayList<>();
    // Метод для добавления задачи в репозиторий
    public void addTask(Task task) {
        taskList.add(task);
    }
    // Метод для получения всех задач из репозитория
    public List<Task> getAllTasks() {
        return taskList;
    }
    // Метод для удаления задачи из репозитория по её идентификатору
    public void deleteTask(Long id) {
        taskList.removeIf(task -> task.getId().equals(id));
    }
}
