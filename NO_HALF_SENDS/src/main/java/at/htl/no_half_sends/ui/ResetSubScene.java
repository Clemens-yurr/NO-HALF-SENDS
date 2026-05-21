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
        Rectangle bg = new Rectangle(1920, 1080, Color.color(0.05, 0.05, 0.05, 0.85)); // ein rechteck das so groß ist wie der screen

        Text titleText = new Text("VON DER STRASSE ABGEKOMMEN!"); // anzeige
        titleText.setFont(Font.font("Impact", 60));
        titleText.setFill(Color.ORANGERED);
        titleText.setStroke(Color.WHITE);
        titleText.setStrokeWidth(1);

        Text timerText = new Text("Zurück zum Start in: 3s"); // anzeige wie lange man warten muss
        timerText.setFont(Font.font("Arial", 36));
        timerText.setFill(Color.WHITE);

        Button btnReturn = new Button("RETURN TO START"); // button zum resetten
        btnReturn.setFont(Font.font("Arial", 24));
        btnReturn.setStyle("-fx-background-color: #333; -fx-text-fill: #777; -fx-padding: 10 40 10 40;");
        btnReturn.setDisable(true); // damit man 3s warten muss

        VBox box = new VBox(30, titleText, timerText, btnReturn); // wird alles in die vbox gemacht in die mitte
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(1920);
        box.setPrefHeight(1080);

        getContentRoot().getChildren().addAll(bg, box);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> { // methode das man 3s warten muss (nicht selber gemacht)
            secondsLeft--;
            if (secondsLeft > 0) {// wenn die 3s noch nicht fertig sind
                timerText.setText("Zurück zum Start in: " + secondsLeft + "s");
            } else {
                timerText.setText("Bereit für den nächsten Versuch!"); // wenn man seine zeit abgewartet hat
                timerText.setFill(Color.LIGHTGREEN);

                btnReturn.setDisable(false); // button wird aktiviert
                btnReturn.setStyle("-fx-background-color: #ff4500; -fx-text-fill: white; -fx-padding: 10 40 10 40; -fx-cursor: hand;");
            }
        }));
        timeline.setCycleCount(3);
        timeline.play();

        btnReturn.setOnAction(e -> { // zum start zurück und neues game starten
            FXGL.getSceneService().popSubScene();
            FXGL.getGameController().startNewGame();
        });
    }
}