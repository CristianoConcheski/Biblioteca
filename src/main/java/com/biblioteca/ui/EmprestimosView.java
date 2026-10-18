package com.biblioteca.ui;

import com.biblioteca.data.Biblioteca;
import com.biblioteca.model.Cliente;
import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.Livro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;

public class EmprestimosView {
    private final VBox root = new VBox(14);

    private final ComboBox<Cliente> comboCliente = new ComboBox<>();
    private final ComboBox<Livro> comboLivro = new ComboBox<>();
    private final Spinner<Integer> prazo = new Spinner<>(1, 90, Biblioteca.PRAZO_PADRAO_DIAS);
    private final ComboBox<String> filtro = new ComboBox<>(
            FXCollections.observableArrayList("Em aberto", "Atrasados", "Devolvidos", "Todos"));

    private final ObservableList<Emprestimo> dados = FXCollections.observableArrayList();
    private final FilteredList<Emprestimo> filtrados = new FilteredList<>(dados, e -> e.isAtivo());
    private final TableView<Emprestimo> tabela = new TableView<>(filtrados);
    private final Label resumo = new Label();

    public EmprestimosView() {
        root.setPadding(new Insets(18));

        // ---- novo empréstimo
        Label tituloNovo = new Label("Novo empréstimo");
        tituloNovo.getStyleClass().add("secao");

        comboCliente.setPromptText("Selecione o cliente");
        comboCliente.setMaxWidth(Double.MAX_VALUE);
        comboLivro.setPromptText("Selecione o livro disponível");
        comboLivro.setMaxWidth(Double.MAX_VALUE);
        prazo.setEditable(true);
        prazo.setPrefWidth(90);
        prazo.focusedProperty().addListener((o, a, focado) -> {
            if (!focado) {
                try {
                    prazo.increment(0);
                } catch (RuntimeException ex) {
                    prazo.getEditor().setText(String.valueOf(prazo.getValue()));
                }
            }
        });

        Button emprestar = new Button("Registrar empréstimo");
        emprestar.getStyleClass().add("primario");
        emprestar.setOnAction(e -> emprestar());

        HBox linha = new HBox(10,
                campo("Cliente", comboCliente, true),
                campo("Livro", comboLivro, true),
                campo("Prazo (dias)", prazo, false),
                emprestar);
        linha.setAlignment(Pos.BOTTOM_LEFT);

        VBox cardNovo = new VBox(12, tituloNovo, linha);
        cardNovo.getStyleClass().add("card");
        cardNovo.setPadding(new Insets(16));

        // ---- lista e devolução
        Label tituloLista = new Label("Empréstimos e devoluções");
        tituloLista.getStyleClass().add("secao");

        filtro.getSelectionModel().selectFirst();
        filtro.valueProperty().addListener((o, a, b) -> aplicarFiltro());

        Button devolver = new Button("Registrar devolução");
        devolver.getStyleClass().add("primario");
        devolver.setOnAction(e -> devolver());
        devolver.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox barra = new HBox(10, tituloLista, espaco, new Label("Mostrar:"), filtro, devolver);
        barra.setAlignment(Pos.CENTER_LEFT);

        montarTabela();
        resumo.getStyleClass().add("dica");

        VBox.setVgrow(tabela, Priority.ALWAYS);
        root.getChildren().addAll(cardNovo, barra, tabela, resumo);
        refresh();
    }

