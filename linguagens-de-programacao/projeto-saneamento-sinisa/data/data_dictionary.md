# Dicionário de dados — SINISA 2025, referência 2024

**Fonte** — [Ministério das Cidades, Resultados SINISA 2025](https://www.gov.br/cidades/pt-br/acesso-a-informacao/acoes-e-programas/saneamento/sinisa/resultados-sinisa/resultados-sinisa-2025).<br>
**Edição** — publicação de 18/12/2025 nas planilhas, com retificação da base municipal de água em 25/05/2026.<br>
**Granularidade** — uma linha por município observado (`municipios`) ou por território oficial consolidado (`territorios`: 27 UFs, cinco macrorregiões e Brasil).<br>
**Arquivos** — `data/processed/municipios.csv` e `.parquet` (5.149 linhas, 15 colunas), `territorios.csv` e `.parquet` (33 linhas, 14 colunas) e `auditoria.json`.

## Entradas

Os quatro XLSX originais foram extraídos dos ZIPs oficiais e renomeados para facilitar a execução. Os bytes foram preservados. A lista de membros dos ZIPs e os hashes SHA-256 estão em [`src/etl.py`](../src/etl.py) e os hashes também constam na [auditoria](processed/auditoria.json).

| Arquivo local | Módulo e nível | Aba | Linha dos códigos |
| --- | --- | --- | ---: |
| `agua_municipal.xlsx` | Água, base municipal retificada | Indicadores de Gestão | 12 |
| `agua_uf.xlsx` | Água, consolidado UF/MR/BR | Indicadores de Gestão | 12 |
| `esgoto_municipal.xlsx` | Esgoto, base municipal | Indicadores - Esgoto | 11 |
| `esgoto_uf.xlsx` | Esgoto, consolidado UF/MR/BR | Indicadores - Esgoto | 11 |

O dicionário descreve todas as colunas que entram no recorte analítico e todas as colunas publicadas nos CSVs. Para as demais variáveis dos arquivos originais, consulte as abas **Nota metodológica** e os glossários oficiais de [água](https://www.gov.br/cidades/pt-br/acesso-a-informacao/acoes-e-programas/saneamento/sinisa/resultados-sinisa/GlossriodeIndicadores_GUA_2024.pdf) e [esgoto](https://www.gov.br/cidades/pt-br/acesso-a-informacao/acoes-e-programas/saneamento/sinisa/resultados-sinisa/GlossrioIndicadores_ESGOTO_2024.pdf).

## Chaves e identificação

| Coluna | Arquivo | Tipo e formato | Significado e origem |
| --- | --- | --- | --- |
| `ibge` | municipios.csv | Texto, 7 algarismos | Código do município (`cod_IBGE`), chave única. |
| `regiao` | municipios.csv | Texto UTF-8 | Macrorregião, uma das cinco regiões do Brasil. |
| `municipio` | municipios.csv | Texto UTF-8 | Nome publicado. Para códigos presentes nos dois arquivos, usa-se a grafia da planilha de água. |
| `uf` | municipios.csv | Texto, 2 letras | Sigla da unidade federativa do município. |
| `capital` | municipios.csv | Booleano (`True`/`False`) | `True` para as 27 capitais, conforme o campo `Capital` das planilhas. |
| `prestador_agua` | municipios.csv | Texto categórico | Natureza jurídica do prestador de água (`CAD0002`): `Sociedade de economia mista`, `Empresa privada`, `Município`, `Autarquia`, `Empresa pública`, `Associação privada` ou `Mais de um tipo`, quando o município tem prestadores de naturezas diferentes. Vazio se o município não consta na planilha de água. |
| `prestador_esgoto` | municipios.csv | Texto categórico | Mesma regra, para o prestador de esgoto. Vazio se o município não consta na planilha de esgoto. |
| `presenca` | municipios.csv | Texto categórico | `Ambos`, `Somente água` ou `Somente esgoto`, conforme presença nas planilhas, inclusive registros com indicador não calculado. |
| `territorio` | territorios.csv | Texto UTF-8 | Nome oficial no campo `UF/Macrorregião`, incluindo Brasil. |
| `nivel` | territorios.csv | Texto categórico | `UF`, `Macrorregião` ou `Brasil`. Parte da chave, junto de `sigla`. |
| `sigla` | territorios.csv | Texto, 1–2 letras | Sigla publicada. `SE` identifica Sergipe ou Sudeste conforme o nível. |
| `regiao` | territorios.csv | Texto UTF-8 | Macrorregião da UF. Vazio nas linhas de macrorregião e do Brasil. |

## Indicadores nos dois CSVs

Os seis indicadores de origem são razões entre população atendida e residente do recorte, multiplicadas por 100. Usam as populações de referência do SINISA. Os valores já publicados em escala de 0 a 100 são mantidos. As fórmulas abaixo reproduzem a notação dos glossários para referência.

| Coluna | Código ou derivação | Tipo e unidade | Definição |
| --- | --- | --- | --- |
| `agua_total_pct` | IAG0001 | Decimal, % | Atendimento da população total por rede de água. (GTA0001 + GTA0002) ÷ DFE0001 × 100. |
| `agua_urbana_pct` | IAG0002 | Decimal, % | Atendimento da população urbana por rede de água. GTA0001 ÷ DFE0002 × 100. |
| `agua_rural_pct` | IAG0003 | Decimal, % | Atendimento da população rural por rede de água. GTA0002 ÷ DFE0003 × 100. |
| `esgoto_total_pct` | IES0001 | Decimal, % | Atendimento da população total por rede coletora. (GTE0001 + GTE0002) ÷ DFE0001 × 100. |
| `esgoto_urbana_pct` | IES0002 | Decimal, % | Atendimento da população urbana por rede coletora. GTE0001 ÷ DFE0002 × 100. |
| `esgoto_rural_pct` | IES0003 | Decimal, % | Atendimento da população rural por rede coletora. GTE0002 ÷ DFE0003 × 100. |
| `esgoto_tratado_pct` | IES2004 | Decimal, % | Apenas em territorios. Esgoto tratado em relação ao esgoto coletado (razão entre volumes). Contextualiza a meta legal, que soma coleta e tratamento. Não foi lido na base municipal, onde passa de 100% em alguns registros. |
| `diferenca_total_pp` | água total − esgoto total | Decimal com sinal, p.p. | Comparação entre percentuais publicados. Vazio se faltar um deles. |
| `hiato_agua_pp` | água urbana − água rural | Decimal com sinal, p.p. | Apenas em territorios.csv. Diferença entre o atendimento urbano e o rural em água. |
| `hiato_esgoto_pp` | esgoto urbano − esgoto rural | Decimal com sinal, p.p. | Apenas em territorios.csv. Diferença entre o atendimento urbano e o rural em esgoto. |

## Formato, faltantes e auditoria

Cada tabela é gravada em dois formatos abertos com o mesmo conteúdo. O CSV é UTF-8 com cabeçalho, separador vírgula, decimal com ponto e duas casas nos campos de indicador. O Parquet (compressão Zstandard) preserva os tipos: `ibge` como texto, `capital` como booleano e indicadores como `float64`. Ao ler o CSV com pandas, informe `dtype={"ibge": "string"}` para manter os sete dígitos.

Célula vazia representa ausência de observação numérica. Os textos de condições não atendidas e de divisão por zero são preservados nas planilhas e contabilizados na auditoria. Zero numérico é um valor válido. Campos com texto inesperado ou valores fora de 0–100 geram erro.

`auditoria.json` contém, por XLSX, o hash (texto hexadecimal), o número de linhas (inteiro) e, por indicador, quantidades válidas, zeros e ocorrências de cada marcador (inteiros). Nas bases municipais, `prestadores` traz a contagem de municípios por natureza jurídica. `cruzamento` registra a união, as contagens por presença e o total de pares válidos.

## Regras de tratamento

| Situação na origem | Tratamento | Onde fica registrado |
| --- | --- | --- |
| Linhas de título acima da tabela | O cabeçalho é localizado pela linha que contém os códigos dos indicadores | `ler_indicadores` em `src/etl.py` |
| "Não calculado (condições não atendidas)" e "Não calculado (divisão por zero)" | Viram ausente (célula vazia). Nenhum valor é imputado | Contagem por marcador em `auditoria.json` |
| Zero numérico | Mantido como valor válido | Contagem de zeros em `auditoria.json` |
| Texto inesperado ou percentual fora de 0–100 | A execução para com erro | Mensagem de erro do ETL |
| Código IBGE lido como número | Mantido como texto de sete dígitos; duplicatas geram erro | Validação em `ler_indicadores` |
| Município presente em um só módulo | Mantido na união, com `presenca` indicando a origem | Coluna `presenca` e bloco `cruzamento` |
| Grafias diferentes do mesmo município | Prevalece a grafia da planilha de água | `unir_municipios` |
| Vários prestadores no mesmo município | Uma categoria se a natureza é a mesma; `Mais de um tipo` se diferem | `natureza_prestador` |
| Arquivo bruto alterado na fonte | Hash SHA-256 diferente do documentado gera erro | Constante `FONTES` |
