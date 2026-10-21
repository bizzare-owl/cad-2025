package client;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TaskManagerForm extends JFrame {
    private JTable taskTable;
    private DefaultTableModel taskTableModel;
    private List<TaskItem> tasks;
    private String authHeader;
    private String userRole;
    private String username;
    private final String API_BASE = "http://localhost:8080/api";

    public TaskManagerForm(String authHeader) {
        this.authHeader = authHeader;
        parseUserFromAuth(authHeader);
        tasks = new ArrayList<>();

        setTitle("Task Manager — " + username);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columns = {"ID", "Title", "Description", "Priority", "Category", "Status", "Created"};
        taskTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        taskTable = new JTable(taskTableModel);
        taskTable.setRowHeight(25);
        taskTable.getTableHeader().setReorderingAllowed(false);

        setupTableCellRenderer();

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton refreshButton = new JButton("Обновить");
        JButton addButton = new JButton("Добавить");
        JButton deleteButton = new JButton("Удалить");
        if ("MODERATOR".equals(userRole)) {
            deleteButton.setForeground(Color.RED);
        }

        JLabel sortLabel = new JLabel("Сортировка:");
        JComboBox<String> sortByCombo = new JComboBox<>(new String[]{
                "CreatedAt", "Priority", "Title"
        });
        sortByCombo.setSelectedItem("CreatedAt");
        JComboBox<String> sortOrderCombo = new JComboBox<>(new String[]{"Desc", "Asc"});
        sortOrderCombo.setSelectedItem("Desc");
        JButton sortButton = new JButton("Применить");

        toolBar.add(refreshButton);
        toolBar.add(addButton);
        if ("MODERATOR".equals(userRole)) {
            toolBar.add(deleteButton);
        }
        toolBar.addSeparator();
        toolBar.add(sortLabel);
        toolBar.add(sortByCombo);
        toolBar.add(sortOrderCombo);
        toolBar.add(sortButton);

        refreshButton.addActionListener(e -> loadTasks());

        addButton.addActionListener(e -> showAddTaskDialog());

        deleteButton.addActionListener(e -> deleteSelectedTask());

        sortButton.addActionListener(e -> {
            String sortBy = (String) sortByCombo.getSelectedItem();
            String order = (String) sortOrderCombo.getSelectedItem();
            loadTasksWithSort(sortBy, order);
        });

        taskTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = taskTable.getSelectedRow();
                    if (row >= 0) {
                        toggleTaskCompletion(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskTable);

        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel roleLabel = new JLabel("Роль: " + userRole);
        JLabel userLabel = new JLabel("Пользователь: " + username);
        statusBar.add(userLabel);
        statusBar.add(roleLabel);
        add(statusBar, BorderLayout.SOUTH);

        loadTasks();
    }

    private void parseUserFromAuth(String authHeader) {
        try {
            String decoded = new String(java.util.Base64.getDecoder().decode(authHeader));
            String[] parts = decoded.split(":", 2);
            username = parts[0];
            if ("moderator".equals(username)) {
                userRole = "MODERATOR";
            } else if ("manager".equals(username)) {
                userRole = "MANAGER";
            } else {
                userRole = "USER";
            }
        } catch (Exception e) {
            username = "user";
            userRole = "USER";
        }
    }

    private String makeRequest(String urlString, String method, String authHeader,
                                String jsonBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Basic " + authHeader);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (jsonBody != null && !jsonBody.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes("UTF-8"));
            }
        }

        int responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        if (responseCode >= 400) {
            if (responseCode == 403) {
                JOptionPane.showMessageDialog(this,
                        "Доступ запрещён. Необходима роль MODERATOR.",
                        "Ошибка доступа", JOptionPane.ERROR_MESSAGE);
            } else if (responseCode == 404) {
                JOptionPane.showMessageDialog(this,
                        "Задача не найдена.",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка сервера: " + responseCode,
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }

        return response.toString();
    }

    private List<TaskItem> parseTaskListJson(String json) {
        List<TaskItem> result = new ArrayList<>();
        if (json == null || json.isEmpty()) return result;

        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1).trim();
        }
        if (json.isEmpty()) return result;
        String[] taskStrings = json.split("\\},\\{");
        for (String ts : taskStrings) {
            ts = ts.replace("{", "").replace("}", "").trim();
            TaskItem item = parseTaskJson(ts);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    private TaskItem parseTaskJson(String taskStr) {
        TaskItem item = new TaskItem();
        String[] fields = taskStr.split(",");
        for (String field : fields) {
            String[] kv = field.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim().replace("\"", "");
            String value = kv[1].trim();

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            switch (key) {
                case "id":
                    item.setId(Long.parseLong(value));
                    break;
                case "title":
                    item.setTitle(value);
                    break;
                case "description":
                    item.setDescription(value);
                    break;
                case "completed":
                    item.setCompleted(Boolean.parseBoolean(value));
                    break;
                case "priority":
                    item.setPriority(value);
                    break;
                case "category":
                    item.setCategory(value);
                    break;
                case "createdAt":
                    item.setCreatedAt(value);
                    break;
            }
        }
        return item;
    }

    private String taskToJson(TaskItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"title\":\"").append(escapeJson(item.getTitle())).append("\",");
        sb.append("\"description\":\"").append(escapeJson(item.getDescription() != null ? item.getDescription() : "")).append("\",");
        sb.append("\"priority\":\"").append(item.getPriority() != null ? item.getPriority() : "MEDIUM").append("\",");
        sb.append("\"category\":\"").append(escapeJson(item.getCategory() != null ? item.getCategory() : "")).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }


    private void loadTasks() {
        loadTasksWithSort("CreatedAt", "Desc");
    }

    private void loadTasksWithSort(String sortBy, String order) {
        try {
            String url = API_BASE + "?sortBy=" + sortBy + "&order=" + order.toLowerCase();
            String json = makeRequest(url, "GET", authHeader, null);
            if (json != null) {
                tasks = parseTaskListJson(json);
                updateTableModel();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки задач: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateTableModel() {
        taskTableModel.setRowCount(0);
        for (TaskItem task : tasks) {
            String status = task.isCompleted() ? "Выполнена" : "Не выполнена";
            String created = task.getCreatedAt() != null
                    ? task.getCreatedAt().replace("T", " ").substring(0, 16)
                    : "—";
            taskTableModel.addRow(new Object[]{
                    task.getId(),
                    task.getTitle(),
                    task.getDescription() != null ? task.getDescription() : "—",
                    task.getPriority() != null ? task.getPriority() : "MEDIUM",
                    task.getCategory() != null ? task.getCategory() : "—",
                    status,
                    created
            });
        }
    }

    private void setupTableCellRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    TaskItem task = tasks.get(row);
                    String priority = task.getPriority();

                    if ("HIGH".equals(priority)) {
                        c.setBackground(new Color(255, 220, 220)); // светло-красный
                    } else if ("MEDIUM".equals(priority)) {
                        c.setBackground(new Color(255, 240, 200)); // светло-оранжевый
                    } else {
                        c.setBackground(new Color(220, 255, 220)); // светло-зелёный
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                }

                return c;
            }
        };

        // Центрирование некоторых колонок
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < taskTable.getColumnCount(); i++) {
            taskTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        // Title и Description — по левому краю
        taskTable.getColumnModel().getColumn(1).setCellRenderer(
                new DefaultTableCellRenderer() {{ setHorizontalAlignment(SwingConstants.LEFT); }});
        taskTable.getColumnModel().getColumn(2).setCellRenderer(
                new DefaultTableCellRenderer() {{ setHorizontalAlignment(SwingConstants.LEFT); }});

        // Ширина колонок
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Title
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(200);  // Description
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(80);   // Priority
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Category
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(90);   // Status
        taskTable.getColumnModel().getColumn(6).setPreferredWidth(150);  // Created
    }

    private void showAddTaskDialog() {
        JDialog dialog = new JDialog(this, "Добавить задачу", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(400, 220);
        dialog.setLocationRelativeTo(this);

        JLabel titleLabel = new JLabel("Название:");
        JTextField titleField = new JTextField();
        JLabel descLabel = new JLabel("Описание:");
        JTextArea descArea = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descArea);
        JLabel priorityLabel = new JLabel("Приоритет:");
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH"});
        JLabel categoryLabel = new JLabel("Категория:");
        JTextField categoryField = new JTextField();

        dialog.add(titleLabel);
        dialog.add(titleField);
        dialog.add(descLabel);
        dialog.add(descScroll);
        dialog.add(priorityLabel);
        dialog.add(priorityCombo);
        dialog.add(categoryLabel);
        dialog.add(categoryField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("Добавить");
        JButton cancelButton = new JButton("Отмена");
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel);

        ActionListener closeAndRefresh = e -> {
            dialog.dispose();
            loadTasks();
        };

        submitButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Название задачи не может быть пустым!",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            TaskItem newItem = new TaskItem();
            newItem.setTitle(title);
            newItem.setDescription(descArea.getText().trim().isEmpty()
                    ? null : descArea.getText().trim());
            newItem.setPriority((String) priorityCombo.getSelectedItem());
            newItem.setCategory(categoryField.getText().trim().isEmpty()
                    ? null : categoryField.getText().trim());

            try {
                String json = taskToJson(newItem);
                String response = makeRequest(API_BASE + "/tasks", "POST", authHeader, json);
                if (response != null) {
                    JOptionPane.showMessageDialog(dialog,
                            "Задача успешно добавлена!",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                    loadTasks();
                }
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Ошибка при добавлении: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }


    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Выберите задачу для удаления.",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TaskItem task = tasks.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить задачу \"" + task.getTitle() + "\"?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String url = API_BASE + "/tasks/" + task.getId();
                String response = makeRequest(url, "DELETE", authHeader, null);
                if (response != null || "204".equals(String.valueOf(
                        HttpURLConnection.class.getDeclaredField("responseCode").get(null)))) {
                    tasks.remove(selectedRow);
                    updateTableModel();
                    JOptionPane.showMessageDialog(this,
                            "Задача удалена.",
                            "Удалено", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                tasks.remove(selectedRow);
                updateTableModel();
                JOptionPane.showMessageDialog(this,
                        "Задача удалена.",
                        "Удалено", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void toggleTaskCompletion(int row) {
        TaskItem task = tasks.get(row);
        task.setCompleted(!task.isCompleted());

        try {
            String json = "{\"completed\":" + task.isCompleted() + "}";
            String response = makeRequest(API_BASE + "/tasks/" + task.getId() + "/complete",
                    "PUT", authHeader, json);
            if (response != null) {
                updateTableModel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TaskManagerForm("VXNlcjpwYXNzd29yZA==").setVisible(true);
        });
    }
}
