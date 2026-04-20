import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel {

    String state = "HOME";
    String mode = "";

    int selectedOption = 0;

    // 🔥 ENGINE
    GameEngine engine = new GameEngine();

    // 🔥 HOLD MOVEMENT FLAGS
    boolean leftPressed = false;
    boolean rightPressed = false;

    public GamePanel() {

        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        setBackground(new Color(40, 40, 60));

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                // HOME
                if (state.equals("HOME")) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        state = "MODE";
                    }
                }

                // MODE
                else if (state.equals("MODE")) {

                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        selectedOption = (selectedOption - 1 + 2) % 2;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        selectedOption = (selectedOption + 1) % 2;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        mode = (selectedOption == 0) ? "CLASSIC" : "HARDCORE";
                        engine.resetGame();
                        state = "PLAYING";
                    }
                }

                // GAME CONTROLS
                else if (state.equals("PLAYING")) {

                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        leftPressed = true;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        rightPressed = true;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftPressed = false;
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightPressed = false;
                }
            }
        });

        Timer timer = new Timer(16, e -> {

            if (state.equals("PLAYING")) {

                // 🔥 SMOOTH MOVEMENT
                if (leftPressed) {
                    engine.movePaddleLeft();
                }

                if (rightPressed) {
                    engine.movePaddleRight();
                }

                engine.updateGame();
            }

            repaint();
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (state) {
            case "HOME" -> drawHome(g);
            case "MODE" -> drawMode(g);
            case "PLAYING" -> drawGame(g);
        }
    }

    // 🏠 HOME
    private void drawHome(Graphics g) {

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        drawCentered(g, "BRICK BREAKER", 150);

        int boxW = 240, boxH = 60;
        int x = (getWidth() - boxW) / 2;
        int y = 350;

        g.setColor(new Color(70, 70, 100));
        g.fillRect(x, y, boxW, boxH);

        g.setColor(Color.WHITE);
        g.drawRect(x, y, boxW, boxH);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        drawCentered(g, "START GAME", y + 38);

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.LIGHT_GRAY);
        drawCentered(g, "Press ENTER", 450);
    }

    // 🎮 MODE
    private void drawMode(Graphics g) {

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        drawCentered(g, "SELECT MODE", 150);

        String[] options = {"Classic Mode", "Hardcore Mode"};

        for (int i = 0; i < 2; i++) {

            int y = 280 + i * 70;

            if (i == selectedOption) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.WHITE);
            }

            g.setFont(new Font("Arial", Font.BOLD, 26));
            drawCentered(g, options[i], y);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.LIGHT_GRAY);
        drawCentered(g, "Use ↑ ↓ + ENTER", 500);
    }

    // 🎯 GAME
    private void drawGame(Graphics g) {

        // Ball
        g.setColor(Color.WHITE);
        g.fillOval(engine.getBallX(), engine.getBallY(),
                   engine.getBallSize(), engine.getBallSize());

        // Paddle
        g.setColor(Color.CYAN);
        g.fillRect(engine.getPaddleX(), engine.getPaddleY(),
                   engine.getPaddleWidth(), engine.getPaddleHeight());

        // Bricks
        boolean[][] bricks = engine.getBricks();

        int totalWidth = engine.getCols() * engine.getBrickWidth()
                + (engine.getCols() - 1) * engine.getBrickGap();

        int startX = (getWidth() - totalWidth) / 2;

        for (int row = 0; row < engine.getRows(); row++) {

            if (row == 0) g.setColor(Color.RED);
            else if (row == 1) g.setColor(Color.ORANGE);
            else g.setColor(Color.GREEN);

            for (int col = 0; col < engine.getCols(); col++) {

                if (!bricks[row][col]) continue;

                int x = startX + col * (engine.getBrickWidth() + engine.getBrickGap());
                int y = 50 + row * (engine.getBrickHeight() + engine.getBrickGap());

                g.fillRect(x, y,
                           engine.getBrickWidth(),
                           engine.getBrickHeight());
            }
        }

        // Score
        g.setColor(Color.WHITE);
        g.drawString("Score: " + engine.getScore(), 10, 20);

        // Game Over
        if (engine.isGameOver()) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            drawCentered(g, "GAME OVER", 300);
        }
    }

    private void drawCentered(Graphics g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }
}