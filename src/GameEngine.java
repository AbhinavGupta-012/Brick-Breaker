public class GameEngine{
    private int ballX = 380;
    private int ballY = 300;
    private int ballDX = 4;
    private int ballDY = -4;
    private int ballSize = 20;

    private int paddleX = 350;
    private int paddleY = 550;
    private int paddleWidth = 100;
    private int paddleHeight = 10;

    private int panelWidth = 800;
    private int panelHeight = 600;

    public void updateGame(){
        ballX += ballDX;
        ballY += ballDY;

        if (ballX <= 0 || ballX >= panelWidth - ballSize){
            ballDX = -ballDX;
        }

        if (ballY <= 0){
            ballDY = -ballDY;
        }

        if (ballY + ballSize >= paddleY && ballY + ballSize <= paddleY + paddleHeight && ballX + ballSize >= paddleX && ballX <= paddleX + paddleWidth){
            ballDY = -Math.abs(ballDY);

            int hitPosition = (ballX + ballSize / 2) - paddleX;
            double hitRatio = (double) hitPosition / paddleWidth;

            ballDX = (int) (8 * (hitRatio - 0.5));
        }
    }

    public void movePaddleLeft(){
        if (paddleX > 0){
            paddleX -= 10;
        }
    }

    public void movePaddleRight(){
        if (paddleX < panelWidth - paddleWidth){
            paddleX += 10;
        }
    }

    public int getBallX(){
        return ballX;
    }

    public int getBallY(){
        return ballY;
    }
    
    public int getBallSize(){
        return ballSize;
    }
    
    public int getPaddleX(){
        return paddleX;
    }
    
    public int getPaddleY(){
        return paddleY;
    }
    
    public int getPaddleWidth(){
        return paddleWidth;
    }
    
    public int getPaddleHeight(){
        return paddleHeight;
    }
}