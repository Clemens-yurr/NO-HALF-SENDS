package at.htl.no_half_sends.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import at.htl.no_half_sends.data.PlayerProfile;
import static com.almasb.fxgl.dsl.FXGL.*;

public class DriftCarComponent extends Component {

    // Basis-Werte (Schwer zu driften, langsam)
    private double baseAcceleration = 150;
    private double baseMaxSpeed = 800;
    private double baseTurnSpeed = 180;
    private double baseLateralGrip = 0.95; // Sehr rutschig am Anfang

    // Aktuelle Werte (nach Upgrades)
    private double acceleration;
    private double maxSpeed;
    private double turnSpeed;
    private double lateralGrip;
    private double drag = 0.998;
    private double displayMultiplier = 2.2;

    private Point2D velocity = Point2D.ZERO;
    public boolean up, down, left, right;

    private PlayerProfile profile;

    public DriftCarComponent(PlayerProfile profile) {
        this.profile = profile;
        applyUpgrades();
    }

    private void applyUpgrades() {
        // Turbo macht das Auto schneller
        acceleration = baseAcceleration + (profile.turboLevel * 50) + (profile.intakeLevel * 10);
        maxSpeed = baseMaxSpeed + (profile.turboLevel * 250);

        // Differential macht das Lenken und Driften aggressiver
        turnSpeed = baseTurnSpeed + (profile.differentialLevel * 30);

        // Reifen verbessern den Grip (weniger Rutschen = kontrollierbarerer Drift)
        // Je kleiner der LateralGrip Wert (z.B. 0.90 statt 0.95), desto MEHR Grip hat das Auto quer!
        lateralGrip = baseLateralGrip - (profile.tiresLevel * 0.02);
    }

    @Override
    public void onUpdate(double tpf) {
        double rotation = entity.getRotation();
        Point2D forwardDir = new Point2D(-Math.cos(Math.toRadians(rotation)), -Math.sin(Math.toRadians(rotation)));

        // Beschleunigung
        if (up) velocity = velocity.add(forwardDir.multiply(acceleration * tpf));
        if (down) velocity = velocity.subtract(forwardDir.multiply(acceleration * 4.0 * tpf));

        double currentSpeed = velocity.magnitude();
        set("speed", (int)((currentSpeed * displayMultiplier) / 10));

        // Lenken (nur wenn das Auto rollt)
        if (currentSpeed > 10) {
            double turning = turnSpeed * tpf;
            // Wenn man rückwärts fährt, Lenkung invertieren
            if (velocity.normalize().dotProduct(forwardDir) < 0) turning = -turning;

            if (left) entity.rotateBy(-turning);
            if (right) entity.rotateBy(turning);
        }

        // Drift-Physik anwenden
        rotation = entity.getRotation();
        Point2D newForward = new Point2D(-Math.cos(Math.toRadians(rotation)), -Math.sin(Math.toRadians(rotation)));
        Point2D rightDir = new Point2D(-newForward.getY(), newForward.getX());

        double forwardVelocity = velocity.dotProduct(newForward);
        double lateralVelocity = velocity.dotProduct(rightDir);

        // Lateraler Grip wendet an, wie stark das Auto zur Seite rutscht
        lateralVelocity *= Math.pow(lateralGrip, tpf * 60);

        velocity = newForward.multiply(forwardVelocity).add(rightDir.multiply(lateralVelocity));
        velocity = velocity.multiply(Math.pow(drag, tpf * 60));

        if (velocity.magnitude() > maxSpeed) {
            velocity = velocity.normalize().multiply(maxSpeed);
        }

        entity.translate(velocity.multiply(tpf));
        calculateDriftScore(tpf, currentSpeed * displayMultiplier, newForward);
    }

    private void calculateDriftScore(double tpf, double visualSpeed, Point2D forwardDir) {
        if (visualSpeed > 200) {
            Point2D moveDir = velocity.normalize();
            double angleDiff = Math.abs(moveDir.angle(forwardDir));

            // Drift Winkel zwischen 15 und 90 Grad gibt Punkte
            if (angleDiff > 15 && angleDiff < 90) {
                int pointsEarned = (int) (angleDiff * (visualSpeed / 100.0) * tpf * 5);
                inc("driftScore", pointsEarned);

                // Geld direkt mit dem Score verknüpfen
                inc("cash", pointsEarned / 10);
            }
        }
    }
}