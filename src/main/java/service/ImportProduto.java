/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;


import excel.LeitorExcel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Produto;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 *
 * @author jose-ilcle
 */
public class ImportProduto {

    /**
     * Mantido por compatibilidade: importa sempre a primeira aba do arquivo.
     */
    public List<Produto> importar(String caminho){
        return importar(caminho, 0);
    }

    /**
     * Importa os produtos de uma aba específica do arquivo, identificada pelo índice
     * (0 = primeira aba). Assume que a linha 0 é cabeçalho e que as colunas são:
     * A = nome, B = código, C = quantidade.
     */
    public List<Produto> importar(String caminho, int indicePlanilha){

        List<Produto> produtos = new ArrayList<>();

        try{
            LeitorExcel leitor = new LeitorExcel();

            Workbook workbook = leitor.abrirWorkbook(caminho);
            Sheet planilha = leitor.abrirPlanilha(workbook, indicePlanilha);

            DataFormatter form = new DataFormatter();

            for (int i = 1; i <= planilha.getLastRowNum(); i++) {

                Row linha = planilha.getRow(i);
                if (linha == null) {
                    continue;
                }

                String nome = form.formatCellValue(linha.getCell(0));
                String codigo = form.formatCellValue(linha.getCell(1));
                String quantidade = form.formatCellValue(linha.getCell(2));

                // pula linhas completamente vazias
                if (nome.isBlank() && codigo.isBlank() && quantidade.isBlank()) {
                    continue;
                }

                Produto produto = new Produto();

                produto.setNome(nome);
                produto.setCodigo(codigo);
                produto.setQuantidade(quantidade);

                produtos.add(produto);

            }

            workbook.close();
        }catch(IOException e){
            System.out.println("Erro ao importar planilha: " + e.getMessage());
        }

        return produtos;
    }

    /**
     * Lista os nomes das abas disponíveis no arquivo, para exibir um menu de escolha.
     */
    public List<String> listarPlanilhas(String caminho) {
        List<String> nomes = new ArrayList<>();
        try {
            LeitorExcel leitor = new LeitorExcel();
            Workbook workbook = leitor.abrirWorkbook(caminho);
            nomes = leitor.listarPlanilhas(workbook);
            workbook.close();
        } catch (IOException e) {
            System.out.println("Erro ao listar planilhas: " + e.getMessage());
        }
        return nomes;
    }
}
