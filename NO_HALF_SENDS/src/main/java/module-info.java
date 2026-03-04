module at.htl.no_half_sends {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens at.htl.no_half_sends to javafx.fxml;
    exports at.htl.no_half_sends;
}