package ijae.xmauroa00;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameBoard extends GridPane {
    private static final int CELL_SIZE = 60;
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
    
    private enum Direction {
        UP, DOWN, LEFT, RIGHT, NONE
    }
    
    // Load images
    private final Image playerImage = new Image("file:Images/PacMan.png");
    private final Image blueGhostImage = new Image("file:Images/blue_ghost.png");
    private final Image orangeGhostImage = new Image("file:Images/orange_ghost.png");
    private final Image pinkGhostImage = new Image("file:Images/pink_ghost.png");
    private final Image redGhostImage = new Image("file:Images/red_ghost.png");
    
    public GameBoard(String levelData, int level) {
        ghostCells = new ArrayList<>();
        currentDirection = Direction.NONE;
        points = 0;
        hasKey = false;
        isGateOpen = false;
        currentLevel = level;
        
        loadLevel(levelData);
        setupGameLoop();
        setupKeyHandlers();
        
        // Add these lines to set the preferred size of the GameBoard
        setPrefSize(cols * CELL_SIZE, rows * CELL_SIZE);
        setMinSize(cols * CELL_SIZE, rows * CELL_SIZE);
        
        // Optional: Center the game board
        setAlignment(Pos.CENTER);
    }
    
    private void setupGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(200), e -> gameStep()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }
    
    public void setGameSpeed(double speedMillis) {
        gameLoop.stop();
        gameLoop = new Timeline(new KeyFrame(Duration.millis(speedMillis), e -> gameStep()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }
    
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
    
    private void gameStep() {
        movePlayer();
        moveGhosts();
        checkCollisions();
    }
    
    private void movePlayer() {
        if (currentDirection == Direction.NONE) return;
        
        int[] newPos = getNewPosition(playerCell, currentDirection);
        if (canMoveTo(newPos[0], newPos[1])) {
            moveEntity(playerCell, newPos[0], newPos[1]);
        }
    }
    
    private void moveGhosts() {
        for (Cell ghostCell : ghostCells) {
            Direction randomDir = getRandomDirection();
            int[] newPos = getNewPosition(ghostCell, randomDir);
            if (canMoveTo(newPos[0], newPos[1])) {
                moveEntity(ghostCell, newPos[0], newPos[1]);
            }
        }
    }
    
    private Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[(int)(Math.random() * (directions.length - 1))]; // Exclude NONE
    }
    
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
    
    private boolean canMoveTo(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;
        Cell targetCell = board[row][col];
        if (targetCell.isWall) return false;
        if (targetCell.isGate && !hasKey) return false;
        return true;
    }
    
    private void moveEntity(Cell entityCell, int newRow, int newCol) {
        Cell targetCell = board[newRow][newCol];
        
        // Handle player collecting items
        if (entityCell == playerCell) {
            if (targetCell.hasPoint) {
                points += 10;
                targetCell.removePoint();
            }
            if (targetCell.hasKey) {
                hasKey = true;
                isGateOpen = true;
                targetCell.removeKey();
            }
            if (targetCell.isGate && hasKey) {
                gameWon();
                return;  // Stop here if game is won
            }
            if (targetCell.hasGhost) {  // Check if moving onto a ghost
                gameLost();
                return;  // Stop here if collision occurs
            }
        }
        
        // Check if a ghost is moving onto the player
        if (entityCell.hasGhost && board[newRow][newCol] == playerCell) {
            gameLost();
            return;  // Stop here if collision occurs
        }
        
        // Swap cells
        int oldRow = GridPane.getRowIndex(entityCell);
        int oldCol = GridPane.getColumnIndex(entityCell);
        getChildren().remove(entityCell);
        getChildren().remove(targetCell);
        add(entityCell, newCol, newRow);
        add(targetCell, oldCol, oldRow);
        
        // Update board array
        board[oldRow][oldCol] = targetCell;
        board[newRow][newCol] = entityCell;
    }
    
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
    
    private void loadLevel(String levelData) {
        // Normalize line endings and trim whitespace
        String[] lines = levelData.trim().split("\\r?\\n");
        
        if (lines.length < 2) {
            throw new IllegalArgumentException("Invalid level format: file must have at least 2 lines");
        }
        
        // Split and parse dimensions, trimming any whitespace
        String[] dimensions = lines[0].trim().split("\\s+");
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
    
    private Cell createCell(char type, int row, int col) {
        Cell cell = new Cell(CELL_SIZE);
        
        switch (type) {
            case 'W':
                cell.setWall();
                break;
            case 'G':
                cell.setGate();
                break;
            case 'P':
                cell.setPlayer();
                break;
            case 'C':
                cell.setGhost();
                break;
            case 'K':
                cell.setKey();
                break;
            case 'o':
                cell.setPoint();
                break;
            default:
                cell.setEmpty();
        }
        
        add(cell, col, row);
        return cell;
    }
    
    private class Cell extends StackPane {
        private boolean isWall;
        private boolean isGate;
        private boolean hasPlayer;
        private boolean hasGhost;
        private boolean hasKey;
        private boolean hasPoint;
        
        public Cell(int size) {
            Rectangle background = new Rectangle(size, size);
            background.setFill(Color.BLACK);
            background.setStroke(Color.BLUE);
            getChildren().add(background);
        }
        
        public void setWall() {
            isWall = true;
            Rectangle wall = new Rectangle(CELL_SIZE, CELL_SIZE);
            wall.setFill(Color.BLUE);
            getChildren().add(wall);
        }
        
        public void setGate() {
            isGate = true;
            Rectangle gate = new Rectangle(CELL_SIZE, CELL_SIZE);
            gate.setFill(Color.ORANGE);
            getChildren().add(gate);
        }
        
        public void setPlayer() {
            hasPlayer = true;
            ImageView playerView = new ImageView(playerImage);
            playerView.setFitWidth(CELL_SIZE);
            playerView.setFitHeight(CELL_SIZE);
            getChildren().add(playerView);
            playerCell = this; // Store reference to player cell
        }
        
        public void setGhost() {
            hasGhost = true;
            Image ghostImage = getRandomGhostImage();
            ImageView ghostView = new ImageView(ghostImage);
            ghostView.setFitWidth(CELL_SIZE);
            ghostView.setFitHeight(CELL_SIZE);
            getChildren().add(ghostView);
            ghostCells.add(this); // Add to ghost cells list
        }
        
        private Image getRandomGhostImage() {
            Image[] ghostImages = {
                redGhostImage,
                blueGhostImage,
                orangeGhostImage,
                pinkGhostImage
            };
            int randomIndex = (int) (Math.random() * ghostImages.length);
            return ghostImages[randomIndex];
        }
        
        public void setKey() {
            hasKey = true;
            Rectangle key = new Rectangle(CELL_SIZE/2, CELL_SIZE/2);
            key.setFill(Color.YELLOW);
            getChildren().add(key);
        }
        
        public void setPoint() {
            hasPoint = true;
            Rectangle point = new Rectangle(CELL_SIZE/4, CELL_SIZE/4);
            point.setFill(Color.WHITE);
            getChildren().add(point);
        }
        
        public void setEmpty() {
            Rectangle background = new Rectangle(CELL_SIZE, CELL_SIZE);
            background.setFill(Color.BLACK);
            background.setStroke(Color.BLUE);
            background.setStrokeWidth(0.5);
            getChildren().add(background);
        }
        
        public void removePoint() {
            hasPoint = false;
            // Remove the point visual (last child in the stack)
            if (getChildren().size() > 1) {
                getChildren().remove(getChildren().size() - 1);
            }
        }
        
        public void removeKey() {
            hasKey = false;
            // Remove the key visual (last child in the stack)
            if (getChildren().size() > 1) {
                getChildren().remove(getChildren().size() - 1);
            }
        }
    }
    
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
                    e.printStackTrace();
                }
            });
        } else {
            // Final level completed
            Platform.runLater(() -> {
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
                            e.printStackTrace();
                        }
                    }
                });
            });
        }
    }
    
    private void gameLost() {
        gameLoop.stop();
        
        Platform.runLater(() -> {
            // Create dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Game Over");
            dialog.setHeaderText("Game Over!\nPoints: " + points);
            
            // Add return to menu button
            ButtonType menuButton = new ButtonType("Return to Menu", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(menuButton);
            
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
            
            // Button styling
            Button button = (Button) dialogPane.lookupButton(menuButton);
            button.setStyle(
                "-fx-background-color: #0000FF;" +  // Blue background
                "-fx-text-fill: #FFFFFF;" +         // White text
                "-fx-font-size: 16px;" +
                "-fx-min-width: 150px;" +
                "-fx-min-height: 40px;" +
                "-fx-background-radius: 20;" +      // Rounded corners
                "-fx-border-radius: 20;" +
                "-fx-cursor: hand;"                 // Hand cursor on hover
            );
            
            // Add hover effect to button
            button.setOnMouseEntered(e -> 
                button.setStyle(
                    "-fx-background-color: #000080;" +  // Darker blue on hover
                    "-fx-text-fill: #FFD700;" +         // Gold text on hover
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
            
            // Show dialog and handle result
            dialog.showAndWait().ifPresent(response -> {
                if (response == menuButton) {
                    // Return to menu
                    Stage stage = (Stage) getScene().getWindow();
                    Menu menu = new Menu();
                    try {
                        menu.start(stage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }
}


