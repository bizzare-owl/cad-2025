package ru.gitverse.bizzareowl.lab2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    // Веб-методы
    @GetMapping("/")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public String index() {
        return "index";
    }

    @PostMapping("/addTask")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public String addTask(@ModelAttribute Task task) {
        task.setCreatedAt(LocalDateTime.now());
        taskRepository.save(task);
        return "redirect:/";
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @PostMapping("/updateTask/{id}")
    public String updateTask(@PathVariable Long id, @RequestParam boolean completed) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.setCompleted(completed);
        taskRepository.save(task);
        return "redirect:/";
    }

    // REST-методы
    @GetMapping("/api")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(order)
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            switch (sortBy.toLowerCase()) {
                case "priority":
                    sort = Sort.by(direction, "priority");
                    break;
                case "createdAt":
                    sort = Sort.by(direction, "createdAt");
                    break;
                case "title":
                    sort = Sort.by(direction, "title");
                    break;
                default:
                    sort = Sort.by(direction, "createdAt");
                    break;
            }
        }

        List<Task> tasks = taskRepository.findAll(sort);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/api/tasks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        task.setCreatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        return ResponseEntity.status(201).body(saved);
    }

    @DeleteMapping("/api/tasks/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (!taskRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/tasks/{id}/complete")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR')")
    public ResponseEntity<Task> toggleComplete(@PathVariable Long id,
                                                @RequestBody Map<String, Boolean> body) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.setCompleted(body.getOrDefault("completed", !task.isCompleted()));
        taskRepository.save(task);
        return ResponseEntity.ok(task);
    }
}
