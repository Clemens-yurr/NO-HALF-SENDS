package at.htl.no_half_sends.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import static com.almasb.fxgl.dsl.FXGL.*;

public class DriftMainMenu extends FXGLMenu {

    public DriftMainMenu() {
        super(MenuType.MAIN_MENU);

        // Hintergrund (Du könntest hier auch ein Bild deines GTRs einfügen)
        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0.1, 0.1, 0.1, 0.9));

        Text title = new Text("NO HALF SENDS");
        title.setFont(Font.font("Impact", 80));
        title.setFill(Color.ORANGERED);
        title.setStroke(Color.WHITE);
        title.setStrokeWidth(2);

        // Buttons
        Button btnPlay = createMenuButton("START DRIFTING", () -> fireNewGame());
        Button btnShop = createMenuButton("GARAGE / UPGRADES", () -> openShop());
        Button btnExit = createMenuButton("EXIT", () -> getGameController().exit());

        VBox menuBox = new VBox(20, title, btnPlay, btnShop, btnExit);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setTranslateX(getAppWidth() / 2.0 - 200);
        menuBox.setTranslateY(getAppHeight() / 2.0 - 200);

        getContentRoot().getChildren().addAll(bg, menuBox);
    }

    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 24));
        btn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 10 40 10 40;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #ff4500; -fx-text-fill: white; -fx-padding: 10 40 10 40;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 10 40 10 40;"));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void openShop() {
        // Hier bauen wir später das Swipe-Menü für die Upgrades ein!
        System.out.println("Shop geöffnet!");
    }
}