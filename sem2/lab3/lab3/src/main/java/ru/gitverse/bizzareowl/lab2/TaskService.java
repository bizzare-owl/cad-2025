package ru.gitverse.bizzareowl.lab2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    // Метод для добавления новой задачи
    public void addTask(Task task) {
        taskRepository.addTask(task);
    }
    // Метод для получения всех задач
    public List<Task> getAllTasks() {
        return taskRepository.getAllTasks();
    }
    // Метод для удаления задачи по её идентификатору
    public void deleteTask(Long id) {
        taskRepository.deleteTask(id);
    }
}

