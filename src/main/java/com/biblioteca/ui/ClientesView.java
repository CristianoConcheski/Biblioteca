package com.biblioteca.ui;

import com.biblioteca.data.Biblioteca;
import com.biblioteca.model.Cliente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ClientesView {
    private final VBox root = new VBox(14);
    private final ObservableList<Cliente> dados = FXCollections.observableArrayList();
    private final FilteredList<Cliente> filtrados = new FilteredList<>(dados, c -> true);
    private final TableView<Cliente> tabela = new TableView<>(filtrados);

    private final TextField nome = new TextField();
    private final TextField cpf = new TextField();
    private final TextField telefone = new TextField();
    private final TextField email = new TextField();
    private final TextField busca = new TextField();
    private final Label formTitulo = new Label("Novo cliente");

    private Cliente editando;

    public ClientesView() {
        root.setPadding(new Insets(18));

        nome.setPromptText("Nome completo");
        cpf.setPromptText("000.000.000-00");
        telefone.setPromptText("(00) 00000-0000");
        email.setPromptText("email@exemplo.com");
        formTitulo.getStyleClass().add("secao");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        form.getColumnConstraints().addAll(c1, c2);
        form.add(rotulo("Nome *", nome), 0, 0, 2, 1);
        form.add(rotulo("CPF *", cpf), 0, 1);
        form.add(rotulo("Telefone", telefone), 1, 1);
        form.add(rotulo("E-mail", email), 0, 2, 2, 1);

        Button salvar = new Button("Salvar");
        salvar.getStyleClass().add("primario");
        salvar.setOnAction(e -> salvar());
        Button novo = new Button("Novo");
        novo.setOnAction(e -> limpar());
        Button excluir = new Button("Excluir");
        excluir.getStyleClass().add("perigo");
        excluir.setOnAction(e -> excluir());
        excluir.disableProperty().bind(tabela.getSelectionModel().selectedItemProperty().isNull());
        HBox acoes = new HBox(8, salvar, novo, excluir);

        VBox cardForm = new VBox(12, formTitulo, form, acoes);
        cardForm.getStyleClass().add("card");
        cardForm.setPadding(new Insets(16));

        busca.setPromptText("Buscar por nome ou CPF…");
        busca.textProperty().addListener((o, a, b) -> {
            String t = b == null ? "" : b.trim().toLowerCase();
            String digitos = t.replaceAll("\\D", "");
            filtrados.setPredicate(c -> t.isEmpty()
                    || c.getNome().toLowerCase().contains(t)
                    || (!digitos.isEmpty() && c.getCpf().contains(digitos)));
        });

        montarTabela();
        tabela.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> {
            if (c != null) carregar(c);
        });

        VBox.setVgrow(tabela, Priority.ALWAYS);
        root.getChildren().addAll(cardForm, busca, tabela);
        refresh();
    }

    private void montarTabela() {
        TableColumn<Cliente, String> cNome = new TableColumn<>("Nome");
        cNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        cNome.setPrefWidth(240);
        TableColumn<Cliente, String> cCpf = new TableColumn<>("CPF");
        cCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        cCpf.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(String v, boolean vazio) {
                super.updateItem(v, vazio);
                setText(vazio || v == null ? null : formatarCpf(v));
            }
        });
        cCpf.setPrefWidth(130);
        TableColumn<Cliente, String> cTel = new TableColumn<>("Telefone");
        cTel.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        cTel.setPrefWidth(130);
        TableColumn<Cliente, String> cEmail = new TableColumn<>("E-mail");
        cEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        cEmail.setPrefWidth(220);
        tabela.getColumns().addAll(java.util.List.of(cNome, cCpf, cTel, cEmail));
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPlaceholder(new Label("Nenhum cliente cadastrado."));
    }

    private static VBox rotulo(String texto, javafx.scene.Node campo) {
        Label l = new Label(texto);
        l.getStyleClass().add("rotulo");
        VBox v = new VBox(4, l, campo);
        if (campo instanceof TextField tf) tf.setMaxWidth(Double.MAX_VALUE);
        return v;
    }

    static String formatarCpf(String d) {
        return d.length() == 11
                ? d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9)
                : d;
    }

    private void carregar(Cliente c) {
        editando = c;
        nome.setText(c.getNome());
        cpf.setText(formatarCpf(c.getCpf()));
        telefone.setText(c.getTelefone());
        email.setText(c.getEmail());
        formTitulo.setText("Editando: " + c.getNome());
    }

    private void limpar() {
        editando = null;
        tabela.getSelectionModel().clearSelection();
        nome.clear();
        cpf.clear();
        telefone.clear();
        email.clear();
        formTitulo.setText("Novo cliente");
        nome.requestFocus();
    }

    private void salvar() {
        try {
            Cliente c = editando != null ? editando : new Cliente("", "", "", "");
            String nomeAnt = c.getNome(), cpfAnt = c.getCpf(), telAnt = c.getTelefone(), emailAnt = c.getEmail();
            c.setNome(nome.getText().trim());
            c.setCpf(cpf.getText());
            c.setTelefone(telefone.getText().trim());
            c.setEmail(email.getText().trim());
            try {
                Biblioteca.get().salvarCliente(c);
            } catch (RuntimeException ex) {
                if (editando != null) { // desfaz alterações em memória se a validação falhar
                    c.setNome(nomeAnt); c.setCpf(cpfAnt); c.setTelefone(telAnt); c.setEmail(emailAnt);
                }
                throw ex;
            }
            limpar();
            refresh();
            Alertas.info("Cliente", "Cliente salvo com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Alertas.erro(ex.getMessage());
        }
    }

    private void excluir() {
        Cliente c = tabela.getSelectionModel().getSelectedItem();
        if (c == null) return;
        if (!Alertas.confirmar("Excluir o cliente \"" + c.getNome() + "\"?")) return;
        try {
            Biblioteca.get().excluirCliente(c);
            limpar();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Alertas.erro(ex.getMessage());
        }
    }

    public void refresh() {
        dados.setAll(Biblioteca.get().getClientes());
    }

    public Parent getRoot() { return root; }
}
