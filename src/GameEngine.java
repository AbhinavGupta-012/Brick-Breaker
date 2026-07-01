import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    public static final int WALL_THICKNESS = 8;

    // Offset for split screen
    private int offsetX;

    // Play area width
    private int areaWidth;

    // Ball
    private double ballX;
    private double ballY;
    private double ballDX = 6.0; // Starts at speed magnitude 6 now
    private double ballDY = -6.0;
    private int ballSize = 20;

    // Paddle
    private int paddleX;
    private int paddleY = 590;
    private int paddleWidth = 100;
    private int paddleHeight = 10;

    // Bricks configuration (dynamic now)
    private int rows;
    private int cols;
    private int brickWidth;
    private int brickHeight;
    private int brickGap;

    // Brick types and health arrays
    // TYPE_BASIC = 1: Green, 1 hit, 10 pts
    // TYPE_TOUGH = 2: Orange/Bronze, 2 hits, 30 pts total (10 on hit, 20 on break)
    // TYPE_ELITE = 3: Silver/Steel, 3 hits, 70 pts total (15 on hit, 20 on hit, 35 on break)
    // TYPE_EXPLOSIVE = 4: Gold/Explosive, 1 hit, 100 pts + explodes adjacent
    // TYPE_ARMORED = 5: Obsidian Purple, 4 hits, 150 pts total (20, 30, 40, 60 on break)
    // TYPE_PLATINUM = 6: Platinum Crystal, 5 hits, 250 pts total (30, 40, 50, 60, 70 on break)
    // TYPE_QUANTUM = 7: Dark Matter Red, 6 hits, 400 pts total (40, 50, 60, 70, 80, 100 on break)
    // TYPE_COSMIC = 8: Cosmic Glass, 7 hits, 600 pts total (50, 60, 70, 80, 90, 100, 150 on break)
    // TYPE_STELLAR = 9: Stellar Core, 8 hits, 800 pts total (70, 80, 90, 100, 110, 120, 130, 100 on break)
    private int[][] brickType;
    private int[][] brickHealth;
    private int[][] brickMaxHealth;

    // Score
    private int score = 0;

    // Game State
    private int level = 1;
    private boolean gameOver = false;
    private boolean victory = false;
    private int lives = 3;
    private boolean hardcore = false;
    
    // Level Spawn Safe Mechanics
    private boolean ballGlued = true;
    private int countdownTicks = 90; // 3-second level start countdown (quicker ticks)

    // Collision & Hit Events for Visual Effects
    public static class BrickEvent {
        public int row, col, x, y, type, points;
        public boolean destroyed;
        public boolean isExplosion;

        public BrickEvent(int row, int col, int x, int y, int type, int points, boolean destroyed, boolean isExplosion) {
            this.row = row;
            this.col = col;
            this.x = x;
            this.y = y;
            this.type = type;
            this.points = points;
            this.destroyed = destroyed;
            this.isExplosion = isExplosion;
        }
    }

    private List<BrickEvent> pendingEvents = new ArrayList<>();

    public GameEngine(int offsetX, int areaWidth) {
        this.offsetX = offsetX;
        this.areaWidth = areaWidth;

        // Paddle spawn
        paddleX = offsetX + areaWidth / 2 - 50;

        // Initial glued ball position centered on top of paddle
        ballX = paddleX + paddleWidth / 2 - ballSize / 2;
        ballY = paddleY - ballSize;

        // Dynamic brick layout depending on layout width to prevent bleeding
        if (areaWidth <= 400) {
            this.rows = 4;
            this.cols = 5;
            this.brickWidth = 60;
            this.brickGap = 10;
            this.brickHeight = 20;
        } else {
            this.rows = 5;
            this.cols = 8;
            this.brickWidth = 80;
            this.brickGap = 12;
            this.brickHeight = 25;
        }

        brickType = new int[rows][cols];
        brickHealth = new int[rows][cols];
        brickMaxHealth = new int[rows][cols];

        initializeBricks();
    }

    private void initializeBricks() {
        // Limits for golden explosive bricks
        int maxGold = (level <= 3) ? 2 : 1;
        int goldCount = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int type = 1; // Basic by default
                
                if (level == 1) {
                    if (r == 0) {
                        type = 3; // Elite top row
                    } else if (r == 1) {
                        type = 2; // Tough row 1
                    } else if (r == 2) {
                        type = (Math.random() < 0.4) ? 2 : 1; // Mixed
                    }

                    // Scatter explosive gold bricks
                    if (r >= 2 && r <= 3 && Math.random() < 0.12 && goldCount < maxGold) {
                        type = 4; // Explosive
                        goldCount++;
                    }
                } else if (level == 2) {
                    // Level 2: Armored bricks (Type 5, 4 hits) on top row
                    if (r == 0) {
                        type = 5; 
                    } else if (r == 1) {
                        type = 3; 
                    } else if (r == 2) {
                        type = 2;
                    }

                    // Scatter explosives
                    if (r >= 1 && Math.random() < 0.15 && goldCount < maxGold) {
                        type = 4;
                        goldCount++;
                    }
                } else if (level == 3 || level == 4) {
                    // Level 3-4: Platinum Bricks (Type 6, 5 hits) on top row!
                    if (r == 0) {
                        type = 6;
                    } else if (r == 1) {
                        type = 5;
                    } else if (r == 2) {
                        type = 3;
                    } else {
                        type = 2;
                    }

                    // Scatter explosives
                    if (r >= 1 && Math.random() < 0.15 && goldCount < maxGold) {
                        type = 4;
                        goldCount++;
                    }
                } else if (level == 5) {
                    // Level 5: Quantum/Dark Matter Bricks (Type 7, 6 hits) on top row!
                    if (r == 0) {
                        type = 7;
                    } else if (r == 1) {
                        type = 6;
                    } else if (r == 2) {
                        type = 5;
                    } else {
                        type = (Math.random() < 0.5) ? 3 : 2;
                    }

                    // Scatter explosives
                    if (r >= 1 && Math.random() < 0.15 && goldCount < maxGold) {
                        type = 4;
                        goldCount++;
                    }
                } else if (level == 6) {
                    // Level 6: Cosmic Glass Bricks (Type 8, 7 hits) on top row!
                    if (r == 0) {
                        type = 8;
                    } else if (r == 1) {
                        type = 7;
                    } else if (r == 2) {
                        type = 6;
                    } else if (r == 3) {
                        type = 5;
                    } else {
                        type = (Math.random() < 0.5) ? 3 : 2;
                    }

                    // Scatter explosives
                    if (r >= 1 && Math.random() < 0.15 && goldCount < maxGold) {
                        type = 4;
                        goldCount++;
                    }
                } else {
                    // Level 7+: Stellar Core Bricks (Type 9, 8 hits) on top row!
                    if (r == 0) {
                        type = 9;
                    } else if (r == 1) {
                        type = 8;
                    } else if (r == 2) {
                        type = 7;
                    } else if (r == 3) {
                        type = 6;
                    } else {
                        type = (Math.random() < 0.5) ? 5 : 3;
                    }

                    // Scatter explosives
                    if (r >= 1 && Math.random() < 0.15 && goldCount < maxGold) {
                        type = 4;
                        goldCount++;
                    }
                }

                brickType[r][c] = type;

                int maxH = 1;
                if (type == 2) maxH = 2;
                else if (type == 3) maxH = 3;
                else if (type == 5) maxH = 4;
                else if (type == 6) maxH = 5;
                else if (type == 7) maxH = 6;
                else if (type == 8) maxH = 7;
                else if (type == 9) maxH = 8;

                brickMaxHealth[r][c] = maxH;
                brickHealth[r][c] = maxH;
            }
        }
    }

    public void updateGame() {
        if (gameOver || victory) return;

        // =========================
        // PADDLE SPAWN GLUE LAYER
        // =========================
        if (ballGlued) {
            // Ball stays perfectly glued to the top of the paddle
            ballX = paddleX + paddleWidth / 2 - ballSize / 2;
            ballY = paddleY - ballSize;

            if (countdownTicks > 0) {
                countdownTicks--;
                if (countdownTicks == 0) {
                    ballGlued = false;
                    // Launch upward diagonally with correct level speed
                    int targetSpeed = 6 + (level - 1);
                    if (targetSpeed > 11) targetSpeed = 11;
                    ballDX = (Math.random() < 0.5) ? -(targetSpeed - 1) : (targetSpeed - 1);
                    ballDY = -targetSpeed;
                }
            }
            return; // Wait for timer countdown to complete
        }

        // Ball movement
        ballX += ballDX;
        ballY += ballDY;

        // =========================
        // WALL COLLISION
        // =========================

        // Left wall
        if (ballX <= offsetX + WALL_THICKNESS) {
            ballX = offsetX + WALL_THICKNESS;
            ballDX = -ballDX;
        }

        // Right wall
        if (ballX + ballSize >= offsetX + areaWidth - WALL_THICKNESS) {
            ballX = offsetX + areaWidth - WALL_THICKNESS - ballSize;
            ballDX = -ballDX;
        }

        // Top wall
        if (ballY <= 0) {
            ballY = 0;
            ballDY = -ballDY;
        }

        // =========================
        // PADDLE COLLISION (Robust AABB)
        // =========================
        if (ballX + ballSize > paddleX && ballX < paddleX + paddleWidth &&
            ballY + ballSize > paddleY && ballY < paddleY + paddleHeight) {

            double overlapLeft = (ballX + ballSize) - paddleX;
            double overlapRight = (paddleX + paddleWidth) - ballX;
            double overlapTop = (ballY + ballSize) - paddleY;
            double overlapBottom = (paddleY + paddleHeight) - ballY;

            double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

            if (minOverlap == overlapTop) {
                ballDY = -Math.abs(ballDY);
                ballY = paddleY - ballSize;

                // Calculate current speed before bounce
                double currentSpeed = Math.sqrt(ballDX * ballDX + ballDY * ballDY);

                // Set a floor speed based on level so it never stalls out at high levels
                double minAllowedSpeed = 6.0 + (level - 1);
                if (minAllowedSpeed > 11.0) minAllowedSpeed = 11.0;
                if (currentSpeed < minAllowedSpeed) currentSpeed = minAllowedSpeed;

                // Adjust horizontal steering angle based on hit ratio on paddle
                int hitPosition = (int) ((ballX + ballSize / 2) - paddleX);
                double hitRatio = (double) hitPosition / paddleWidth;
                if (hitRatio < 0.0) hitRatio = 0.0;
                if (hitRatio > 1.0) hitRatio = 1.0;

                // Map hitRatio (0.0 to 1.0) -> 145 degrees (left) to 35 degrees (right)
                double minAngle = Math.toRadians(145);
                double maxAngle = Math.toRadians(35);
                double angle = minAngle + hitRatio * (maxAngle - minAngle);

                // Preserve speed while assigning velocities based on bounce angle
                ballDX = currentSpeed * Math.cos(angle);
                ballDY = -currentSpeed * Math.sin(angle);

                // Prevent perfectly vertical trajectory boredom
                if (Math.abs(ballDX) < 1) {
                    ballDX = (Math.random() < 0.5) ? -2 : 2;
                }
                // Prevent perfectly flat trajectory that loops forever
                if (Math.abs(ballDY) < 2) {
                    ballDY = -3;
                }
            } else if (minOverlap == overlapLeft) {
                ballDX = -Math.abs(ballDX);
                ballX = paddleX - ballSize;
            } else if (minOverlap == overlapRight) {
                ballDX = Math.abs(ballDX);
                ballX = paddleX + paddleWidth;
            } else {
                ballDY = Math.abs(ballDY);
                ballY = paddleY + paddleHeight;
            }
        }

        // =========================
        // BRICK COLLISION (Robust AABB)
        // =========================
        int totalWidth = cols * brickWidth + (cols - 1) * brickGap;
        int startX = offsetX + (areaWidth - totalWidth) / 2;

        boolean hitDetected = false;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (brickHealth[r][c] <= 0) continue;

                int brickX = startX + c * (brickWidth + brickGap);
                int brickY = 90 + r * (brickHeight + brickGap);

                if (ballX + ballSize > brickX && ballX < brickX + brickWidth &&
                    ballY + ballSize > brickY && ballY < brickY + brickHeight) {

                    double overlapLeft = (ballX + ballSize) - brickX;
                    double overlapRight = (brickX + brickWidth) - ballX;
                    double overlapTop = (ballY + ballSize) - brickY;
                    double overlapBottom = (brickY + brickHeight) - ballY;

                    double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

                    // Snapping position & bouncing appropriately
                    if (minOverlap == overlapTop) {
                        ballDY = -Math.abs(ballDY);
                        ballY = brickY - ballSize;
                    } else if (minOverlap == overlapBottom) {
                        ballDY = Math.abs(ballDY);
                        ballY = brickY + brickHeight;
                    } else if (minOverlap == overlapLeft) {
                        ballDX = -Math.abs(ballDX);
                        ballX = brickX - ballSize;
                    } else {
                        ballDX = Math.abs(ballDX);
                        ballX = brickX + brickWidth;
                    }

                    damageBrick(r, c);
                    hitDetected = true;
                    break;
                }
            }
            if (hitDetected) break;
        }

        // =========================
        // GAME OVER OR LIFE LOSS
        // =========================
        if (ballY > 600) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
            } else {
                // Immediately position ball on paddle and launch without countdown!
                ballGlued = false;
                countdownTicks = 0;
                
                // Position ball exactly on paddle's current location without centering the paddle
                ballX = paddleX + paddleWidth / 2 - ballSize / 2;
                ballY = paddleY - ballSize;
                
                int targetSpeed = 6 + (level - 1);
                if (targetSpeed > 11) targetSpeed = 11;
                ballDX = (Math.random() < 0.5) ? -(targetSpeed - 1) : (targetSpeed - 1);
                ballDY = -targetSpeed;
                
                // Add special event for life lost to shake screen & clear trail
                pendingEvents.add(new BrickEvent(-2, -2, (int) ballX, (int) ballY, 0, 0, false, false));
            }
        }

        // =========================
        // VICTORY CHECK & PROGRESSION
        // =========================
        boolean anyLeft = false;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (brickHealth[r][c] > 0) {
                    anyLeft = true;
                    break;
                }
            }
            if (anyLeft) break;
        }

        if (!anyLeft) {
            // Infinite bricks: Advance level and reinitialize bricks
            level++;
            
            paddleX = offsetX + areaWidth / 2 - paddleWidth / 2;
            ballGlued = true;
            countdownTicks = 90; // 3 second countdown for next level
            
            // Adjust ball speed (Level 1: 6, Level 2: 7, Level 3: 8, etc.)
            int targetSpeed = 6 + (level - 1);
            if (targetSpeed > 11) targetSpeed = 11; // Cap speed
            
            ballDX = (ballDX > 0 ? targetSpeed - 1 : -(targetSpeed - 1));
            ballDY = -targetSpeed;

            initializeBricks();

            // Trigger LEVEL UP notification event for floating text popup
            pendingEvents.add(new BrickEvent(-1, -1, offsetX + areaWidth / 2 - 40, 150, 0, 0, false, false));
        }

        // Keep ball within wall boundaries at all times to prevent clipping through side walls
        if (ballX < offsetX + WALL_THICKNESS) {
            ballX = offsetX + WALL_THICKNESS;
            ballDX = Math.abs(ballDX);
        } else if (ballX + ballSize > offsetX + areaWidth - WALL_THICKNESS) {
            ballX = offsetX + areaWidth - WALL_THICKNESS - ballSize;
            ballDX = -Math.abs(ballDX);
        }
    }

    private void damageBrick(int r, int c) {
        if (brickHealth[r][c] <= 0) return;

        int type = brickType[r][c];
        brickHealth[r][c]--;

        int pointsAwarded = 0;
        boolean destroyed = (brickHealth[r][c] <= 0);

        if (type == 1) { // Basic
            pointsAwarded = 10;
        } else if (type == 2) { // Tough
            pointsAwarded = destroyed ? 20 : 10;
        } else if (type == 3) { // Elite
            if (brickHealth[r][c] == 2) pointsAwarded = 15;
            else if (brickHealth[r][c] == 1) pointsAwarded = 20;
            else pointsAwarded = 35;
        } else if (type == 4) { // Explosive
            pointsAwarded = 100;
        } else if (type == 5) { // Armored
            if (brickHealth[r][c] == 3) pointsAwarded = 20;
            else if (brickHealth[r][c] == 2) pointsAwarded = 30;
            else if (brickHealth[r][c] == 1) pointsAwarded = 40;
            else pointsAwarded = 60; // 150 total
        } else if (type == 6) { // Platinum
            if (brickHealth[r][c] == 4) pointsAwarded = 30;
            else if (brickHealth[r][c] == 3) pointsAwarded = 40;
            else if (brickHealth[r][c] == 2) pointsAwarded = 50;
            else if (brickHealth[r][c] == 1) pointsAwarded = 60;
            else pointsAwarded = 70; // 250 total
        } else if (type == 7) { // Quantum
            if (brickHealth[r][c] == 5) pointsAwarded = 40;
            else if (brickHealth[r][c] == 4) pointsAwarded = 50;
            else if (brickHealth[r][c] == 3) pointsAwarded = 60;
            else if (brickHealth[r][c] == 2) pointsAwarded = 70;
            else if (brickHealth[r][c] == 1) pointsAwarded = 80;
            else pointsAwarded = 100; // 400 total
        } else if (type == 8) { // Cosmic
            if (brickHealth[r][c] == 6) pointsAwarded = 50;
            else if (brickHealth[r][c] == 5) pointsAwarded = 60;
            else if (brickHealth[r][c] == 4) pointsAwarded = 70;
            else if (brickHealth[r][c] == 3) pointsAwarded = 80;
            else if (brickHealth[r][c] == 2) pointsAwarded = 90;
            else if (brickHealth[r][c] == 1) pointsAwarded = 100;
            else pointsAwarded = 150; // 600 total
        } else if (type == 9) { // Stellar
            if (brickHealth[r][c] == 7) pointsAwarded = 70;
            else if (brickHealth[r][c] == 6) pointsAwarded = 80;
            else if (brickHealth[r][c] == 5) pointsAwarded = 90;
            else if (brickHealth[r][c] == 4) pointsAwarded = 100;
            else if (brickHealth[r][c] == 3) pointsAwarded = 110;
            else if (brickHealth[r][c] == 2) pointsAwarded = 120;
            else if (brickHealth[r][c] == 1) pointsAwarded = 130;
            else pointsAwarded = 100; // 800 total
        }

        score += pointsAwarded;

        int totalWidth = cols * brickWidth + (cols - 1) * brickGap;
        int startX = offsetX + (areaWidth - totalWidth) / 2;
        int brickX = startX + c * (brickWidth + brickGap);
        int brickY = 90 + r * (brickHeight + brickGap);

        pendingEvents.add(new BrickEvent(r, c, brickX, brickY, type, pointsAwarded, destroyed, false));

        // Trigger chain explosion if destroyed
        if (type == 4 && destroyed) {
            triggerExplosion(r, c);
        }
    }

    private void triggerExplosion(int expRow, int expCol) {
        // Collect all active adjacent neighbor coordinates
        List<int[]> activeNeighbors = new ArrayList<>();
        for (int r = expRow - 1; r <= expRow + 1; r++) {
            for (int c = expCol - 1; c <= expCol + 1; c++) {
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    if (r == expRow && c == expCol) continue;
                    if (brickHealth[r][c] > 0) {
                        activeNeighbors.add(new int[]{r, c});
                    }
                }
            }
        }

        // Determine maximum adjacent bricks allowed to break based on level (diminishing impact)
        int maxBreaks = 8;
        if (level == 3 || level == 4) maxBreaks = 6;
        else if (level == 5 || level == 6) maxBreaks = 4;
        else if (level >= 7) maxBreaks = 2;

        // Shuffle neighbor list to pick randomly
        java.util.Collections.shuffle(activeNeighbors);

        int breaksCount = 0;
        for (int[] coord : activeNeighbors) {
            if (breaksCount >= maxBreaks) break;

            int r = coord[0];
            int c = coord[1];

            int type = brickType[r][c];
            int points = 0;
            if (type == 1) points = 10;
            else if (type == 2) points = 30;
            else if (type == 3) points = 70;
            else if (type == 5) points = 150;
            else if (type == 6) points = 250;
            else if (type == 7) points = 400;
            else if (type == 8) points = 600;
            else if (type == 9) points = 800;
            else if (type == 4) points = 100;

            brickHealth[r][c] = 0; // Vaporize!
            score += points;

            int totalWidth = cols * brickWidth + (cols - 1) * brickGap;
            int startX = offsetX + (areaWidth - totalWidth) / 2;
            int brickX = startX + c * (brickWidth + brickGap);
            int brickY = 90 + r * (brickHeight + brickGap);

            pendingEvents.add(new BrickEvent(r, c, brickX, brickY, type, points, true, true));
            breaksCount++;

            // Chain explosion
            if (type == 4) {
                triggerExplosion(r, c);
            }
        }
    }

    // Paddle movement
    public void moveLeft() {
        if (paddleX > offsetX + WALL_THICKNESS) {
            paddleX -= 12;
            if (paddleX < offsetX + WALL_THICKNESS) paddleX = offsetX + WALL_THICKNESS;
        }
    }

    public void moveRight() {
        if (paddleX < offsetX + areaWidth - WALL_THICKNESS - paddleWidth) {
            paddleX += 12;
            if (paddleX > offsetX + areaWidth - WALL_THICKNESS - paddleWidth) {
                paddleX = offsetX + areaWidth - WALL_THICKNESS - paddleWidth;
            }
        }
    }

    // Reset
    public void resetGame() {
        paddleX = offsetX + areaWidth / 2 - 50;

        ballGlued = true;
        countdownTicks = 90; // 3-second level start countdown (quicker ticks)

        ballX = paddleX + paddleWidth / 2 - ballSize / 2;
        ballY = paddleY - ballSize;

        ballDX = 6.0;
        ballDY = -6.0;

        level = 1;
        initializeBricks();

        lives = hardcore ? 1 : 3;

        score = 0;
        gameOver = false;
        victory = false;
        pendingEvents.clear();
    }

    // Getters
    public int getBallX() { return (int) Math.round(ballX); }
    public int getBallY() { return (int) Math.round(ballY); }
    public int getBallSize() { return ballSize; }
    public int getLevel() { return level; }
    public boolean isBallGlued() { return ballGlued; }
    public int getCountdownTicks() { return countdownTicks; }

    public int getPaddleX() { return paddleX; }
    public int getPaddleY() { return paddleY; }
    public int getPaddleWidth() { return paddleWidth; }
    public int getPaddleHeight() { return paddleHeight; }

    public boolean[][] getBricks() {
        boolean[][] activeBricks = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                activeBricks[r][c] = (brickHealth[r][c] > 0);
            }
        }
        return activeBricks;
    }

    public int[][] getBrickType() { return brickType; }
    public int[][] getBrickHealth() { return brickHealth; }
    public int[][] getBrickMaxHealth() { return brickMaxHealth; }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int getBrickWidth() { return brickWidth; }
    public int getBrickHeight() { return brickHeight; }
    public int getBrickGap() { return brickGap; }

    public int getScore() { return score; }

    public boolean isGameOver() { return gameOver; }
    public boolean isVictory() { return victory; }

    public int getOffsetX() { return offsetX; }
    public int getAreaWidth() { return areaWidth; }
    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
        this.lives = hardcore ? 1 : 3;
    }
    public int getLives() { return lives; }
    public boolean isHardcore() { return hardcore; }

    public List<BrickEvent> getPendingEvents() {
        List<BrickEvent> copy = new ArrayList<>(pendingEvents);
        pendingEvents.clear();
        return copy;
    }
}