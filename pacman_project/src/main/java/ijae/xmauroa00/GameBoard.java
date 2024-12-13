package ijae.xmauroa00;

import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameBoard extends GridPane {
    private static final int CELL_SIZE = 40;
    private Cell[][] board;
    private int rows;
    private int cols;
    
    // Load images
    private final Image playerImage = new Image("file:Images/PacMan.png");
    private final Image blueGhostImage = new Image("file:Images/blue_ghost.png");
    private final Image orangeGhostImage = new Image("file:Images/orange_ghost.png");
    private final Image pinkGhostImage = new Image("file:Images/pink_ghost.png");
    private final Image redGhostImage = new Image("file:Images/red_ghost.png");
    
    public GameBoard(String levelData) {
        loadLevel(levelData);
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
        }
        
        public void setGhost() {
            hasGhost = true;
            Image ghostImage = getRandomGhostImage();
            ImageView ghostView = new ImageView(ghostImage);
            ghostView.setFitWidth(CELL_SIZE);
            ghostView.setFitHeight(CELL_SIZE);
            getChildren().add(ghostView);
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
    }
}