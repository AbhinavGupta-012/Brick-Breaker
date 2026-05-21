import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel {

    // =========================
    // 🎮 GAME MODES
    // =========================

    String state = "MENU";

    int selectedOption = 0;

    // =========================
    // 🟢 SINGLE PLAYER
    // =========================

    GameEngine singleEngine = new GameEngine(200);

    // =========================
    // 🔴 MULTIPLAYER
    // =========================

    GameEngine leftEngine = new GameEngine(0);
    GameEngine rightEngine = new GameEngine(400);

    // =========================
    // 🎹 INPUT FLAGS
    // =========================

    // Single Player
    boolean leftPressed = false;
    boolean rightPressed = false;

    // Multiplayer P1
    boolean aPressed = false;
    boolean dPressed = false;

    // Multiplayer P2
    boolean multiLeftPressed = false;
    boolean multiRightPressed = false;

    public GamePanel() {

        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        setBackground(new Color(40, 40, 60));

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                // =========================
                // 🏠 MENU
                // =========================

                if (state.equals("MENU")) {

                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        selectedOption =
                                (selectedOption - 1 + 2) % 2;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        selectedOption =
                                (selectedOption + 1) % 2;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                        if (selectedOption == 0) {

                            singleEngine.resetGame();
                            state = "SINGLE";

                        } else {

                            leftEngine.resetGame();
                            rightEngine.resetGame();
                            state = "MULTI";
                        }
                    }
                }

                // =========================
                // 🟢 SINGLE PLAYER
                // =========================

                else if (state.equals("SINGLE")) {

                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        leftPressed = true;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        rightPressed = true;
                    }

                    // Restart
                    if (singleEngine.isGameOver()
                            && e.getKeyCode() == KeyEvent.VK_ENTER) {

                        singleEngine.resetGame();
                    }
                }

                // =========================
                // 🔴 MULTIPLAYER
                // =========================

                else if (state.equals("MULTI")) {

                    // PLAYER 1
                    if (e.getKeyCode() == KeyEvent.VK_A) {
                        aPressed = true;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_D) {
                        dPressed = true;
                    }

                    // PLAYER 2
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        multiLeftPressed = true;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        multiRightPressed = true;
                    }

                    // Restart
                    if (leftEngine.isGameOver()
                            && rightEngine.isGameOver()
                            && e.getKeyCode() == KeyEvent.VK_ENTER) {

                        leftEngine.resetGame();
                        rightEngine.resetGame();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

                // SINGLE
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftPressed = false;
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightPressed = false;
                }

                // MULTI P1
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    aPressed = false;
                }

                if (e.getKeyCode() == KeyEvent.VK_D) {
                    dPressed = false;
                }

                // MULTI P2
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    multiLeftPressed = false;
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    multiRightPressed = false;
                }
            }
        });

        // =========================
        // 🔄 GAME LOOP
        // =========================

        Timer timer = new Timer(16, e -> {

            // SINGLE PLAYER
            if (state.equals("SINGLE")) {

                if (leftPressed) {
                    singleEngine.moveLeft();
                }

                if (rightPressed) {
                    singleEngine.moveRight();
                }

                singleEngine.updateGame();
            }

            // MULTIPLAYER
            else if (state.equals("MULTI")) {

                // PLAYER 1
                if (aPressed) {
                    leftEngine.moveLeft();
                }

                if (dPressed) {
                    leftEngine.moveRight();
                }

                // PLAYER 2
                if (multiLeftPressed) {
                    rightEngine.moveLeft();
                }

                if (multiRightPressed) {
                    rightEngine.moveRight();
                }

                leftEngine.updateGame();
                rightEngine.updateGame();
            }

            repaint();
        });

        timer.start();
    }

    // =========================
    // 🎨 PAINT
    // =========================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (state) {

            case "MENU" -> drawMenu(g);

            case "SINGLE" -> drawSingle(g);

            case "MULTI" -> drawMulti(g);
        }
    }

    // =========================
    // 🏠 MENU SCREEN
    // =========================

    private void drawMenu(Graphics g) {

        g.setColor(Color.WHITE);

        g.setFont(new Font("Arial", Font.BOLD, 48));
        drawCentered(g, "BRICK BREAKER", 150);

        String[] options = {
                "Single Player",
                "Two Player"
        };

        for (int i = 0; i < options.length; i++) {

            if (i == selectedOption) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.WHITE);
            }

            g.setFont(new Font("Arial", Font.BOLD, 28));

            drawCentered(g,
                    options[i],
                    300 + i * 70);
        }

        g.setColor(Color.LIGHT_GRAY);

        g.setFont(new Font("Arial", Font.PLAIN, 16));

        drawCentered(g,
                "Use ↑ ↓ and ENTER",
                500);
    }

    // =========================
    // 🟢 SINGLE PLAYER
    // =========================

    private void drawSingle(Graphics g) {

        drawEngine(g, singleEngine);

        g.setColor(Color.WHITE);

        g.setFont(new Font("Arial", Font.BOLD, 16));

        g.drawString("Single Player", 20, 20);

        // GAME OVER
        if (singleEngine.isGameOver()) {

            g.setFont(new Font("Arial", Font.BOLD, 40));

            drawCentered(g,
                    "GAME OVER",
                    280);

            g.setFont(new Font("Arial", Font.PLAIN, 20));

            drawCentered(g,
                    "Press ENTER to Restart",
                    330);
        }
    }

    // =========================
    // 🔴 MULTIPLAYER
    // =========================

    private void drawMulti(Graphics g) {

        // Divider
        g.setColor(Color.GRAY);

        g.fillRect(getWidth() / 2 - 2,
                0,
                4,
                getHeight());

        drawEngine(g, leftEngine);
        drawEngine(g, rightEngine);

        // Labels
        g.setColor(Color.WHITE);

        g.setFont(new Font("Arial", Font.BOLD, 16));

        g.drawString("PLAYER 1 (A/D)", 20, 20);

        g.drawString("PLAYER 2 (← →)", 500, 20);

        // BOTH LOST
        if (leftEngine.isGameOver()
                && rightEngine.isGameOver()) {

            g.setFont(new Font("Arial", Font.BOLD, 36));

            String result;

            if (leftEngine.getScore()
                    > rightEngine.getScore()) {

                result = "PLAYER 1 WINS";

            }

            else if (rightEngine.getScore()
                    > leftEngine.getScore()) {

                result = "PLAYER 2 WINS";

            }

            else {

                result = "DRAW";
            }

            drawCentered(g,
                    result,
                    280);

            g.setFont(new Font("Arial", Font.PLAIN, 20));

            drawCentered(g,
                    "Press ENTER to Play Again",
                    330);
        }
    }

    // =========================
    // 🎮 DRAW ENGINE
    // =========================

    private void drawEngine(Graphics g,
                            GameEngine engine) {

        // Ball
        g.setColor(Color.WHITE);

        g.fillOval(engine.getBallX(),
                engine.getBallY(),
                engine.getBallSize(),
                engine.getBallSize());

        // Paddle
        g.setColor(Color.CYAN);

        g.fillRect(engine.getPaddleX(),
                engine.getPaddleY(),
                engine.getPaddleWidth(),
                engine.getPaddleHeight());

        // Bricks
        boolean[][] bricks = engine.getBricks();

        int totalWidth =
                engine.getCols() * engine.getBrickWidth()
                        + (engine.getCols() - 1)
                        * engine.getBrickGap();

        int startX =
                engine.getOffsetX()
                        + (engine.getAreaWidth()
                        - totalWidth) / 2;

        for (int row = 0;
             row < engine.getRows();
             row++) {

            if (row == 0) {
                g.setColor(Color.RED);
            }

            else if (row == 1) {
                g.setColor(Color.ORANGE);
            }

            else {
                g.setColor(Color.GREEN);
            }

            for (int col = 0;
                 col < engine.getCols();
                 col++) {

                if (!bricks[row][col]) {
                    continue;
                }

                int x =
                        startX
                                + col
                                * (engine.getBrickWidth()
                                + engine.getBrickGap());

                int y =
                        50
                                + row
                                * (engine.getBrickHeight()
                                + engine.getBrickGap());

                g.fillRect(x,
                        y,
                        engine.getBrickWidth(),
                        engine.getBrickHeight());
            }
        }

        // Score
        g.setColor(Color.WHITE);

        g.drawString(
                "Score: " + engine.getScore(),
                engine.getOffsetX() + 20,
                45
        );

        // Game Over
        if (engine.isGameOver()) {

            g.setFont(new Font("Arial",
                    Font.BOLD,
                    24));

            g.drawString("LOST",
                    engine.getOffsetX() + 120,
                    300);
        }
    }

    // =========================
    // 🧠 CENTER TEXT
    // =========================

    private void drawCentered(Graphics g,
                              String text,
                              int y) {

        FontMetrics fm =
                g.getFontMetrics();

        int x =
                (getWidth()
                        - fm.stringWidth(text))
                        / 2;

        g.drawString(text,
                x,
                y);
    }
}