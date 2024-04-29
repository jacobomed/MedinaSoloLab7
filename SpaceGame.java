/** Project: Solo Lab 7 Assignment
 * Purpose Details: To create a space game
 * Course: IST242
 * Author: Jacobo Medina
 * Date Developed: 5/18/2024
 * Last Date Changed: 5/25/2024
 * Rev: First finished version 5/25/2024
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

/**
 * Jacobo Medina
 */
public class SpaceGame extends JFrame implements KeyListener {
/**
 * Runs and intializes all methods and objects needed to create the SpaceGame
 * GUI
 */
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 100;
    private static final int PLAYER_HEIGHT = 100;
    private static final int OBSTACLE_WIDTH = 50;
    private static final int OBSTACLE_HEIGHT = 50;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 15;
    private static final int PLAYER_SPEED = 30;
    private int obstacleSpeed = 5;
    private static final int PROJECTILE_SPEED = 10;
    private static final int POWERUP_WIDTH = 40;
    private static final int POWERUP_HEIGHT = 40;
    private int score = 0;
    private int health = 10;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel challengeLabel;
    private Timer timer;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private List<Point> obstacles;
    private List<Point> stars;
    private BufferedImage shipImage;
    private BufferedImage fireImage;
    private BufferedImage spriteSheet;
    private int spriteWidth = 50;
    private int spriteHeight = 50;
    private Clip clip;
    private Clip clip2;
    private BufferedImage powerupImage;
    private List<Point> powerups;
    private JLabel timerLabel;
    private int remainingTime;
    private JLabel levelLabel;
    private int level;
    private Timer countdownTimer;
    private boolean shieldActive = false;
    private int shieldDuration = 5000;
    private long shieldStartTime;

