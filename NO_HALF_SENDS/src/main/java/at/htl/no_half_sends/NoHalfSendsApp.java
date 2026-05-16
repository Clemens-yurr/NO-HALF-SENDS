package at.htl.no_half_sends;

import at.htl.no_half_sends.components.DriftCarComponent;
import at.htl.no_half_sends.data.PlayerProfile;
import at.htl.no_half_sends.data.ProfileManager;
import at.htl.no_half_sends.ui.DriftMainMenu;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;

public class NoHalfSendsApp extends GameApplication {

    private Entity player;
    private ProfileManager profileManager;
    private PlayerProfile currentProfile;

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("NO HALF SENDS");
        gameSettings.setWidth(1920);
        gameSettings.setHeight(1080);
        gameSettings.setFullScreenAllowed(true);
        gameSettings.setFullScreenFromStart(true);
        gameSettings.setDeveloperMenuEnabled(true);
        gameSettings.setMainMenuEnabled(true);

        // Hier überschreiben wir das Hauptmenü UND das Pause-Menü!
        gameSettings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new DriftMainMenu();
            }

            // Das überschreibt das hässliche FXGL Menü im Spiel (wenn man ESC drückt)
            // Wir nutzen der Einfachheit halber fürs Erste einfach dein MainMenu dafür,
            // damit das Standard-Menü komplett weg ist!
            @Override
            public FXGLMenu newGameMenu() {
                return new DriftMainMenu();
            }
        });
    }

    @Override
    protected void onPreInit() {
        // Lade den Speicherstand BEVOR das Spiel startet
        profileManager = new ProfileManager();
        currentProfile = profileManager.loadProfile();
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("driftScore", 0);
        vars.put("speed", 0);
        // Wir setzen das Cash aus der JSON-Datei als Spiel-Variable
        vars.put("cash", currentProfile.cash);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Gas geben") {
            @Override protected void onAction() { player.getComponent(DriftCarComponent.class).up = true; }
            @Override protected void onActionEnd() { player.getComponent(DriftCarComponent.class).up = false; }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Bremsen / Rückwärts") {
            @Override protected void onAction() { player.getComponent(DriftCarComponent.class).down = true; }
            @Override protected void onActionEnd() { player.getComponent(DriftCarComponent.class).down = false; }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Links lenken") {
            @Override protected void onAction() { player.getComponent(DriftCarComponent.class).left = true; }
            @Override protected void onActionEnd() { player.getComponent(DriftCarComponent.class).left = false; }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Rechts lenken") {
            @Override protected void onAction() { player.getComponent(DriftCarComponent.class).right = true; }
            @Override protected void onActionEnd() { player.getComponent(DriftCarComponent.class).right = false; }
        }, KeyCode.D);

        // Speicher-Button (Optional, z.B. wenn man das Auto gewechselt hat)
        getInput().addAction(new UserAction("Speichern") {
            @Override protected void onActionBegin() { saveGameData(); }
        }, KeyCode.F5);
    }

    @Override
    protected void initGame() {
        setLevelFromMap("Test_MK9.tmx");

        // WICHTIG: Hier laden wir jetzt das Bild aus dem Profil und übergeben das Profil an die Physik!
        player = entityBuilder()
                .at(490, 570)
                .viewWithBBox(currentProfile.currentCar) // <- Dynamisches Auto!
                .with(new CollidableComponent(true))
                .with(new DriftCarComponent(currentProfile)) // <- Hier werden Upgrades geladen!
                .scale(0.1, 0.1)
                .rotate(90)
                .buildAndAttach();

        player.getTransformComponent().setRotationOrigin(new Point2D(player.getWidth() / 2, player.getHeight() / 2));

        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
        getGameScene().getViewport().setBounds(0, 0, 1920, 1080);
        getGameScene().getViewport().setZoom(10);
    }

    @Override
    protected void initUI() {
        Text scoreText = new Text();
        scoreText.setFont(Font.font("Arial", 48));
        scoreText.setFill(Color.WHITE);
        scoreText.setStroke(Color.BLACK);
        scoreText.setStrokeWidth(2);
        scoreText.setTranslateX(20);
        scoreText.setTranslateY(50);
        scoreText.textProperty().bind(getip("driftScore").asString("DRIFT SCORE: %d"));

        Text speedText = new Text();
        speedText.setFont(Font.font("Arial", 48));
        speedText.setFill(Color.WHITE);
        speedText.setStroke(Color.BLACK);
        speedText.setStrokeWidth(2);
        speedText.setTranslateX(20);
        speedText.setTranslateY(110);
        speedText.textProperty().bind(getip("speed").asString("KM/H: %d"));

        // NEU: Cash-Anzeige im Spiel
        Text cashText = new Text();
        cashText.setFont(Font.font("Arial", 48));
        cashText.setFill(Color.LIGHTGREEN);
        cashText.setStroke(Color.BLACK);
        cashText.setStrokeWidth(2);
        cashText.setTranslateX(20);
        cashText.setTranslateY(170);
        cashText.textProperty().bind(getip("cash").asString("CASH: $%d"));

        addUINode(scoreText);
        addUINode(speedText);
        addUINode(cashText);
    }

    // Hilfsmethode zum Speichern des Geldes
    public void saveGameData() {
        currentProfile.cash = geti("cash");
        profileManager.saveProfile(currentProfile);
    }

    public static void main(String[] args) {
        launch(args);
    }
}