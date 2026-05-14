package at.htl.no_half_sends.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class ProfileManager {
    private static final String SAVE_FILE = "no_half_sends_save.json";
    private Gson gson;

    public ProfileManager() {
        // PrettyPrinting macht die JSON-Datei schön lesbar für uns
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public PlayerProfile loadProfile() {
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                return gson.fromJson(reader, PlayerProfile.class);
            } catch (IOException e) {
                System.out.println("Fehler beim Laden des Profils: " + e.getMessage());
            }
        }
        // Wenn keine Datei existiert, erstelle ein neues, frisches Profil
        System.out.println("Kein Spielstand gefunden. Erstelle neues Profil.");
        return new PlayerProfile();
    }

    public void saveProfile(PlayerProfile profile) {
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            gson.toJson(profile, writer);
            System.out.println("Spielstand erfolgreich gespeichert!");
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern: " + e.getMessage());
        }
    }
}