    /**
     * Jacobo Medina
     */
    public SpaceGame() {
    /**
     * SpaceGame method used to initialize images, challenge level, audio,and scan what user input has been
     * in order to draw and update
     */

        if (level > 2) {
            challengeLevel();
            challengeLabel.setText("CHALLENGE: NO POWERUPS || NO SHIELD");
        }
        try {
            shipImage = ImageIO.read(new File("SpaceShip.png"));
            spriteSheet = ImageIO.read(new File("Asteroid.png"));
            fireImage = ImageIO.read(new File("Fire.png"));
            powerupImage = ImageIO.read(new File("powerup.png"));

            // Load audio file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("raygun.wav"));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            AudioInputStream audioInputStream2 = AudioSystem.getAudioInputStream(new File("explosion.wav"));
            clip2 = AudioSystem.getClip();
            clip2.open(audioInputStream2);

        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            ex.printStackTrace();
        }

        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        obstacles = new ArrayList<>();
        powerups = new ArrayList<>();

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 100, 20);
        gamePanel.add(scoreLabel);
        scoreLabel.setForeground(Color.BLUE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));

        healthLabel = new JLabel("Health: " + health);
        healthLabel.setBounds(10, 30, 100, 20);
        gamePanel.add(healthLabel);
        healthLabel.setForeground(Color.RED);
        healthLabel.setFont(new Font("Arial", Font.BOLD, 14));

        timerLabel = new JLabel("Time Left: 45");
        timerLabel.setBounds(10, 50, 150, 20);
        gamePanel.add(timerLabel);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);
        remainingTime = 45;

        levelLabel = new JLabel("Level: 1");
        levelLabel.setBounds(10, 10, 100, 20);
        gamePanel.add(levelLabel);
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 14));

        challengeLabel = new JLabel("Survive to Level 3 for a CHALLENGE!");
        challengeLabel.setBounds(10, 30, 100, 10);
        gamePanel.add(challengeLabel);
        challengeLabel.setForeground(Color.PINK);
        challengeLabel.setFont(new Font("Vagabond", Font.BOLD, 18));

        remainingTime = 45;
        level = 1;
        generateNewStars(); // Ensure stars list is initialized before draw is called
        gamePanel.repaint();
        countdownTimer = new Timer(1000, new ActionListener() {

            /**
             * Jacobo Medina
             */
            @Override
            public void actionPerformed(ActionEvent e){
            /**
             * analyzes the actions performed in game in order to update properly
             *  ex: update timer seconds
             */

                remainingTime--;
                if (remainingTime <= 0) {
                    level++;
                    levelLabel.setText("Level: " + level);
                    remainingTime = 45;
                    timerLabel.setText("Time Left: " + remainingTime);

                    if (level > 2) {
                        challengeLevel();
                        challengeLabel.setText("CHALLENGE: NO POWERUPS || NO SHIELD");
                    }
                } else {
                    timerLabel.setText("Time Left: " + remainingTime);
                }
            }
        });

        countdownTimer.start();

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;

        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;

        timer.start();

        // Initialize and start the stars timer
        Timer starsTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateNewStars();
                gamePanel.repaint(); // Repaint the panel to show the new stars
            }
        });
        starsTimer.start();
    }

    /**
     * Jacobo Medina
     */
    private void generateNewStars() {
    /**
     *generates 150 stars for the background. stars will later be continuously
     * randomly plotted
     */

        stars = generateStars(150);
    }

    /**
     * Jacobo Medina
     */
    private List<Point> generateStars(int numStars) {

    /**
     *generates stars at random points throughput the array within the boundaries set
     * by constants WIDTH and HEIGHT and returns them into a list
     */

        List<Point> starList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numStars; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            starList.add(new Point(x, y));
        }
        return starList;
    }

    /**
     * Jacobo Medina
     */
    private void generatePowerUps() {

    /**
     * spawns powerups into the game. doesn't work after level 2 due to challenge level
     */
        if (level <= 2 && Math.random() < 0.009) { //powerup frequency
            int powerUpX = (int) (Math.random() * (WIDTH - POWERUP_WIDTH));
            int powerUpY = 0;
            powerups.add(new Point(powerUpX, powerUpY));
        }
    }

    /**
     * Jacobo Medina
     */
    private void movePowerUps() {
    /**
     * takes the powerupsfrom array and moves then down the screen
     */

        for (int i = 0; i < powerups.size(); i++) {
            powerups.get(i).y += obstacleSpeed; // Move power-up downwards
            if (powerups.get(i).y > HEIGHT) { // Remove power-up if it goes beyond the game panel
                powerups.remove(i);
                i--;
            }
        }
    }

    /**
     * Jacobo Medina
     */
    private Color generateRandomColor() {

    /**
     * generates random colors for stars in background
     */
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return new Color(r, g, b);
    }

    /**
     * Jacobo Medina
     */
    private void draw(Graphics g) {

    /**
     * draw method takes in the info from update and draws it into the screen
     * draw method is used to assign colors, make sure that png files are read and to show text
     * on screen all based on current state
     */

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        for (Point star : stars) {
            g.setColor(generateRandomColor());
            g.fillOval(star.x, star.y, 2, 2);
        }

        for (Point powerUp : powerups) {
            g.drawImage(powerupImage, powerUp.x, powerUp.y, null);
        }

        if (shipImage != null) {
            Image scaledShipImage = shipImage.getScaledInstance(PLAYER_WIDTH, PLAYER_HEIGHT, Image.SCALE_DEFAULT);
            g.drawImage(scaledShipImage, playerX, playerY, null);
        }
        if (isProjectileVisible) {
            g.drawImage(fireImage, projectileX, projectileY, null);
        }

        for (Point obstacle : obstacles) {
            if (spriteSheet != null) {
                Random random = new Random();
                int spriteIndex = random.nextInt(4);
                int spriteX = spriteIndex * spriteWidth;
                int spriteY = 0;
                g.drawImage(spriteSheet.getSubimage(spriteX, spriteY, spriteWidth, spriteHeight), obstacle.x, obstacle.y, null);
            }
        }

        if (shieldActive) {
            g.setColor(new Color(255, 182, 193, 100));
            g.fillOval(playerX - 10, playerY - 10, PLAYER_WIDTH + 20, PLAYER_HEIGHT + 20);
        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
            String levelText = "You reached level: " + level;
            int levelTextWidth = g.getFontMetrics().stringWidth(levelText);
            g.drawString(levelText, (WIDTH - levelTextWidth) / 2, HEIGHT / 2 + 30);
        }
    }

    /**
     * Jacobo Medina
     */
    private void update() {

    /**
     * reads what is happening in game and updates object location, generates new obstacles and powerups,
     * and checks hit registration/collision using assigned width and height values and positioning. Also update scores
     */
        if (!isGameOver) {
            // Update obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += obstacleSpeed;
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }

            // Generate new obstacles with a fixed probability
            if (Math.random() < 0.02) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                obstacles.add(new Point(obstacleX, 0));
            }


            // Generate and move power-ups
            generatePowerUps();
            movePowerUps();

            // Check collision with power-ups
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (int i = 0; i < powerups.size(); i++) {
                Rectangle powerupRect = new Rectangle(powerups.get(i).x, powerups.get(i).y, POWERUP_WIDTH, POWERUP_HEIGHT);
                if (playerRect.intersects(powerupRect)) {
                    powerups.remove(i);
                    health += 2;
                    healthLabel.setText("Health: " + health);
                    activateShield(1000); //
                    break;
                }
            }

            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with obstacles
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (projectileRect.intersects(obstacleRect)) {
                    obstacles.remove(i);
                    score += 10;
                    isProjectileVisible = false;
                    break;

                }
            }

            // Check collision with player
            playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (Point obstacle : obstacles) {
                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect) && !shieldActive) {
                    health -= 5;
                    healthLabel.setText("Health: " + health);
                    if (health <= 0) {
                        isGameOver = true;
                    } else {
                        playHitSound();
                    }
                    obstacles.remove(obstacle);
                    break;
                }
            }

            // Update score label
            scoreLabel.setText("Score: " + score);
        }
    }


