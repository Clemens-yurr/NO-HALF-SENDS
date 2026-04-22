package at.htl.no_half_sends;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
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
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("driftScore", 0);
        vars.put("speed", 0);
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
    }

    @Override
    protected void initGame() {
        setLevelFromMap("Test_MK9.tmx");

        player = entityBuilder()
                .at(1150, 400)
                .viewWithBBox("Nissan_GTR_R35_mk2.png")
                .with(new CollidableComponent(true))
                .with(new DriftCarComponent())
                .scale(0.1, 0.1)
                .rotate(90)
                .buildAndAttach();

        player.getTransformComponent().setRotationOrigin(new Point2D(player.getWidth() / 2, player.getHeight() / 2));

        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
        getGameScene().getViewport().setBounds(0, 0, 4000, 4000);

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

        addUINode(scoreText);
        addUINode(speedText);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // =========================================================================================
    // DRIFT PHYSIK COMPONENT
    // =========================================================================================
    public static class DriftCarComponent extends Component {

        // --- REALISTIC TUNED NISSAN GT-R R35 SPECS ---
        private double acceleration = 450;  // Realistischer Schub, kein Arcade-Raketenantrieb
        private double maxSpeed = 3300;     // 330 km/h
        private double turnSpeed = 260;
        private double drag = 0.998;        // Fast kein Rollwiderstand, erzeugt die perfekte Asymptoten-Kurve
        private double lateralGrip = 0.90;

        private Point2D velocity = Point2D.ZERO;
        public boolean up, down, left, right;

        @Override
        public void onUpdate(double tpf) {
            double rotation = entity.getRotation();

            Point2D forwardDir = new Point2D(-Math.cos(Math.toRadians(rotation)), -Math.sin(Math.toRadians(rotation)));

            if (up) {
                velocity = velocity.add(forwardDir.multiply(acceleration * tpf));
            }
            if (down) {
                // Carbon-Keramik-Bremsen! Da der Basis-Schub (450) jetzt kleiner ist,
                // multiplizieren wir die Bremse mit 4.0, damit das Auto stark verzögert.
                velocity = velocity.subtract(forwardDir.multiply(acceleration * 4.0 * tpf));
            }

            double currentSpeed = velocity.magnitude();

            set("speed", (int)(currentSpeed / 10));

            if (currentSpeed > 10) {
                double turning = turnSpeed * tpf;

                if (velocity.normalize().dotProduct(forwardDir) < 0) {
                    turning = -turning;
                }

                if (left) entity.rotateBy(-turning);
                if (right) entity.rotateBy(turning);
            }

            rotation = entity.getRotation();

            Point2D newForward = new Point2D(-Math.cos(Math.toRadians(rotation)), -Math.sin(Math.toRadians(rotation)));
            Point2D rightDir = new Point2D(-newForward.getY(), newForward.getX());

            double forwardVelocity = velocity.dotProduct(newForward);
            double lateralVelocity = velocity.dotProduct(rightDir);

            lateralVelocity *= Math.pow(lateralGrip, tpf * 60);

            velocity = newForward.multiply(forwardVelocity).add(rightDir.multiply(lateralVelocity));

            velocity = velocity.multiply(Math.pow(drag, tpf * 60));

            if (velocity.magnitude() > maxSpeed) {
                velocity = velocity.normalize().multiply(maxSpeed);
            }

            entity.translate(velocity.multiply(tpf));

            calculateDriftScore(tpf, currentSpeed, newForward);
        }

        private void calculateDriftScore(double tpf, double currentSpeed, Point2D forwardDir) {
            if (currentSpeed > 200) {
                Point2D moveDir = velocity.normalize();
                double angleDiff = Math.abs(moveDir.angle(forwardDir));

                if (angleDiff > 15 && angleDiff < 160) {
                    int pointsEarned = (int) (angleDiff * (currentSpeed / 100.0) * tpf * 5);
                    inc("driftScore", pointsEarned);
                }
            }
        }
    }
}