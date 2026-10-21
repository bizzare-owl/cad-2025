package ru.gitverse.bizzareowl.lab2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
@Controller
public class TaskController {

        @Autowired
        private TaskRepository taskRepository;
        @GetMapping("/")
        public String index(Model model) {
            model.addAttribute("tasks", taskRepository.findAll());
            return "index";
        }
        @PostMapping("/addTask")
        public String addTask(@ModelAttribute Task task) {
            taskRepository.save(task);
            return "redirect:/";
        }
        @PreAuthorize("hasRole('MODERATOR')")
        @GetMapping("/deleteTask/{id}")
        public String deleteTask(@PathVariable Long id) {
            taskRepository.deleteById(id);
            return "redirect:/";
        }
        @PreAuthorize("hasRole('MODERATOR')")
        @PostMapping("/updateTask/{id}")
        public String updateTask(@PathVariable Long id, @RequestParam boolean
                completed) {
            Task task = taskRepository.findById(id).orElseThrow();
            task.setCompleted(completed);
            taskRepository.save(task);
            return "redirect:/";
        }


    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/updatePriority/{id}")
    public String updatePriority(@RequestBody Priority priority, @PathVariable Long id) {
        for (Task task : taskRepository.findAll()) {
            if (task.getId().equals(id)) {
                task.setPriority(priority);
                break;
            }
        }
        return "redirect:/";
    }
}