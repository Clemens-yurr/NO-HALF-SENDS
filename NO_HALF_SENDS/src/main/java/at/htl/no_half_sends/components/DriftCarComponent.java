package at.htl.no_half_sends.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import at.htl.no_half_sends.data.PlayerProfile;
import static com.almasb.fxgl.dsl.FXGL.*;

public class DriftCarComponent extends Component {

    private double baseAcceleration = 150;
    private double baseMaxSpeed = 800;
    private double baseTurnSpeed = 180;
    private double baseLateralGrip = 0.95;

    private double acceleration;
    private double maxSpeed;
    private double turnSpeed;
    private double lateralGrip;
    private double drag = 0.998;
    private double displayMultiplier = 2.2;

    private Point2D velocity = Point2D.ZERO;
    public boolean up, down, left, right;

    private PlayerProfile profile;

    // NEU: Zählt die Nachkommastellen für das Geld mit, damit nichts verloren geht!
    private double internalCashCounter = 0;

    public DriftCarComponent(PlayerProfile profile) {
        this.profile = profile;
        applyUpgrades();
    }

    private void applyUpgrades() {
        if (profile.currentCar.contains("Nissan")) {
            baseAcceleration = 150; baseMaxSpeed = 800; baseTurnSpeed = 180;
        } else if (profile.currentCar.contains("Subaru")) {
            baseAcceleration = 180; baseMaxSpeed = 950; baseTurnSpeed = 190;
        } else if (profile.currentCar.contains("Toyota")) {
            baseAcceleration = 220; baseMaxSpeed = 1100; baseTurnSpeed = 200;
        } else if (profile.currentCar.contains("Ferrari")) {
            baseAcceleration = 280; baseMaxSpeed = 1300; baseTurnSpeed = 220;
        } else {
            baseAcceleration = 150; baseMaxSpeed = 800; baseTurnSpeed = 180;
        }

        acceleration = baseAcceleration + (profile.turboLevel * 30) + (profile.intakeLevel * 10) + (profile.transmissionLevel * 20);
        maxSpeed = baseMaxSpeed + (profile.turboLevel * 150) + (profile.chassisLevel * 50);
        turnSpeed = baseTurnSpeed + (profile.differentialLevel * 20) + (profile.chassisLevel * 5);
        lateralGrip = baseLateralGrip - (profile.tiresLevel * 0.015);
    }

    @Override
    public void onUpdate(double tpf) {
        double rotation = entity.getRotation();
        Point2D forwardDir = new Point2D(-Math.cos(Math.toRadians(rotation)), -Math.sin(Math.toRadians(rotation)));

        if (up) velocity = velocity.add(forwardDir.multiply(acceleration * tpf));
        if (down) velocity = velocity.subtract(forwardDir.multiply(acceleration * 4.0 * tpf));

        double currentSpeed = velocity.magnitude();
        set("speed", (int)((currentSpeed * displayMultiplier) / 10));

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
        double lateralVelocity = velocity.dotProduct(rightDir);
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

            if (angleDiff > 15 && angleDiff < 90) {
                int pointsEarned = (int) (angleDiff * (visualSpeed / 100.0) * tpf * 5);

                if (pointsEarned > 0) {
                    inc("driftScore", pointsEarned);

                    // NEU: Kommazahlen sammeln, damit kein Geld verschwindet
                    internalCashCounter += (pointsEarned / 10.0);
                    if (internalCashCounter >= 1.0) {
                        int cashToAdd = (int) internalCashCounter;
                        inc("cash", cashToAdd);
                        internalCashCounter -= cashToAdd; // Restbetrag behalten
                    }
                }
            }
        }
    }
}