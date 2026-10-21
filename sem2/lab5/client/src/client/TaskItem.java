package client;

public class TaskItem {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private String priority;
    private String category;
    private String createdAt;

    public TaskItem() {
    }

    public TaskItem(Long id, String title, String description, boolean completed,
                    String priority, String category, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.priority = priority;
        this.category = category;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
