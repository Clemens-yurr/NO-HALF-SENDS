package at.htl.no_half_sends;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;

public class NoHalfSendsApp extends GameApplication {

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("HALF SENDS");
        gameSettings.setWidth(800);
        gameSettings.setHeight(600);

    }

    @Override
    protected void initGame() {
        super.initGame();
        FXGL.setLevelFromMap("Test_MK1.tmx");
    }


    public static void main(String[] args) {
        launch(args);
    }
}