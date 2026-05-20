package at.htl.no_half_sends.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import at.htl.no_half_sends.data.PlayerProfile;
import static com.almasb.fxgl.dsl.FXGL.*;

public class DriftCarComponent extends Component {

    // --- NEUE, AUSGEGLICHENE PHYSIK-BASISWERTE ---
    private double baseAcceleration = 220;
    private double baseMaxSpeed = 900;
    private double baseTurnSpeed = 200;

    // Höherer Wert (0.97) sorgt dafür, dass das Auto im Querstehen Schwung BEHÄLT
    private double baseLateralGrip = 0.97;

    private double acceleration;
    private double maxSpeed;
    private double turnSpeed;
    private double lateralGrip;
    private double drag = 0.996;

    // Erhöht, damit der Tacho trotz niedrigerer Engine-Werte coole Km/H anzeigt
    private double speedMultiplier = 2.2;

    private Point2D velocity = Point2D.ZERO;
    public boolean up, down, left, right;

    private PlayerProfile profile;
    private double internalCashCounter = 0;

    public DriftCarComponent(PlayerProfile profile) {
        this.profile = profile;
        applyUpgrades();
    }

    private void applyUpgrades() {
        // Skalierung der Autoklassen an die neuen, spielbaren Werte angepasst
        if (profile.currentCar.contains("Nissan")) {
            baseAcceleration = 220; baseMaxSpeed = 900; baseTurnSpeed = 200;
        } else if (profile.currentCar.contains("Subaru")) {
            baseAcceleration = 240; baseMaxSpeed = 950; baseTurnSpeed = 210;
        } else if (profile.currentCar.contains("Toyota")) {
            baseAcceleration = 260; baseMaxSpeed = 1000; baseTurnSpeed = 220;
        } else if (profile.currentCar.contains("Ferrari") || profile.currentCar.contains("Ferarri")) {
            baseAcceleration = 300; baseMaxSpeed = 1100; baseTurnSpeed = 240;
        } else {
            baseAcceleration = 220; baseMaxSpeed = 900; baseTurnSpeed = 200;
        }

        // Upgrades skalieren feinfühliger
        acceleration = baseAcceleration + (profile.turboLevel * 30) + (profile.intakeLevel * 10) + (profile.transmissionLevel * 20);
        maxSpeed = baseMaxSpeed + (profile.turboLevel * 80) + (profile.chassisLevel * 30);
        turnSpeed = baseTurnSpeed + (profile.differentialLevel * 15) + (profile.chassisLevel * 5);

        // Reifen verbessern die Stabilität leicht
        lateralGrip = baseLateralGrip - (profile.tiresLevel * 0.005);
    }

    @Override
    public void onUpdate(double tpf) {
        double rotation = entity.getRotation();
        Point2D forwardDir = new Point2D(-Math.cos(Math.toRadians(rotation)), -Math.sin(Math.toRadians(rotation)));

        if (up) velocity = velocity.add(forwardDir.multiply(acceleration * tpf));

        if (down) {
            double currentForwardSpeed = velocity.dotProduct(forwardDir);
            if (currentForwardSpeed > 20) {
                // Starkes Bremsen vorwärts
                velocity = velocity.subtract(forwardDir.multiply(acceleration * 3.0 * tpf));
            } else {
                // Sanftes Rückwärtsfahren
                velocity = velocity.subtract(forwardDir.multiply(acceleration * 0.5 * tpf));
            }
        }

        double currentSpeed = velocity.magnitude();

        // Tacho berechnen
        int visualKmH = (int)((currentSpeed * speedMultiplier) / 10);
        set("speed", visualKmH);

        if (currentSpeed > 10) {
            double turning = turnSpeed * tpf;
            if (velocity.normalize().dotProduct(forwardDir) < 0) turning = -turning;
            if (left) entity.rotateBy(-turning);
            if (right) entity.rotateBy(turning);
        }

        rotation = entity.getRotation();
        Point2D newForward = new Point2D(-Math.cos(Math.toRadians(rotation)), -Math.sin(Math.toRadians(rotation)));
        Point2D rightDir = new Point2D(-newForward.getY(), newForward.getX());

        double forwardVelocity = velocity.dotProduct(newForward);
        double maxReverseSpeed = maxSpeed * 0.25;

        if (forwardVelocity > maxSpeed) forwardVelocity = maxSpeed;
        if (forwardVelocity < -maxReverseSpeed) forwardVelocity = -maxReverseSpeed;

        double lateralVelocity = velocity.dotProduct(rightDir);
        lateralVelocity *= Math.pow(lateralGrip, tpf * 60);

        velocity = newForward.multiply(forwardVelocity).add(rightDir.multiply(lateralVelocity));
        velocity = velocity.multiply(Math.pow(drag, tpf * 60));

        entity.translate(velocity.multiply(tpf));

        // Drift-Score berechnen
        calculateDriftScore(tpf, visualKmH, newForward);
    }

    private void calculateDriftScore(double tpf, int visualKmH, Point2D forwardDir) {
        // Ab 25 km/h zählt der Drift (angepasst an die neue Geschwindigkeit)
        if (visualKmH > 25 && velocity.magnitude() > 50) {
            Point2D moveDir = velocity.normalize();
            double angleDiff = Math.abs(moveDir.angle(forwardDir));

            // Wenn das Auto zwischen 12 und 90 Grad quer steht, gibt es Punkte!
            if (angleDiff > 12 && angleDiff < 90) {
                int pointsEarned = (int) (angleDiff * (visualKmH / 12.0) * tpf * 8);

                if (pointsEarned > 0) {
                    inc("driftScore", pointsEarned);

                    // Geldberechnung ausführen
                    internalCashCounter += (pointsEarned / 8.0);
                    if (internalCashCounter >= 1.0) {
                        int cashToAdd = (int) internalCashCounter;
                        inc("cash", cashToAdd);
                        internalCashCounter -= cashToAdd;
                    }
                }
            }
        }
    }
}