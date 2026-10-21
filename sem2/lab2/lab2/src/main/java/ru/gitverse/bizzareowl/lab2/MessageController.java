package ru.gitverse.bizzareowl.lab2;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MessageController {

    private List<String> userMessages = new ArrayList<>();


    @GetMapping("/")
    public String helloWorld() {
        return "Hello, World!";
    }

    @GetMapping("/messages")
    public List<String> getAllMessages() {
        return userMessages;
    }

    @PostMapping("/messages")
    public String publishMessages(@RequestBody String message) {
        userMessages.add(message);
        return "Message published successfully";
    }

    @PutMapping("/messages/{index}")
    public String publishMessages(@PathVariable int index, @RequestBody String message) {
        if (index >= 0 && index < userMessages.size()) {
            userMessages.set(index, message);
            return "Message update successfully";
        }

        return "Message not found at index" + index;
    }

    @DeleteMapping("/messages/{index}")
    public String publishMessages(@PathVariable int index) {
        if (index >= 0 && index < userMessages.size()) {
            userMessages.remove(index);
            return "Message deleted successfully";
        }

        return "Message not found at index" + index;
    }

    // Дополнительное задание

    @GetMapping("/message/top")
    public List<String> getFirst5Messages() {
        return userMessages.subList(0, 5);
    }

    @GetMapping("/message/sorted")
    public List<String> getSortedMessages() {
        return userMessages.stream().sorted().toList();
    }

    // Вариант 15 (3) Создать метод getMessageByIndex(int index)
    @GetMapping("/message/{index}")
    public String getMessageByIndex(@PathVariable int index) {
        return index >= 0 && index < userMessages.size() ? userMessages.get(index) : "Message not found at index" + index;
    }

}


