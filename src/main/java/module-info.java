module mx.uaemex.fi.bases.libreria {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens mx.uaemex.fi.bases.libreria to javafx.fxml;
    opens mx.uaemex.fi.bases.libreria.controlador to javafx.fxml;
    opens mx.uaemex.fi.bases.libreria.modelo.data to javafx.base;
    exports mx.uaemex.fi.bases.libreria;
    opens mx.uaemex.fi.bases.libreria.modelo to javafx.base;
}