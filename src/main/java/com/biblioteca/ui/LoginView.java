package com.biblioteca.ui;

import com.biblioteca.data.Biblioteca;
import com.biblioteca.model.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginView {
    private final StackPane root = new StackPane();

    public LoginView(Consumer<Usuario> aoEntrar) {
        Label titulo = new Label("📚 Biblioteca");
        titulo.getStyleClass().add("titulo");
        Label subtitulo = new Label("Acesse com seu usuário e senha");
        subtitulo.getStyleClass().add("subtitulo");

        TextField login = new TextField();
        login.setPromptText("Usuário");
        PasswordField senha = new PasswordField();
        senha.setPromptText("Senha");

        Label erro = new Label();
        erro.getStyleClass().add("erro");
        erro.setWrapText(true);

        Button entrar = new Button("Entrar");
        entrar.setDefaultButton(true);
        entrar.setMaxWidth(Double.MAX_VALUE);
        entrar.getStyleClass().add("primario");

        Label dica = new Label("Primeiro acesso: admin / admin123");
        dica.getStyleClass().add("dica");

        Runnable tentar = () -> {
            Usuario u = Biblioteca.get().autenticar(login.getText(), senha.getText());
            if (u == null) {
                erro.setText("Usuário ou senha inválidos.");
                senha.clear();
                senha.requestFocus();
            } else {
                erro.setText("");
                aoEntrar.accept(u);
            }
        };
        entrar.setOnAction(e -> tentar.run());

        VBox card = new VBox(12, titulo, subtitulo, login, senha, erro, entrar, dica);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(32));
        card.setMaxWidth(340);
        card.setMaxHeight(VBox.USE_PREF_SIZE);

        root.getChildren().add(card);
        root.getStyleClass().add("login-fundo");
        root.setPadding(new Insets(24));
    }

    public Parent getRoot() { return root; }
}
