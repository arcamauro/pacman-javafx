package ijae.xmauroa00;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * This class represents the game board.
 * It contains the game logic such as moving the player and ghosts, checking for collisions, and loading levels.
 * @author Arcangelo Mauro - xmauroa00
 */
public class GameBoard extends GridPane {
    private Cell[][] board;
    private int rows;
    private int cols;
    private Cell playerCell;
    private List<Cell> ghostCells;
    private boolean isGateOpen;
    private int points;
    private boolean hasKey;
    private Timeline gameLoop;
    private Direction currentDirection;
    private int currentLevel = 1;
    private static final int TOT_LEVEL = 2;
    private boolean isStoryMode;
    
    /**
     * This enum represents the possible directions the player can move.
     */
    private enum Direction {
        UP, DOWN, LEFT, RIGHT, NONE
    }
    
    /**
     * This constructor initializes the game board.
     * It loads the level data and starts the game loop.
     * @param levelData the level data, so the level layout
     * @param level the level number, so the current level number to start from
     * @param isStoryMode whether the game is in story mode
     */
    public GameBoard(String levelData, int level, boolean isStoryMode) {
        ghostCells = new ArrayList<>();
        currentDirection = Direction.NONE;
        points = 0;
        hasKey = false;
        isGateOpen = false;
        currentLevel = level;
        this.isStoryMode = isStoryMode;
        loadLevel(levelData);
        setupGameLoop();
        setupKeyHandlers();
        
        setPrefSize(cols * Cell.getCellSize(), rows * Cell.getCellSize());
        setMinSize(cols * Cell.getCellSize(), rows * Cell.getCellSize());
        setAlignment(Pos.CENTER);
    }
    
