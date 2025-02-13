package maven.example.com;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Random;

public class GameField extends JPanel implements ActionListener {
    private final int SIZE = 512;
    private final int DOT_SIZE = 16;
    private final int ALL_DOTS = (SIZE / DOT_SIZE) * (SIZE / DOT_SIZE);
    private Image dot;
    private Image apple;
    private ImageIcon gameIcon;
    private int appleX;
    private int appleY;
    private int[] x = new int[ALL_DOTS];
    private int[] y = new int[ALL_DOTS];
    private int dots;
    private Timer timer;
    private boolean left = false;
    private boolean right = true;
    private boolean up = false;
    private boolean down = false;
    private boolean inGame = true;
    private boolean dialogShown = false;
    private int score = 0;


    public GameField() {
        setBackground(Color.BLACK);
        loadImages();
        initGame();
        addKeyListener(new FieldKeyListener());
        setFocusable(true);
    }

    private void resetGame() {
        // 1. Сброс состояний управления
        left = false;
        right = true;
        up = false;
        down = false;

        // 2. Сброс позиции змейки
        dots = 3;
        for (int i = 0; i < dots; i++) {
            x[i] = 48 - i * DOT_SIZE;
            y[i] = 48;
        }

        // 3. Генерация нового яблока
        createApple();

        // 4. Сброс игровых флагов
        inGame = true;
        dialogShown = false;

        // 5. Перезапуск таймера
        timer.start();
    }

    public void initGame() {
        dots = 3;
        int startX = SIZE / 2;
        int startY = SIZE / 2;
        for (int i = 0; i < dots; i++) {
            x[i] = startX - i * DOT_SIZE;
            y[i] = startY;
        }
        timer = new Timer(100, this);
        timer.start();
        createApple();
    }


    public void createApple() {
        int maxCell = SIZE/DOT_SIZE;
        boolean onSnake;
        do {
            onSnake = false;
            appleX = new Random().nextInt(maxCell) * DOT_SIZE;
            appleY = new Random().nextInt(maxCell) * DOT_SIZE;

            for (int i = 0; i < dots; i++) {
                if (appleX == x[i] && appleY == y[i]) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);
    }

    public void loadImages() {
        ImageIcon iia = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("apple.png"))
        );
        if (iia.getImageLoadStatus() != MediaTracker.COMPLETE) {
            JOptionPane.showMessageDialog(this, "load failure apple.png");
            System.exit(1);
        }
        apple = iia.getImage();

        ImageIcon iid = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("dot.png"))
        );
        if (iid.getImageLoadStatus() != MediaTracker.COMPLETE) {
            JOptionPane.showMessageDialog(this, "load failure dot.png");
            System.exit(1);
        }
        dot = iid.getImage();
        gameIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("snake1.png")));

        // Проверка загрузки
        if(gameIcon.getImage().getWidth(null) < 0) {
            JOptionPane.showMessageDialog(this, "Icon load failure");
            System.exit(1);
        }
    }
    private void playSound(String soundFile) { try {
        // Используем прямой путь к файлам в папке resources
        URL soundUrl = getClass().getClassLoader().getResource(soundFile);
        if (soundUrl == null) {
            throw new IllegalArgumentException("File not found: " + soundFile);
        }

        //AudioInputStream ais = AudioSystem.getAudioInputStream(soundUrl);
        AudioInputStream ais = AudioSystem.getAudioInputStream(
                Objects.requireNonNull(getClass().getClassLoader().getResource("eat.wav")));
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        clip.start();
    } catch (UnsupportedAudioFileException | IOException e) {
        JOptionPane.showMessageDialog(this, "Ошибка формата аудио: " + e.getMessage());
    } catch (LineUnavailableException e) {
        JOptionPane.showMessageDialog(this, "Аудиоустройство занято: " + e.getMessage());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Ошибка: " + e.getClass().getSimpleName());
    }
    }



    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (inGame) {
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 10, 20);
            g.drawImage(apple, appleX, appleY, DOT_SIZE, DOT_SIZE, this);
            for (int i = 0; i < dots; i++) {
                g.drawImage(dot, x[i], y[i], DOT_SIZE, DOT_SIZE, this);
            }
        } else {
            String str = "Game Over";
            Font font = new Font("Arial", Font.BOLD, 20);
            FontMetrics metrics = g.getFontMetrics(font);
            int x = (getWidth() - metrics.stringWidth(str)) / 2;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

            g.setColor(Color.WHITE);
            g.setFont(font);
            g.drawString(str, x, y);
        }
    }

    public void move() {
        for (int i = dots; i > 0; i--) {
            if (i < x.length) { // Проверяем, чтобы не выйти за пределы массива
                x[i] = x[i - 1];
                y[i] = y[i - 1];
            }
        }
        if (left) {
            x[0] -= DOT_SIZE;
        }
        if (right) {
            x[0] += DOT_SIZE;
        }
        if (up) {
            y[0] -= DOT_SIZE;
        }
        if (down) {
            y[0] += DOT_SIZE;
        }
    }


    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            score += 10;
            playSound("eat.wav"); // Вызов звука
            dots++;
            createApple();
        }
    }

    public void checkCollisions() {
        // Проверка выхода за границы
        if (x[0] >= SIZE || x[0] < 0 || y[0] >= SIZE || y[0] < 0) {
            inGame = false;
            return;
        }

        // Проверка самопересечения (только для хвоста длиной >4)
        for (int i = 4; i < dots; i++) {
            if (x[0] == x[i] && y[0] == y[i]) {
                inGame = false;
                return;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            checkApple();
            checkCollisions();
            move();
        } else if (!dialogShown) {
            dialogShown = true;
            timer.stop();
            Object[] options = {"Yes", "No"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Game Over! Score is: " + score+ "\nOne more time?",
                    "Restart",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    gameIcon,    // наша иконка
                    options,     // массив кнопок
                    options[0]   // кнопка по умолчанию
            );
            if (choice == JOptionPane.YES_OPTION) {
                score = 0;
                resetGame();
            } else {
                System.exit(0);
            }

        }
        repaint();
    }

    class FieldKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            //super.keyPressed(e);
            if (!inGame) return;

            int key = e.getKeyCode();
            if (key == KeyEvent.VK_UP && !down) {
                up = true;
                left = false;
                right = false;
                down = false;
            }
            else if (key == KeyEvent.VK_DOWN && !up) {
                down = true;
                left = false;
                up = false;
                right = false;
            }
            else if (key == KeyEvent.VK_LEFT && !right) {
                left = true;
                right = false;
                up = false;
                down = false;
            }
            else if (key == KeyEvent.VK_RIGHT && !left) {
                right = true;
                left = false;
                up = false;
                down = false;
            }
        }
    }
}
