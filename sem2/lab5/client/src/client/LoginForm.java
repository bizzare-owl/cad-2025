package client;

import javax.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;
public class LoginForm extends JFrame {
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("Войти");
    public LoginForm() {
        setTitle("Вход в менеджер задач");
        setSize(380, 190);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        form.add(new JLabel("Логин:"));
        form.add(usernameField);
        form.add(new JLabel("Пароль:"));
        form.add(passwordField);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(loginButton);

        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        // Обработчик авторизации
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Попытка авторизации
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String encodedAuth =
                        Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
                // Если авторизация успешна, открываем главную форму
                TaskManagerForm taskManagerForm = new
                        TaskManagerForm(encodedAuth);
                taskManagerForm.setVisible(true);
                dispose(); // Закрываем форму авторизации
                System.out.println("Encoded Auth: " + encodedAuth);
            }
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}