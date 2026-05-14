package at.htl.no_half_sends;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;

public class MyMenuFactory extends SceneFactory {
    @Override
    public FXGLMenu newMainMenu() {
        return new DriftMainMenu();
    }
}
