package ijae.xmauroa00;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Represents a single cell in the Pacman game board.
 * Each cell can contain various game elements such as walls, gates, players, ghosts, keys, or points.
 * The cell extends StackPane to allow layering of multiple visual elements.
 *
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
     * Constructs a new empty cell with default black background and blue border.
     * The cell size is determined by the {@link #CELL_SIZE} constant.
     */
    public Cell() {
        background = new Rectangle(CELL_SIZE, CELL_SIZE);
        background.setFill(Color.BLACK);
        background.setStroke(Color.BLUE);
        getChildren().add(background);
    }
    
    /**
     * Converts this cell into a wall cell.
     * A wall cell blocks movement and is represented by 'W' in the map file.
     * The wall is visualized as a blue rectangle.
     */
    public void setWall() {
        isWall = true;
        Rectangle wall = new Rectangle(CELL_SIZE, CELL_SIZE);
        wall.setFill(Color.BLUE);
        getChildren().add(wall);
    }
    
    /**
     * Converts this cell into a gate cell.
     * A gate can be opened with a key and is represented by 'G' in the map file.
     * The gate is visualized using the gate image asset.
     */
    public void setGate() {
        isGate = true;
        ImageView gateView = new ImageView(gateImage);
        gateView.setFitWidth(CELL_SIZE);
        gateView.setFitHeight(CELL_SIZE);
        getChildren().add(gateView);
    }
    
    /**
     * Places the player character in this cell.
     * The player is represented by 'P' in the map file and is visualized
     * using the Pacman image asset.
     */
    public void setPlayer() {
        hasPlayer = true;
        ImageView playerView = new ImageView(playerImage);
        playerView.setFitWidth(CELL_SIZE);
        playerView.setFitHeight(CELL_SIZE);
        getChildren().add(playerView);
    }
    
    /**
     * Places a ghost in this cell.
     * Ghosts are represented by 'R' (red) or 'O' (orange) in the map file.
     * The ghost type is randomly selected between red and orange variants.
     * 
     * @see #getRandomGhostImage()
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
     * Selects a random ghost image from the available ghost types.
     *
     * @return Image - Either a red or orange ghost image
     */
    private Image getRandomGhostImage() {
        Image[] ghostImages = {redGhostImage, orangeGhostImage};
        int randomIndex = (int) (Math.random() * ghostImages.length);
        return ghostImages[randomIndex];
    }
    
    /**
     * Places a key item in this cell.
     * Keys are represented by 'K' in the map file and can be collected
     * by the player to open gates.
     */
    public void setKey() {
        hasKey = true;
        ImageView keyView = new ImageView(keyImage);
        keyView.setFitWidth(CELL_SIZE/2);
        keyView.setFitHeight(CELL_SIZE/2);
        getChildren().add(keyView);
    }
    
    /**
     * Places a point item in this cell.
     * Points are represented by 'P' in the map file and can be collected
     * by the player to increase score.
     */
    public void setPoint() {
        hasPoint = true;
        Rectangle point = new Rectangle(CELL_SIZE/4, CELL_SIZE/4);
        point.setFill(Color.WHITE);
        getChildren().add(point);
    }
    
    /**
     * Resets this cell to an empty state.
     * Empty cells are represented by 'E' in the map file.
     * Ensures the background is present and visible.
     */
    public void setEmpty() {
        if (!getChildren().contains(background)) {
            getChildren().add(0, background);
        }
    }
    
    /**
     * Removes the point from the cell.
     * In the text file, the empty fields are represented by the letter 'E'.
     */
    public void removePoint() {
        hasPoint = false;
        getChildren().removeIf(node -> node instanceof Rectangle && ((Rectangle) node).getFill() == Color.WHITE);
    }
    
    /**
     * Removes the key from the cell.
     */
    public void removeKey() {
        hasKey = false;
        getChildren().removeIf(node -> node instanceof ImageView && ((ImageView) node).getImage() == keyImage);
    }
    
    public boolean isWall() { return isWall; }
    public boolean isGate() { return isGate; }
    public boolean hasPlayer() { return hasPlayer; }
    public boolean hasGhost() { return hasGhost; }
    public boolean hasKey() { return hasKey; }
    public boolean hasPoint() { return hasPoint; }
    public static int getCellSize() { return CELL_SIZE; }
    public static Image getPlayerImage() { return playerImage; }
    
    /**
     * Gets the current ghost image displayed in this cell, if any.
     *
     * @return Image - The current ghost image, or null if no ghost is present
     */
    public Image getGhostImage() {
        for (javafx.scene.Node node : getChildren()) {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                if (imageView.getImage() == redGhostImage || imageView.getImage() == orangeGhostImage) {
                    return imageView.getImage();
                }
            }
        }
        return null;
    }
    
    /**
     * Places a specific ghost type in this cell.
     *
     * @param ghostImage The specific ghost image to use (red or orange)
     */
    public void setGhostWithImage(Image ghostImage) {
        hasGhost = true;
        ImageView ghostView = new ImageView(ghostImage);
        ghostView.setFitWidth(CELL_SIZE);
        ghostView.setFitHeight(CELL_SIZE);
        getChildren().add(ghostView);
    }
}