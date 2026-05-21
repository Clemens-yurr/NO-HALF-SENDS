package at.htl.no_half_sends.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class ProfileManager {
    private static final String SAVE_FILE = "no_half_sends_save.json"; //json datei wo alles gespeichert wird
    private Gson gson; // google version von json deswegen gson

    public ProfileManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create(); // gson builder ist sowas wie string builder und pretty printing formatiert das
    }

    public PlayerProfile loadProfile() { // profil laden
        File file = new File(SAVE_FILE); // file als variable
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) { // file reader
                return gson.fromJson(reader, PlayerProfile.class); // aus json wird gson gemacht und die daten werden aus playerprofile entnommen
            } catch (IOException e) {
                System.out.println("Fehler beim Laden des Profils: " + e.getMessage()); // wenns nicht geht
            }
        }
        System.out.println("Kein Spielstand gefunden. Erstelle neues Profil.");
        return new PlayerProfile();
    }

    public void saveProfile(PlayerProfile profile) { // wird gespeichert
        try (Writer writer = new FileWriter(SAVE_FILE)) { // in file schreiben
            gson.toJson(profile, writer); // das profile wird mit dem writer in die json gespeichert
            System.out.println("Spielstand erfolgreich gespeichert!");
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern: " + e.getMessage());
        }
    }
}