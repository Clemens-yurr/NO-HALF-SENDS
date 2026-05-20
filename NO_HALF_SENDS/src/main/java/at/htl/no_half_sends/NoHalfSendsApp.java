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
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
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

        gameSettings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() { return new DriftMainMenu(); }
            @Override
            public FXGLMenu newGameMenu() { return new DriftMainMenu(); }
        });
    }

    @Override
    protected void onPreInit() {
        profileManager = new ProfileManager();
        currentProfile = profileManager.loadProfile();
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("driftScore", 0);
        vars.put("speed", 0);
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

        getInput().addAction(new UserAction("Speichern") {
            @Override protected void onActionBegin() { saveGameData(); }
        }, KeyCode.F5);
    }

    @Override
    protected void initGame() {
        setLevelFromMap("FinalMap.tmx");

        // Profil frisch ladenw
        currentProfile = profileManager.loadProfile();

        // --- MAßGESCHNEIDERTE PROPORTIONEN FÜR 3/4 FAHRBAHN ---
        double carWidth = 15;  // Kompakt genug, um perfekt in die Kurven zu passen
        double carHeight = 7.5; // Korrektes 2:1 Seitenverhältnis

        // 1. Textur laden
        Texture carTexture = texture(currentProfile.currentCar);

        // 2. Pixel-Art scharf halten
        ((ImageView) carTexture.getNode()).setSmooth(false);

        // 3. Auf exakte Maße skalieren
        carTexture.setFitWidth(carWidth);
        carTexture.setFitHeight(carHeight);
        carTexture.setPreserveRatio(true);

        // 4. Spieler spawnen
        player = entityBuilder()
                .at(250, 200) // Startposition leicht angepasst für die FinalMap
                .viewWithBBox(carTexture)
                .with(new CollidableComponent(true))
                .with(new DriftCarComponent(currentProfile))
                .buildAndAttach();

        // 5. Rotations-Mittelpunkt exakt zentrieren
        player.getTransformComponent().setRotationOrigin(new Point2D(player.getWidth() / 2, player.getHeight() / 2));

        // Startrichtung ausrichten
        player.setRotation(90);

        // Kamera-Setup
        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);

        // Grenzen der Kamera basierend auf deiner 20x20 Tiles Map (20 * 64 = 1280)
        getGameScene().getViewport().setBounds(0, 0, 1280, 1280);

        // --- STARKER CAMERA-ZOOM ---
        // Holt die Map extrem nah ran, damit das kleine Auto perfekt lesbar bleibt!
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

    public void saveGameData() {
        currentProfile.cash = geti("cash");
        profileManager.saveProfile(currentProfile);
    }

    public static void main(String[] args) {
        launch(args);
    }
}