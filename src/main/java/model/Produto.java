/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author jose-ilcle
 */
public class Produto {

    private String nome;
    private String codigo;
    private String quantidade;


    public Produto(){
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo){
        this.codigo = codigo;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(String quantidade){
        this.quantidade = quantidade;
    }

    /**
     * Retorna a quantidade convertida para inteiro.
     * Caso a célula venha vazia, com texto ou com valor quebrado (ex: "3,0"),
     * o método tenta normalizar antes de converter, evitando NumberFormatException
     * na hora de decidir quantas etiquetas imprimir.
     */
    public int getQuantidadeComoInteiro() {
        if (quantidade == null || quantidade.isBlank()) {
            return 0;
        }
        String limpo = quantidade.trim().replace(",", ".");
        try {
            return (int) Double.parseDouble(limpo);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Produto{" +
                "nome='" + nome + '\'' +
                ", codigo=" + codigo +
                ", quantidade='" + quantidade + '\'' +
                '}';
    }
}
