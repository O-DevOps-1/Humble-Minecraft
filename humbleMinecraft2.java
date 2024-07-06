import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Minecraft extends Application {

    // constants for the game window size and block size
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int BLOCK_SIZE = 20;

    private GraphicsContext gc;

    // 2D arrays to represent the game world and blocks to break
    private boolean[][] world;
    private boolean[][] blocksToBreak;

    private int playerX, playerY;
    private int playerSpeed = 5;

    @Override
    public void start(Stage primaryStage) {
        // Ca¡anvas to draw on
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // creating a stack pane to hold the canvas
        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        // create a scene and set it to the primary stage
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();

        initWorld();

        drawWorld();

   
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W:
                    // Move player up
                    movePlayer(0, -playerSpeed);
                    break;
                case S:
                    // Move player down
                    movePlayer(0, playerSpeed);
                    break;
                case A:
                    // Move player left
                    movePlayer(-playerSpeed, 0);
                    break;
                case D:
                    // Move player right
                    movePlayer(playerSpeed, 0);
                    break;
                case SPACE:
                    // Break block
                    breakBlock();
                    break;
            }
            // Redraw the game world after each key press
            drawWorld();
        });
    }

    // initializing the game world with random blocks
    private void initWorld() {
        world = new boolean[WIDTH / BLOCK_SIZE][HEIGHT / BLOCK_SIZE];
        blocksToBreak = new boolean[WIDTH / BLOCK_SIZE][HEIGHT / BLOCK_SIZE];
        Random random = new Random();
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[x].length; y++) {
                world[x][y] = random.nextBoolean();
                blocksToBreak[x][y] = false;
            }
        }
        // set the player's initial position to the center of the world
        playerX = world.length / 2;
        playerY = world[0].length / 2;
    }

    private void drawWorld() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[x].length; y++) {
                if (world[x][y]) {
                    gc.setFill(Color.DARKGRAY);
                } else {
                    gc.setFill(Color.LIGHTGRAY);
                }
                // draw a block at the current position
                gc.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
        // drawing the player as a red block, as an example
        gc.setFill(Color.RED);
        gc.fillRect(playerX * BLOCK_SIZE, playerY * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
    }

    // move the player by the given amount
    private void movePlayer(int dx, int dy) {
        int newX = playerX + dx / BLOCK_SIZE;
        int newY = playerY + dy / BLOCK_SIZE;
        if (newX >= 0 && newX < world.length && newY >= 0 && newY < world[0].length) {
            // check if the new position is not a solid block, to avoid present and future bugs
            if (!world[newX][newY]) {
                playerX = newX;
                playerY = newY;
            }}}

    private void breakBlock() {
        blocksToBreak[playerX][playerY] = true;
        new Thread(() -> {
            try {
                // wait for 500 milliseconds to create a breaking animation, the amount setted is actually a sample
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // set the block to false to remove it from the world
            world[playerX][playerY] = false;
            blocksToBreak[playerX][playerY] = false;
        })}}