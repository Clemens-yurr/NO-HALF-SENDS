package at.htl.no_half_sends.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerProfile {
    public int cash = 0;
    public int highestScore = 0;

    // Upgrades (0 = Standard, 1 bis 5 = Upgradestufen)
    public int turboLevel = 0;
    public int differentialLevel = 0;
    public int tiresLevel = 0;
    public int intakeLevel = 0;
    public int chassisLevel = 0;
    public int transmissionLevel = 0;

    public String currentCar = "Nissan_GTR_R35_mk2.png";

    // Liste der gekauften Autos (Der Nissan ist standardmäßig drin)
    public List<String> ownedCars = new ArrayList<>(Arrays.asList("Nissan_GTR_R35_mk2.png"));
}