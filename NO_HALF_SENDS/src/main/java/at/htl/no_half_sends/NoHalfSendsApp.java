package at.htl.no_half_sends;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.*;

public class NoHalfSendsApp extends GameApplication {

    private Entity player;

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("NO HALF SENDS");
        gameSettings.setWidth(1920);
        gameSettings.setHeight(1080);
        gameSettings.setFullScreenAllowed(true);
        gameSettings.setFullScreenFromStart(true);
        gameSettings.setDeveloperMenuEnabled(true);
    }

    @Override
    protected void initInput() {
        double speed = 8;         // Wie schnell das Auto fährt
        double turnSpeed = 3.5;   // Wie schnell das Auto lenkt

        // FIX: Der Wert wurde von -90 auf 0 geändert.
        // Wenn das Auto jetzt beim Drücken von 'W' genau rückwärts fährt, ändere die 0 auf -180 !
        double angleOffset = 0;

        // W = Vorwärts fahren
        onKey(KeyCode.S, () -> {
            // Berechne die genaue Blickrichtung mit Sinus und Cosinus
            double angle = Math.toRadians(player.getRotation() + angleOffset);
            player.translateX(Math.cos(angle) * speed);
            player.translateY(Math.sin(angle) * speed);
        });

        // S = Rückwärts fahren
        onKey(KeyCode.W, () -> {
            double angle = Math.toRadians(player.getRotation() + angleOffset);
            // Rückwärts ist meistens langsamer, daher speed * 0.5
            player.translateX(-Math.cos(angle) * (speed * 0.5));
            player.translateY(-Math.sin(angle) * (speed * 0.5));
        });

        // A = Nach links lenken
        onKey(KeyCode.A, () -> {
            player.rotateBy(-turnSpeed);
        });

        // D = Nach rechts lenken
        onKey(KeyCode.D, () -> {
            player.rotateBy(turnSpeed);
        });
    }

    @Override
    protected void initGame() {
        // Map laden
        setLevelFromMap("Test_MK2.tmx");

        // Das Auto als Entity erstellen und hinzufügen
        player = entityBuilder()
                .at(1150, 400)
                .view("Nissan_GTR_R35_mk2.png")
                .with(new CollidableComponent(true))
                .scale(4.0, 4.0)
                .rotate(90) // Setzt die anfängliche Drehung des Autos
                .buildAndAttach();

        // WICHTIG: Setzt den Drehpunkt genau in die Mitte des Bildes!
        player.getTransformComponent().setRotationOrigin(new Point2D(player.getWidth() / 2, player.getHeight() / 2));

        // Kamera-Fokus: Setzt das Auto in die Mitte und bewegt die Map
        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);

        // Kamera-Grenzen an die Größe deiner Map anpassen
        getGameScene().getViewport().setBounds(0, 0, 2000, 2000);
    }

    public static void main(String[] args) {
        launch(args);
    }
}