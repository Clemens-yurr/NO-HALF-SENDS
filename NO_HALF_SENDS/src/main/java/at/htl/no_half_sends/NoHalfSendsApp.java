package at.htl.no_half_sends;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
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
        int speed = 5;


        onKey(KeyCode.W, () -> {
            player.translateY(-speed);
        }
        );
        onKey(KeyCode.S, () -> {
            player.translateY(speed);
            player.rotateBy(Math.toRadians(0));
        }
        );
        onKey(KeyCode.A, () -> {
            player.translateY(speed);
            player.rotateBy(Math.toRadians(135));
        }
        );
        onKey(KeyCode.D, () -> {
            player.translateY(speed);
            player.rotateBy(Math.toRadians(45));
        }
        );
    }

    @Override
    protected void initGame() {
        // Map laden
        setLevelFromMap("Test_MK2.tmx");

        // 2. Das Auto als Entity erstellen und hinzufügen
        player = entityBuilder()
                .at(1150, 400)
                .view("Nissan_GTR_R35_mk2.png") // Bild wird automatisch aus src/main/resources/assets/textures/ geladen
                .with(new CollidableComponent(true))
                .scale(4.0, 4.0)
                .rotate(90)
                .buildAndAttach();

        // 3. Kamera-Fokus: Setzt das Auto in die Mitte und bewegt die Map
        // -> Das ist die Zeile, die dafür sorgt, dass sich die Welt "mitbewegt"!
        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);

        // Optional: Kamera-Grenzen an die Größe deiner Map anpassen (hier z.B. 2000x2000)
        getGameScene().getViewport().setBounds(0, 0, 2000, 2000);
    }

    public static void main(String[] args) {
        launch(args);
    }
}