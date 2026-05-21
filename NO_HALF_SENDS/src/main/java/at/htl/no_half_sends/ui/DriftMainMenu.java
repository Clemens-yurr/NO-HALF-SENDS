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

public class DriftMainMenu extends FXGLMenu {

    private ProfileManager profileManager;
    private PlayerProfile profile;

    private VBox mainBox;
    private VBox upgradesBox;
    private VBox garageBox;

    private final int[] upgradePrices = {3000, 5000, 6000, 8000, 10000};

    public DriftMainMenu() {
        super(MenuType.MAIN_MENU);

        profileManager = new ProfileManager();
        profile = profileManager.loadProfile();

        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0.1, 0.1, 0.1, 0.9));

        Text title = new Text("NO HALF SENDS");
        title.setFont(Font.font("Impact", 80));
        title.setFill(Color.ORANGERED);
        title.setStroke(Color.WHITE);
        title.setStrokeWidth(2);

        Button btnPlay = createMenuButton("START DRIFTING", () -> fireNewGame());
        Button btnUpgrades = createMenuButton("UPGRADES", () -> showMenu(upgradesBox));
        Button btnGarage = createMenuButton("CARS", () -> showMenu(garageBox));
        Button btnExit = createMenuButton("EXIT", () -> getGameController().exit());

        mainBox = new VBox(20, title, btnPlay, btnUpgrades, btnGarage, btnExit);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setTranslateX(getAppWidth() / 2.0 - 200);
        mainBox.setTranslateY(getAppHeight() / 2.0 - 300);

        upgradesBox = buildUpgradesMenu();
        upgradesBox.setVisible(false);

        garageBox = buildGarageMenu();
        garageBox.setVisible(false);

        getContentRoot().getChildren().addAll(bg, mainBox, upgradesBox, garageBox);
    }

    private void showMenu(VBox menuToShow) {
        profile = profileManager.loadProfile();

        if (getApp() != null && getWorldProperties().exists("cash")) {
            profile.cash = geti("cash");
        }

        upgradesBox.getChildren().setAll(buildUpgradesMenu().getChildren());
        garageBox.getChildren().setAll(buildGarageMenu().getChildren());

        mainBox.setVisible(false);
        upgradesBox.setVisible(false);
        garageBox.setVisible(false);
        menuToShow.setVisible(true);
    }

    private VBox buildUpgradesMenu() {
        Text title = new Text("UPGRADE SHOP");
        title.setFont(Font.font("Impact", 60));
        title.setFill(Color.WHITE);

        Text cashText = new Text("CASH: $" + profile.cash);
        cashText.setFont(Font.font("Arial", 30));
        cashText.setFill(Color.LIGHTGREEN);

        HBox row1 = new HBox(20,
                createUpgradeCard("Turbo", profile.turboLevel, "turbo", "Turbo.png"),
                createUpgradeCard("Differential", profile.differentialLevel, "diff", "Differential.png"),
                createUpgradeCard("Tires", profile.tiresLevel, "tires", "Tires.png")
        );
        row1.setAlignment(Pos.CENTER);

        HBox row2 = new HBox(20,
                createUpgradeCard("Intake", profile.intakeLevel, "intake", "Intake.png"),
                createUpgradeCard("Chassis", profile.chassisLevel, "chassis", "Chassis.png"),
                createUpgradeCard("Transmission", profile.transmissionLevel, "trans", "Transmission.png")
        );
        row2.setAlignment(Pos.CENTER);

        Button btnBack = createMenuButton("BACK", () -> showMenu(mainBox));

        VBox box = new VBox(30, title, cashText, row1, row2, btnBack);
        box.setAlignment(Pos.CENTER);
        box.setTranslateX(getAppWidth() / 2.0 - 450);
        box.setTranslateY(100);
        return box;
    }

    private VBox createUpgradeCard(String name, int currentLevel, String type, String imageName) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #222; -fx-border-color: #555; -fx-border-width: 2; -fx-padding: 15;");
        card.setPrefSize(250, 290); // Höhe leicht erhöht für das Bild

        Text title = new Text(name);
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Impact", 24));

        // FXGL Texture-Loader lädt das Bild direkt aus assets/textures/ und skaliert es auf 70x70 Pixel
        Texture texture = texture(imageName, 70, 70);

        Text levelText = new Text("Level: " + currentLevel + " / 5");
        levelText.setFill(Color.LIGHTGRAY);

        Button buyBtn = new Button();
        if (currentLevel >= 5) {
            buyBtn.setText("MAXED OUT");
            buyBtn.setDisable(true);
        } else {
            int cost = upgradePrices[currentLevel];
            buyBtn.setText("BUY ( " + cost + " )");
            buyBtn.setOnAction(e -> handleUpgradeBuy(type, currentLevel, cost));
        }

        // Texture wurde hier in die addAll-Liste hinzugefügt!
        card.getChildren().addAll(title, texture, levelText, buyBtn);
        return card;
    }

    private void handleUpgradeBuy(String type, int currentLevel, int cost) {
        if (profile.cash >= cost) {
            profile.cash -= cost;
            switch(type) {
                case "turbo": profile.turboLevel++; break;
                case "diff": profile.differentialLevel++; break;
                case "tires": profile.tiresLevel++; break;
                case "intake": profile.intakeLevel++; break;
                case "chassis": profile.chassisLevel++; break;
                case "trans": profile.transmissionLevel++; break;
            }

            profileManager.saveProfile(profile);

            if (getApp() != null && getWorldProperties().exists("cash")) {
                set("cash", profile.cash);
            }

            upgradesBox.getChildren().setAll(buildUpgradesMenu().getChildren());
        }
    }

    private VBox buildGarageMenu() {
        Text title = new Text("GARAGE");
        title.setFont(Font.font("Impact", 60));
        title.setFill(Color.WHITE);

        HBox carList = new HBox(20,
                createCarCard("Nissan GTR", "Nissan_GTR_R35_mk4.png", 0, "Nissan_GTR_R35_mk4.png"),
                createCarCard("Subaru WRX", "Subaru_WRX_STI_mk2.png", 20000, "Subaru_WRX_STI_mk2.png"),
                createCarCard("Toyota Supra", "Toyota_Supra_mk2.png", 35000, "Toyota_Supra_mk2.png"),
                createCarCard("Ferrari F12", "Ferarri_F12_mk2.png", 65000, "Ferarri_F12_mk2.png")
        );
        carList.setAlignment(Pos.CENTER);

        Button btnBack = createMenuButton("BACK", () -> showMenu(mainBox));

        VBox box = new VBox(40, title, carList, btnBack);
        box.setAlignment(Pos.CENTER);
        box.setTranslateX(getAppWidth() / 2.0 - 550);
        box.setTranslateY(200);
        return box;
    }

    private VBox createCarCard(String name, String imagePath, int price, String imageName) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #222; -fx-border-color: #555; -fx-border-width: 2; -fx-padding: 15;");
        card.setPrefSize(260, 340); // Größe angepasst, damit das Auto schön Platz hat

        Text title = new Text(name);
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Impact", 20));

        // Lädt die Auto-Textur und passt sie für die UI-Card an (z.B. Breite 140, Höhe behalten via Seitenverhältnis)
        Texture texture = texture(imageName);
        texture.setFitWidth(140);
        texture.setPreserveRatio(true);

        Button actionBtn = new Button();
        boolean ownsCar = profile.ownedCars.contains(imagePath);

        if (profile.currentCar.equals(imagePath)) {
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

        // Texture wurde hier in die addAll-Liste hinzugefügt!
        card.getChildren().addAll(title, texture, actionBtn);
        return card;
    }

    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 24));
        btn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 10 40 10 40;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #ff4500; -fx-text-fill: white;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #333; -fx-text-fill: white;"));
        btn.setOnAction(e -> action.run());
        return btn;
    }
}