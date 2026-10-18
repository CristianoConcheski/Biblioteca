package com.biblioteca.ui;

import com.biblioteca.model.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class MainView {
    private final BorderPane root = new BorderPane();

    public MainView(Usuario usuario, Runnable aoSair) {
        Label marca = new Label("📚 Biblioteca");
        marca.getStyleClass().add("marca");
        Label ola = new Label("Olá, " + usuario.getNome());
        ola.getStyleClass().add("ola");
        Button sair = new Button("Sair");
        sair.setOnAction(e -> aoSair.run());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox topo = new HBox(14, marca, espaco, ola, sair);
        topo.setAlignment(Pos.CENTER_LEFT);
        topo.setPadding(new Insets(12, 18, 12, 18));
        topo.getStyleClass().add("topo");

        EmprestimosView emprestimos = new EmprestimosView();
        ClientesView clientes = new ClientesView();
        AcervoView acervo = new AcervoView();

        Tab tEmp = new Tab("Empréstimos e devoluções", emprestimos.getRoot());
        Tab tCli = new Tab("Clientes", clientes.getRoot());
        Tab tAce = new Tab("Acervo", acervo.getRoot());
        tEmp.setClosable(false);
        tCli.setClosable(false);
        tAce.setClosable(false);

        // sempre que a aba é aberta, recarrega os dados para refletir alterações feitas nas outras
        tEmp.setOnSelectionChanged(e -> { if (tEmp.isSelected()) emprestimos.refresh(); });
        tCli.setOnSelectionChanged(e -> { if (tCli.isSelected()) clientes.refresh(); });
        tAce.setOnSelectionChanged(e -> { if (tAce.isSelected()) acervo.refresh(); });

        TabPane abas = new TabPane(tEmp, tCli, tAce);
        root.setTop(topo);
        root.setCenter(abas);
    }

    public Parent getRoot() { return root; }
}
