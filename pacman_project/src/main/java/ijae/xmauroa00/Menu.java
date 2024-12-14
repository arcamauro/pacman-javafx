package ijae.xmauroa00;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Menu extends Application {

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 500;
    private VBox menuContainer;
    private GameBoard gameBoard;

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
        Button helpButton = createMenuButton("Help");
        Button exitButton = createMenuButton("Exit");

        // Add event handlers
        startButton.setOnAction(e -> startGame());
        speedButton.setOnAction(e -> toggleSpeed(speedButton));
        highScoresButton.setOnAction(e -> showHighScores());
        helpButton.setOnAction(e -> showHelp());
        exitButton.setOnAction(e -> primaryStage.close());

        // Add all elements to container
        menuContainer.getChildren().addAll(
            titleText,
            startButton,
            speedButton,
            highScoresButton,
            helpButton,
            exitButton
        );

        // Create scene and show
        Scene scene = new Scene(menuContainer, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Pacman Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

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

    private double currentSpeed = 200; // Store current speed

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

    private void startGame() {
        try {
            String levelData = Files.readString(Path.of("levels/level1.txt"));
            gameBoard = new GameBoard(levelData, 1);
            gameBoard.setGameSpeed(currentSpeed); // Apply current speed when starting
            
            Stage primaryStage = (Stage) menuContainer.getScene().getWindow();
            Scene gameScene = new Scene(gameBoard);
            primaryStage.setScene(gameScene);
            
            // Give focus to the game board for key events
            gameBoard.requestFocus();
            
        } catch (IOException e) {
            System.err.println("Error loading level file: " + e.getMessage());
        }
    }

    private void showHighScores() {
        
    }

    private void showHelp() {
       
    }

    public static void main(String[] args) {
        launch(args);
    }
}