import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class Minecraft extends Application {

    // constants for the game window size and block size
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int BLOCK_SIZE = 20;

    private GraphicsContext gc;

    // 2D array to represent the game world
    private Block[][] world;

    private int playerX, playerY;
    private int playerSpeed = 5;

    private Timeline gameLoop;

    private Mob mob;

    private int hungerLevel = 100;
    private int health = 100;

    private boolean isDay = true;

    private WeatherType weather = WeatherType.CLEAR;

    private Biome biome;

    private SoundEffects soundEffects;

    private GameMenu gameMenu;

    private Inventory inventory;

    private CraftingTable craftingTable;

    @Override
    public void start(Stage primaryStage) {
        // Canvas to draw on
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
                case E:
                    // Place block
                    placeBlock();
                    break;
                case C:
                    // Open crafting menu
                    openCraftingMenu();
                    break;
                case ESCAPE:
                    // Open game menu
                    openGameMenu();
                    break;
            }
        });

        gameLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            // Update game logic here
            updateGame();
            drawWorld();
        }));

        gameLoop.setCycleCount(Animation.INDEFINITE);
        gameLoop.play();

        // Initialize mob
        mob = new Mob(world, playerX, playerY);

        // Initialize inventory
        inventory = new Inventory();

        // Initialize biome
        biome = new Biome(world);

        // Initialize sound effects
        soundEffects = new SoundEffects();

        // Initialize game menu
        gameMenu = new GameMenu(primaryStage);

        // Initialize crafting table
        craftingTable = new CraftingTable(inventory);
    }

    // initializing the game world with random blocks
    private void initWorld() {
        world = new Block[WIDTH / BLOCK_SIZE][HEIGHT / BLOCK_SIZE];
        Random random = new Random();
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[x].length; y++) {
                BlockType type = random.nextBoolean()? BlockType.STONE : BlockType.AIR;
                world[x][y] = new Block(type);
            }
        }
        // set the player's initial position to the center of the world
        playerX = world.length / 2;
        playerY = world[0].length / 2;
    }

    private void drawWorld() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // draw a simple background
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // draw blocks
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[x].length; y++) {
                Block block = world[x][y];
                if (block!= null) {
                    switch (block.getType()) {
                        case STONE:
                            gc.setFill(Color.DARKGRAY);
                            break;
                        case AIR:
                            gc.setFill(Color.LIGHTGRAY);
                            break;
                        case DIRT:
                            gc.setFill(Color.BROWN);
                            break;
                        case GRASS:
                            gc.setFill(Color.GREEN);
                            break;
                        case WATER:
                            gc.setFill(Color.BLUE);
                            break;
                        default:
                            gc.setFill(Color.WHITE);
                    }
                    // draw a block at the current position
                    gc.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE,BLOCK_SIZE);
                }}}

        // draw player configuration
        gc.setFill(Color.RED);
        gc.fillRect(playerX * BLOCK_SIZE, playerY * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

        // draw mob
        gc.setFill(Color.BLACK);
        gc.fillRect(mob.getX() * BLOCK_SIZE, mob.getY() * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

        // Draw hunger level
        gc.setFill(Color.RED);
        gc.fillRect(10, 10, hungerLevel, 10);

        // draws day/night cycle
        if (isDay) {
            gc.setFill(Color.BLUE);
        } else {
            gc.setFill(Color.BLACK);
        }
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // draw weather
        switch (weather) {

            case RAIN:
                gc.setFill(Color.BLUE);
                gc.fillRect(0, 0, WIDTH, HEIGHT);
                break;
            case SNOW:
                gc.setFill(Color.WHITE);
                gc.fillRect(0, 0, WIDTH, HEIGHT);
                break;
            case STORM:
                gc.setFill(Color.DARKGRAY);
                gc.fillRect(0, 0, WIDTH, HEIGHT);
                break;
            default:
                break;
        }

        // draw biomes, not fully incorporated yet
        biome.draw(gc);

        // draw inventory
        inventory.draw(gc);
    }

    // move the player by the given amount
    private void movePlayer(int dx, int dy) {
        int newX = playerX + dx / BLOCK_SIZE;
        int newY = playerY + dy / BLOCK_SIZE;
        if (newX >= 0 && newX < world.length && newY >= 0 && newY < world[0].length) {
            // check if the new position is not a solid block, to avoid present and future bugs
            Block block = world[newX][newY];
             if (block!= null && block.getType() == BlockType.AIR) {
                playerX = newX;
                playerY = newY; }}}

    private void breakBlock() {
        Block block = world[playerX][playerY];
        if (block!= null &&!block.isBroken()) {
            block.breakBlock();
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                block.setType(BlockType.AIR);
            }).start();}}

    private void placeBlock() {
        if (playerX >= 0 && playerX < world.length && playerY >= 0 && playerY < world[0].length) {
            world[playerX][playerY] = new Block(BlockType.DIRT);
        }
    }

    private void openCraftingMenu() {
        // opens a crafting menu, allowing players to craft new items
        craftingTable.show();
    }

    private void openGameMenu() {
        // Opens a game menu, allowing players to: access options, save and load games, and exit the game
        gameMenu.show();
    }

    private void updateGame() {
        // check for player collision with blocks
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[x].length; y++) {
                Block block = world[x][y];
                if (block!= null && block.getType() == BlockType.STONE) {
                    // check if player is colliding with the block
                    if (playerX == x && playerY == y) {
                        //Colission
                        System.out.println("Player collided with stone block!");
                    }}}}

        // Update mob
        mob.update();

        // update hunger level
        hungerLevel--;

        // update day/night cycle
        if (isDay) {
            isDay = false;
        } else {
            isDay = true;
        }

        // update weather
        if (Math.random() < 0.01) {
            weather = WeatherType.RAIN;


        } else if (Math.random() < 0.005) {
            weather = WeatherType.SNOW;
        } else if (Math.random() < 0.002) {
            weather = WeatherType.STORM;
        } else {
            weather = WeatherType.CLEAR;
        }

        for (int x = 0; x < world.length; x++) {

            for (int y = 0; y < world[x].length; y++) {
                Block block = world[x][y];
                if (block!= null) {
                    block.update();
                }}}}

    public enum BlockType {
        AIR, STONE, DIRT, GRASS, WATER
    }

    public static class Block {
        private BlockType type;
        private boolean broken;

        public Block(BlockType type) {
            this.type = type;
            this.broken = false;
        }

        public BlockType getType() {
            return type;
        }

        public boolean isBroken() {
            return broken;
        }

        public void breakBlock() {
            broken = true;
        }

        public void setType(BlockType type) {
            this.type = type;
        }

        public void update() {
            if (isBroken()) {
                // If the block is broken, change its type to AIR after 1 second
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    setType(BlockType.AIR);
                }).start(); }}}

                //mob ai
    public static class Mob {
        private int x, y;
        private int speed = 1;
        private Block[][] world;
        private Player player;

        public Mob(Block[][] world, Player player) {
            this.world = world;
            this.player = player;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void update() {
            // Simple mob AI
            if (x < player.getX()) {
                x += speed;
            } else if (x > player.getX()) {
                x -= speed;
            }

            if (y < player.getY()) {
                y += speed;
            } else if (y > player.getY()) {
                y -= speed;}

            if (distanceToPlayer() < 5) {
                // attacking the player
                player.takeDamage(10);
            } else if (distanceToPlayer() > 10) {
                // flee from the player
                x -= speed;
                y -= speed;
            }
        }

        private int distanceToPlayer() {
            return (int) Math.sqrt(Math.pow(x - player.getX(), 2) + Math.pow(y - player.getY(), 2));
        }}

    public enum WeatherType {
        RAIN, SNOW, STORM, CLEAR
    }

    public static class Inventory {
        // inventory management system beta
        private Item[] items;

        public Inventory() {
            items = new Item[10];
        }

        public void addItem(Item item) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) {
                    items[i] = item;
                    return;
                }}}

        public void removeItem(Item item) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] == item) {
                    items[i] = null;
                    return ; }}}

        public void draw(GraphicsContext gc) {
            // draw inventory items
            for (int i = 0; i < items.length; i++) {
                if (items[i]!= null) {
                    gc.setFill(Color.WHITE);
                    gc.fillRect(10 + i * 20, 10, 20, 20);
                }}}}

    public static class Item {
        private String name;

        public Item(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class Biome {
        private Block[][] world;

        public Biome(Block[][] world) {
            this.world = world;
        }

        public void draw(GraphicsContext gc) {
            // draw biome features
            for (int x = 0; x < world.length; x++) {
                for (int y = 0; y < world[x].length; y++) {
                    Block block = world[x][y];
                    if (block!= null) {
                        if (block.getType() == BlockType.GRASS) {
                            gc.setFill(Color.GREEN);
                            gc.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                        } else if (block.getType() == BlockType.WATER) {
                            gc.setFill(Color.BLUE);
                            gc.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                        }}}}}}

    public static class SoundEffects {
        // Sound effects system not incorporated yet!
    }

    public static class GameMenu {
        private Stage primaryStage;

        public GameMenu(Stage primaryStage) {
            this.primaryStage = primaryStage;
        }

        public void show() {
            // show game menu, will be improved in future
            Menu menu = new Menu();
            menu.show(primaryStage);
        }
    }

    public static class CraftingTable {
        private Inventory inventory;

        public CraftingTable(Inventory inventory) {
            this.inventory = inventory;
       }

        public void show() {
            // show crafting table
            CraftingMenu menu = new CraftingMenu(inventory);
            menu.show();
        }
    }

    public static class CraftingMenu {
        private Inventory inventory;

        public CraftingMenu(Inventory inventory) {
            this.inventory = inventory;
        }

        public void show() {
            // Show crafting menu
            System.out.println("Crafting menu opened!");
        }
    }

    public static class Player {
        private int x, y;
        private int health = 100;

        public Player(int x, int y) {

            this.x = x;
            this.y = y;

        }


        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void takeDamage(int damage) {
            health -= damage;
            if (health <= 0) {
                // game over
                System.out.println("Game over!");
            }}}

    public static void main(String[] args) {
        launch(args);
    }
}