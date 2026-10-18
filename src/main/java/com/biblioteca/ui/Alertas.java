package com.biblioteca.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class Alertas {
    private Alertas() {}

    public static void info(String titulo, String mensagem) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensagem);
    }

    public static void erro(String mensagem) {
        mostrar(Alert.AlertType.ERROR, "Atenção", mensagem);
    }

    public static boolean confirmar(String mensagem) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirmação");
        a.setHeaderText(null);
        return a.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert a = new Alert(tipo, mensagem, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
