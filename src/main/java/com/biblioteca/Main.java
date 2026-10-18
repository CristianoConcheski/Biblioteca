package com.biblioteca;

import com.biblioteca.data.Biblioteca;
import com.biblioteca.model.Usuario;
import com.biblioteca.ui.LoginView;
import com.biblioteca.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Biblioteca.get(); // carrega (ou cria) a base de dados
        stage.setTitle("Biblioteca");
        mostrarLogin();
        stage.show();
    }

    private void mostrarLogin() {
        LoginView login = new LoginView(this::mostrarPrincipal);
        trocarTela(new Scene(login.getRoot(), 460, 520));
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.centerOnScreen();
    }

    private void mostrarPrincipal(Usuario usuario) {
        MainView principal = new MainView(usuario, this::mostrarLogin);
        trocarTela(new Scene(principal.getRoot(), 1050, 720));
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.centerOnScreen();
    }

    private void trocarTela(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/estilo.css")).toExternalForm());
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

