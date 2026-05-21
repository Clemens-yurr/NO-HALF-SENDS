package at.htl.no_half_sends.ui;

import at.htl.no_half_sends.data.PlayerProfile;
import at.htl.no_half_sends.data.ProfileManager;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class DriftMainMenu extends FXGLMenu { // main menu mit upgrades usw

    private ProfileManager profileManager; // playerprofil für daten
    private PlayerProfile profile;

    private VBox mainBox;
    private VBox upgradesBox;
    private VBox garageBox;

    private final int[] upgradePrices = {3000, 5000, 6000, 8000, 10000}; // wie viel die upgrades kosten

    public DriftMainMenu() {
        super(MenuType.MAIN_MENU); // vererbung

        profileManager = new ProfileManager();
        profile = profileManager.loadProfile(); // damit das profil geladen wird

        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0.1, 0.1, 0.1, 0.9)); // wieder ein rechteck das so groß ist wie der bildschirm

        Text title = new Text("NO HALF SENDS"); // coolen titel
        title.setFont(Font.font("Impact", 80));
        title.setFill(Color.ORANGERED);
        title.setStroke(Color.WHITE);
        title.setStrokeWidth(2);

        Button btnPlay = createMenuButton("START DRIFTING", () -> fireNewGame()); // die menü punkt buttons
        Button btnUpgrades = createMenuButton("UPGRADES", () -> showMenu(upgradesBox));
        Button btnGarage = createMenuButton("CARS", () -> showMenu(garageBox));
        Button btnExit = createMenuButton("EXIT", () -> getGameController().exit());

        mainBox = new VBox(20, title, btnPlay, btnUpgrades, btnGarage, btnExit); // anzeigen lassen in einer vbox in der mitte
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setTranslateX(getAppWidth() / 2.0 - 200);
        mainBox.setTranslateY(getAppHeight() / 2.0 - 300);

        upgradesBox = buildUpgradesMenu(); // upgrades aufrufen
        upgradesBox.setVisible(false);

        garageBox = buildGarageMenu(); // autos aufrufen
        garageBox.setVisible(false);

        getContentRoot().getChildren().addAll(bg, mainBox, upgradesBox, garageBox);
    }

    private void showMenu(VBox menuToShow) { // menü anzeigen
        profile = profileManager.loadProfile();

        if (getApp() != null && getWorldProperties().exists("cash")) { // cash anzeigen
            profile.cash = geti("cash");
        }

        upgradesBox.getChildren().setAll(buildUpgradesMenu().getChildren());
        garageBox.getChildren().setAll(buildGarageMenu().getChildren());

        mainBox.setVisible(false);
        upgradesBox.setVisible(false);
        garageBox.setVisible(false);
        menuToShow.setVisible(true);
    }

    private VBox buildUpgradesMenu() { // upgrade logik
        Text title = new Text("UPGRADE SHOP"); // titel
        title.setFont(Font.font("Impact", 60));
        title.setFill(Color.WHITE);

        Text cashText = new Text("CASH: $" + profile.cash); // cash anzeigen
        cashText.setFont(Font.font("Arial", 30));
        cashText.setFill(Color.LIGHTGREEN);

        HBox row1 = new HBox(30, // die obere reihe
                createUpgradeCard("Turbo", profile.turboLevel, "turbo", "Turbo.png"),
                createUpgradeCard("Differential", profile.differentialLevel, "diff", "Differential.png"),
                createUpgradeCard("Tires", profile.tiresLevel, "tires", "Tires.png")
        );
        row1.setAlignment(Pos.CENTER);

        HBox row2 = new HBox(30, // untere reihe
                createUpgradeCard("Intake", profile.intakeLevel, "intake", "Intake.png"),
                createUpgradeCard("Chassis", profile.chassisLevel, "chassis", "Chassis.png"),
                createUpgradeCard("Transmission", profile.transmissionLevel, "trans", "Transmission.png")
        );
        row2.setAlignment(Pos.CENTER);

        Button btnBack = createMenuButton("BACK", () -> showMenu(mainBox));// zurück button

        VBox box = new VBox(25, title, cashText, row1, row2, btnBack); // vbox machen und in die mitte machen
        box.setAlignment(Pos.CENTER);
        box.setTranslateX(getAppWidth() / 2.0 - 450);
        box.setTranslateY(50);
        return box;
    }

    private VBox createUpgradeCard(String name, int currentLevel, String type, String imageName) { // die upgrade cards
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #222; -fx-border-color: #555; -fx-border-width: 2; -fx-padding: 20;");
        card.setPrefSize(280, 380);

        Text title = new Text(name);  // titel
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Impact", 28)); // Titel etwas größer

        Texture texture = texture(imageName, 150, 150);

        Text levelText = new Text("Level: " + currentLevel + " / 5"); // das aktuelle upgrade level
        levelText.setFill(Color.LIGHTGRAY);
        levelText.setFont(Font.font("Arial", 18));

        Button buyBtn = new Button();
        buyBtn.setFont(Font.font("Arial", 16));
        if (currentLevel >= 5) {
            buyBtn.setText("MAXED OUT"); // wenn man 5 hat
            buyBtn.setDisable(true);
        } else {
            int cost = upgradePrices[currentLevel];
            buyBtn.setText("BUY ( " + cost + " )"); // buy butten und wie viel es kostet
            buyBtn.setOnAction(e -> handleUpgradeBuy(type, currentLevel, cost));
        }

        card.getChildren().addAll(title, texture, levelText, buyBtn);
        return card;
    }

    private void handleUpgradeBuy(String type, int currentLevel, int cost) { // käufe handlen
        if (profile.cash >= cost) {
            profile.cash -= cost; // geld abziehen
            switch(type) { // level upgraden
                case "turbo": profile.turboLevel++; break;
                case "diff": profile.differentialLevel++; break;
                case "tires": profile.tiresLevel++; break;
                case "intake": profile.intakeLevel++; break;
                case "chassis": profile.chassisLevel++; break;
                case "trans": profile.transmissionLevel++; break;
            }

            profileManager.saveProfile(profile); // profil speichern

            if (getApp() != null && getWorldProperties().exists("cash")) { // den cash updaten
                set("cash", profile.cash);
            }

            upgradesBox.getChildren().setAll(buildUpgradesMenu().getChildren());
        }
    }

    private VBox buildGarageMenu() { // gareagen / auto logik
        Text title = new Text("GARAGE"); // garage
        title.setFont(Font.font("Impact", 60));
        title.setFill(Color.WHITE);

        HBox carList = new HBox(20, // alle optionen
                createCarCard("Nissan GTR", "Nissan_GTR_R35_mk4.png", 0, "Nissan_GTR_R35_mk4.png"),
                createCarCard("Subaru WRX", "Subaru_WRX_STI_mk2.png", 20000, "Subaru_WRX_STI_mk2.png"),
                createCarCard("Toyota Supra", "Toyota_Supra_mk2.png", 35000, "Toyota_Supra_mk2.png"),
                createCarCard("Ferrari F12", "Ferarri_F12_mk2.png", 65000, "Ferarri_F12_mk2.png")
        );
        carList.setAlignment(Pos.CENTER);

        Button btnBack = createMenuButton("BACK", () -> showMenu(mainBox)); // zurück button

        VBox box = new VBox(40, title, carList, btnBack);
        box.setAlignment(Pos.CENTER);
        box.setTranslateX(getAppWidth() / 2.0 - 550);
        box.setTranslateY(200);
        return box;
    }

    private VBox createCarCard(String name, String imagePath, int price, String imageName) { // cards erstellen wie bei den upgrades
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #222; -fx-border-color: #555; -fx-border-width: 2; -fx-padding: 15;");
        card.setPrefSize(260, 340);

        Text title = new Text(name);
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Impact", 20));

        Texture texture = texture(imageName);
        texture.setFitWidth(140);
        texture.setPreserveRatio(true);

        Button actionBtn = new Button();
        boolean ownsCar = profile.ownedCars.contains(imagePath);

        if (profile.currentCar.equals(imagePath)) { // anzeige was man hat und ausgewählt hat und was man noch kaufen kann
            actionBtn.setText("SELECTED");
            actionBtn.setDisable(true);
            actionBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        } else if (ownsCar) {
            actionBtn.setText("SELECT");
            actionBtn.setOnAction(e -> {
                profile.currentCar = imagePath;
                profileManager.saveProfile(profile);
                garageBox.getChildren().setAll(buildGarageMenu().getChildren());
            });
        } else {
            actionBtn.setText("BUY: " + price);
            actionBtn.setOnAction(e -> {
                if (profile.cash >= price) {
                    profile.cash -= price;
                    profile.ownedCars.add(imagePath);
                    profile.currentCar = imagePath;
                    profileManager.saveProfile(profile);

                    if (getApp() != null && getWorldProperties().exists("cash")) {
                        set("cash", profile.cash);
                    }

                    garageBox.getChildren().setAll(buildGarageMenu().getChildren());
                }
            });
        }

        card.getChildren().addAll(title, texture, actionBtn);
        return card;
    }

    private Button createMenuButton(String text, Runnable action) { // button zum menü
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 24));
        btn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 10 40 10 40;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #ff4500; -fx-text-fill: white;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #333; -fx-text-fill: white;"));
        btn.setOnAction(e -> action.run());
        return btn;
    }
}