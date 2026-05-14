package at.htl.no_half_sends;

import at.htl.no_half_sends.components.DriftCarComponent;
import at.htl.no_half_sends.data.PlayerProfile;
import at.htl.no_half_sends.data.ProfileManager;
import at.htl.no_half_sends.ui.MyMenuFactory;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
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
    private PlayerProfile currentProfile;
    private ProfileManager profileManager;

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("NO HALF SENDS");
        gameSettings.setWidth(1920);
        gameSettings.setHeight(1080);
        gameSettings.setFullScreenAllowed(true);
        gameSettings.setFullScreenFromStart(true);
        gameSettings.setDeveloperMenuEnabled(true);

        // Menü aktivieren und eigene Menü-Factory setzen
        gameSettings.setMainMenuEnabled(true);
        gameSettings.setSceneFactory(new MyMenuFactory());
    }

    @Override
    protected void onPreInit() {
        // Bevor das Spiel startet, laden wir den Speicherstand
        profileManager = new ProfileManager();
        currentProfile = profileManager.loadProfile();
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("driftScore", 0);
        vars.put("speed", 0);
        vars.put("cash", currentProfile.cash); // Cash aus dem Profil laden
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

        // Shortcut zum Speichern testen (Optional)
        getInput().addAction(new UserAction("Spiel Speichern") {
            @Override protected void onActionBegin() { saveGameData(); }
        }, KeyCode.F5);
    }

    @Override
    protected void initGame() {
        setLevelFromMap("Test_MK9.tmx");

        // Wir übergeben das aktuelle Profil an die DriftCarComponent
        player = entityBuilder()
                .at(490, 570)
                .viewWithBBox(currentProfile.currentCar)
                .with(new CollidableComponent(true))
                .with(new DriftCarComponent(currentProfile)) // <--- Hier nutzen wir das Profil für Upgrades
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
        Text scoreText = createUIElement(20, 50, "DRIFT SCORE: %d", "driftScore", Color.WHITE);
        Text speedText = createUIElement(20, 110, "KM/H: %d", "speed", Color.WHITE);
        Text cashText = createUIElement(20, 170, "CASH: $%d", "cash", Color.LIGHTGREEN); // Cash Anzeige

        addUINode(scoreText);
        addUINode(speedText);
        addUINode(cashText);
    }

    // Hilfsmethode, damit der UI Code sauberer bleibt
    private Text createUIElement(double x, double y, String format, String varName, Color color) {
        Text text = new Text();
        text.setFont(Font.font("Impact", 48)); // Impact passt gut zu Rennspielen
        text.setFill(color);
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(2);
        text.setTranslateX(x);
        text.setTranslateY(y);
        text.textProperty().bind(getip(varName).asString(format));
        return text;
    }

    // Diese Methode rufen wir auf, wenn das Rennen vorbei ist oder wir ins Menü gehen
    public void saveGameData() {
        currentProfile.cash = geti("cash"); // Aktualisiertes Geld übernehmen
        profileManager.saveProfile(currentProfile);
    }

    public static void main(String[] args) {
        launch(args);
    }
}