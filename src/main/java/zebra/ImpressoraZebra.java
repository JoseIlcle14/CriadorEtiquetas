package zebra;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

/**
 * Responsável pela comunicação com a impressora Zebra conectada via USB.
 *
 * A forma mais simples e portável de enviar ZPL "cru" (raw) para uma
 * impressora Zebra USB em Java é através da API javax.print, que enxerga
 * a impressora da mesma forma que o sistema operacional (Windows/Linux/Mac)
 * a enxerga. Para isso funcionar:
 *
 *  1. A impressora Zebra deve estar instalada no sistema operacional como
 *     uma impressora "RAW" (no Windows, normalmente o driver "ZDesigner" já
 *     permite isso; no Linux/CUPS, crie uma fila do tipo "raw").
 *  2. Ligue a impressora via USB e instale o driver antes de rodar o programa.
 *
 * Alternativa mais "baixo nível": usar acesso direto à porta USB via
 * bibliotecas como usb4java/javax.usb. Isso exige instalar drivers WinUSB/
 * libusb e é mais complexo de configurar; a abordagem via javax.print
 * costuma resolver o caso de uso de "imprimir etiqueta ZPL" sem essa
 * complexidade extra.
 *
 * @author jose-ilcle
 */
public class ImpressoraZebra {

    /**
     * Lista todas as impressoras que o sistema operacional enxerga
     * (USB, rede, etc.), para que o usuário escolha qual usar.
     */
    public List<PrintService> listarImpressoras() {
        PrintService[] servicos = PrintServiceLookup.lookupPrintServices(null, null);
        List<PrintService> lista = new ArrayList<>();
        for (PrintService servico : servicos) {
            lista.add(servico);
        }
        return lista;
    }

    /**
     * Tenta localizar automaticamente impressoras cujo nome sugira ser uma
     * Zebra (ex: "ZDesigner", "Zebra", "GK420", "ZD410" etc.), para facilitar
     * a escolha quando há várias impressoras instaladas no sistema.
     */
    public List<PrintService> listarImpressorasZebra() {
        List<PrintService> zebras = new ArrayList<>();
        for (PrintService servico : listarImpressoras()) {
            String nome = servico.getName().toLowerCase();
            if (nome.contains("zebra") || nome.contains("zdesigner") || nome.contains("zd") || nome.contains("gk4") || nome.contains("gx4")) {
                zebras.add(servico);
            }
        }
        return zebras;
    }

    /**
     * Envia o comando ZPL diretamente (raw) para a impressora informada.
     * A impressora Zebra interpreta o texto ZPL recebido e imprime a etiqueta.
     *
     * @param impressora impressora de destino (obtida via listarImpressoras())
     * @param zpl        comando ZPL gerado por {@link GeradorZPL}
     */
    public void imprimir(PrintService impressora, String zpl) throws PrintException {
        if (impressora == null) {
            throw new PrintException("Nenhuma impressora selecionada.");
        }

        byte[] dados = zpl.getBytes(StandardCharsets.UTF_8);

        // AUTOSENSE deixa o próprio serviço de impressão identificar que os
        // bytes são dados "crus" a serem repassados sem reprocessamento,
        // o que é o comportamento esperado para enviar ZPL a uma impressora
        // configurada como raw/passthrough.
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(dados, flavor, null);

        DocPrintJob job = impressora.createPrintJob();
        PrintRequestAttributeSet atributos = new HashPrintRequestAttributeSet();

        job.print(doc, atributos);
    }

    /**
     * Alternativa: envia o ZPL escrevendo diretamente em um OutputStream
     * (útil caso você já tenha, por exemplo, uma porta serial/USB aberta
     * manualmente, em vez de usar o spooler de impressão do sistema).
     */
    public void imprimir(OutputStream saida, String zpl) throws IOException {
        saida.write(zpl.getBytes(StandardCharsets.UTF_8));
        saida.flush();
    }
}
