module at.htl.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens at.htl.demo to javafx.fxml;
    exports at.htl.demo;
}