    /**
     * This method sets up the game loop.
     * It creates a timeline with a key frame that calls the gameStep method every 200 milliseconds.
     */
    private void setupGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(200), e -> gameStep()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }
    
    /**
     * This method sets the game speed.
     * It stops the current game loop, creates a new timeline with the given speed, and starts the game loop.
     * @param speedMillis the speed in milliseconds which changes based on user choice in the menu
     */
    public void setGameSpeed(double speedMillis) {
        gameLoop.stop();
        gameLoop = new Timeline(new KeyFrame(Duration.millis(speedMillis), e -> gameStep()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }
    
    /**
     * This method sets up the key handlers.
     * The handlers are used to move the player based on the key pressed on the keyboard.
     */
    private void setupKeyHandlers() {
        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP:    currentDirection = Direction.UP; break;
                case DOWN:  currentDirection = Direction.DOWN; break;
                case LEFT:  currentDirection = Direction.LEFT; break;
                case RIGHT: currentDirection = Direction.RIGHT; break;
            }
        });
    }
    
    /**
     * This method is the game loop.
     * It moves the player and ghosts, checks for collisions, and updates the game state.
     */
    private void gameStep() {
        movePlayer();
        moveGhosts();
        checkCollisions();
    }
    
    /**
     * This method moves the player.
     * It checks if the player is moving and moves the player in the current direction.
     */
    private void movePlayer() {
        if (currentDirection == Direction.NONE) return;
        
        // Rotate player even if they can't move in that direction
        rotatePlayer(currentDirection);
        
        int[] newPos = getNewPosition(playerCell, currentDirection);
        if (canMoveTo(newPos[0], newPos[1])) {
            moveEntity(playerCell, newPos[0], newPos[1]);
        }
    }
    
    /**
     * This method moves the ghosts.
     * It moves each ghost in a random direction.
     */
    private void moveGhosts() {
        List<Cell> ghostCellsCopy = new ArrayList<>(ghostCells);
        
        for (Cell ghostCell : ghostCellsCopy) {
            Direction randomDir = getRandomDirection();
            int[] newPos = getNewPosition(ghostCell, randomDir);
            if (canMoveTo(newPos[0], newPos[1])) {
                moveEntity(ghostCell, newPos[0], newPos[1]);
            }
        }
    }
    
    /**
     * This method gets a random direction.
     * This method will be then used to move the ghosts.
     * It returns a random direction from the Direction enum.
     * @return a random direction
     */
    private Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[(int)(Math.random() * (directions.length - 1))]; // Exclude NONE
    }
    
    /**
     * This method gets the new position of the player or ghost.
     * It calculates the new position based on the current position and the direction.
     * @param cell the current cell of the player or ghost
     * @param dir the direction of the player or ghost
     * @return the new position
     */
    private int[] getNewPosition(Cell cell, Direction dir) {
        int row = GridPane.getRowIndex(cell);
        int col = GridPane.getColumnIndex(cell);
        
        switch (dir) {
            case UP:    return new int[]{row - 1, col};
            case DOWN:  return new int[]{row + 1, col};
            case LEFT:  return new int[]{row, col - 1};
            case RIGHT: return new int[]{row, col + 1};
            default:    return new int[]{row, col};
        }
    }
    
    /**
     * Checks if a move to the specified position is valid.
     * A move is valid if:
     * - The position is within board bounds
     * - The position is not a wall
     * - If the position is a gate, the player must have the key
     * @param row the row coordinate to check
     * @param col the column coordinate to check
     * @return true if the move is valid, false otherwise
     */
    private boolean canMoveTo(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;
        Cell targetCell = board[row][col];
        if (targetCell.isWall()) return false;
        if (targetCell.isGate() && !hasKey) return false;
        return true;
    }
    
    /**
     * This method moves an entity.
     * It handles the player collecting items and moving the player or ghost to the new position.
     * @param entityCell the entity to move
     * @param newRow the new row coordinate
     * @param newCol the new column coordinate
     */
    private void moveEntity(Cell entityCell, int newRow, int newCol) {
        Cell targetCell = board[newRow][newCol];
        
        // Handle player collecting items
        if (entityCell == playerCell) {
            if (targetCell.hasPoint()) {
                points += 10;
                targetCell.removePoint();
            }
            if (targetCell.hasKey()) {
                hasKey = true;
                isGateOpen = true;
                targetCell.removeKey();
            }
            if (targetCell.isGate() && hasKey) {
                gameWon();
                return;
            }
            if (targetCell.hasGhost()) {
                gameLost();
                return;
            }
        }
        
        // Check if a ghost is moving onto the player
        if (entityCell.hasGhost() && board[newRow][newCol] == playerCell) {
            gameLost();
            return;
        }
        
        // Get current position
        int oldRow = GridPane.getRowIndex(entityCell);
        int oldCol = GridPane.getColumnIndex(entityCell);
        
        // Move ghost similar to player movement
        getChildren().remove(entityCell);
        getChildren().remove(targetCell);
        add(entityCell, newCol, newRow);
        add(targetCell, oldCol, oldRow);
        
        // Update board array
        board[oldRow][oldCol] = targetCell;
        board[newRow][newCol] = entityCell;
    }
    
    /**
     * This method checks for collisions.
     * It checks if the player collides with a ghost.
     * If the player collides with a ghost, the game is lost.
     */
    private void checkCollisions() {
        int playerRow = GridPane.getRowIndex(playerCell);
        int playerCol = GridPane.getColumnIndex(playerCell);
        
        for (Cell ghostCell : ghostCells) {
            int ghostRow = GridPane.getRowIndex(ghostCell);
            int ghostCol = GridPane.getColumnIndex(ghostCell);
            
            if (playerRow == ghostRow && playerCol == ghostCol) {
                gameLost();
                break;  // Stop checking other ghosts
            }
        }
    }
    
    /**
     * This method loads the level data.
     * It normalizes the line endings and trims whitespace, then splits the level data into lines.
     * It checks if the level data has at least 2 lines and if the dimensions are valid.
     * It then parses the dimensions and creates the board.
     * @param levelData the level data, so the level layout
     */
    private void loadLevel(String levelData) {
        // Normalize line endings and trim whitespace
        String[] lines = levelData.lines().toArray(String[]::new);
        
        if (lines.length < 2) {
            throw new IllegalArgumentException("Invalid level format: file must have at least 2 lines");
        }
        
        // Split and parse dimensions, trimming any whitespace
        String[] dimensions = lines[0].trim().split(" ");
        if (dimensions.length != 2) {
            throw new IllegalArgumentException("Invalid dimension format: expected 2 numbers");
        }
        
        try {
            rows = Integer.parseInt(dimensions[0].trim());
            cols = Integer.parseInt(dimensions[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid dimensions: must be valid integers");
        }
        
        board = new Cell[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            String row = lines[i + 1].trim();
            if (row.length() != cols) {
                throw new IllegalArgumentException(
                    "Invalid row length at line " + (i + 2) + 
                    ": expected " + cols + " but got " + row.length()
                );
            }
            for (int j = 0; j < cols; j++) {
                char cellType = row.charAt(j);
                board[i][j] = createCell(cellType, i, j);
            }
        }
    }
    
    /**
     * This method creates a cell.
     * It creates a cell of the given type and adds it to the board.
     * The types are:
     * W - wall
     * G - gate
     * P - player
     * C - ghost
     * K - key
     * o - empty field with point
     * @param type the type of the cell
     * @param row the row of the cell
     * @param col the column of the cell
     * @return the cell
     */
    private Cell createCell(char type, int row, int col) {
        Cell cell = new Cell();
        
        switch (type) {
            case 'W': cell.setWall(); break;
            case 'G': cell.setGate(); break;
            case 'P': 
                cell.setPlayer(); 
                playerCell = cell;
                break;
            case 'C': 
                cell.setGhost();
                ghostCells.add(cell);
                break;
            case 'K': cell.setKey(); break;
            case 'o': cell.setPoint(); break;
            default: cell.setEmpty();
        }
        
        add(cell, col, row);
        return cell;
    }
    
    /**
     * This method is called when the player wins the game.
     * It stops the game loop, loads the next level, and restarts the game loop.
     */
    private void gameWon() {
        gameLoop.stop();
        
        if (currentLevel < TOT_LEVEL) {
            // Load next level
            currentLevel++;
            Platform.runLater(() -> {
                try {
                    // Load the next level's data using File I/O
                    String levelData = Files.readString(Path.of("levels/level" + currentLevel + ".txt"));
                    
                    // Clear current board
                    getChildren().clear();
                    ghostCells.clear();
                    
                    // Reset game state
                    points = 0;
                    hasKey = false;
                    isGateOpen = false;
                    currentDirection = Direction.NONE;
                    
                    // Load new level
                    loadLevel(levelData);
                    
                    // Restart game loop
                    setupGameLoop();
                    
                } catch (Exception e) {
                    System.out.println("Error loading next level");
                }
            });
        } else {
            // Final level completed
            Platform.runLater(() -> {
                // Save the score
                Menu.saveHighScore(points);
                
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Congratulations!");
                dialog.setHeaderText("You've completed all levels!\nTotal Points: " + points);
                
                ButtonType menuButton = new ButtonType("Return to Menu", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().add(menuButton);
                
                // Add the same styling as in gameLost()
                DialogPane dialogPane = dialog.getDialogPane();
                // [Add all the styling code from gameLost() here]
                
                dialog.showAndWait().ifPresent(response -> {
                    if (response == menuButton) {
                        Stage stage = (Stage) getScene().getWindow();
                        Menu menu = new Menu();
                        try {
                            menu.start(stage);
                        } catch (Exception e) {
                            System.out.println("Error returning to menu");
                        }
                    }
                });
            });
        }
    }
    
    /**
     * This method is called when the player loses the game.
     * It stops the game loop, saves the score, and shows a dialog with the score.
     * In the dialog the player can choose to return to the menu or restart from level 1.
     */
    private void gameLost() {
        gameLoop.stop();
        
        // Save the score
        Menu.saveHighScore(points);
        
        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Over");
        dialog.setHeaderText("Game Over!\nPoints: " + points);
        
        // Add buttons
        ButtonType menuButton = new ButtonType("Return to Menu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(menuButton);
        
        // Add restart button only if in story mode
        ButtonType restartButton = null;
        if (isStoryMode) {
            restartButton = new ButtonType("Restart from Level 1", ButtonBar.ButtonData.OTHER);
            dialog.getDialogPane().getButtonTypes().add(restartButton);
        }
        
        // Style the dialog
        DialogPane dialogPane = dialog.getDialogPane();
        
        // Main dialog styling
        dialogPane.setStyle(
            "-fx-background-color: #000000;" +
            "-fx-border-color: #FFD700;" +  // Gold border
            "-fx-border-width: 3px;"
        );
        
        // Header styling
        dialogPane.lookup(".header-panel").setStyle(
            "-fx-background-color: #000000;" +
            "-fx-border-color: #FFD700;" +  // Gold border
            "-fx-border-width: 0 0 2 0;"    // Bottom border only
        );
        
        dialogPane.lookup(".header-panel .label").setStyle(
            "-fx-text-fill: #FF0000;" +     // Red text
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Arial';"
        );
        
        // Content styling
        dialogPane.lookup(".content.label").setStyle(
            "-fx-text-fill: #FFFFFF;" +     // White text
            "-fx-font-size: 20px;" +
            "-fx-font-family: 'Arial';"
        );
        
        // Style both buttons
        for (ButtonType buttonType : dialog.getDialogPane().getButtonTypes()) {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            button.setStyle(
                "-fx-background-color: #0000FF;" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-font-size: 16px;" +
                "-fx-min-width: 150px;" +
                "-fx-min-height: 40px;" +
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-cursor: hand;"
            );
            
            // Add hover effect to buttons
            button.setOnMouseEntered(e -> 
                button.setStyle(
                    "-fx-background-color: #000080;" +
                    "-fx-text-fill: #FFD700;" +
                    "-fx-font-size: 16px;" +
                    "-fx-min-width: 150px;" +
                    "-fx-min-height: 40px;" +
                    "-fx-background-radius: 20;" +
                    "-fx-border-radius: 20;" +
                    "-fx-cursor: hand;"
                )
            );
            
            button.setOnMouseExited(e -> 
                button.setStyle(
                    "-fx-background-color: #0000FF;" +
                    "-fx-text-fill: #FFFFFF;" +
                    "-fx-font-size: 16px;" +
                    "-fx-min-width: 150px;" +
                    "-fx-min-height: 40px;" +
                    "-fx-background-radius: 20;" +
                    "-fx-border-radius: 20;" +
                    "-fx-cursor: hand;"
                )
            );
        }
        
        // Show dialog and handle result
        ButtonType finalRestartButton = restartButton;
        Stage stage = (Stage) getScene().getWindow();
        
        dialog.setOnCloseRequest(event -> {
            dialog.close();
            ButtonType result = dialog.getResult();
            
            if (result == menuButton) {
                Menu menu = new Menu();
                try {
                    menu.start(stage);
                } catch (Exception e) {
                    System.out.println("Error returning to menu");
                }
            } else if (result == finalRestartButton) {
                try {
                    String levelData = Files.readString(Path.of("levels/level1.txt"));
                    GameBoard newGame = new GameBoard(levelData, 1, true);
                    Scene gameScene = new Scene(newGame);
                    stage.setScene(gameScene);
                    newGame.requestFocus();
                } catch (IOException e) {
                    System.out.println("Error restarting from level 1");
                }
            }
        });
        
        dialog.show();
    }
    
    /**
     * Rotates the player image based on the movement direction.
     * @param direction the direction the player is moving
     */
    private void rotatePlayer(Direction direction) {
        ImageView playerView = (ImageView) playerCell.getChildren().get(playerCell.getChildren().size() - 1);
        
        switch (direction) {
            case UP:    playerView.setRotate(90); break;
            case DOWN:  playerView.setRotate(270);  break;
            case LEFT:  playerView.setRotate(0); break;
            case RIGHT: playerView.setRotate(180);   break;
            default: break;
        }
    }
}