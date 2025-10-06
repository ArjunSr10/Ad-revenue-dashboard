module main.dash {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
	requires javafx.swing;
	requires org.apache.pdfbox;
    requires jbcrypt;
	requires org.controlsfx.controls;
	requires kernel;
	requires layout;
	requires org.xerial.sqlitejdbc;

	exports main.dash.event;
	exports main.dash.data;
	exports main.dash.scene;
	exports main.dash.enums;
	exports main.dash.scene.controller;

	opens main.dash.scene.controller to javafx.fxml;
    exports main.dash.common;
}