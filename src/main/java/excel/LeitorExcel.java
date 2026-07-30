/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author jose-ilcle
 */
public class LeitorExcel {

    /**
     * Abre o arquivo .xls/.xlsx e devolve o Workbook completo (todas as abas),
     * para que o chamador possa escolher qual planilha usar.
     */
    public Workbook abrirWorkbook(String caminho) {
        try (FileInputStream arquivo = new FileInputStream(caminho)) {
            return WorkbookFactory.create(arquivo);
        }catch(Exception e){
            return null;
        }
        
    }

    /**
     * Mantido por compatibilidade: abre sempre a primeira aba do arquivo.
     */
    public Sheet abrirPlanilha(String caminho) throws IOException {
        Workbook workb = abrirWorkbook(caminho);
        return workb.getSheetAt(0);
    }

    /**
     * Retorna a planilha (aba) de acordo com o índice informado (0 = primeira aba).
     */
    public Sheet abrirPlanilha(Workbook workbook, int indice) {
        return workbook.getSheetAt(indice);
    }

    /**
     * Lista os nomes de todas as abas existentes no arquivo, na ordem em que aparecem.
     * Útil para exibir um menu e deixar o usuário escolher qual planilha imprimir.
     */
    public List<String> listarPlanilhas(Workbook workbook) {
        List<String> nomes = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            nomes.add(workbook.getSheetName(i));
        }
        return nomes;
    }
    
    
}
