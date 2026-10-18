package com.biblioteca.model;

import java.io.Serializable;

public class Livro implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String titulo;
    private String autor;
    private String isbn;
    private String categoria;
    private int totalExemplares;
    private int disponiveis;

    public Livro(String titulo, String autor, String isbn, String categoria, int totalExemplares) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.categoria = categoria;
        this.totalExemplares = totalExemplares;
        this.disponiveis = totalExemplares;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getTotalExemplares() { return totalExemplares; }
    public void setTotalExemplares(int totalExemplares) { this.totalExemplares = totalExemplares; }
    public int getDisponiveis() { return disponiveis; }
    public void setDisponiveis(int disponiveis) { this.disponiveis = disponiveis; }

    public int getEmprestados() { return totalExemplares - disponiveis; }

    @Override
    public String toString() {
        return titulo + " - " + autor + " [" + disponiveis + " disp.]";
    }
}
