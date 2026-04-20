import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel{
    private GameEngine engine;

    boolean leftPressed = false;
    boolean rightPressed = false;

    int rows = 3;
    int cols = 7;
    int brickWidth = 80;
    int brickHeight = 25;
    int brickGap = 10;

    public GamePanel(){
        engine = new GameEngine();

        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
            }

            public void keyReleased(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
            }
        });

        Timer timer = new Timer(16, e ->{
            if (leftPressed){
                engine.movePaddleLeft();
            }
            if (rightPressed){
                engine.movePaddleRight();
            }

            engine.updateGame();

            repaint();
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        g.fillOval(engine.getBallX(), engine.getBallY(), engine.getBallSize(), engine.getBallSize());

        g.fillRect(
            engine.getPaddleX(),
            engine.getPaddleY(),
            engine.getPaddleWidth(),
            engine.getPaddleHeight()
        );

        int totalWidth = cols * brickWidth + (cols - 1) * brickGap;
        int startX = (getWidth() - totalWidth) / 2;

        for (int row = 0; row < rows; row++){
            for (int col = 0; col < cols; col++){
                int x = startX + col * (brickWidth + brickGap);
                int y = 50 + row * (brickHeight + brickGap);

                g.fillRect(x, y, brickWidth, brickHeight);
            }
        }
    }
}