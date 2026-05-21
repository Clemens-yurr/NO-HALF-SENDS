package at.htl.no_half_sends.components;

import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import at.htl.no_half_sends.data.PlayerProfile;
import static com.almasb.fxgl.dsl.FXGL.*;

public class DriftCarComponent extends Component {

    private double baseAcceleration = 20;
    private double baseMaxSpeed = 200;
    private double baseTurnSpeed = 110;


    private double baseLateralGrip = 0.94; // sorgt dafür das das auto quer speed behält

    private double acceleration;
    private double maxSpeed;
    private double turnSpeed;
    private double lateralGrip;
    private double drag = 0.995;

    private double speedMultiplier = 4.4; //erhöht den tacho damit es realistisch wirkt

    private Point2D velocity = Point2D.ZERO;
    public boolean up, down, left, right;

    private PlayerProfile profile;
    private double internalCashCounter = 0;

    public DriftCarComponent(PlayerProfile profile) {
        this.profile = profile;
        applyUpgrades();
    }

    private void applyUpgrades() { //werte für die verschiedenen autos festlegen
        if (profile.currentCar.contains("Nissan")) {
            baseAcceleration = 140; baseMaxSpeed = 900; baseTurnSpeed = 170;
        } else if (profile.currentCar.contains("Subaru")) {
            baseAcceleration = 150; baseMaxSpeed = 950; baseTurnSpeed = 180;
        } else if (profile.currentCar.contains("Toyota")) {
            baseAcceleration = 160; baseMaxSpeed = 1000; baseTurnSpeed = 190;
        } else if (profile.currentCar.contains("Ferrari") || profile.currentCar.contains("Ferarri")) { // weil ichs paar mal falsch geschrieben hab
            baseAcceleration = 180; baseMaxSpeed = 1100; baseTurnSpeed = 200;
        } else {
            baseAcceleration = 140; baseMaxSpeed = 900; baseTurnSpeed = 200;
        }

        acceleration = baseAcceleration + (profile.turboLevel * 30) + (profile.intakeLevel * 10) + (profile.transmissionLevel * 20); // für die upgrades
        maxSpeed = baseMaxSpeed + (profile.turboLevel * 80) + (profile.chassisLevel * 30);
        turnSpeed = baseTurnSpeed + (profile.differentialLevel * 15) + (profile.chassisLevel * 5);

        lateralGrip = baseLateralGrip - (profile.tiresLevel * 0.005); // reifen
    }

    @Override
    public void onUpdate(double tpf) { // ist für die physik zuständig
        double rotation = entity.getRotation();
        Point2D forwardDir = new Point2D(-Math.cos(Math.toRadians(rotation)), -Math.sin(Math.toRadians(rotation)));   // drift feeling

        if (up) velocity = velocity.add(forwardDir.multiply(acceleration * tpf)); // gas geben wenn up true ist

        if (down) { // bremsen
            double currentForwardSpeed = velocity.dotProduct(forwardDir);
            if (currentForwardSpeed > 20) {
                velocity = velocity.subtract(forwardDir.multiply(acceleration * 1.5 * tpf));// mal 1.5 damit es gscheit bremst
            } else {
                velocity = velocity.subtract(forwardDir.multiply(acceleration * 0.5 * tpf));// wenn man unter 20 fährt bremst er nicht so schnell
            }
        }

        double currentSpeed = velocity.magnitude(); // .magnitude wandelt zweidimensionalen vektor in eine ganz normale zahl um

        int visualKmH = (int)((currentSpeed * speedMultiplier) / 10); // der angezeigte speed soll realistisch sein damit ein ferarri auch 300 geht und nicht 30
        set("speed", visualKmH);

        if (currentSpeed > 10) { // macht das man im stehen nicht lenken kann
            double turning = turnSpeed * tpf;
            if (velocity.normalize().dotProduct(forwardDir) < 0) turning = -turning; // damit das lenken invertiert ist
            if (left) entity.rotateBy(-turning);
            if (right) entity.rotateBy(turning);
        }

        rotation = entity.getRotation();
        //rechnet winkel um in werte; - ist dafür da das es auch wirklich die optische spiel richtung ist
        Point2D newForward = new Point2D(-Math.cos(Math.toRadians(rotation)), -Math.sin(Math.toRadians(rotation))); // damit das spiel weiß wo vorne und rechts ist
        Point2D rightDir = new Point2D(-newForward.getY(), newForward.getX());

        double forwardVelocity = velocity.dotProduct(newForward);// rechnet durch skalarprodukt wie schnell das auto nach vorn fährt
        double maxReverseSpeed = maxSpeed * 0.10; // damit man rückwärts nicht so schnell ist

        if (forwardVelocity > maxSpeed) forwardVelocity = maxSpeed; // topspeed begrenzen damit man nicht 500 fährt
        if (forwardVelocity < -maxReverseSpeed) forwardVelocity = -maxReverseSpeed; // rückwärtsspeed begrenzen

        double lateralVelocity = velocity.dotProduct(rightDir); // qher geschwindigkeit fürs drift feeling
        lateralVelocity *= Math.pow(lateralGrip, tpf * 60);

        velocity = newForward.multiply(forwardVelocity).add(rightDir.multiply(lateralVelocity)); // finale geschwindigkeit berechnen
        velocity = velocity.multiply(Math.pow(drag, tpf * 60)); // damit man rutscht

        entity.translate(velocity.multiply(tpf)); // bewegt auto auf dem bildschrim

        calculateDriftScore(tpf, visualKmH, newForward); // driftscore ausrechnen
    }

    private void calculateDriftScore(double tpf, int visualKmH, Point2D forwardDir) { // parameter für den driftscore mitgeben
        if (visualKmH > 25 && velocity.magnitude() > 50) { // wenn man schneller wie 25 kmh fährt kann man drift punkte bekommen
            Point2D moveDir = velocity.normalize(); // berechnet aus geschwindigkeits vektor einheitsvektor
            double angleDiff = Math.abs(moveDir.angle(forwardDir)); // rechnet den drift aus

            if (angleDiff > 12 && angleDiff < 90) { // wenn man über 12 grad drift winkel hat
                int pointsEarned = (int) (angleDiff * (visualKmH / 12.0) * tpf * 8); // punkte ausrechnen aus anglediff und kmh

                if (pointsEarned > 0) {// geld ausrechnen
                    inc("driftScore", pointsEarned);

                    internalCashCounter += (pointsEarned / 8.0); // durch 8 damit aus score geld wird
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