import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;
public class Minecraft extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int BLOCK_SIZE = 20;
    private GraphicsContext gc;
    private boolean[][] world;

    private int playerX, playerY;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();

        initWorld();
        drawWorld();

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W:
                    movePlayer(0, -1);
                    break;
                case S:
                    movePlayer(0, 1);
                    break;
                case A:
                    movePlayer(-1, 0);
                    break;
                case D:
                    movePlayer(1, 0);
                    break;
                case SPACE:
                    breakBlock();
                    break;
            }
            drawWorld();
        });
    }

    private void initWorld() {
        world = new boolean[WIDTH / BLOCK_SIZE][HEIGHT / BLOCK_SIZE];
        Random random = new Random();
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[x].length; y++) {
                world[x][y] = random.nextBoolean();
            }}
        playerX = world.length / 2;
        playerY = world[0].length / 2;}
    private void drawWorld() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[x].length; y++) {
                if (world[x][y]) {
                    gc.setFill(Color.DARKGRAY);
                } else {
                    gc.setFill(Color.LIGHTGRAY);}
                gc.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);}}
        gc.setFill(Color.RED);
        gc.fillRect(playerX * BLOCK_SIZE, playerY * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);}

    private void movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;
        if (newX >= 0 && newX < world.length && newY >= 0 && newY < world[0].length) {
            playerX = newX;
            playerY = newY;}}
    private void breakBlock() {
        world[playerX][playerY] = false;}

    public static void main(String[] args) {
        launch(args);
    }
}