package app;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import javax.print.PrintException;
import javax.print.PrintService;
import model.Produto;
import service.ImportProduto;
import view.TelaPrincipal;
import zebra.GeradorZPL;
import zebra.ImpressoraZebra;

/**
 * Programa de linha de comando que:
 *  1. Pede o caminho de um arquivo Excel (.xls/.xlsx);
 *  2. Lista as abas (planilhas) existentes no arquivo e deixa o usuário escolher qual imprimir;
 *  3. Importa os produtos daquela aba (colunas: A=nome, B=código, C=quantidade);
 *  4. Lista as impressoras encontradas no sistema e deixa o usuário escolher a Zebra desejada;
 *  5. Gera o ZPL de cada produto e envia para a impressora via USB.
 *
 * @author jose-ilcle
 */
public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ImportProduto importador = new ImportProduto();
        GeradorZPL geradorZpl = new GeradorZPL();
        ImpressoraZebra impressoraZebra = new ImpressoraZebra();

        
        java.awt.EventQueue.invokeLater(() -> {
        new TelaPrincipal().setVisible(true);
        });
        // 1) Caminho do arquivo -------------------------------------------------
        String caminho = obterCaminhoArquivo(scanner, args);
        if (caminho == null) {
            return;
        }

        // 2) Escolha da aba/planilha --------------------------------------------
        List<String> planilhas = importador.listarPlanilhas(caminho);
        if (planilhas.isEmpty()) {
            System.out.println("Não foi possível ler nenhuma aba do arquivo. Encerrando.");
            return;
        }

        int indicePlanilha = 0;
        if (planilhas.size() > 1) {
            System.out.println("\nPlanilhas encontradas no arquivo:");
            for (int i = 0; i < planilhas.size(); i++) {
                System.out.println("  [" + i + "] " + planilhas.get(i));
            }
            indicePlanilha = lerOpcaoInteira(scanner, "Escolha o número da planilha que deseja imprimir: ", 0, planilhas.size() - 1);
        } else {
            System.out.println("\nUsando a única planilha encontrada: " + planilhas.get(0));
        }

        // 3) Importação dos produtos --------------------------------------------
        List<Produto> produtos = importador.importar(caminho, indicePlanilha);

        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto encontrado nessa planilha. Encerrando.");
            return;
        }

        System.out.println("\nProdutos encontrados (" + produtos.size() + "):");
        for (int i = 0; i < produtos.size(); i++) {
            System.out.println("  [" + i + "] " + produtos.get(i));
        }

        // 4) Escolha de quais produtos imprimir ----------------------------------
        System.out.print("\nDeseja imprimir TODOS os produtos listados? (S/N): ");
        String respostaTodos = scanner.nextLine().trim().toUpperCase();

        List<Produto> selecionados;
        if (respostaTodos.startsWith("S")) {
            selecionados = produtos;
        } else {
            selecionados = selecionarProdutosPorIndice(scanner, produtos);
            if (selecionados.isEmpty()) {
                System.out.println("Nenhum produto selecionado. Encerrando.");
                return;
            }
        }
        
        int totalEtiquetas = 0;
        for (Produto produto : selecionados) {
            int copias = produto.getQuantidadeComoInteiro();
            totalEtiquetas += (copias <= 0) ? 1 : copias;
        }
        System.out.println("\nResumo antes de imprimir:");
        System.out.println("  Produtos selecionados: " + selecionados.size());
        System.out.println("  Total de etiquetas a imprimir (somando as quantidades): " + totalEtiquetas);
        System.out.print("Confirma a impressão? (S/N): ");
        if (!scanner.nextLine().trim().toUpperCase().startsWith("S")) {
            System.out.println("Impressão cancelada.");
            return;
        }
        // 5) Escolha da impressora ------------------------------------------------
        List<PrintService> zebras = impressoraZebra.listarImpressorasZebra();
        List<PrintService> todasImpressoras = zebras.isEmpty() ? impressoraZebra.listarImpressoras() : zebras;

        if (todasImpressoras.isEmpty()) {
            System.out.println("\nNenhuma impressora encontrada no sistema. Verifique se a impressora Zebra");
            System.out.println("está ligada, conectada via USB e instalada como impressora no sistema operacional.");
            return;
        }

        System.out.println("\nImpressoras disponíveis:");
        for (int i = 0; i < todasImpressoras.size(); i++) {
            System.out.println("  [" + i + "] " + todasImpressoras.get(i).getName());
        }
        int indiceImpressora = lerOpcaoInteira(scanner, "Escolha o número da impressora Zebra: ", 0, todasImpressoras.size() - 1);
        PrintService impressoraEscolhida = todasImpressoras.get(indiceImpressora);

        // 6) Geração do ZPL e impressão -------------------------------------------
        System.out.println("\nEnviando etiquetas para \"" + impressoraEscolhida.getName() + "\"...");

        int produtosOk = 0;
        int etiquetasEnviadas = 0;
        for (Produto produto : selecionados) {
            int copias = produto.getQuantidadeComoInteiro();
            if (copias <= 0) {
                copias = 1; // se a quantidade vier vazia/zerada, imprime ao menos 1 etiqueta
            }

            String zpl = geradorZpl.gerarZplComCopias(produto, copias);

            try {
                impressoraZebra.imprimir(impressoraEscolhida, zpl);
                produtosOk++;
                etiquetasEnviadas += copias;
                System.out.println("  OK  - " + produto.getNome() + " (código " + produto.getCodigo() + ", " + copias + " via(s))");
            } catch (PrintException e) {
                System.out.println("  FALHOU - " + produto.getNome() + " (" + copias + " via(s) não enviada(s)): " + e.getMessage());
            }
        }

        System.out.println("\nConcluído:");
        System.out.println("  Produtos processados com sucesso: " + produtosOk + " de " + selecionados.size());
        System.out.println("  Total de etiquetas efetivamente enviadas à impressora: " + etiquetasEnviadas + " de " + totalEtiquetas);

    }

    /**
     * Obtém o caminho do arquivo Excel, seja pelo primeiro argumento da linha
     * de comando, seja perguntando interativamente ao usuário.
     */
    private static String obterCaminhoArquivo(Scanner scanner, String[] args) {
        String caminho;
        if (args.length > 0) {
            caminho = args[0];
        } else {
            System.out.print("Caminho do arquivo Excel (.xls/.xlsx): ");
            caminho = scanner.nextLine().trim();
        }

        File arquivo = new File(caminho);
        if (!arquivo.exists() || !arquivo.isFile()) {
            System.out.println("Arquivo não encontrado: " + caminho);
            return null;
        }
        return caminho;
    }

    /**
     * Lê um número inteiro do usuário, repetindo a pergunta até que um valor
     * válido dentro do intervalo [min, max] seja informado.
     */
    private static int lerOpcaoInteira(Scanner scanner, String mensagem, int min, int max) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine().trim();
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException ignored) {
                // cai no aviso abaixo
            }
            System.out.println("Valor inválido. Digite um número entre " + min + " e " + max + ".");
        }
    }

    /**
     * Permite ao usuário escolher, pelos índices exibidos na listagem, quais
     * produtos deseja imprimir (ex: "0,2,5" ou "0-3").
     */
    private static List<Produto> selecionarProdutosPorIndice(Scanner scanner, List<Produto> produtos) {
        System.out.print("Digite os números dos produtos separados por vírgula (ex: 0,2,5) ou um intervalo (ex: 0-3): ");
        String entrada = scanner.nextLine().trim();

        List<Produto> selecionados = new java.util.ArrayList<>();

        if (entrada.contains("-") && !entrada.contains(",")) {
            String[] partes = entrada.split("-");
            try {
                int inicio = Integer.parseInt(partes[0].trim());
                int fim = Integer.parseInt(partes[1].trim());
                for (int i = inicio; i <= fim && i < produtos.size(); i++) {
                    if (i >= 0) {
                        selecionados.add(produtos.get(i));
                    }
                }
                return selecionados;
            } catch (Exception e) {
                System.out.println("Intervalo inválido, nenhum produto selecionado.");
                return selecionados;
            }
        }

        for (String parte : entrada.split(",")) {
            try {
                int indice = Integer.parseInt(parte.trim());
                if (indice >= 0 && indice < produtos.size()) {
                    selecionados.add(produtos.get(indice));
                }
            } catch (NumberFormatException ignored) {
                // ignora entradas inválidas
            }
        }

        return selecionados;
    }
}
