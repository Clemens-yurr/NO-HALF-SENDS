package at.htl.no_half_sends.data;

public class PlayerProfile {
    public int cash = 0;
    public int highestScore = 0;

    // Upgrade Level (0 = Stock, 1-3 = Upgraded)
    public int turboLevel = 0;       // Erhöht Acceleration & Max Speed
    public int differentialLevel = 0; // Erhöht Turn Speed (leichteres Einleiten von Drifts)
    public int tiresLevel = 0;       // Grip (Spikes, Sommer, Winter) -> Erhöht Lateral Grip
    public int intakeLevel = 0;      // Gibt einen kleinen Boost auf alles

    public String currentCar = "Nissan_GTR_R35_mk2";
}