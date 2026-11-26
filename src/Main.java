import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends JFrame {
    private final TaskDAO dao = new TaskDAO();
    private final ExecutorService dbThread = Executors.newSingleThreadExecutor();
    private final JTextField titleField = new JTextField(22);
    private final JTextArea descriptionArea = new JTextArea(3, 22);
    private final JComboBox<String> priorityBox = new JComboBox<>(new String[]{"Low", "Medium", "High"});
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"Pending", "In Progress", "Completed"});
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel(
        new Object[]{"ID", "Task", "Description", "Priority", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JLabel statusLabel = new JLabel("Ready");
    private final JButton addButton = new JButton("Add Task");
    private final JButton updateButton = new JButton("Update");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton refreshButton = new JButton("Refresh");
    private int selectedId = -1;

    public Main() {
        setTitle("To Do List Yola");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 570);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("TO DO LIST YOLA", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 26));
        heading.setForeground(new Color(94, 65, 150));
        heading.setBorder(BorderFactory.createEmptyBorder(16, 8, 4, 8));
        add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Task Details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 7, 5, 7);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Task title:"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(titleField, c);
        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        form.add(new JLabel("Description:"), c);
        c.gridx = 1; c.gridy = 1;
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        form.add(new JScrollPane(descriptionArea), c);
        c.gridx = 0; c.gridy = 2;
        form.add(new JLabel("Priority:"), c);
        c.gridx = 1; c.gridy = 2;
        form.add(priorityBox, c);
        c.gridx = 0; c.gridy = 3;
        form.add(new JLabel("Status:"), c);
        c.gridx = 1; c.gridy = 3;
        form.add(statusBox, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton); buttons.add(updateButton);
        buttons.add(deleteButton); buttons.add(refreshButton);
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        form.add(buttons, c);

        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
                titleField.setText(model.getValueAt(row, 1).toString());
                descriptionArea.setText(model.getValueAt(row, 2).toString());
                priorityBox.setSelectedItem(model.getValueAt(row, 3).toString());
                statusBox.setSelectedItem(model.getValueAt(row, 4).toString());
            }
        });

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        center.add(form, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        add(statusLabel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> saveNewTask());
        updateButton.addActionListener(e -> updateSelectedTask());
        deleteButton.addActionListener(e -> deleteSelectedTask());
        refreshButton.addActionListener(e -> loadTasks());

        loadTasks();
    }

    private Task readForm() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task title tidak boleh kosong.");
            return null;
        }
        return new Task(title, descriptionArea.getText().trim(),
            priorityBox.getSelectedItem().toString(), statusBox.getSelectedItem().toString());
    }

    private void saveNewTask() {
        Task task = readForm();
        if (task == null) return;
        setBusy(true, "Saving...");
        dbThread.submit(() -> {
            try {
                dao.insertTask(task);
                SwingUtilities.invokeLater(() -> {
                    setBusy(false, "Task berhasil ditambahkan.");
                    clearForm();
                    loadTasks();
                });
            } catch (Exception ex) {
                showError(ex);
            }
        });
    }

    private void updateSelectedTask() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih task pada tabel terlebih dahulu.");
            return;
        }
        Task formTask = readForm();
        if (formTask == null) return;
        Task task = new Task(selectedId, formTask.getTitle(), formTask.getDescription(),
            formTask.getPriority(), formTask.getStatus());
        setBusy(true, "Updating...");
        dbThread.submit(() -> {
            try {
                dao.updateTask(task);
                SwingUtilities.invokeLater(() -> {
                    setBusy(false, "Task berhasil diperbarui.");
                    clearForm();
                    loadTasks();
                });
            } catch (Exception ex) {
                showError(ex);
            }
        });
    }

    private void deleteSelectedTask() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih task pada tabel terlebih dahulu.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Hapus task yang dipilih?",
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        int id = selectedId;
        setBusy(true, "Deleting...");
        dbThread.submit(() -> {
            try {
                dao.deleteTask(id);
                SwingUtilities.invokeLater(() -> {
                    setBusy(false, "Task berhasil dihapus.");
                    clearForm();
                    loadTasks();
                });
            } catch (Exception ex) {
                showError(ex);
            }
        });
    }

    private void loadTasks() {
        refreshButton.setEnabled(false);
        statusLabel.setText("Loading tasks...");
        dbThread.submit(() -> {
            try {
                var tasks = dao.getAllTasks();
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (Task task : tasks) {
                        model.addRow(new Object[]{task.getId(), task.getTitle(),
                            task.getDescription(), task.getPriority(), task.getStatus()});
                    }
                    refreshButton.setEnabled(true);
                    statusLabel.setText("Ready | " + tasks.size() + " task(s)");
                });
            } catch (Exception ex) {
                showError(ex);
            }
        });
    }

    private void setBusy(boolean busy, String message) {
        addButton.setEnabled(!busy);
        updateButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
        statusLabel.setText(message);
    }

    private void clearForm() {
        selectedId = -1;
        table.clearSelection();
        titleField.setText("");
        descriptionArea.setText("");
        priorityBox.setSelectedIndex(0);
        statusBox.setSelectedIndex(0);
    }

    private void showError(Exception ex) {
        SwingUtilities.invokeLater(() -> {
            setBusy(false, "Operation failed.");
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
