package ijae.xmauroa00;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * This is the main menu of the game.
 * It contains the main entry point for the game.
 * It allows the user to select the game mode, speed, and upload levels.
 * It also allows the user to view high scores.
 * @author Arcangelo Mauro - xmauroa00
 */

public class Menu extends Application {

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 500;
    private VBox menuContainer;
    private GameBoard gameBoard;
    private double currentSpeed = 200; // Store current speed

    /**
     * This is the main entry point for the game.
     * It creates the main menu and allows the user to select the game mode, speed, and upload levels.
     * It also allows the user to view high scores and exit the game.
     * @param primaryStage the primary stage for the game
     */
    @Override
    public void start(Stage primaryStage) {
        // Create main container
        menuContainer = new VBox(20); // 20 is spacing between elements
        menuContainer.setAlignment(Pos.CENTER);
        menuContainer.setStyle("-fx-background-color: black;");

        // Create title
        Text titleText = new Text("PACMAN");
        titleText.setFont(Font.font("Arial", 48));
        titleText.setStyle("-fx-fill: yellow;");

        // Create buttons
        Button startButton = createMenuButton("Start Game");
        Button speedButton = createMenuButton("Speed: Normal");
        Button highScoresButton = createMenuButton("High Scores");
        Button uploadLevelButton = createMenuButton("Upload Level");
        Button exitButton = createMenuButton("Exit");

        // Add event handlers
        startButton.setOnAction(e -> startGame());
        speedButton.setOnAction(e -> toggleSpeed(speedButton));
        highScoresButton.setOnAction(e -> showHighScores());
        uploadLevelButton.setOnAction(e -> uploadLevel(primaryStage));
        exitButton.setOnAction(e -> primaryStage.close());

        // Add all elements to container
        menuContainer.getChildren().addAll(
            titleText,
            startButton,
            speedButton,
            highScoresButton,
            uploadLevelButton,
            exitButton
        );

        // Create scene and show
        Scene scene = new Scene(menuContainer, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Pacman Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * This method creates a button with the given text.
     * It also adds hover effects to the button with simple css styles.
     * @param text the text to display on the button
     * @return the button
     */
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: blue;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20px;" +
            "-fx-min-width: 200px;" +
            "-fx-min-height: 40px;"
        );

        // Add hover effect
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: darkblue;" +
                "-fx-text-fill: yellow;" +
                "-fx-font-size: 20px;" +
                "-fx-min-width: 200px;" +
                "-fx-min-height: 40px;"
            )
        );

        button.setOnMouseExited(e -> 
            button.setStyle(
                "-fx-background-color: blue;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 20px;" +
                "-fx-min-width: 200px;" +
                "-fx-min-height: 40px;"
            )
        );

        return button;
    }

    /**
     * This method toggles the speed of the game.
     * It changes the speed of the game between fast, slow, and normal.
     * @param speedButton the button to toggle the speed
     */
    private void toggleSpeed(Button speedButton) {
        switch (speedButton.getText()) {
            case "Speed: Normal":
                currentSpeed = 100;  // Fast speed
                speedButton.setText("Speed: Fast");
                break;
            case "Speed: Fast":
                currentSpeed = 300;  // Slow speed
                speedButton.setText("Speed: Slow");
                break;
            default:
                currentSpeed = 200;  // Normal speed
                speedButton.setText("Speed: Normal");
                break;
        }
        
        if (gameBoard != null) {
            gameBoard.setGameSpeed(currentSpeed);
        }
    }

    /**
     * This method starts the game.
     * It makes a mode selection dialog and allows the user to select the game mode.
     */
    private void startGame() {
        // Create mode selection dialog
        Stage modeSelect = new Stage();
        modeSelect.initModality(Modality.APPLICATION_MODAL);
        modeSelect.setTitle("Select Game Mode");

        VBox modeContainer = new VBox(10);
        modeContainer.setAlignment(Pos.CENTER);
        modeContainer.setStyle("-fx-background-color: black; -fx-padding: 20px;");

        // Add title
        Text titleText = new Text("SELECT MODE");
        titleText.setFont(Font.font("Arial", 32));
        titleText.setStyle("-fx-fill: yellow;");
        modeContainer.getChildren().add(titleText);

        // Create mode buttons
        Button storyButton = createMenuButton("Story Mode");
        Button customButton = createMenuButton("Custom Levels");
        Button backButton = createMenuButton("Back to Menu");

        // Story Mode handler
        storyButton.setOnAction(e -> {
            startStoryMode();
            modeSelect.close();
        });

        // Custom Levels handler
        customButton.setOnAction(e -> {
            showCustomLevels();
            modeSelect.close();
        });

        // Back button handler
        backButton.setOnAction(e -> modeSelect.close());

        modeContainer.getChildren().addAll(storyButton, customButton, backButton);

        Scene scene = new Scene(modeContainer, 300, 400);
        modeSelect.setScene(scene);
        modeSelect.showAndWait();
    }

    /**
     * This method starts the story mode.
     * It loads the level1.txt, which is the first level of the game, and starts the game.
     */
    private void startStoryMode() {
        try {
            String levelData = Files.readString(Path.of("levels/level1.txt"));
            gameBoard = new GameBoard(levelData, 1);
            gameBoard.setGameSpeed(currentSpeed);
            
            Stage primaryStage = (Stage) menuContainer.getScene().getWindow();
            Scene gameScene = new Scene(gameBoard);
            primaryStage.setScene(gameScene);
            gameBoard.requestFocus();
        } catch (IOException e) {
            System.err.println("Error loading level file: " + e.getMessage());
        }
    }

    /**
     * This method shows the custom levels when choosing from the menu.
     * It loads all the custom levels from the levels directory and allows the user to select a level.
     */
    private void showCustomLevels() {
        Stage levelSelect = new Stage();
        levelSelect.initModality(Modality.APPLICATION_MODAL);
        levelSelect.setTitle("Select Custom Level");

        VBox levelContainer = new VBox(10);
        levelContainer.setAlignment(Pos.CENTER);
        levelContainer.setStyle("-fx-background-color: black; -fx-padding: 20px;");

        // Add title
        Text titleText = new Text("CUSTOM LEVELS");
        titleText.setFont(Font.font("Arial", 32));
        titleText.setStyle("-fx-fill: yellow;");
        levelContainer.getChildren().add(titleText);

        try {
            // Get all custom level files from the levels directory
            File levelsDir = new File("levels");
            File[] levelFiles = levelsDir.listFiles((dir, name) -> {
                // Exclude level1.txt, level2.txt, etc.
                return name.toLowerCase().endsWith(".txt") && !name.matches("level[0-9]+\\.txt");
            });

            if (levelFiles != null && levelFiles.length > 0) {
                for (File levelFile : levelFiles) {
                    String levelName = levelFile.getName();
                    // Remove .txt extension for display
                    levelName = levelName.substring(0, levelName.lastIndexOf('.'));
                    // Capitalize first letter and replace underscores with spaces
                    levelName = levelName.substring(0, 1).toUpperCase() + 
                               levelName.substring(1).replace('_', ' ');
                    
                    Button levelButton = createMenuButton(levelName);
                    levelButton.setOnAction(e -> {
                        try {
                            String levelData = Files.readString(levelFile.toPath());
                            gameBoard = new GameBoard(levelData, 1);
                            gameBoard.setGameSpeed(currentSpeed);
                            
                            Stage primaryStage = (Stage) menuContainer.getScene().getWindow();
                            Scene gameScene = new Scene(gameBoard);
                            primaryStage.setScene(gameScene);
                            gameBoard.requestFocus();
                            
                            levelSelect.close();
                        } catch (IOException ex) {
                            System.err.println("Error loading level file: " + ex.getMessage());
                        }
                    });
                    levelContainer.getChildren().add(levelButton);
                }
            } else {
                Text noLevelsText = new Text("No custom levels available!");
                noLevelsText.setFont(Font.font("Arial", 20));
                noLevelsText.setStyle("-fx-fill: white;");
                levelContainer.getChildren().add(noLevelsText);
            }

            // Add back button
            Button backButton = createMenuButton("Back to Menu");
            backButton.setOnAction(e -> levelSelect.close());
            levelContainer.getChildren().add(backButton);

            Scene scene = new Scene(levelContainer, 300, 400);
            levelSelect.setScene(scene);
            levelSelect.showAndWait();

        } catch (Exception e) {
            System.err.println("Error loading levels: " + e.getMessage());
        }
    }

    /**
     * This method shows the high scores when choosing from the menu.
     * It loads the high scores from a file called highscores.txt and displays them.
     */
    private void showHighScores() {
        // Create a new stage for high scores
        Stage highScoreStage = new Stage();
        highScoreStage.initModality(Modality.APPLICATION_MODAL);
        highScoreStage.setTitle("High Scores");

        VBox scoreContainer = new VBox(10);
        scoreContainer.setAlignment(Pos.CENTER);
        scoreContainer.setStyle("-fx-background-color: black; -fx-padding: 20px;");

        // Add title
        Text titleText = new Text("HIGH SCORES");
        titleText.setFont(Font.font("Arial", 32));
        titleText.setStyle("-fx-fill: yellow;");

        scoreContainer.getChildren().add(titleText);

        // Read and display high scores
        try {
            List<String> scores = readHighScores();
            for (String score : scores) {
                Text scoreText = new Text(score);
                scoreText.setFont(Font.font("Arial", 20));
                scoreText.setStyle("-fx-fill: white;");
                scoreContainer.getChildren().add(scoreText);
            }
        } catch (IOException e) {
            Text errorText = new Text("No high scores yet!");
            errorText.setFont(Font.font("Arial", 20));
            errorText.setStyle("-fx-fill: white;");
            scoreContainer.getChildren().add(errorText);
        }

        // Add back button
        Button backButton = createMenuButton("Back to Menu");
        backButton.setOnAction(e -> highScoreStage.close());
        scoreContainer.getChildren().add(backButton);

        Scene scene = new Scene(scoreContainer, 300, 400);
        highScoreStage.setScene(scene);
        highScoreStage.show();
    }

    private static final String HIGH_SCORES_FILE = "highscores.txt";
    private static final int MAX_HIGH_SCORES = 5;

    /**
     * This method saves the high score to the highscores.txt file.
     * It also keeps the top 5 high scores.
     * @param score the score to save
     */
    public static void saveHighScore(int score) {
        try {
            List<Integer> scores = new ArrayList<>();
            
            // Read existing scores
            try {
                List<String> existingScores = Files.readAllLines(Path.of(HIGH_SCORES_FILE));
                for (String line : existingScores) {
                    scores.add(Integer.parseInt(line.trim()));
                }
            } catch (IOException e) {
                // File doesn't exist yet, that's okay
            }

            // Add new score
            scores.add(score);

            // Sort in descending order
            scores.sort(Collections.reverseOrder());

            // Keep only top scores
            while (scores.size() > MAX_HIGH_SCORES) {
                scores.remove(scores.size() - 1);
            }

            // Write back to file
            List<String> scoreStrings = new ArrayList<>();
            for (Integer s : scores) {
                scoreStrings.add(s.toString());
            }
            Files.write(Path.of(HIGH_SCORES_FILE), scoreStrings);

        } catch (IOException e) {
            System.err.println("Error saving high score: " + e.getMessage());
        }
    }

    /**
     * This method reads the high scores from the highscores.txt file.
     * It also formats the high scores to be displayed in the high scores window.
     * @return the high scores
     */
    private List<String> readHighScores() throws IOException {
        List<String> scores = new ArrayList<>();
        List<String> rawScores = Files.readAllLines(Path.of(HIGH_SCORES_FILE));
        
        for (int i = 0; i < rawScores.size(); i++) {
            scores.add((i + 1) + ". " + rawScores.get(i));
        }
        
        return scores;
    }

    /**
     * This method allows the user to upload a level file.
     * It allows the user to select a level file from the file chooser and validates it.
     * @param primaryStage the primary stage for the game
     */
    private void uploadLevel(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Level File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                // Read and validate the level file
                String levelContent = Files.readString(selectedFile.toPath());
                if (isValidLevelFormat(levelContent)) {
                    // Get original filename without extension
                    String originalName = selectedFile.getName();
                    String nameWithoutExtension = originalName.substring(0, originalName.lastIndexOf('.'));
                    
                    // Create the new filename
                    String fileName = nameWithoutExtension + ".txt";
                    
                    Path levelsDir = Path.of("levels");
                    if (!Files.exists(levelsDir)) {
                        Files.createDirectory(levelsDir);
                    }
                    
                    // Check if file already exists
                    if (Files.exists(levelsDir.resolve(fileName))) {
                        boolean overwrite = showConfirmationDialog(
                            "Level already exists",
                            "A level with this name already exists. Do you want to overwrite it?"
                        );
                        if (!overwrite) {
                            return;
                        }
                    }
                    
                    Files.write(levelsDir.resolve(fileName), levelContent.getBytes());

                    showAlert(Alert.AlertType.INFORMATION, 
                        "Success", 
                        "Level uploaded successfully!", 
                        "The level '" + nameWithoutExtension + "' has been added to your levels."
                    );
                } else {
                    showAlert(Alert.AlertType.ERROR, 
                        "Invalid Format", 
                        "Invalid level format", 
                        "The level file must follow the correct format:\n" +
                        "First line: rows columns\n" +
                        "Following lines: level layout using W,P,G,C,K,o characters"
                    );
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, 
                    "Error", 
                    "Error uploading level", 
                    "Could not upload the level file: " + e.getMessage()
                );
            }
        }
    }

    /**
     * This method validates the level file format.
     * It checks if the level file is in the correct format.
     * @param content the level file content
     * @return true if the level file is valid, false otherwise
     */
    private boolean isValidLevelFormat(String content) {
        try {
            String[] lines = content.trim().split("\\r?\\n");
            if (lines.length < 2) return false;

            // Check first line contains two numbers
            String[] dimensions = lines[0].trim().split("\\s+");
            if (dimensions.length != 2) return false;
            int rows = Integer.parseInt(dimensions[0]);
            int cols = Integer.parseInt(dimensions[1]);

            // Check if number of rows matches declared rows
            if (lines.length - 1 != rows) return false;

            // Check each row
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                // Check if row length matches declared columns
                if (line.length() != cols) return false;
                // Check if row contains only valid characters
                if (!line.matches("[WPGCKo.]+")) return false;
            }

            // Check if there is exactly one player (P) and one gate (G)
            String fullContent = String.join("", lines);
            if (fullContent.chars().filter(ch -> ch == 'P').count() != 1) return false;
            if (fullContent.chars().filter(ch -> ch == 'G').count() != 1) return false;
            if (fullContent.chars().filter(ch -> ch == 'K').count() != 1) return false;

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * This method shows a confirmation dialog.
     * It shows a confirmation dialog with the given title and content.
     * @param title the title of the dialog
     * @param content the content of the dialog
     * @return true if the user confirms, false otherwise
     */
    private boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: black;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        
        // Style the buttons
        alert.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
            button.setStyle(
                "-fx-background-color: blue;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;"
            );
        });
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * This method shows an alert.
     * It shows an alert with the given type, title, header, and content.
     * @param type the type of the alert
     * @param title the title of the alert
     * @param header the header of the alert
     * @param content the content of the alert
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: black;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: black;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: yellow;");
        
        alert.showAndWait();
    }

    /**
     * This method is the main entry point for the game.
     * It launches the game.
     */
    public static void main(String[] args) {
        launch(args);
    }
}