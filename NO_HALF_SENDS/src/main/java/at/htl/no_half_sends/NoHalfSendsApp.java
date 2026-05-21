package at.htl.no_half_sends;

import at.htl.no_half_sends.components.DriftCarComponent;
import at.htl.no_half_sends.data.PlayerProfile;
import at.htl.no_half_sends.data.ProfileManager;
import at.htl.no_half_sends.ui.DriftMainMenu;
import at.htl.no_half_sends.ui.GameEntityFactory;
import at.htl.no_half_sends.ui.GameType;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
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
            public FXGLMenu newMainMenu() { return new DriftMainMenu(); }   // tauscht standard menü durch driftmainmenu aus
            @Override
            public FXGLMenu newGameMenu() { return new DriftMainMenu(); } // tauscht pause menu aus
        });
    }

    @Override
    protected void onPreInit() { //json wird ausglesen
        profileManager = new ProfileManager();
        currentProfile = profileManager.loadProfile();
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {  //spiel variablen in map
        vars.put("driftScore", 0);
        vars.put("speed", 0);
        vars.put("cash", currentProfile.cash);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Gas geben") {
            @Override protected void onAction() {
                player.getComponent(DriftCarComponent.class).up = true; // wenn man die taste hält und wieder loslässt
            }
            @Override protected void onActionEnd() {
                player.getComponent(DriftCarComponent.class).up = false;
            }
        }, KeyCode.W); //mit w gas geben

        getInput().addAction(new UserAction("Bremsen / Rückwärts") {
            @Override protected void onAction() {
                player.getComponent(DriftCarComponent.class).down = true;
            }
            @Override protected void onActionEnd() {
                player.getComponent(DriftCarComponent.class).down = false;
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Links lenken") {
            @Override protected void onAction() {
                player.getComponent(DriftCarComponent.class).left = true;
            }
            @Override protected void onActionEnd() {
                player.getComponent(DriftCarComponent.class).left = false;
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Rechts lenken") {
            @Override protected void onAction() {
                player.getComponent(DriftCarComponent.class).right = true;
            }
            @Override protected void onActionEnd() {
                player.getComponent(DriftCarComponent.class).right = false;
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("Speichern") { // speichern
            @Override protected void onActionBegin() { saveGameData(); }
        }, KeyCode.F5);
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameEntityFactory()); // Entity factory
        setLevelFromMap("FinalMap.tmx");

        currentProfile = profileManager.loadProfile(); // profil laden

        double carWidth = 15; // auto größe
        double carHeight = 7.5;

        Texture carTexture = texture(currentProfile.currentCar); // auto laden
        ((ImageView) carTexture.getNode()).setSmooth(false); // damit es pixelig ist aber nicht verschwommen

        carTexture.setFitWidth(carWidth);
        carTexture.setFitHeight(carHeight);
        carTexture.setPreserveRatio(true);

        player = entityBuilder() // player wird geladen
                .type(GameType.PLAYER)
                .at(925, 661) //spawnpunkt
                .viewWithBBox(carTexture)
                .with(new CollidableComponent(true))
                .with(new DriftCarComponent(currentProfile))
                .buildAndAttach();

        player.getTransformComponent().setRotationOrigin(new Point2D(player.getWidth() / 2, player.getHeight() / 2)); // sorgt dafür das sih das auto um die eigene mitte dreht
        player.setRotation(90); // damit das auto nach oben schaut

        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0); // sorgt dafür das die kamera dem auto folgt aber nicht über die map hinaus schaut
        getGameScene().getViewport().setBounds(0, 0, 1280, 1280); // grenzen für kamera bewegung
        getGameScene().getViewport().setZoom(10); // in die map zoomen
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.PLAYER, GameType.BORDER) { // macht kollision zwischen border und player
            @Override
            protected void onCollisionBegin(Entity player, Entity border) {
                saveGameData(); // damit keine data verloren geht

                getSceneService().pushSubScene(new at.htl.no_half_sends.ui.ResetSubScene()); // ruft das fenster auf wenn man hinaus fährt
            }
        });
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
        scoreText.textProperty().bind(getip("driftScore").asString("DRIFT SCORE: %d")); // für den drift score text

        Text speedText = new Text();
        speedText.setFont(Font.font("Arial", 48));
        speedText.setFill(Color.WHITE);
        speedText.setStroke(Color.BLACK);
        speedText.setStrokeWidth(2);
        speedText.setTranslateX(20);
        speedText.setTranslateY(110);
        speedText.textProperty().bind(getip("speed").asString("KM/H: %d")); // für die kmh

        Text cashText = new Text();
        cashText.setFont(Font.font("Arial", 48));
        cashText.setFill(Color.LIGHTGREEN);
        cashText.setStroke(Color.BLACK);
        cashText.setStrokeWidth(2);
        cashText.setTranslateX(20);
        cashText.setTranslateY(170);
        cashText.textProperty().bind(getip("cash").asString("CASH: $%d")); // geld

        addUINode(scoreText); // projeziert auf die UI
        addUINode(speedText);
        addUINode(cashText);
    }

    public void saveGameData() { // damit die werte gespeichert werden
        currentProfile.cash = geti("cash");
        profileManager.saveProfile(currentProfile);
    }

    public static void main(String[] args) {
        launch(args);
    }
}