public class GameEngine {

    // Offset for split screen
    private int offsetX;

    // Play area width
    private int areaWidth = 400;

    // Ball
    private int ballX;
    private int ballY;
    private int ballDX = 4;
    private int ballDY = -4;
    private int ballSize = 20;

    // Paddle
    private int paddleX;
    private int paddleY = 550;
    private int paddleWidth = 100;
    private int paddleHeight = 10;

    // Bricks
    private int rows = 3;
    private int cols = 5;
    private int brickWidth = 60;
    private int brickHeight = 25;
    private int brickGap = 10;

    private boolean[][] bricks;

    // Score
    private int score = 0;

    // Game State
    private boolean gameOver = false;

    public GameEngine(int offsetX) {

        this.offsetX = offsetX;

        // Ball spawn
        ballX = offsetX + 190;
        ballY = 300;

        // Paddle spawn
        paddleX = offsetX + 150;

        // Bricks
        bricks = new boolean[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                bricks[i][j] = true;
            }
        }
    }

    public void updateGame() {

        if (gameOver) return;

        // Ball movement
        ballX += ballDX;
        ballY += ballDY;

        // Wall collision
        if (ballX <= offsetX || ballX >= offsetX + areaWidth - ballSize) {
            ballDX = -ballDX;
        }

        if (ballY <= 0) {
            ballDY = -ballDY;
        }

        // Paddle collision
        if (ballY + ballSize >= paddleY &&
            ballY + ballSize <= paddleY + paddleHeight &&
            ballX + ballSize >= paddleX &&
            ballX <= paddleX + paddleWidth) {

            ballDY = -Math.abs(ballDY);

            int hitPosition = (ballX + ballSize / 2) - paddleX;
            double hitRatio = (double) hitPosition / paddleWidth;

            ballDX = (int)(8 * (hitRatio - 0.5));

            ballY = paddleY - ballSize;
        }

        // Brick collision
        int totalWidth = cols * brickWidth + (cols - 1) * brickGap;
        int startX = offsetX + (areaWidth - totalWidth) / 2;

        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < cols; col++) {

                if (!bricks[row][col]) continue;

                int brickX = startX + col * (brickWidth + brickGap);
                int brickY = 50 + row * (brickHeight + brickGap);

                if (ballX < brickX + brickWidth &&
                    ballX + ballSize > brickX &&
                    ballY < brickY + brickHeight &&
                    ballY + ballSize > brickY) {

                    bricks[row][col] = false;

                    score += 10;

                    ballDY = -ballDY;

                    return;
                }
            }
        }

        // Game Over
        if (ballY > 600) {
            gameOver = true;
        }
    }

    // Paddle movement
    public void moveLeft() {

        if (paddleX > offsetX) {
            paddleX -= 10;
        }
    }

    public void moveRight() {

        if (paddleX < offsetX + areaWidth - paddleWidth) {
            paddleX += 10;
        }
    }

    // Reset
    public void resetGame() {

        ballX = offsetX + 190;
        ballY = 300;

        ballDX = 4;
        ballDY = -4;

        paddleX = offsetX + 150;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                bricks[i][j] = true;
            }
        }

        score = 0;
        gameOver = false;
    }

    // Getters
    public int getBallX() { return ballX; }
    public int getBallY() { return ballY; }
    public int getBallSize() { return ballSize; }

    public int getPaddleX() { return paddleX; }
    public int getPaddleY() { return paddleY; }
    public int getPaddleWidth() { return paddleWidth; }
    public int getPaddleHeight() { return paddleHeight; }

    public boolean[][] getBricks() { return bricks; }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int getBrickWidth() { return brickWidth; }
    public int getBrickHeight() { return brickHeight; }
    public int getBrickGap() { return brickGap; }

    public int getScore() { return score; }

    public boolean isGameOver() { return gameOver; }

    public int getOffsetX() { return offsetX; }

    public int getAreaWidth() { return areaWidth; }
}