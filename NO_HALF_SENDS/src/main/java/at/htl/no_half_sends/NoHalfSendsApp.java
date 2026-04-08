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
        gameSettings.setFullScreenFromStart(true); // Hier das Kommentarzeichen entfernt für Fullscreen!
        gameSettings.setDeveloperMenuEnabled(true);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        // Hier speichern wir die Punkte, die während des Drifts gesammelt werden
        vars.put("driftScore", 0);
    }

    @Override
    protected void initInput() {
        // WICHTIG: Um flüssige Bewegungen zu haben, nutzen wir UserActions anstatt einfachem onKey().
        // Dadurch wissen wir in jedem Frame genau, ob die Taste gerade gedrückt gehalten wird.

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
        // Map laden
        setLevelFromMap("Test_MK6.tmx");

        // Das Auto als Entity erstellen und hinzufügen
        player = entityBuilder()
                .at(1150, 400)
                .viewWithBBox("Nissan_GTR_R35_mk2.png") // viewWithBBox generiert direkt die Hitbox aus dem Bild
                .with(new CollidableComponent(true))
                // Hier fügen wir unsere neue Fahrphysik-Komponente hinzu!
                .with(new DriftCarComponent())
                .scale(4.0, 4.0)
                .rotate(90)
                .buildAndAttach();

        // Drehpunkt in die Mitte setzen
        player.getTransformComponent().setRotationOrigin(new Point2D(player.getWidth() / 2, player.getHeight() / 2));

        // Kamera-Fokus
        getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
        getGameScene().getViewport().setBounds(0, 0, 4000, 4000); // Passe diese Werte an die tatsächliche Pixel-Größe deiner Map an
    }

    @Override
    protected void initUI() {
        // Ein einfaches UI-Element, das den Drift Score oben links anzeigt
        Text scoreText = new Text();
        scoreText.setFont(Font.font("Arial", 48));
        scoreText.setFill(Color.WHITE);
        scoreText.setStroke(Color.BLACK);
        scoreText.setStrokeWidth(2);
        scoreText.setTranslateX(50);
        scoreText.setTranslateY(80);

        // Verbindet den Text automatisch mit der Variable "driftScore"
        scoreText.textProperty().bind(getip("driftScore").asString("DRIFT SCORE: %d"));

        addUINode(scoreText);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // =========================================================================================
    // DIE MAGIE PASSIERT HIER: Eine Custom Component für die Drift-Physik
    // =========================================================================================
    public static class DriftCarComponent extends Component {

        // --- AUTO STATS (Diese kannst du später für Upgrades oder andere Autos anpassen!) ---
        private double acceleration = 1200; // Wie schnell das Auto beschleunigt
        private double maxSpeed = 1000;     // Höchstgeschwindigkeit
        private double turnSpeed = 160;     // Lenkgeschwindigkeit (Grad pro Sekunde)
        private double drag = 0.98;         // Luftwiderstand (verlangsamt das Auto generell)

        // DER WICHTIGSTE WERT FÜRS DRIFTEN:
        // 1.0 = Fährt wie auf Schienen (kein Drift).
        // 0.85 = Rutscht stark (Eis).
        // 0.92 = Guter Arcade-Drift!
        private double lateralGrip = 0.94;

        // --- INTERNE VARIABLEN ---
        private Point2D velocity = Point2D.ZERO; // Der aktuelle Bewegungsvektor
        public boolean up, down, left, right;    // Wird durch UserActions gesteuert

        @Override
        public void onUpdate(double tpf) {
            // 1. Aktuelle Blickrichtung des Autos berechnen
            double rotation = entity.getRotation();
            Point2D forwardDir = new Point2D(Math.cos(Math.toRadians(rotation)), Math.sin(Math.toRadians(rotation)));

            // 2. Gas geben und Bremsen (Geschwindigkeit zum Vektor hinzufügen)
            if (up) {
                velocity = velocity.add(forwardDir.multiply(acceleration * tpf));
            }
            if (down) {
                // Bremsen / Rückwärts ist meist etwas schwächer
                velocity = velocity.subtract(forwardDir.multiply(acceleration * 0.6 * tpf));
            }

            double currentSpeed = velocity.magnitude();

            // 3. Lenkung (Auto dreht sich nur, wenn es sich bewegt)
            if (currentSpeed > 10) {
                double turning = turnSpeed * tpf;

                // Realismus-Check: Wenn wir rückwärts fahren, muss die Lenkung invertiert werden
                if (velocity.normalize().dotProduct(forwardDir) < 0) {
                    turning = -turning;
                }

                if (left) entity.rotateBy(-turning);
                if (right) entity.rotateBy(turning);
            }

            // 4. DRIFT PHYSIK: Vektor in Vorwärts- und Seitwärts-Bewegung aufteilen
            // (Nach dem Lenken brauchen wir die neue Blickrichtung)
            rotation = entity.getRotation();
            Point2D newForward = new Point2D(Math.cos(Math.toRadians(rotation)), Math.sin(Math.toRadians(rotation)));
            Point2D rightDir = new Point2D(-newForward.getY(), newForward.getX()); // Vektor der 90 Grad nach rechts zeigt

            // Projiziere die aktuelle Geschwindigkeit auf die Vorwärts- und Seitwärts-Achse
            double forwardVelocity = velocity.dotProduct(newForward);
            double lateralVelocity = velocity.dotProduct(rightDir);

            // Grip anwenden: Wir reduzieren die seitliche Geschwindigkeit in jedem Frame.
            // Je niedriger lateralGrip, desto länger bleibt die seitliche Bewegung erhalten = Drift!
            lateralVelocity *= Math.pow(lateralGrip, tpf * 60);

            // Setze den Vektor aus den bereinigten Werten wieder zusammen
            velocity = newForward.multiply(forwardVelocity).add(rightDir.multiply(lateralVelocity));

            // 5. Luftwiderstand (generelles Ausrollen)
            velocity = velocity.multiply(Math.pow(drag, tpf * 60));

            // 6. Max Speed Limitierung
            if (velocity.magnitude() > maxSpeed) {
                velocity = velocity.normalize().multiply(maxSpeed);
            }

            // 7. Bewegung auf die Entity anwenden
            entity.translate(velocity.multiply(tpf));

            // 8. DRIFT SCORE BERECHNUNG
            calculateDriftScore(tpf, currentSpeed, newForward);
        }

        private void calculateDriftScore(double tpf, double currentSpeed, Point2D forwardDir) {
            // Nur driften, wenn man schnell genug ist
            if (currentSpeed > 200) {
                // Berechne den Winkel zwischen Fahrtrichtung und Blickrichtung
                Point2D moveDir = velocity.normalize();
                double angleDiff = Math.abs(moveDir.angle(forwardDir));

                // Wenn der Winkel zwischen 15° und 160° liegt (auto rutscht quer)
                if (angleDiff > 15 && angleDiff < 160) {
                    // Punkte basierend auf Geschwindigkeit und Drift-Winkel generieren
                    int pointsEarned = (int) (angleDiff * (currentSpeed / 100.0) * tpf * 5);
                    inc("driftScore", pointsEarned);
                }
            }
        }
    }
}