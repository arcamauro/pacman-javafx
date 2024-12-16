package ijae.xmauroa00;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * This class represents a cell in the game board.
 * It contains the cell's properties such as if it is a wall, gate, player, ghost, key, or point.
 * @author Arcangelo Mauro - xmauroa00
 */
public class Cell extends StackPane {
    private boolean isWall;
    private boolean isGate;
    private boolean hasPlayer;
    private boolean hasGhost;
    private boolean hasKey;
    private boolean hasPoint;
    
    private static final int CELL_SIZE = 60;
    private static final Image playerImage = new Image("file:Images/PacMan.png");
    private static final Image orangeGhostImage = new Image("file:Images/orange_ghost.png");
    private static final Image redGhostImage = new Image("file:Images/red_ghost.png");
    private static final Image keyImage = new Image("file:Images/key.png");
    private static final Image gateImage = new Image("file:Images/gate.png");
    
    private Rectangle background;
    
    /**
     * This constructor initializes the cell.
     * It creates a rectangle with the given size and adds it to the cell.
     */
    public Cell() {
        background = new Rectangle(CELL_SIZE, CELL_SIZE);
        background.setFill(Color.BLACK);
        background.setStroke(Color.BLUE);
        getChildren().add(background);
    }
    
    /**
     * This method sets the cell to a wall.
     */
    public void setWall() {
        isWall = true;
        Rectangle wall = new Rectangle(CELL_SIZE, CELL_SIZE);
        wall.setFill(Color.BLUE);
        getChildren().add(wall);
    }
    
    /**
     * This method sets the cell to the gate.
     */
    public void setGate() {
        isGate = true;
        ImageView gateView = new ImageView(gateImage);
        gateView.setFitWidth(CELL_SIZE);
        gateView.setFitHeight(CELL_SIZE);
        getChildren().add(gateView);
    }
    
    /**
     * This method sets the cell to the player.
     */
    public void setPlayer() {
        hasPlayer = true;
        ImageView playerView = new ImageView(playerImage);
        playerView.setFitWidth(CELL_SIZE);
        playerView.setFitHeight(CELL_SIZE);
        getChildren().add(playerView);
    }
    
    /**
     * This method sets the cell to a ghost.
     */
    public void setGhost() {
        hasGhost = true;
        Image ghostImage = getRandomGhostImage();
        ImageView ghostView = new ImageView(ghostImage);
        ghostView.setFitWidth(CELL_SIZE);
        ghostView.setFitHeight(CELL_SIZE);
        getChildren().add(ghostView);
    }
    
    /**
     * This method gets a random ghost image.
     * @return a random ghost image
     */
    private Image getRandomGhostImage() {
        Image[] ghostImages = {redGhostImage, orangeGhostImage};
        int randomIndex = (int) (Math.random() * ghostImages.length);
        return ghostImages[randomIndex];
    }
    
    /**
     * This method sets the cell to a key.
     */
    public void setKey() {
        hasKey = true;
        ImageView keyView = new ImageView(keyImage);
        keyView.setFitWidth(CELL_SIZE/2);
        keyView.setFitHeight(CELL_SIZE/2);
        getChildren().add(keyView);
    }
    
    /**
     * This method sets the cell to a point.
     */
    public void setPoint() {
        hasPoint = true;
        Rectangle point = new Rectangle(CELL_SIZE/4, CELL_SIZE/4);
        point.setFill(Color.WHITE);
        getChildren().add(point);
    }
    
    /**
     * This method sets the cell to an empty field.
     */
    public void setEmpty() {
        if (!getChildren().contains(background)) {
            getChildren().add(0, background);
        }
    }
    
    /**
     * This method removes the point from the cell.
     */
    public void removePoint() {
        hasPoint = false;
        getChildren().removeIf(node -> node instanceof Rectangle && ((Rectangle) node).getFill() == Color.WHITE);
    }
    
    /**
     * This method removes the key from the cell.
     */
    public void removeKey() {
        hasKey = false;
        getChildren().removeIf(node -> node instanceof ImageView && ((ImageView) node).getImage() == keyImage);
    }
    
    // Getters for properties
    public boolean isWall() { return isWall; }
    public boolean isGate() { return isGate; }
    public boolean hasPlayer() { return hasPlayer; }
    public boolean hasGhost() { return hasGhost; }
    public boolean hasKey() { return hasKey; }
    public boolean hasPoint() { return hasPoint; }
    public static int getCellSize() { return CELL_SIZE; }
    public static Image getPlayerImage() { return playerImage; }
}