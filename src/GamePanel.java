import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel {

    // Ball position
    int ballX = 380;
    int ballY = 300;

    // Ball movement (velocity)
    int ballDX = 4;
    int ballDY = -4;

    // Paddle
    int paddleX = 350;
    int paddleWidth = 100;

    // Movement flags
    boolean leftPressed = false;
    boolean rightPressed = false;

    // Bricks
    int rows = 3;
    int cols = 7;
    int brickWidth = 80;
    int brickHeight = 25;
    int brickGap = 10;

    public GamePanel() {

        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
            }
        });

        Timer timer = new Timer(16, e -> {

            // Move ball
            ballX += ballDX;
            ballY += ballDY;

            // Bounce off left/right walls
            if (ballX <= 0 || ballX >= getWidth() - 20) {
                ballDX = -ballDX;
            }

            // Bounce off top wall
            if (ballY <= 0) {
                ballDY = -ballDY;
            }

            // Paddle movement
            if (leftPressed && paddleX > 0) {
                paddleX -= 10;
            }
            if (rightPressed && paddleX < getWidth() - paddleWidth) {
                paddleX += 10;
            }

            repaint();
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Ball
        g.fillOval(ballX, ballY, 20, 20);

        // Paddle
        g.fillRect(paddleX, 550, paddleWidth, 10);

        // Bricks (centered)
        int totalWidth = cols * brickWidth + (cols - 1) * brickGap;
        int startX = (getWidth() - totalWidth) / 2;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                int x = startX + col * (brickWidth + brickGap);
                int y = 50 + row * (brickHeight + brickGap);

                g.fillRect(x, y, brickWidth, brickHeight);
            }
        }
    }
}