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

    private int rows = 3;
    private int cols = 7;
    private int brickWidth = 80;
    private int brickHeight = 25;
    private int brickGap = 10;
    private boolean[][] bricks;

    private int score = 0;

    public GameEngine(){
        bricks = new boolean[rows][cols];

        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                bricks[i][j] = true;
            }
        }
    }

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
            ballY = paddleY - ballSize;
        }

        int totalWidth = cols * brickWidth + (cols - 1) * brickGap;
        int startX = (panelWidth - totalWidth) / 2;

        for (int row = 0; row < rows; row++){
            for (int col = 0; col < cols; col++){
                if (!bricks[row][col]){
                    continue;
                }

                int brickX = startX + col * (brickWidth + brickGap);
                int brickY = 50 + row * (brickHeight + brickGap);

                if (ballX < brickX + brickWidth && ballX + ballSize > brickX && ballY < brickY + brickHeight && ballY + ballSize > brickY){
                    bricks[row][col] = false;
                    score += 10;

                    ballDY = -ballDY;
                    return;
                }
            }
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

    public boolean[][] getBricks(){
        return bricks;
    }

    public int getRows(){
        return rows;
    }

    public int getCols(){
        return cols;
    }

    public int getBrickWidth(){
        return brickWidth;
    }

    public int getBrickHeight(){
        return brickHeight;
    }

    public int getBrickGap(){
        return brickGap;
    }

    public int getScore(){
        return score;
    }
}