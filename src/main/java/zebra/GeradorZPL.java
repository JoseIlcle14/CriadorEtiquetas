package zebra;

import model.Produto;

/**
 * Responsável por transformar um {@link Produto} em um comando ZPL
 * (Zebra Programming Language), que é a linguagem que as impressoras
 * Zebra entendem para desenhar e imprimir etiquetas.
 *
 * O layout gerado por padrão contém:
 *  - Nome do produto
 *  - Código de barras (Code128) com o código do produto
 *  - O código também em texto legível abaixo da barra
 *
 * As medidas estão em "dots" (pontos), pois é assim que o ZPL trabalha.
 * Para uma impressora configurada a 203 dpi, 1 mm ≈ 8 dots.
 *
 * @author jose-ilcle
 */
public class GeradorZPL {

    // Largura e altura da etiqueta em dots. Ajuste conforme o tamanho
    // físico da etiqueta usada na sua impressora (ex: 400x240 ~= 50x30mm a 203dpi).
    private int larguraEtiqueta = 560;   // ~70mm a 203dpi
    private int alturaEtiqueta = 320;    // ~40mm a 203dpi

    public GeradorZPL() {
    }

    public GeradorZPL(int larguraEtiqueta, int alturaEtiqueta) {
        this.larguraEtiqueta = larguraEtiqueta;
        this.alturaEtiqueta = alturaEtiqueta;
    }

    /**
     * Gera o comando ZPL completo para imprimir 1 via da etiqueta do produto.
     * Use {@link #gerarZplComCopias(Produto, int)} caso queira usar o próprio
     * comando de repetição da impressora (^PQ) em vez de enviar o job várias vezes.
     */
    public String gerarZpl(Produto produto) {
        return gerarZplComCopias(produto, 1);
    }

    /**
     * Gera o comando ZPL para imprimir "copias" vias da mesma etiqueta em um
     * único job de impressão, usando o comando ^PQ (quantidade) da própria
     * impressora Zebra, o que é mais eficiente do que enviar o job várias vezes.
     */
    public String gerarZplComCopias(Produto produto, int copias) {

        String nome = sanitizar(produto.getNome());
        String codigo = sanitizar(produto.getCodigo());

        if (copias < 1) {
            copias = 1;
        }

        StringBuilder zpl = new StringBuilder();

        zpl.append("^XA\n");
        zpl.append("^PW").append(larguraEtiqueta).append("\n");
        zpl.append("^LL").append(alturaEtiqueta).append("\n");
        zpl.append("^CI28\n");

        // QR Code
        zpl.append("^FO20,20\n");
        zpl.append("^BQN,2,4\n");
        zpl.append("^FDLA,").append(codigo).append("^FS\n");

        // Nome
        zpl.append("^FO140,20\n");
        zpl.append("^A0N,30,30\n");
        zpl.append("^FB250,2,0,L,0\n");
        zpl.append("^FD").append(nome).append("^FS\n");

        // Código abaixo do nome
        zpl.append("^FO140,80\n");
        zpl.append("^A0N,26,26\n");
        zpl.append("^FD").append(codigo).append("^FS\n");

        zpl.append("^PQ").append(copias).append("\n");
        zpl.append("^XZ");

        return zpl.toString();
    }

    /**
     * Remove/escapa caracteres que têm significado especial em ZPL
     * (^ inicia comandos, ~ inicia comandos de controle) para que texto
     * vindo da planilha não quebre o layout da etiqueta.
     */
    private String sanitizar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("^", "").replace("~", "").trim();
    }

    public void setLarguraEtiqueta(int larguraEtiqueta) {
        this.larguraEtiqueta = larguraEtiqueta;
    }

    public void setAlturaEtiqueta(int alturaEtiqueta) {
        this.alturaEtiqueta = alturaEtiqueta;
    }
}