/**
 * Jacobo Medina
 */
private void activateShield(int shieldDuration) {

    /**
     *activates shield. Doesn't work after level 2 due to challenge level. regular shield only remains active
     * for 5 seconds
     */
        if (level < 3) {

            // Activate shield for 10 seconds (10000 milliseconds)
            shieldActive = true;
            shieldStartTime = System.currentTimeMillis();

            Timer shieldTimer = new Timer(5000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shieldActive = false;
                }
            });
            shieldTimer.setRepeats(false);
            shieldTimer.start();
        }
    }

    /**
     * Jacobo Medina
     */
    public void playHitSound() {

    /**
     * reads wav file whenever collision occurs and clip isn't null anymore
     */

        new Thread(new Runnable() {
            public void run() {
                try {
                    if (clip2 != null) {
                        clip2.setFramePosition(0);
                        clip2.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Jacobo Medina
     */
    public void playSound() {
    /**
     * reads wav file whenever collision occurs and clip isn't null anymore
     */

        new Thread(new Runnable() {
            public void run() {
                try {
                    if (clip != null) {
                        clip.setFramePosition(0);
                        clip.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Jacobo Medina
     */
    @Override
    public void keyPressed(KeyEvent e) {
    /**
     * reads in inputted keys and if they are assigned an action, the action is performed
     */

        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_X) {
            activateShield(shieldDuration);
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            playSound();
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        isFiring = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
/**
 * Jacobo Medina
 */
public void challengeLevel() {

    /**
     * challenge level is initiated at the start of level 3. Increases obstacle occurance and speed
     * and disables shield and powerups
     */

        if (level > 2) {
            obstacleSpeed = 15; // Increase obstacle speed
            if (Math.random() < 1) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                obstacles.add(new Point(obstacleX, 0));
            }
        }
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }}


