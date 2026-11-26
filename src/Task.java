public class Task {
    private int id;
    private String title;
    private String description;
    private String priority;
    private String status;

    public Task(int id, String title, String description, String priority, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
    }

    public Task(String title, String description, String priority, String status) {
        this(0, title, description, priority, status);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
}
