import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel {

    // =========================
    // 🎮 GAME STATES
    // =========================

    String state = "MENU";
    int selectedOption = 0;
    boolean paused = false;

    // End game button selection option (0: Restart, 1: Home Menu)
    int selectedEndOption = 0;

    // Pause menu button selection option (0: Resume, 1: Restart, 2: Home Menu)
    int selectedPauseOption = 0;

    // Mode selection difficulty
    int selectedModeOption = 0;
    boolean modeSelectMulti = false;

    // =========================
    // 🟢 SINGLE PLAYER
    // =========================

    GameEngine singleEngine = new GameEngine(0, 800);

    // =========================
    // 🔴 MULTIPLAYER
    // =========================

    GameEngine leftEngine = new GameEngine(0, 400);
    GameEngine rightEngine = new GameEngine(400, 400);

    // =========================
    // 🎹 INPUT FLAGS
    // =========================

    boolean leftPressed = false;
    boolean rightPressed = false;

    boolean aPressed = false;
    boolean dPressed = false;

    boolean multiLeftPressed = false;
    boolean multiRightPressed = false;

    // =========================
    // ✨ ARCADE EFFECTS SYSTEM
    // =========================

    // Particles
    static class Particle {
        double x, y;
        double dx, dy;
        Color color;
        int life;
        int maxLife;

        public Particle(double x, double y, double dx, double dy, Color color, int life) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
            this.life = life;
            this.maxLife = life;
        }

        public boolean update() {
            x += dx;
            y += dy;
            dy += 0.15; // Gravity
            life--;
            return life > 0;
        }

        public void draw(Graphics2D g2d) {
            float alpha = (float) life / maxLife;
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            int size = (int) (3 + 4 * alpha);
            g2d.fillRect((int) x, (int) y, size, size);
        }
    }

    // Floating Scores
    static class FloatingScore {
        double x, y;
        String text;
        Color color;
        int life;
        int maxLife;

        public FloatingScore(double x, double y, String text, Color color, int life) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
            this.life = life;
            this.maxLife = life;
        }

        public boolean update() {
            y -= 1.0; // Float upward
            life--;
            return life > 0;
        }

        public void draw(Graphics2D g2d) {
            float alpha = (float) life / maxLife;
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            g2d.setFont(new Font("SansSerif", Font.BOLD, (int) (12 + 5 * alpha)));
            g2d.drawString(text, (int) x, (int) y);
        }
    }

    // Ball History Positions
    static class BallPos {
        int x, y;
        public BallPos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // =========================
    // 🎨 CUSTOMIZABLE BALL COLORS
    // =========================

    public static class BallColorOption {
        public String name;
        public Color baseColor;
        public Color glowColor;
        public Color specColor;

        public BallColorOption(String name, Color baseColor, Color glowColor, Color specColor) {
            this.name = name;
            this.baseColor = baseColor;
            this.glowColor = glowColor;
            this.specColor = specColor;
        }
    }

    public List<BallColorOption> ballColors = new ArrayList<>();
    public int selectedBallColorIndex = 0;

    // Single Player Lists
    List<Particle> singleParticles = new ArrayList<>();
    List<FloatingScore> singleScores = new ArrayList<>();
    List<BallPos> singleBallTrail = new ArrayList<>();
    double singleShake = 0.0;

    // Multiplayer Lists
    List<Particle> leftParticles = new ArrayList<>();
    List<FloatingScore> leftScores = new ArrayList<>();
    List<BallPos> leftBallTrail = new ArrayList<>();
    double leftShake = 0.0;

    List<Particle> rightParticles = new ArrayList<>();
    List<FloatingScore> rightScores = new ArrayList<>();
    List<BallPos> rightBallTrail = new ArrayList<>();
    double rightShake = 0.0;

    // Background Themes configuration
    public static class BackgroundTheme {
        public String name;
        public Color darkTop;
        public Color lightTop;
        public Color darkBottom;
        public Color lightBottom;
        public Color gridColor;
        public Color logoColor1;
        public Color logoColor2;
        public Color headingColor;
        public Color headingGlowColor;
        
        public BackgroundTheme(String name, Color dt, Color lt, Color db, Color lb, Color gc,
                               Color lc1, Color lc2, Color hc, Color hgc) {
            this.name = name;
            this.darkTop = dt;
            this.lightTop = lt;
            this.darkBottom = db;
            this.lightBottom = lb;
            this.gridColor = gc;
            this.logoColor1 = lc1;
            this.logoColor2 = lc2;
            this.headingColor = hc;
            this.headingGlowColor = hgc;
        }
    }

    public List<BackgroundTheme> bgThemes = new ArrayList<>();
    public int selectedBgThemeIndex = 0;

    // Ambient floating particles configuration
    public static class MenuParticle {
        public double x, y, dx, dy;
        public Color color;
        public double size;
        
        public MenuParticle(double x, double y, double dx, double dy, Color color, double size) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
            this.size = size;
        }
        
        public void update(int w, int h) {
            x += dx;
            y += dy;
            if (x < 0) x = w;
            if (x > w) x = 0;
            if (y < 0) y = h;
            if (y > h) y = 0;
        }
        
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval((int) x, (int) y, (int) size, (int) size);
        }
    }

    public List<MenuParticle> menuParticles = new ArrayList<>();
    public int selectedSettingRow = 0;
    public int tempBallColorIndex = 0;
    public int tempBgThemeIndex = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        // Populate beautiful customization colors
        ballColors.add(new BallColorOption("Cyber Cyan", new Color(0, 120, 190), new Color(0, 255, 255), new Color(195, 245, 255)));
        ballColors.add(new BallColorOption("Neon Pink", new Color(190, 0, 110), new Color(255, 0, 255), new Color(255, 195, 245)));
        ballColors.add(new BallColorOption("Plasma Orange", new Color(190, 90, 0), new Color(255, 160, 0), new Color(255, 220, 180)));
        ballColors.add(new BallColorOption("Lime Green", new Color(0, 150, 30), new Color(80, 255, 80), new Color(190, 255, 190)));
        ballColors.add(new BallColorOption("Laser Red", new Color(160, 0, 0), new Color(255, 40, 40), new Color(255, 180, 180)));
        ballColors.add(new BallColorOption("Stellar Gold", new Color(190, 140, 0), new Color(255, 215, 0), new Color(255, 250, 205)));
        ballColors.add(new BallColorOption("Polaris White", new Color(150, 180, 200), new Color(200, 240, 255), new Color(255, 255, 255)));
        ballColors.add(new BallColorOption("Nebula Purple", new Color(90, 0, 160), new Color(180, 80, 255), new Color(230, 200, 255)));
        ballColors.add(new BallColorOption("Supernova Rose", new Color(180, 30, 80), new Color(255, 90, 120), new Color(255, 210, 220)));

        // Populate beautiful background themes (Blue, Red, Black, Green, Purple)
        bgThemes.add(new BackgroundTheme("Blue", new Color(10, 15, 35), new Color(15, 45, 90), new Color(25, 10, 45), new Color(35, 12, 65), new Color(255, 255, 255, 10), new Color(0, 220, 255), new Color(0, 100, 255), new Color(255, 255, 255), new Color(0, 220, 255, 100)));
        bgThemes.add(new BackgroundTheme("Red", new Color(15, 5, 5), new Color(55, 10, 10), new Color(35, 8, 8), new Color(85, 15, 15), new Color(255, 50, 50, 8), new Color(255, 80, 0), new Color(255, 0, 80), new Color(255, 220, 220), new Color(255, 40, 40, 100)));
        bgThemes.add(new BackgroundTheme("Black", new Color(5, 5, 5), new Color(35, 35, 35), new Color(10, 10, 10), new Color(45, 45, 45), new Color(255, 255, 255, 8), new Color(245, 245, 245), new Color(140, 140, 150), new Color(255, 255, 255), new Color(200, 220, 255, 80)));
        bgThemes.add(new BackgroundTheme("Green", new Color(5, 15, 5), new Color(15, 45, 20), new Color(10, 25, 12), new Color(25, 65, 30), new Color(80, 255, 80, 8), new Color(80, 255, 80), new Color(0, 180, 60), new Color(220, 255, 220), new Color(50, 255, 100, 100)));
        bgThemes.add(new BackgroundTheme("Purple", new Color(15, 5, 20), new Color(45, 10, 60), new Color(25, 8, 35), new Color(65, 12, 85), new Color(180, 80, 255, 8), new Color(200, 100, 255), new Color(255, 50, 150), new Color(250, 230, 255), new Color(180, 80, 255, 100)));

        // Initialize ambient menu particles
        Random rGen = new Random();
        for (int i = 0; i < 25; i++) {
            menuParticles.add(new MenuParticle(
                rGen.nextInt(800),
                rGen.nextInt(600),
                (rGen.nextDouble() - 0.5) * 1.0,
                (rGen.nextDouble() - 0.5) * 1.0,
                new Color(0, 255, 255, rGen.nextInt(40) + 15),
                rGen.nextDouble() * 4.0 + 2.0
            ));
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // ===================================
                // ⏸️ INTERCEPT INPUTS WHEN PAUSED
                // ===================================
                if (paused) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        paused = false;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        selectedPauseOption = (selectedPauseOption - 1 + 3) % 3;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        selectedPauseOption = (selectedPauseOption + 1) % 3;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (selectedPauseOption == 0) {
                            paused = false;
                        } else if (selectedPauseOption == 1) {
                            // Restart
                            if (state.equals("SINGLE")) {
                                singleEngine.resetGame();
                                singleBallTrail.clear();
                                singleParticles.clear();
                                singleScores.clear();
                                singleShake = 0.0;
                            } else if (state.equals("MULTI")) {
                                leftEngine.resetGame();
                                rightEngine.resetGame();
                                leftBallTrail.clear();
                                rightBallTrail.clear();
                                leftParticles.clear();
                                rightParticles.clear();
                                leftScores.clear();
                                rightScores.clear();
                                leftShake = 0.0;
                                rightShake = 0.0;
                            }
                            paused = false;
                        } else {
                            // Home Menu
                            paused = false;
                            state = "MENU";
                        }
                    }
                    return; // Block other gameplay key bindings
                }

                // Pausing mechanics
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (state.equals("SINGLE") || state.equals("MULTI")) {
                        selectedPauseOption = 0; // Default to resume
                        paused = true;
                    }
                }

                // =========================
                // 🔄 UNIVERSAL RESTART KEY R
                // =========================
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    if (state.equals("SINGLE")) {
                        singleEngine.resetGame();
                        singleBallTrail.clear();
                        singleParticles.clear();
                        singleScores.clear();
                        singleShake = 0.0;
                        selectedEndOption = 0;
                    } else if (state.equals("MULTI")) {
                        leftEngine.resetGame();
                        rightEngine.resetGame();
                        leftBallTrail.clear();
                        rightBallTrail.clear();
                        leftParticles.clear();
                        rightParticles.clear();
                        leftScores.clear();
                        rightScores.clear();
                        leftShake = 0.0;
                        rightShake = 0.0;
                        selectedEndOption = 0;
                    }
                }

                if (state.equals("MENU")) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        selectedOption = (selectedOption - 1 + 3) % 3;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        selectedOption = (selectedOption + 1) % 3;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (selectedOption == 0) { // Single Player
                            modeSelectMulti = false;
                            selectedModeOption = 0;
                            state = "MODE_SELECT";
                        } else if (selectedOption == 1) { // Two Player
                            modeSelectMulti = true;
                            selectedModeOption = 0;
                            state = "MODE_SELECT";
                        } else { // Ball Customisation
                            state = "SETTINGS";
                        }
                    }
                } else if (state.equals("MODE_SELECT")) {
                    if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_LEFT) {
                        selectedModeOption = (selectedModeOption - 1 + 2) % 2;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        selectedModeOption = (selectedModeOption + 1) % 2;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        state = "MENU";
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        boolean hc = (selectedModeOption == 1);
                        if (!modeSelectMulti) {
                            singleEngine.setHardcore(hc);
                            singleEngine.resetGame();
                            singleBallTrail.clear();
                            singleParticles.clear();
                            singleScores.clear();
                            singleShake = 0.0;
                            selectedEndOption = 0;
                            state = "SINGLE";
                        } else {
                            leftEngine.setHardcore(hc);
                            rightEngine.setHardcore(hc);
                            leftEngine.resetGame();
                            rightEngine.resetGame();
                            leftBallTrail.clear();
                            rightBallTrail.clear();
                            leftParticles.clear();
                            rightParticles.clear();
                            leftScores.clear();
                            rightScores.clear();
                            leftShake = 0.0;
                            rightShake = 0.0;
                            selectedEndOption = 0;
                            state = "MULTI";
                        }
                    }
                } else if (state.equals("SETTINGS")) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        selectedSettingRow = (selectedSettingRow - 1 + 2) % 2;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        selectedSettingRow = (selectedSettingRow + 1) % 2;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (selectedSettingRow == 0) {
                            tempBallColorIndex = selectedBallColorIndex;
                            state = "SETTINGS_BALL";
                        } else {
                            tempBgThemeIndex = selectedBgThemeIndex;
                            state = "SETTINGS_BG";
                        }
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        state = "MENU";
                    }
                } else if (state.equals("SETTINGS_BALL")) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        tempBallColorIndex = (tempBallColorIndex - 1 + ballColors.size()) % ballColors.size();
                    }
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        tempBallColorIndex = (tempBallColorIndex + 1) % ballColors.size();
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        selectedBallColorIndex = tempBallColorIndex;
                        state = "SETTINGS";
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        state = "SETTINGS";
                    }
                } else if (state.equals("SETTINGS_BG")) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        tempBgThemeIndex = (tempBgThemeIndex - 1 + bgThemes.size()) % bgThemes.size();
                    }
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        tempBgThemeIndex = (tempBgThemeIndex + 1) % bgThemes.size();
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        selectedBgThemeIndex = tempBgThemeIndex;
                        state = "SETTINGS";
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        state = "SETTINGS";
                    }
                } else if (state.equals("SINGLE")) {
                    if (singleEngine.isGameOver() || singleEngine.isVictory()) {
                        // End game interactive button controls
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            selectedEndOption = 0; // Highlight Restart
                        }
                        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            selectedEndOption = 1; // Highlight Home Menu
                        }
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            if (selectedEndOption == 0) {
                                singleEngine.resetGame();
                                singleBallTrail.clear();
                                singleParticles.clear();
                                singleScores.clear();
                                singleShake = 0.0;
                            } else {
                                state = "MENU";
                            }
                        }
                    } else {
                        // Regular playing controls
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            leftPressed = true;
                        }
                        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            rightPressed = true;
                        }
                    }
                } else if (state.equals("MULTI")) {
                    if ((leftEngine.isGameOver() || leftEngine.isVictory()) &&
                        (rightEngine.isGameOver() || rightEngine.isVictory())) {
                        // End game interactive button controls
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            selectedEndOption = 0; // Highlight Restart
                        }
                        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            selectedEndOption = 1; // Highlight Home Menu
                        }
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            if (selectedEndOption == 0) {
                                leftEngine.resetGame();
                                rightEngine.resetGame();
                                leftBallTrail.clear();
                                rightBallTrail.clear();
                                leftParticles.clear();
                                rightParticles.clear();
                                leftScores.clear();
                                rightScores.clear();
                                leftShake = 0.0;
                                rightShake = 0.0;
                            } else {
                                state = "MENU";
                            }
                        }
                    } else {
                        // Regular playing controls
                        if (e.getKeyCode() == KeyEvent.VK_A) {
                            aPressed = true;
                        }
                        if (e.getKeyCode() == KeyEvent.VK_D) {
                            dPressed = true;
                        }
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            multiLeftPressed = true;
                        }
                        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            multiRightPressed = true;
                        }
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
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    aPressed = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_D) {
                    dPressed = false;
                }
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
            if (state.equals("SINGLE") && !paused) {
                if (leftPressed) {
                    singleEngine.moveLeft();
                }
                if (rightPressed) {
                    singleEngine.moveRight();
                }

                // Ball Trail
                singleBallTrail.add(0, new BallPos(singleEngine.getBallX(), singleEngine.getBallY()));
                if (singleBallTrail.size() > 8) {
                    singleBallTrail.remove(singleBallTrail.size() - 1);
                }

                singleEngine.updateGame();
                processEngineEvents(singleEngine, singleParticles, singleScores, "SINGLE");
            } else if (state.equals("MULTI") && !paused) {
                if (aPressed) {
                    leftEngine.moveLeft();
                }
                if (dPressed) {
                    leftEngine.moveRight();
                }
                if (multiLeftPressed) {
                    rightEngine.moveLeft();
                }
                if (multiRightPressed) {
                    rightEngine.moveRight();
                }

                // Ball Trails
                leftBallTrail.add(0, new BallPos(leftEngine.getBallX(), leftEngine.getBallY()));
                if (leftBallTrail.size() > 8) {
                    leftBallTrail.remove(leftBallTrail.size() - 1);
                }
                rightBallTrail.add(0, new BallPos(rightEngine.getBallX(), rightEngine.getBallY()));
                if (rightBallTrail.size() > 8) {
                    rightBallTrail.remove(rightBallTrail.size() - 1);
                }

                leftEngine.updateGame();
                rightEngine.updateGame();
                processEngineEvents(leftEngine, leftParticles, leftScores, "LEFT");
                processEngineEvents(rightEngine, rightParticles, rightScores, "RIGHT");
            }

            // Always update animations and screen shake decay when unpaused
            if (!paused) {
                // Update background particles in non-gameplay screens
                if (state.equals("MENU") || state.equals("MODE_SELECT") || state.equals("SETTINGS")) {
                    for (MenuParticle mp : menuParticles) {
                        mp.update(getWidth(), getHeight());
                    }
                }

                singleParticles.removeIf(p -> !p.update());
                leftParticles.removeIf(p -> !p.update());
                rightParticles.removeIf(p -> !p.update());

                singleScores.removeIf(s -> !s.update());
                leftScores.removeIf(s -> !s.update());
                rightScores.removeIf(s -> !s.update());

                if (singleShake > 0.1) singleShake *= 0.85; else singleShake = 0.0;
                if (leftShake > 0.1) leftShake *= 0.85; else leftShake = 0.0;
                if (rightShake > 0.1) rightShake *= 0.85; else rightShake = 0.0;
            }

            repaint();
        });

        timer.start();
    }

    private void processEngineEvents(GameEngine engine, List<Particle> particles, List<FloatingScore> floatingScores, String side) {
        List<GameEngine.BrickEvent> events = engine.getPendingEvents();
        for (GameEngine.BrickEvent ev : events) {
            // ===================================
            // 💔 LIFE LOSS EVENT
            // ===================================
            if (ev.row == -2 && ev.col == -2) {
                double shakeAmt = 15.0;
                if (side.equals("SINGLE")) {
                    singleBallTrail.clear();
                    singleShake += shakeAmt;
                } else if (side.equals("LEFT")) {
                    leftBallTrail.clear();
                    leftShake += shakeAmt;
                } else {
                    rightBallTrail.clear();
                    rightShake += shakeAmt;
                }
                continue;
            }
            // ===================================
            // 🎉 LEVEL UP PROGRESSION EVENT
            // ===================================
            if (ev.row == -1 && ev.col == -1) {
                double levelUpShake = 16.0;
                if (side.equals("SINGLE")) {
                    singleShake += levelUpShake;
                } else if (side.equals("LEFT")) {
                    leftShake += levelUpShake;
                } else {
                    rightShake += levelUpShake;
                }

                // Center floating Level Up text
                floatingScores.add(new FloatingScore(
                    ev.x, ev.y,
                    "LEVEL " + engine.getLevel() + "!",
                    new Color(255, 215, 0), // Golden glowing text
                    45
                ));
                continue;
            }

            double shakeAmount = 3.5;
            if (ev.type == 4) { // Explosive
                shakeAmount = 14.0;
            } else if (ev.type == 3) { // Elite
                shakeAmount = 7.0;
            } else if (ev.type == 5) { // Armored
                shakeAmount = 9.0;
            } else if (ev.type == 6) { // Platinum
                shakeAmount = 11.0;
            } else if (ev.type == 7) { // Quantum
                shakeAmount = 13.0;
            } else if (ev.type == 8) { // Cosmic
                shakeAmount = 15.0;
            } else if (ev.type == 9) { // Stellar
                shakeAmount = 18.0;
            }

            if (side.equals("SINGLE")) {
                singleShake += shakeAmount;
            } else if (side.equals("LEFT")) {
                leftShake += shakeAmount;
            } else {
                rightShake += shakeAmount;
            }

            // Create particles
            Color pColor;
            if (ev.type == 7) pColor = new Color(255, 30, 30); // Crimson Quantum Red
            else if (ev.type == 6) pColor = new Color(210, 235, 255); // Shiny Platinum White
            else if (ev.type == 5) pColor = new Color(130, 0, 255); // Purple
            else if (ev.type == 3) pColor = new Color(180, 200, 220); // Silver
            else if (ev.type == 2) pColor = new Color(255, 130, 0); // Orange
            else if (ev.type == 4) pColor = new Color(255, 215, 0); // Gold
            else if (ev.type == 8) pColor = new Color(0, 191, 255); // Deep Sky Blue (Cosmic Cyan)
            else if (ev.type == 9) pColor = new Color(255, 20, 147); // Deep Pink (Stellar Magenta)
            else pColor = new Color(0, 230, 118); // Emerald Green

            int count = ev.destroyed ? 18 : 6;
            if (ev.isExplosion) {
                count = 14;
                pColor = new Color(255, 75, 0); // Fire red
            }

            for (int i = 0; i < count; i++) {
                double speed = 1.0 + Math.random() * 4.0;
                double angle = Math.random() * Math.PI * 2.0;
                double pdx = Math.cos(angle) * speed;
                double pdy = Math.sin(angle) * speed - 1.0; // Gravity makes it fall, so pop it up initially

                particles.add(new Particle(
                    ev.x + engine.getBrickWidth() / 2.0,
                    ev.y + engine.getBrickHeight() / 2.0,
                    pdx, pdy, pColor,
                    15 + (int) (Math.random() * 20)
                ));
            }

            // Floating Scores
            floatingScores.add(new FloatingScore(
                ev.x + engine.getBrickWidth() / 2.0 - 15,
                ev.y + engine.getBrickHeight() / 2.0,
                "+" + ev.points,
                (ev.points >= 50) ? new Color(255, 215, 0) : Color.WHITE,
                30
            ));
        }
    }

    // =========================
    // 🎨 PAINT
    // =========================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        // Premium HSB theme shifting gradient background (live preview in SETTINGS_BG state)
        int bgIndex = selectedBgThemeIndex;
        if (state.equals("SETTINGS_BG")) {
            bgIndex = tempBgThemeIndex;
        }
        BackgroundTheme bgTheme = bgThemes.isEmpty() ? 
            new BackgroundTheme("Blue", new Color(10, 15, 35), new Color(15, 45, 90), new Color(25, 10, 45), new Color(35, 12, 65), new Color(255, 255, 255, 10), new Color(0, 220, 255), new Color(0, 100, 255), new Color(255, 255, 255), new Color(0, 220, 255, 100)) : 
            bgThemes.get(bgIndex);

        double shiftVal = Math.sin(System.currentTimeMillis() / 2500.0) * 0.5 + 0.5; // Very slow shifting cycle

        Color topColor = interpolate(bgTheme.lightTop, bgTheme.darkTop, shiftVal);
        Color bottomColor = interpolate(bgTheme.lightBottom, bgTheme.darkBottom, shiftVal);

        GradientPaint gradient = new GradientPaint(
            0, 0, topColor,
            0, getHeight(), bottomColor
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Grid lines overlay for digital aesthetic
        g2d.setColor(bgTheme.gridColor);
        g2d.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i < getWidth(); i += 40) {
            g2d.drawLine(i, 0, i, getHeight());
        }
        for (int j = 0; j < getHeight(); j += 40) {
            g2d.drawLine(0, j, getWidth(), j);
        }

        switch (state) {
            case "MENU" -> drawMenu(g2d);
            case "MODE_SELECT" -> drawModeSelect(g2d);
            case "SINGLE" -> drawSingle(g2d);
            case "MULTI" -> drawMulti(g2d);
            case "SETTINGS" -> drawSettings(g2d);
            case "SETTINGS_BALL" -> drawSettingsBall(g2d);
            case "SETTINGS_BG" -> drawSettingsBg(g2d);
        }
    }

    // =========================
    // 🏠 MENU DRAWING
    // =========================

    private void drawMenuParticles(Graphics2D g2d) {
        for (MenuParticle mp : menuParticles) {
            mp.draw(g2d);
        }
    }

    private void drawMenu(Graphics2D g2d) {
        // Floating ambient menu particles background
        drawMenuParticles(g2d);

        // Minimalist horizontal gradient logo that slowly color shifts between two similar colors derived from active theme
        BackgroundTheme theme = bgThemes.get(selectedBgThemeIndex);
        float[] hsb1 = Color.RGBtoHSB(theme.logoColor1.getRed(), theme.logoColor1.getGreen(), theme.logoColor1.getBlue(), null);
        float[] hsb2 = Color.RGBtoHSB(theme.logoColor2.getRed(), theme.logoColor2.getGreen(), theme.logoColor2.getBlue(), null);

        double time = System.currentTimeMillis() / 1500.0;
        float shift1 = (float) (Math.sin(time) * 0.04);
        float shift2 = (float) (Math.cos(time) * 0.04);

        Color c1 = Color.getHSBColor(hsb1[0] + shift1, hsb1[1], hsb1[2]);
        Color c2 = Color.getHSBColor(hsb2[0] + shift2, hsb2[1], hsb2[2]);

        g2d.setFont(new Font("Outfit", Font.BOLD, 62));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth("BRICK BREAKER")) / 2;
        int y = 150;

        // Bouncing shadow for 3D glow effect
        g2d.setColor(new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), 60));
        g2d.drawString("BRICK BREAKER", x - 2, y + 2);
        g2d.drawString("BRICK BREAKER", x + 2, y + 2);

        GradientPaint logoGrad = new GradientPaint(
            x, y, c1,
            x + fm.stringWidth("BRICK BREAKER"), y, c2
        );
        g2d.setPaint(logoGrad);
        g2d.drawString("BRICK BREAKER", x, y);

        String[] options = {
            "Single Player",
            "Two Player",
            "Settings"
        };

        for (int i = 0; i < options.length; i++) {
            int boxWidth = 320;
            int boxHeight = 50;
            int boxX = (getWidth() - boxWidth) / 2;
            int boxY = 230 + i * 80;

            if (i == selectedOption) {
                // Shiny selection bar
                GradientPaint selectedGrad = new GradientPaint(
                    boxX, boxY, new Color(0, 255, 255),
                    boxX + boxWidth, boxY + boxHeight, new Color(0, 150, 255)
                );
                g2d.setPaint(selectedGrad);
                g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 25, 25);
                g2d.setColor(Color.BLACK);
            } else {
                // Glass panel
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 25, 25);
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 25, 25);
                g2d.setColor(Color.WHITE);
            }

            g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
            fm = g2d.getFontMetrics();
            int textX = boxX + (boxWidth - fm.stringWidth(options[i])) / 2;
            int textY = boxY + 32;
            g2d.drawString(options[i], textX, textY);
        }

        // Instructions
        g2d.setColor(new Color(200, 200, 255, 180));
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
        drawCentered(g2d, "Use UP/DOWN Arrows & ENTER to select", 490);
        
        g2d.setFont(new Font("Monospaced", Font.ITALIC, 14));
        g2d.setColor(new Color(0, 255, 255, 180));
        drawCentered(g2d, "Single: Left/Right keys. Multi: A/D (P1) & Left/Right (P2)", 520);
    }

    // =========================
    // ⚙️ SETTINGS SYSTEM
    // =========================

    private void drawSettings(Graphics2D g2d) {
        // Floating background particles
        drawMenuParticles(g2d);

        BackgroundTheme theme = bgThemes.get(selectedBgThemeIndex);
        g2d.setFont(new Font("Outfit", Font.BOLD, 52));
        g2d.setColor(theme.headingGlowColor);
        drawCentered(g2d, "SETTINGS", 104);
        g2d.setColor(theme.headingColor);
        drawCentered(g2d, "SETTINGS", 100);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2d.setColor(new Color(200, 200, 255, 180));
        drawCentered(g2d, "CUSTOMIZE YOUR ARCADE EXPERIENCE", 132);

        // Render Settings glass cards
        String[] settingLabels = {
            "Ball Customisation",
            "Change background color"
        };
        String[] settingDescs = {
            "Select your ball's color scheme & neon motion trail",
            "Choose from 5 slow color-shifting matrix grids"
        };

        for (int i = 0; i < 2; i++) {
            int cardW = 540;
            int cardH = 120;
            int cardX = (getWidth() - cardW) / 2;
            int cardY = 175 + i * 155;

            if (i == selectedSettingRow) {
                // Glow boundary selection
                g2d.setColor(new Color(0, 255, 255, 25));
                g2d.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);
                g2d.setColor(new Color(0, 255, 255, 240));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);
            } else {
                // Glass card
                g2d.setColor(new Color(255, 255, 255, 12));
                g2d.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);
                g2d.setColor(new Color(255, 255, 255, 45));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);
            }

            // Draw label
            g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
            g2d.setColor(i == selectedSettingRow ? new Color(0, 255, 240) : Color.WHITE);
            g2d.drawString(settingLabels[i], cardX + 30, cardY + 48);

            // Draw description
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
            g2d.setColor(i == selectedSettingRow ? new Color(220, 255, 255, 200) : new Color(200, 200, 220, 160));
            g2d.drawString(settingDescs[i], cardX + 30, cardY + 78);

            // Draw quick preview/indicator icon on right side
            if (i == 0) {
                // Orbiting ball indicator
                int bX = cardX + 465;
                int bY = cardY + 60;
                List<BallPos> tempTrail = new ArrayList<>();
                double time = System.currentTimeMillis() / 200.0;
                for (int j = 4; j >= 1; j--) {
                    int tx = bX - (int) (Math.cos(time - j * 0.45) * j * 6);
                    int ty = bY + (int) (Math.sin(time - j * 0.45) * j * 3);
                    tempTrail.add(new BallPos(tx, ty));
                }
                drawBall(g2d, bX - 10, bY - 10, 20, tempTrail, selectedBallColorIndex);
            } else {
                // Color swatches indicator
                g2d.setColor(theme.darkTop);
                g2d.fillRoundRect(cardX + 440, cardY + 45, 25, 25, 6, 6);
                g2d.setColor(theme.darkBottom);
                g2d.fillRoundRect(cardX + 475, cardY + 45, 25, 25, 6, 6);
                g2d.setColor(theme.gridColor.darker());
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(cardX + 440, cardY + 45, 60, 25, 6, 6);
            }
        }

        // Instructions
        g2d.setColor(new Color(200, 200, 255, 180));
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 15));
        drawCentered(g2d, "Use UP/DOWN to select option, ENTER to customize", 500);
        drawCentered(g2d, "Press ESC to return to Main Menu", 530);
    }

    private void drawSettingsBall(Graphics2D g2d) {
        drawMenuParticles(g2d);

        BackgroundTheme theme = bgThemes.get(selectedBgThemeIndex);
        g2d.setFont(new Font("Outfit", Font.BOLD, 46));
        g2d.setColor(theme.headingGlowColor);
        drawCentered(g2d, "BALL CUSTOMISATION", 104);
        g2d.setColor(theme.headingColor);
        drawCentered(g2d, "BALL CUSTOMISATION", 100);

        int cardW = 540;
        int cardH = 280;
        int cardX = (getWidth() - cardW) / 2;
        int cardY = 160;

        // Big glassmorphic container card
        g2d.setColor(new Color(255, 255, 255, 15));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);

        BallColorOption opt = ballColors.get(tempBallColorIndex);

        // Orbiting Preview Ball on top (Perfect centered orbital ellipse)
        int bX = cardX + 270;
        int bY = cardY + 80;
        int bSize = 30;
        List<BallPos> tempTrail = new ArrayList<>();
        double time = System.currentTimeMillis() / 150.0;
        
        // Calculate smooth trail positions following the ball
        for (int j = 6; j >= 1; j--) {
            int tx = bX + (int) (Math.cos(time - j * 0.15) * 55);
            int ty = bY + (int) (Math.sin(time - j * 0.15) * 25);
            tempTrail.add(new BallPos(tx - bSize / 2, ty - bSize / 2));
        }
        
        // Calculate main ball position
        int bx = bX + (int) (Math.cos(time) * 55);
        int by = bY + (int) (Math.sin(time) * 25);
        drawBall(g2d, bx - bSize / 2, by - bSize / 2, bSize, tempTrail, tempBallColorIndex);

        // Customize labels
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2d.setColor(new Color(200, 200, 255, 200));
        drawCentered(g2d, "BALL COLOR SCHEMA:", cardY + 175);

        // Cycle option text
        g2d.setFont(new Font("Monospaced", Font.BOLD, 26));
        g2d.setColor(opt.glowColor);
        drawCentered(g2d, "←  " + opt.name.toUpperCase() + "  →", cardY + 225);

        // Instructions
        g2d.setColor(new Color(200, 200, 255, 180));
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 15));
        drawCentered(g2d, "Press LEFT/RIGHT Arrow to cycle color palettes", 490);
        drawCentered(g2d, "Press ENTER to Select & Save | ESC to Cancel & Revert", 520);
    }

    private void drawSettingsBg(Graphics2D g2d) {
        drawMenuParticles(g2d);

        BackgroundTheme theme = bgThemes.get(tempBgThemeIndex);
        g2d.setFont(new Font("Outfit", Font.BOLD, 46));
        g2d.setColor(theme.headingGlowColor);
        drawCentered(g2d, "BACKGROUND COLOR", 104);
        g2d.setColor(theme.headingColor);
        drawCentered(g2d, "BACKGROUND COLOR", 100);

        int cardW = 540;
        int cardH = 280;
        int cardX = (getWidth() - cardW) / 2;
        int cardY = 160;

        // Big glassmorphic container card
        g2d.setColor(new Color(255, 255, 255, 15));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);

        // Customize labels
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2d.setColor(new Color(200, 200, 255, 200));
        drawCentered(g2d, "AMBIENCE MATRIX THEME:", cardY + 115);

        // Cycle option text
        g2d.setFont(new Font("Monospaced", Font.BOLD, 26));
        g2d.setColor(new Color(0, 255, 120)); // Vivid Matrix theme color
        drawCentered(g2d, "←  " + theme.name.toUpperCase() + "  →", cardY + 185);

        // Instructions
        g2d.setColor(new Color(200, 200, 255, 180));
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 15));
        drawCentered(g2d, "Press LEFT/RIGHT Arrow to cycle background gradients", 490);
        drawCentered(g2d, "Press ENTER to Select & Save | ESC to Cancel & Revert", 520);
    }

    // =========================
    // 🟢 SINGLE PLAYER DRAWING
    // =========================

    private void drawSingle(Graphics2D g2d) {
        AffineTransform originalTransform = g2d.getTransform();

        // Apply Screen Shake
        if (singleShake > 0.1) {
            double shakeX = (Math.random() - 0.5) * singleShake;
            double shakeY = (Math.random() - 0.5) * singleShake;
            g2d.translate(shakeX, shakeY);
        }

        drawEngine(g2d, singleEngine);

        // Draw active particles & float text popups inside the shake
        for (Particle p : singleParticles) {
            p.draw(g2d);
        }
        for (FloatingScore s : singleScores) {
            s.draw(g2d);
        }

        g2d.setTransform(originalTransform);

        // HUD - Solid HUD not shaken
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("SINGLE PLAYER", 20, 20);

        // GAME OVER / VICTORY OVERLAYS
        if (singleEngine.isGameOver()) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setFont(new Font("Outfit", Font.BOLD, 56));
            g2d.setColor(new Color(255, 80, 80));
            drawCentered(g2d, "GAME OVER", 220);
            
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 24));
            g2d.setColor(Color.WHITE);
            drawCentered(g2d, "Final Score: " + singleEngine.getScore() + "  |  Level Cleared: " + (singleEngine.getLevel() - 1), 280);

            drawEndGameButtons(g2d, selectedEndOption);
        }

        if (paused) {
            drawPauseOverlay(g2d);
        }
    }

    // =========================
    // 🔴 MULTIPLAYER DRAWING
    // =========================

    private void drawMulti(Graphics2D g2d) {
        // Player 1 (Left Area)
        AffineTransform leftTransform = g2d.getTransform();
        if (leftShake > 0.1) {
            double shakeX = (Math.random() - 0.5) * leftShake;
            double shakeY = (Math.random() - 0.5) * leftShake;
            g2d.translate(shakeX, shakeY);
        }
        drawEngine(g2d, leftEngine);
        for (Particle p : leftParticles) {
            p.draw(g2d);
        }
        for (FloatingScore s : leftScores) {
            s.draw(g2d);
        }
        g2d.setTransform(leftTransform);

        // Screen divider (Neon Divider)
        g2d.setColor(new Color(0, 255, 255, 100));
        g2d.fillRect(getWidth() / 2 - 2, 0, 4, getHeight());
        g2d.setColor(new Color(0, 255, 255, 40));
        g2d.fillRect(getWidth() / 2 - 6, 0, 12, getHeight());

        // Player 2 (Right Area)
        AffineTransform rightTransform = g2d.getTransform();
        if (rightShake > 0.1) {
            double shakeX = (Math.random() - 0.5) * rightShake;
            double shakeY = (Math.random() - 0.5) * rightShake;
            g2d.translate(shakeX, shakeY);
        }
        drawEngine(g2d, rightEngine);
        for (Particle p : rightParticles) {
            p.draw(g2d);
        }
        for (FloatingScore s : rightScores) {
            s.draw(g2d);
        }
        g2d.setTransform(rightTransform);

        // Labels
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("PLAYER 1 (A/D)", 20, 20);
        g2d.drawString("PLAYER 2 (← / →)", 500, 20);

        // Check if both games are completed (either lost or victory)
        if (leftEngine.isGameOver() && rightEngine.isGameOver()) {

            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setFont(new Font("Outfit", Font.BOLD, 46));

            String result;
            if (leftEngine.getScore() > rightEngine.getScore()) {
                result = "PLAYER 1 WINS BY SCORE!";
                g2d.setColor(new Color(100, 200, 255));
            } else if (rightEngine.getScore() > leftEngine.getScore()) {
                result = "PLAYER 2 WINS BY SCORE!";
                g2d.setColor(new Color(100, 200, 255));
            } else {
                result = "IT'S A TIE DRAW!";
                g2d.setColor(Color.YELLOW);
            }

            drawCentered(g2d, result, 210);

            g2d.setFont(new Font("SansSerif", Font.PLAIN, 22));
            g2d.setColor(Color.WHITE);
            drawCentered(g2d, "P1 Score: " + leftEngine.getScore() + " (Lvl " + leftEngine.getLevel() + ")  |  P2 Score: " + rightEngine.getScore() + " (Lvl " + rightEngine.getLevel() + ")", 270);

            drawEndGameButtons(g2d, selectedEndOption);
        }

        if (paused) {
            drawPauseOverlay(g2d);
        }
    }

    // ===================================
    // ⏸️ NAVIGABLE PAUSE MENU OVERLAY
    // ===================================

    private void drawPauseOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(10, 10, 20, 230));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(new Color(0, 255, 255));
        g2d.setFont(new Font("Outfit", Font.BOLD, 52));
        drawCentered(g2d, "GAME PAUSED", 200);

        String[] btnLabels = {"RESUME (ESC)", "RESTART (R)", "HOME MENU"};
        int btnWidth = 170;
        int btnHeight = 46;
        int spacing = 30;
        int startX = (getWidth() - (btnWidth * 3 + spacing * 2)) / 2;
        int btnY = 320;

        for (int i = 0; i < btnLabels.length; i++) {
            int x = startX + i * (btnWidth + spacing);
            
            if (i == selectedPauseOption) {
                // Neon glow background
                GradientPaint glowBtn = new GradientPaint(
                    x, btnY, new Color(0, 255, 255),
                    x + btnWidth, btnY + btnHeight, new Color(0, 150, 255)
                );
                g2d.setPaint(glowBtn);
                g2d.fillRoundRect(x, btnY, btnWidth, btnHeight, 15, 15);
                g2d.setColor(Color.BLACK);
            } else {
                // Glass panel
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(x, btnY, btnWidth, btnHeight, 15, 15);
                g2d.setColor(new Color(255, 255, 255, 70));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(x, btnY, btnWidth, btnHeight, 15, 15);
                g2d.setColor(Color.WHITE);
            }

            g2d.setFont(new Font("SansSerif", Font.BOLD, 17));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (btnWidth - fm.stringWidth(btnLabels[i])) / 2;
            int textY = btnY + 28;
            g2d.drawString(btnLabels[i], textX, textY);
        }

        // Help instructions below buttons
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(200, 200, 255, 140));
        drawCentered(g2d, "Use ← or → Arrows and ENTER to select option", 410);
    }

    // =========================
    // 🎛️ END GAME INTERACTIVE BUTTONS
    // =========================

    private void drawEndGameButtons(Graphics2D g2d, int selectedOption) {
        String[] btnLabels = {"RESTART (R)", "HOME MENU"};
        int btnWidth = 170;
        int btnHeight = 46;
        int spacing = 40;
        int startX = (getWidth() - (btnWidth * 2 + spacing)) / 2;
        int btnY = 345;

        for (int i = 0; i < btnLabels.length; i++) {
            int x = startX + i * (btnWidth + spacing);
            
            if (i == selectedOption) {
                // Neon glow background
                GradientPaint glowBtn = new GradientPaint(
                    x, btnY, new Color(0, 255, 255),
                    x + btnWidth, btnY + btnHeight, new Color(0, 150, 255)
                );
                g2d.setPaint(glowBtn);
                g2d.fillRoundRect(x, btnY, btnWidth, btnHeight, 15, 15);
                g2d.setColor(Color.BLACK);
            } else {
                // Glassmorphic unselected
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(x, btnY, btnWidth, btnHeight, 15, 15);
                g2d.setColor(new Color(255, 255, 255, 70));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(x, btnY, btnWidth, btnHeight, 15, 15);
                g2d.setColor(Color.WHITE);
            }

            g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (btnWidth - fm.stringWidth(btnLabels[i])) / 2;
            int textY = btnY + 28;
            g2d.drawString(btnLabels[i], textX, textY);
        }

        // Help text
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(200, 200, 255, 140));
        drawCentered(g2d, "Use ← or → Arrows and ENTER to select option", 425);
    }

    // =========================
    // 🎮 ENGINE ELEMENTS RENDER
    // =========================

    private void drawEngine(Graphics2D g2d, GameEngine engine) {
        Shape oldClip = g2d.getClip();

        g2d.setClip(
            engine.getOffsetX(),
            0,
            engine.getAreaWidth(),
            getHeight()
        );

        // ===================================
        // 🧱 CYBER GLOWING NEON BORDER WALLS
        // ===================================

        // Left Colored Wall
        GradientPaint leftWallGrad = new GradientPaint(
            engine.getOffsetX(), 0, new Color(0, 255, 255, 180),
            engine.getOffsetX() + GameEngine.WALL_THICKNESS, 0, new Color(0, 100, 180, 100)
        );
        g2d.setPaint(leftWallGrad);
        g2d.fillRect(engine.getOffsetX(), 0, GameEngine.WALL_THICKNESS, getHeight());
        // Highlight Glow Line
        g2d.setColor(new Color(0, 255, 255, 245));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(engine.getOffsetX() + GameEngine.WALL_THICKNESS, 0,
                     engine.getOffsetX() + GameEngine.WALL_THICKNESS, getHeight());

        // Right Colored Wall
        GradientPaint rightWallGrad = new GradientPaint(
            engine.getOffsetX() + engine.getAreaWidth() - GameEngine.WALL_THICKNESS, 0, new Color(0, 100, 180, 100),
            engine.getOffsetX() + engine.getAreaWidth(), 0, new Color(0, 255, 255, 180)
        );
        g2d.setPaint(rightWallGrad);
        g2d.fillRect(engine.getOffsetX() + engine.getAreaWidth() - GameEngine.WALL_THICKNESS, 0,
                     GameEngine.WALL_THICKNESS, getHeight());
        // Highlight Glow Line
        g2d.setColor(new Color(0, 255, 255, 245));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(engine.getOffsetX() + engine.getAreaWidth() - GameEngine.WALL_THICKNESS, 0,
                     engine.getOffsetX() + engine.getAreaWidth() - GameEngine.WALL_THICKNESS, getHeight());


        // Fetch Ball Trail
        List<BallPos> trail;
        if (engine == singleEngine) {
            trail = singleBallTrail;
        } else if (engine == leftEngine) {
            trail = leftBallTrail;
        } else {
            trail = rightBallTrail;
        }

        // 1. Draw Ball & Trail
        drawBall(g2d, engine.getBallX(), engine.getBallY(), engine.getBallSize(), trail, selectedBallColorIndex);

        // 2. Draw Paddle
        drawPaddle(g2d, engine.getPaddleX(), engine.getPaddleY(), engine.getPaddleWidth(), engine.getPaddleHeight());

        // 3. Draw Bricks (Dynamic Grid Rendering - Starting at Y=90 to avoid HUD overlaps)
        int[][] brickType = engine.getBrickType();
        int[][] brickHealth = engine.getBrickHealth();
        int[][] brickMaxHealth = engine.getBrickMaxHealth();

        int rows = engine.getRows();
        int cols = engine.getCols();

        int totalWidth = cols * engine.getBrickWidth() + (cols - 1) * engine.getBrickGap();
        int startX = engine.getOffsetX() + (engine.getAreaWidth() - totalWidth) / 2;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (brickHealth[r][c] <= 0) continue;

                int x = startX + c * (engine.getBrickWidth() + engine.getBrickGap());
                int y = 90 + r * (engine.getBrickHeight() + engine.getBrickGap());

                drawBrick(g2d, x, y, engine.getBrickWidth(), engine.getBrickHeight(),
                    brickType[r][c], brickHealth[r][c], brickMaxHealth[r][c]);
            }
        }

        // 4. Draw Score HUD glass panel (Widened dynamically to accommodate score, level & lives)
        int hudWidth = engine.isHardcore() ? 195 : 295;
        g2d.setColor(new Color(255, 255, 255, 12));
        g2d.fillRoundRect(engine.getOffsetX() + 20, 30, hudWidth, 32, 8, 8);
        g2d.setColor(new Color(255, 255, 255, 35));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(engine.getOffsetX() + 20, 30, hudWidth, 32, 8, 8);

        g2d.setColor(new Color(0, 255, 240));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        String hudStr = "SCORE: " + String.format("%04d", engine.getScore()) + " | LVL: " + engine.getLevel();
        if (!engine.isHardcore()) {
            hudStr += " | LIVES: " + engine.getLives();
        }
        g2d.drawString(hudStr, engine.getOffsetX() + 27, 51);

        // ===================================
        // 🏁 GET READY / COUNTDOWN TIMER RENDER
        // ===================================
        if (engine.isBallGlued() && engine.getCountdownTicks() > 0) {
            int seconds = (engine.getCountdownTicks() + 29) / 30; // 3, 2, 1

            // Glass Overlay backing panel for countdown
            g2d.setColor(new Color(10, 10, 20, 160));
            g2d.fillRoundRect(engine.getOffsetX() + 30, 160, engine.getAreaWidth() - 60, 95, 15, 15);
            g2d.setColor(new Color(255, 255, 255, 25));
            g2d.setStroke(new BasicStroke(1.2f));
            g2d.drawRoundRect(engine.getOffsetX() + 30, 160, engine.getAreaWidth() - 60, 95, 15, 15);

            // Draw "GET READY" header
            g2d.setFont(new Font("Outfit", Font.BOLD, 20));
            g2d.setColor(new Color(220, 220, 255, 200));
            int grX = engine.getOffsetX() + (engine.getAreaWidth() - g2d.getFontMetrics().stringWidth("GET READY")) / 2;
            g2d.drawString("GET READY", grX, 192);

            // Draw Number / GO!
            String cdStr = String.valueOf(seconds);
            if (engine.getCountdownTicks() < 15) {
                cdStr = "GO!";
                g2d.setColor(new Color(0, 255, 120)); // Glowing green
            } else {
                g2d.setColor(new Color(255, 215, 0)); // Glowing gold
            }

            g2d.setFont(new Font("Outfit", Font.BOLD, 42));
            int numX = engine.getOffsetX() + (engine.getAreaWidth() - g2d.getFontMetrics().stringWidth(cdStr)) / 2;
            g2d.drawString(cdStr, numX, 238);
        }

        // Lost overlay inside game panel
        if (engine.isGameOver()) {
            g2d.setColor(new Color(255, 50, 50, 40));
            g2d.fillRect(engine.getOffsetX() + GameEngine.WALL_THICKNESS, 0,
                         engine.getAreaWidth() - GameEngine.WALL_THICKNESS * 2, getHeight());
            
            g2d.setColor(new Color(255, 80, 80, 180));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
            int textX = engine.getOffsetX() + (engine.getAreaWidth() - 90) / 2;
            g2d.drawString("FAILED", textX, 300);
        }

        g2d.setClip(oldClip);
    }

    // =========================
    // 🧱 INDIVIDUAL GLASS BRICK RENDERER
    // =========================

    private void drawBrick(Graphics2D g2d, int x, int y, int width, int height, int type, int health, int maxHealth) {
        Color baseColor;
        Color shineColor;

        if (type == 3) { // Elite Silver/Steel
            baseColor = new Color(145, 160, 175);
            shineColor = new Color(240, 245, 255);
        } else if (type == 2) { // Tough Bronze/Orange
            baseColor = new Color(255, 110, 0);
            shineColor = new Color(255, 200, 120);
        } else if (type == 5) { // Armored Obsidian Purple
            baseColor = new Color(75, 0, 130);
            shineColor = new Color(240, 180, 255);
        } else if (type == 6) { // Platinum Metallic White
            baseColor = new Color(190, 215, 235);
            shineColor = new Color(255, 255, 255);
        } else if (type == 7) { // Quantum Void Crimson (Dynamic Pulsing Crimson Neon Borders)
            double voidPulse = Math.sin(System.currentTimeMillis() / 120.0) * 0.15 + 0.85;
            baseColor = new Color(30, 0, 45); // Void indigo
            shineColor = new Color((int) (255 * voidPulse), 35, 35); // Pulsing crimson
        } else if (type == 8) { // Cosmic Glass (Deep cosmic blue with bright electric cyan border)
            baseColor = new Color(10, 45, 110);
            shineColor = new Color(0, 255, 255); // Electric cyan border
        } else if (type == 9) { // Stellar Core (Solar-magenta and gold gradient that pulses)
            double solarPulse = Math.sin(System.currentTimeMillis() / 150.0) * 0.15 + 0.85;
            int rVal = (int) (255 * solarPulse);
            baseColor = new Color(rVal, 20, 147); // Pulsing Deep Pink/Magenta
            shineColor = new Color(255, 215, 0); // Golden shine
        } else if (type == 4) { // Explosive Shiny Gold
            double pulse = Math.sin(System.currentTimeMillis() / 90.0) * 0.18 + 0.82;
            int r = (int) (255 * pulse);
            int g = (int) (210 * pulse);
            baseColor = new Color(r, Math.max(0, Math.min(255, g)), 0);
            shineColor = new Color(255, 255, 160);
        } else { // Basic Vivid Emerald Green
            baseColor = new Color(0, 200, 110);
            shineColor = new Color(140, 255, 195);
        }

        // Draw 3D/Gloss Gradient
        GradientPaint gradient = new GradientPaint(
            x, y, shineColor,
            x, y + height, baseColor.darker()
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(x, y, width, height, 7, 7);

        // Highlight Bevel Edge
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(x + 1, y + 1, width - 2, height - 2, 6, 6);

        // Dark boundary
        g2d.setColor(new Color(0, 0, 0, 75));
        g2d.drawRoundRect(x, y, width, height, 7, 7);

        // Glass highlight layer
        g2d.setPaint(new GradientPaint(
            x, y, new Color(255, 255, 255, 80),
            x, y + height / 2, new Color(255, 255, 255, 0)
        ));
        g2d.fillRoundRect(x + 2, y + 2, width - 4, height / 2 - 2, 5, 5);

        // Cracks animation
        if (health < maxHealth && maxHealth > 1) {
            g2d.setColor(new Color(15, 15, 15, 190));
            g2d.setStroke(new BasicStroke(1.6f));

            int seed = x * y + health;
            Random rand = new Random(seed);

            int crackSegments = (maxHealth - health) * 2;
            for (int i = 0; i < crackSegments; i++) {
                int startX = x + rand.nextInt(width - 12) + 6;
                int startY = y + rand.nextInt(height - 8) + 4;
                int endX = startX + rand.nextInt(22) - 11;
                int endY = startY + rand.nextInt(12) - 6;

                endX = Math.max(x + 3, Math.min(x + width - 3, endX));
                endY = Math.max(y + 3, Math.min(y + height - 3, endY));

                g2d.drawLine(startX, startY, endX, endY);

                if (rand.nextBoolean()) {
                    int branchX = endX + rand.nextInt(16) - 8;
                    int branchY = endY + rand.nextInt(10) - 5;
                    branchX = Math.max(x + 3, Math.min(x + width - 3, branchX));
                    branchY = Math.max(y + 3, Math.min(y + height - 3, branchY));
                    g2d.drawLine(endX, endY, branchX, branchY);
                }
            }
        }
    }

    // =========================
    // 🏓 PADDLE RENDERER
    // =========================

    private void drawPaddle(Graphics2D g2d, int x, int y, int width, int height) {
        // Soft neon cyan glowing border
        g2d.setColor(new Color(0, 255, 255, 30));
        g2d.setStroke(new BasicStroke(5f));
        g2d.drawRoundRect(x - 2, y - 2, width + 4, height + 4, 9, 9);

        // Core cyan to steel blue gradient fill
        GradientPaint grad = new GradientPaint(
            x, y, new Color(0, 255, 255),
            x, y + height, new Color(0, 100, 190)
        );
        g2d.setPaint(grad);
        g2d.fillRoundRect(x, y, width, height, 7, 7);

        // Highlight Bevel Edge
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(x + 1, y + 1, width - 2, height - 2, 6, 6);

        // Black outline
        g2d.setColor(new Color(0, 40, 80, 150));
        g2d.drawRoundRect(x, y, width, height, 7, 7);

        // Sleek metal cap caps on edges
        g2d.setPaint(new GradientPaint(x, y, Color.WHITE, x + 7, y, new Color(90, 90, 90)));
        g2d.fillRoundRect(x, y, 7, height, 5, 5);
        
        g2d.setPaint(new GradientPaint(x + width - 7, y, new Color(90, 90, 90), x + width, y, Color.WHITE));
        g2d.fillRoundRect(x + width - 7, y, 7, height, 5, 5);
    }

    // =========================
    // 🔮 BALL & TRAIL RENDERER
    // =========================

    private void drawBall(Graphics2D g2d, int x, int y, int size, List<BallPos> trail, int colorIndex) {
        BallColorOption opt = ballColors.get(colorIndex);

        // 1. Render glowing motion trail
        for (int i = 0; i < trail.size(); i++) {
            BallPos p = trail.get(i);
            float alpha = 1.0f - (float) i / trail.size();
            int trailSize = (int) (size * (1.0f - 0.45f * ((float) i / trail.size())));

            // Dynamic outer glowing trail bubbles based on customized settings color
            g2d.setColor(new Color(opt.glowColor.getRed(), opt.glowColor.getGreen(), opt.glowColor.getBlue(), (int) (alpha * 70)));
            g2d.fillOval(p.x + (size - trailSize) / 2, p.y + (size - trailSize) / 2, trailSize, trailSize);
        }

        // 2. Draw outer glow sphere layers
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.fillOval(x - 3, y - 3, size + 6, size + 6);

        g2d.setColor(new Color(opt.glowColor.getRed(), opt.glowColor.getGreen(), opt.glowColor.getBlue(), 110));
        g2d.fillOval(x - 1, y - 1, size + 2, size + 2);

        // 3. Main ball sphere: rich radial plasma gradients making the ball itself deeply colored!
        RadialGradientPaint radialGrad = new RadialGradientPaint(
            new Point2D.Double(x + size * 0.3, y + size * 0.3),
            (float) size,
            new float[]{0.0f, 0.4f, 1.0f},
            new Color[]{Color.WHITE, opt.glowColor, opt.baseColor.darker()}
        );
        g2d.setPaint(radialGrad);
        g2d.fillOval(x, y, size, size);
    }

    // ===================================
    // 🎚️ MODE/DIFFICULTY SELECT SUB-MENU
    // ===================================

    private void drawModeSelect(Graphics2D g2d) {
        // Ambient background particles
        drawMenuParticles(g2d);

        g2d.setFont(new Font("Outfit", Font.BOLD, 52));
        
        BackgroundTheme theme = bgThemes.get(selectedBgThemeIndex);
        g2d.setColor(theme.headingGlowColor);
        drawCentered(g2d, "SELECT DIFFICULTY", 154);
        g2d.setColor(theme.headingColor);
        drawCentered(g2d, "SELECT DIFFICULTY", 150);

        String[] modeOptions = {
            "Classic Mode",
            "Hardcore Mode"
        };
        // Render Settings glass cards
        String[] settingLabels = {
            "Ball Customisation",
            "Change background color"
        };
        
        String[] descriptions = {
            "3 lives",
            "1 life only"
        };

        for (int i = 0; i < modeOptions.length; i++) {
            int boxWidth = 380;
            int boxHeight = 85;
            int boxX = (getWidth() - boxWidth) / 2;
            int boxY = 220 + i * 115;

            if (i == selectedModeOption) {
                // Shiny selection bar
                GradientPaint selectedGrad = new GradientPaint(
                    boxX, boxY, new Color(0, 255, 255),
                    boxX + boxWidth, boxY + boxHeight, new Color(0, 150, 255)
                );
                g2d.setPaint(selectedGrad);
                g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
                g2d.setColor(Color.BLACK);
            } else {
                // Glass panel
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
                g2d.setColor(Color.WHITE);
            }

            // Title of mode
            g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = boxX + (boxWidth - fm.stringWidth(modeOptions[i])) / 2;
            int textY = boxY + 34;
            g2d.drawString(modeOptions[i], textX, textY);

            // Description
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
            if (i == selectedModeOption) {
                g2d.setColor(new Color(30, 30, 30));
            } else {
                g2d.setColor(new Color(200, 200, 250));
            }
            FontMetrics fmDesc = g2d.getFontMetrics();
            int descX = boxX + (boxWidth - fmDesc.stringWidth(descriptions[i])) / 2;
            int descY = boxY + 62;
            g2d.drawString(descriptions[i], descX, descY);
            
            g2d.setColor(Color.WHITE); // Reset color
        }

        // Instructions
        g2d.setColor(new Color(200, 200, 255, 180));
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 15));
        drawCentered(g2d, "Use Arrow Keys to select, ENTER to play", 495);
        drawCentered(g2d, "Press ESC to return to Main Menu", 525);
    }

    // ===================================
    // 🧠 TEXT ALIGNMENT HELPERS
    // ===================================

    public static Color interpolate(Color c1, Color c2, double blend) {
        int r = (int) (c1.getRed() * blend + c2.getRed() * (1.0 - blend));
        int g = (int) (c1.getGreen() * blend + c2.getGreen() * (1.0 - blend));
        int b = (int) (c1.getBlue() * blend + c2.getBlue() * (1.0 - blend));
        return new Color(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b))
        );
    }

    private void drawCentered(Graphics2D g2d, String text, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}