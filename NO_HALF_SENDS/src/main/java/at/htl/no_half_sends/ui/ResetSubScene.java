package at.htl.no_half_sends.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.scene.SubScene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ResetSubScene extends SubScene {
    private int secondsLeft = 3;

    public ResetSubScene() {
        // 1. Hintergrund abdunkeln (1920x1080)
        Rectangle bg = new Rectangle(1920, 1080, Color.color(0.05, 0.05, 0.05, 0.85));

        // 2. UI Texte erstellen
        Text titleText = new Text("VON DER STRASSE ABGEKOMMEN!");
        titleText.setFont(Font.font("Impact", 60));
        titleText.setFill(Color.ORANGERED);
        titleText.setStroke(Color.WHITE);
        titleText.setStrokeWidth(1);

        Text timerText = new Text("Zurück zum Start in: 3s");
        timerText.setFont(Font.font("Arial", 36));
        timerText.setFill(Color.WHITE);

        // 3. Button erstellen (standardmäßig deaktiviert)
        Button btnReturn = new Button("RETURN TO START");
        btnReturn.setFont(Font.font("Arial", 24));
        btnReturn.setStyle("-fx-background-color: #333; -fx-text-fill: #777; -fx-padding: 10 40 10 40;");
        btnReturn.setDisable(true);

        // Layout-Box zentrieren
        VBox box = new VBox(30, titleText, timerText, btnReturn);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(1920);
        box.setPrefHeight(1080);

        getContentRoot().getChildren().addAll(bg, box);

        // 4. JavaFX Timeline für den Countdown
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsLeft--;
            if (secondsLeft > 0) {
                timerText.setText("Zurück zum Start in: " + secondsLeft + "s");
            } else {
                timerText.setText("Bereit für den nächsten Versuch!");
                timerText.setFill(Color.LIGHTGREEN);

                btnReturn.setDisable(false);
                btnReturn.setStyle("-fx-background-color: #ff4500; -fx-text-fill: white; -fx-padding: 10 40 10 40; -fx-cursor: hand;");
            }
        }));
        timeline.setCycleCount(3);
        timeline.play();

        // 5. Aktion beim Klick (Nutzt jetzt die statische FXGL-Klasse)
        btnReturn.setOnAction(e -> {
            FXGL.getSceneService().popSubScene();     // Schließt dieses Overlay
            FXGL.getGameController().startNewGame(); // Startet das Spiel neu
        });
    }
}