    private static VBox campo(String rotulo, javafx.scene.Node n, boolean cresce) {
        Label l = new Label(rotulo);
        l.getStyleClass().add("rotulo");
        VBox v = new VBox(4, l, n);
        if (cresce) HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    private void montarTabela() {
        TableColumn<Emprestimo, String> cCliente = col("Cliente", "clienteNome", 180);
        TableColumn<Emprestimo, String> cLivro = col("Livro", "livroTitulo", 200);
        TableColumn<Emprestimo, String> cSaida = col("Empréstimo", "dataEmprestimoFmt", 95);
        TableColumn<Emprestimo, String> cPrev = col("Previsão", "dataPrevistaFmt", 95);
        TableColumn<Emprestimo, String> cDev = col("Devolução", "dataDevolucaoFmt", 95);
        TableColumn<Emprestimo, String> cMulta = col("Multa", "multaFmt", 80);
        TableColumn<Emprestimo, String> cStatus = col("Status", "status", 110);
        cStatus.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean vazio) {
                super.updateItem(v, vazio);
                getStyleClass().removeAll("status-atrasado", "status-devolvido");
                if (vazio || v == null) {
                    setText(null);
                    return;
                }
                setText(v);
                if ("Atrasado".equals(v)) getStyleClass().add("status-atrasado");
                else if ("Devolvido".equals(v)) getStyleClass().add("status-devolvido");
            }
        });
        tabela.getColumns().addAll(java.util.List.of(cCliente, cLivro, cSaida, cPrev, cDev, cMulta, cStatus));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum empréstimo para exibir."));
    }

    private static TableColumn<Emprestimo, String> col(String nome, String prop, double largura) {
        TableColumn<Emprestimo, String> c = new TableColumn<>(nome);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(largura);
        return c;
    }

    private void aplicarFiltro() {
        String f = filtro.getValue();
        filtrados.setPredicate(e -> switch (f == null ? "Todos" : f) {
            case "Em aberto" -> e.isAtivo();
            case "Atrasados" -> e.isAtrasado();
            case "Devolvidos" -> !e.isAtivo();
            default -> true;
        });
        atualizarResumo();
    }

    private void atualizarResumo() {
        long abertos = dados.stream().filter(Emprestimo::isAtivo).count();
        long atrasados = dados.stream().filter(Emprestimo::isAtrasado).count();
        resumo.setText(abertos + " em aberto · " + atrasados + " em atraso · multa de R$ 1,00 por dia de atraso");
    }

    private void emprestar() {
        try {
            prazo.increment(0);
        } catch (RuntimeException ex) {
            Alertas.erro("Informe um prazo válido em dias.");
            return;
        }
        try {
            Emprestimo emp = Biblioteca.get().emprestar(
                    comboCliente.getValue(), comboLivro.getValue(), prazo.getValue());
            Alertas.info("Empréstimo registrado",
                    "\"" + emp.getLivroTitulo() + "\" emprestado a " + emp.getClienteNome()
                            + ".\nDevolução prevista para " + emp.getDataPrevistaFmt() + ".");
            comboCliente.getSelectionModel().clearSelection();
            comboLivro.getSelectionModel().clearSelection();
            prazo.getValueFactory().setValue(Biblioteca.PRAZO_PADRAO_DIAS);
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Alertas.erro(ex.getMessage());
        }
    }

    private void devolver() {
        Emprestimo sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (!sel.isAtivo()) {
            Alertas.erro("Este empréstimo já foi devolvido.");
            return;
        }
        String msg = "Confirmar a devolução de \"" + sel.getLivroTitulo() + "\" por " + sel.getClienteNome() + "?";
        if (sel.isAtrasado()) msg += "\n\nMulta por atraso: " + sel.getMultaFmt();
        if (!Alertas.confirmar(msg)) return;
        try {
            Emprestimo emp = Biblioteca.get().devolver(sel);
            if (emp.getMulta().signum() > 0) {
                Alertas.info("Devolução registrada", "Cobrar multa de " + emp.getMultaFmt() + " do cliente.");
            } else {
                Alertas.info("Devolução registrada", "Devolução registrada com sucesso.");
            }
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Alertas.erro(ex.getMessage());
        }
    }

    public void refresh() {
        Biblioteca b = Biblioteca.get();
        Cliente cSel = comboCliente.getValue();
        Livro lSel = comboLivro.getValue();

        comboCliente.setItems(FXCollections.observableArrayList(
                b.getClientes().stream().sorted(Comparator.comparing(c -> c.getNome().toLowerCase())).toList()));
        comboLivro.setItems(FXCollections.observableArrayList(
                b.getLivros().stream().filter(l -> l.getDisponiveis() > 0)
                        .sorted(Comparator.comparing(l -> l.getTitulo().toLowerCase())).toList()));

        if (cSel != null) comboCliente.getItems().stream().filter(c -> c.getId() == cSel.getId()).findFirst()
                .ifPresent(comboCliente::setValue);
        if (lSel != null) comboLivro.getItems().stream().filter(l -> l.getId() == lSel.getId()).findFirst()
                .ifPresent(comboLivro::setValue);

        dados.setAll(b.getEmprestimos().stream()
                .sorted(Comparator.comparing(Emprestimo::getId).reversed()).toList());
        aplicarFiltro();
        tabela.refresh();
    }

    public Parent getRoot() { return root; }
}
