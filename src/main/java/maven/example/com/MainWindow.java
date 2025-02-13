package maven.example.com;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    public MainWindow() {
        setTitle("Snake");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        GameField field = new GameField(); // Создаем ОДИН экземпляр
        field.setPreferredSize(new Dimension(512, 512));

        setLayout(new BorderLayout());
        add(field, BorderLayout.CENTER); // Добавляем созданный экземпляр

        pack(); // Применяем pack() после добавления всех компонентов
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true); // Делаем окно видимым в конце
    }

    public static void main(String[] args) {
        MainWindow mainWindow = new MainWindow();
        // все должно работать
    }
}