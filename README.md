# Criador de Etiquetas

Programa Java que lê produtos de uma planilha Excel e imprime etiquetas
em uma impressora **Zebra** conectada via **USB**, usando a linguagem **ZPL**.

## O que foi adicionado

- `zebra/GeradorZPL.java` — transforma um `Produto` em um comando ZPL (nome,
  código de barras Code128 e quantidade).
- `zebra/ImpressoraZebra.java` — localiza as impressoras instaladas no
  sistema e envia o ZPL "cru" (raw) para a impressora escolhida via USB.
- `excel/LeitorExcel.java` e `service/ImportProduto.java` — agora permitem
  listar e escolher qual **aba** da planilha usar, em vez de sempre a primeira.
- `app/Main.java` — menu interativo no terminal que guia o usuário do início ao fim.
- `model/Produto.java` — ganhou getters e um conversor seguro de quantidade
  (texto → número), necessário para saber quantas vias imprimir.

> Observação: o arquivo original `app/CriadorEtiquetas.java` continha uma
> classe `public class Main`, o que não compila em Java (o nome do arquivo
> precisa bater com o da classe pública). Renomeei para `app/Main.java` —
> é o mesmo `exec.mainClass` já configurado no `pom.xml`.

## Formato esperado da planilha

Cada aba deve ter cabeçalho na linha 1 e dados a partir da linha 2:

| Coluna A (nome) | Coluna B (código) | Coluna C (quantidade) |
|---|---|---|
| Caneta Azul     | 7891234560001 | 10 |
| Caderno 96fls   | 7891234560002 | 5  |

## Como configurar a impressora Zebra (USB)

O programa usa a API padrão do Java (`javax.print`), que enxerga a impressora
da mesma forma que o Windows/Linux/Mac a enxergam. Por isso, **a impressora
precisa estar instalada no sistema operacional antes de rodar o programa**:

### Windows
1. Instale o driver **ZDesigner** da Zebra e conecte a impressora via USB.
2. Nas propriedades da impressora, garanta que ela aceita dados "raw"
   (o driver ZDesigner já faz isso por padrão ao receber ZPL).

### Linux (CUPS)
1. Conecte a impressora via USB.
2. Crie uma fila do tipo **raw**, por exemplo:
   ```bash
   lpadmin -p Zebra -E -v usb://Zebra/ZTC%20ZD410 -m raw
   ```
   (ajuste a URI conforme o retorno de `lpinfo -v`).

Depois disso, o programa consegue listar essa impressora automaticamente.

## Como rodar

```bash
mvn clean package
mvn exec:java -Dexec.mainClass=app.Main
# ou, passando o caminho do arquivo direto como argumento:
mvn exec:java -Dexec.mainClass=app.Main -Dexec.args="/caminho/produtos.xlsx"
```

O programa vai:
1. Pedir o caminho do arquivo Excel (se não for passado como argumento).
2. Listar as abas do arquivo para você escolher qual imprimir.
3. Mostrar os produtos encontrados e perguntar se quer imprimir todos ou
   só alguns (por índice, ex: `0,2,5` ou `0-3`).
4. Listar as impressoras encontradas (priorizando as que parecem ser Zebra)
   para você escolher qual usar.
5. Gerar o ZPL de cada produto e enviar para a impressora — a quantidade de
   vias impressas de cada etiqueta vem da coluna "quantidade" da planilha.

## Ajustando o layout da etiqueta

O tamanho da etiqueta (em "dots") pode ser ajustado ao criar o `GeradorZPL`:

```java
// 203 dpi: 1mm ≈ 8 dots. Exemplo para etiqueta de 50x30mm:
GeradorZPL gerador = new GeradorZPL(400, 240);
```

O layout (posição do texto e do código de barras) fica no método
`gerarZplComCopias` dessa classe, caso queira customizar mais.
