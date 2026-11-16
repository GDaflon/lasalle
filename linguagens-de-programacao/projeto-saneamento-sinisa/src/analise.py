"""Estatísticas e figuras do artigo a partir das tabelas processadas."""
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import plotly.graph_objects as go

from src.etl import REGIOES, ROOT

AGUA = "#14788a"
ESGOTO = "#a44f2a"
CORES_REGIAO = ["#126d93", "#aa592e", "#387c61", "#7650a0", "#a03765"]
TOTAIS = ["agua_total_pct", "esgoto_total_pct"]
DIFERENCA = "diferenca_total_pp"
# Metas de universalização para 31/12/2033 (Lei nº 11.445/2007, art. 11-B, redação da Lei nº 14.026/2020).
# A meta de esgoto fala em coleta e tratamento. Aqui ela serve de régua para a coleta por rede.
META_AGUA, META_ESGOTO = 99.0, 90.0
FAIXAS = [-100, -5, 5, 25, 50, 100]
ROTULOS_FAIXAS = ["Esgoto acima (< −5)", "Próximos (−5 a 5)", "5 a 25", "25 a 50", "Acima de 50"]


def carregar(pasta=ROOT / "data/processed"):
    """Lê os Parquet, que preservam o IBGE como texto e a capital como booleano."""
    return pd.read_parquet(pasta / "municipios.parquet"), pd.read_parquet(pasta / "territorios.parquet")


def pares_validos(municipios):
    return municipios.dropna(subset=TOTAIS).copy()


def spearman(tabela):
    return tabela[TOTAIS].rank().corr().iloc[0, 1]


# ---------------------------------------------------------------- RQ1

def ranking_ufs(territorios):
    """UFs ordenadas pela diferença, com a distância de cada indicador até a meta de 2033."""
    ufs = territorios.loc[territorios["nivel"].eq("UF")].copy()
    ufs["falta_agua_pp"] = (META_AGUA - ufs["agua_total_pct"]).clip(lower=0)
    ufs["falta_esgoto_pp"] = (META_ESGOTO - ufs["esgoto_total_pct"]).clip(lower=0)
    colunas = ["sigla", "territorio", "regiao", *TOTAIS, DIFERENCA, "falta_agua_pp", "falta_esgoto_pp",
               "esgoto_tratado_pct"]
    return ufs.sort_values(DIFERENCA, ascending=False)[colunas].reset_index(drop=True)


def resumo_ufs(territorios):
    ufs = ranking_ufs(territorios)
    return {
        "ufs_na_meta_agua": int(ufs["agua_total_pct"].ge(META_AGUA).sum()),
        "ufs_na_meta_esgoto": int(ufs["esgoto_total_pct"].ge(META_ESGOTO).sum()),
        "ufs_esgoto_abaixo_de_50": int(ufs["esgoto_total_pct"].lt(50).sum()),
        "diferenca_mediana_pp": ufs[DIFERENCA].median(),
        "diferenca_desvio_padrao_pp": ufs[DIFERENCA].std(),
        "amplitude_pp": ufs[DIFERENCA].max() - ufs[DIFERENCA].min(),
        "pearson_entre_ufs": ufs[TOTAIS].corr().iloc[0, 1],
    }


# ---------------------------------------------------------------- RQ2

def hiatos_ufs(territorios):
    """Atendimento urbano e rural por UF, com o hiato (urbano − rural) de cada serviço."""
    ufs = territorios.loc[territorios["nivel"].eq("UF")]
    colunas = ["sigla", "regiao", "agua_urbana_pct", "agua_rural_pct", "hiato_agua_pp",
               "esgoto_urbana_pct", "esgoto_rural_pct", "hiato_esgoto_pp"]
    return ufs[colunas].sort_values("hiato_agua_pp", ascending=False).reset_index(drop=True)


def resumo_rural(territorios):
    ufs = territorios.loc[territorios["nivel"].eq("UF")]
    return {
        "ufs_esgoto_rural_abaixo_de_5": int(ufs["esgoto_rural_pct"].lt(5).sum()),
        "ufs_esgoto_rural_zero": int(ufs["esgoto_rural_pct"].eq(0).sum()),
        "ufs_agua_rural_acima_de_50": int(ufs["agua_rural_pct"].gt(50).sum()),
        "agua_rural_mediana": ufs["agua_rural_pct"].median(),
        "esgoto_rural_mediana": ufs["esgoto_rural_pct"].median(),
    }


# ---------------------------------------------------------------- RQ3

def cobertura(municipios):
    """Quantos municípios de cada região entram na comparação pareada."""
    tabela = municipios.groupby("regiao").agg(
        municipios=("ibge", "size"),
        agua_valida=("agua_total_pct", "count"),
        esgoto_valido=("esgoto_total_pct", "count"),
        pares=(DIFERENCA, "count"),
    ).reindex(REGIOES)
    tabela.loc["Brasil"] = tabela.sum()
    tabela = tabela.astype(int)
    tabela["pares_pct"] = tabela["pares"] / tabela["municipios"] * 100
    return tabela


def vies_de_selecao(municipios):
    """Mediana do indicador de água dentro e fora da amostra pareada."""
    com_agua = municipios.dropna(subset=["agua_total_pct"])
    pareado = com_agua[DIFERENCA].notna()
    return {
        "mediana_agua_pareados": com_agua.loc[pareado, "agua_total_pct"].median(),
        "mediana_agua_fora": com_agua.loc[~pareado, "agua_total_pct"].median(),
        "mediana_agua_todos": com_agua["agua_total_pct"].median(),
    }


def resumo_municipal(pares):
    grupos = pares.groupby("regiao")
    tabela = grupos[DIFERENCA].agg(
        n="size", mediana="median", q1=lambda s: s.quantile(0.25), q3=lambda s: s.quantile(0.75),
        desvio_padrao="std", esgoto_maior=lambda s: int(s.lt(0).sum()),
    )
    tabela["pearson"] = grupos.apply(lambda g: g[TOTAIS].corr().iloc[0, 1], include_groups=False)
    tabela["spearman"] = grupos.apply(spearman, include_groups=False)
    return tabela.reindex(REGIOES)


def limites_iqr(diferenca):
    q1, q3 = diferenca.quantile([0.25, 0.75])
    iqr = q3 - q1
    return q1 - 1.5 * iqr, q3 + 1.5 * iqr


def diagnostico(pares):
    diferenca = pares[DIFERENCA]
    q1, q3 = diferenca.quantile([0.25, 0.75])
    limite_inferior, limite_superior = limites_iqr(diferenca)
    return {
        "n": len(pares),
        "media_pp": diferenca.mean(),
        "mediana_pp": diferenca.median(),
        "desvio_padrao_pp": diferenca.std(),
        "q1_pp": q1,
        "q3_pp": q3,
        "iqr_pp": q3 - q1,
        "assimetria": diferenca.skew(),
        "pearson": pares[TOTAIS].corr().iloc[0, 1],
        "spearman": spearman(pares),
        "abaixo_limite_iqr": int(diferenca.lt(limite_inferior).sum()),
        "acima_limite_iqr": int(diferenca.gt(limite_superior).sum()),
        "esgoto_maior": int(diferenca.lt(0).sum()),
        "na_meta_agua": int(pares["agua_total_pct"].ge(META_AGUA).sum()),
        "na_meta_esgoto": int(pares["esgoto_total_pct"].ge(META_ESGOTO).sum()),
        "nas_duas_metas": int((pares["agua_total_pct"].ge(META_AGUA)
                               & pares["esgoto_total_pct"].ge(META_ESGOTO)).sum()),
    }


def faixas(pares):
    """Concentração dos municípios por faixa de diferença, em pontos percentuais."""
    classes = pd.cut(pares[DIFERENCA], FAIXAS, labels=ROTULOS_FAIXAS, include_lowest=True)
    tabela = pd.crosstab(pares["regiao"], classes).reindex(REGIOES)
    tabela.loc["Brasil"] = tabela.sum()
    return tabela


def extremos(pares, quantidade=5):
    campos = ["municipio", "uf", *TOTAIS, DIFERENCA, "prestador_agua", "prestador_esgoto"]
    return pd.concat([pares.nlargest(quantidade, DIFERENCA), pares.nsmallest(quantidade, DIFERENCA)])[campos]


def por_prestador(pares, coluna="prestador_agua", minimo=30):
    """Medianas por natureza jurídica do prestador. Grupos pequenos ficam de fora."""
    tabela = pares.groupby(coluna).agg(
        n=("ibge", "size"), agua_mediana=("agua_total_pct", "median"),
        esgoto_mediana=("esgoto_total_pct", "median"), diferenca_mediana=(DIFERENCA, "median"),
    )
    return tabela.loc[tabela["n"].ge(minimo)].sort_values("n", ascending=False)


def prestadores_distintos(pares):
    """Parcela de municípios em que água e esgoto têm prestadores de naturezas diferentes."""
    distintos = pares["prestador_agua"].ne(pares["prestador_esgoto"])
    negativos = pares[DIFERENCA].lt(0)
    return {
        "todos_pct": distintos.mean() * 100,
        "esgoto_maior_pct": distintos[negativos].mean() * 100,
        "demais_pct": distintos[~negativos].mean() * 100,
    }


def capitais(pares):
    campos = ["municipio", "uf", *TOTAIS, DIFERENCA]
    return pares.loc[pares["capital"], campos].sort_values(DIFERENCA, ascending=False).reset_index(drop=True)


# ---------------------------------------------------------------- figuras

def percent(valor, casas=1):
    return f"{valor:.{casas}f}%".replace(".", ",")


def eixo_percentual(ax):
    ax.set_xlim(0, 104)
    ax.set_xticks(np.arange(0, 101, 20), [f"{v}%" for v in range(0, 101, 20)])
    ax.grid(axis="x", color="#dce5e7")
    ax.set_axisbelow(True)
    ax.tick_params(length=0)
    ax.spines[["top", "right", "left", "bottom"]].set_visible(False)


def grafico_regioes(territorios):
    dados = territorios.loc[territorios["nivel"].ne("UF")]
    y = np.arange(len(dados))
    fig, ax = plt.subplots(figsize=(9.2, 4.8), layout="constrained")
    for coluna, rotulo, cor, deslocamento in [
        ("agua_total_pct", "Rede de água", AGUA, -0.19),
        ("esgoto_total_pct", "Rede de esgoto", ESGOTO, 0.19),
    ]:
        barras = ax.barh(y + deslocamento, dados[coluna], height=0.33, label=rotulo, color=cor)
        ax.bar_label(barras, labels=[percent(v) for v in dados[coluna]], padding=4, fontsize=9)
    ax.set_yticks(y, dados["territorio"])
    ax.invert_yaxis()
    eixo_percentual(ax)
    ax.set_xlabel("Atendimento da população total")
    ax.legend(loc="lower left", bbox_to_anchor=(0, 1), ncols=2, frameon=False)
    return fig


def grafico_ufs(territorios):
    """Halteres interativos: um par de pontos por UF, com botões de ordenação."""
    ufs = ranking_ufs(territorios)
    ordens = {
        "Maior diferença": ufs.sort_values(DIFERENCA)["territorio"],
        "Rede de água": ufs.sort_values("agua_total_pct")["territorio"],
        "Rede de esgoto": ufs.sort_values("esgoto_total_pct")["territorio"],
        "Região": ufs.assign(ordem=ufs["regiao"].map({r: i for i, r in enumerate(REGIOES)}))
                     .sort_values(["ordem", "territorio"], ascending=False)["territorio"],
    }
    linhas_x, linhas_y = [], []
    for linha in ufs.itertuples():
        linhas_x += [linha.esgoto_total_pct, linha.agua_total_pct, None]
        linhas_y += [linha.territorio, linha.territorio, None]
    detalhe = ufs[["regiao", *TOTAIS, DIFERENCA]].values.tolist()
    dica = ("<b>%{y}</b> · %{customdata[0]}<br>Água: %{customdata[1]:.2f}%<br>"
            "Esgoto: %{customdata[2]:.2f}%<br>Diferença: %{customdata[3]:.2f} p.p.<extra></extra>")
    fig = go.Figure()
    fig.add_trace(go.Scatter(x=linhas_x, y=linhas_y, mode="lines", hoverinfo="skip", showlegend=False,
                             line={"color": "#9fb0b5", "width": 3}))
    for coluna, rotulo, cor, simbolo in [
        ("esgoto_total_pct", "Rede de esgoto", ESGOTO, "square"),
        ("agua_total_pct", "Rede de água", AGUA, "circle"),
    ]:
        fig.add_trace(go.Scatter(
            x=ufs[coluna].tolist(), y=ufs["territorio"].tolist(), mode="markers", name=rotulo,
            customdata=detalhe, hovertemplate=dica, marker={"color": cor, "size": 11, "symbol": simbolo},
        ))
    for meta, rotulo, cor, altura in [
        (META_ESGOTO, "Meta esgoto 90%", ESGOTO, 1.0), (META_AGUA, "Meta água 99%", AGUA, 1.028),
    ]:
        fig.add_vline(x=meta, line={"dash": "dot", "color": cor, "width": 1.5})
        fig.add_annotation(x=meta, y=altura, yref="paper", yanchor="bottom", text=rotulo, showarrow=False,
                           xanchor="right", font={"color": cor, "size": 11})
    fig.update_layout(
        template="plotly_white", margin={"l": 8, "r": 16, "t": 84, "b": 56},
        font={"family": "system-ui, sans-serif", "color": "#223e48", "size": 12}, separators=",.",
        legend={"orientation": "h", "yanchor": "top", "y": -0.045, "x": 0},
        xaxis={"range": [0, 103], "ticksuffix": "%"},
        yaxis={"categoryorder": "array", "categoryarray": ordens["Maior diferença"].tolist(),
               "automargin": True},
        updatemenus=[{
            "type": "dropdown", "direction": "down", "x": 0, "xanchor": "left", "y": 1.075,
            "yanchor": "bottom", "showactive": True,
            "buttons": [{"label": f"Ordenar por: {rotulo.lower()}", "method": "relayout",
                         "args": [{"yaxis.categoryarray": ordem.tolist()}]} for rotulo, ordem in ordens.items()],
        }],
    )
    return fig


def grafico_urbano_rural(territorios):
    dados = territorios.loc[territorios["nivel"].ne("UF")]
    y = np.arange(len(dados))
    fig, eixos = plt.subplots(1, 2, figsize=(10.2, 4.1), sharey=True, layout="constrained")
    for ax, modulo, titulo in zip(eixos, ["agua", "esgoto"], ["Rede de água", "Rede de esgoto"]):
        rural, urbana = dados[f"{modulo}_rural_pct"], dados[f"{modulo}_urbana_pct"]
        ax.hlines(y, rural, urbana, color="#8a9ca4", linewidth=2)
        ax.scatter(rural, y, c=ESGOTO, label="Rural", marker="s", s=38, zorder=3)
        ax.scatter(urbana, y, c=AGUA, label="Urbana", s=44, zorder=3)
        eixo_percentual(ax)
        ax.set_yticks(y, dados["territorio"])
        ax.set_title(titulo, loc="left", fontsize=12)
        ax.set_xlabel("Atendimento no recorte")
        ax.legend(loc="lower left", frameon=False, ncols=2)
    eixos[0].set_ylim(len(y) - 0.15, -0.5)
    return fig


def grafico_calor_ufs(territorios):
    """Mapa de calor: quatro recortes por UF, agrupados por macrorregião."""
    ufs = territorios.loc[territorios["nivel"].eq("UF")].copy()
    ufs["ordem"] = ufs["regiao"].map({regiao: i for i, regiao in enumerate(REGIOES)})
    ufs = ufs.sort_values(["ordem", "agua_urbana_pct"], ascending=[True, False])
    colunas = ["agua_urbana_pct", "agua_rural_pct", "esgoto_urbana_pct", "esgoto_rural_pct"]
    valores = ufs[colunas].to_numpy()
    fig, ax = plt.subplots(figsize=(6.6, 9.6), layout="constrained")
    imagem = ax.imshow(valores, cmap="YlGnBu", vmin=0, vmax=100, aspect="auto")
    for (linha, coluna), valor in np.ndenumerate(valores):
        ax.text(coluna, linha, f"{valor:.1f}".replace(".", ","), ha="center", va="center", fontsize=8.5,
                color="white" if valor > 55 else "#17323a")
    ax.set_xticks(range(4), ["Água\nurbana", "Água\nrural", "Esgoto\nurbano", "Esgoto\nrural"])
    ax.xaxis.tick_top()
    ax.set_yticks(range(len(ufs)), [f"{uf}" for uf in ufs["territorio"]], fontsize=9)
    ax.tick_params(length=0)
    ax.spines[:].set_visible(False)
    limites = ufs.groupby("ordem").size().cumsum().to_numpy()
    inicio = 0
    for fim, regiao in zip(limites, REGIOES):
        if fim < len(ufs):
            ax.axhline(fim - 0.5, color="white", linewidth=3)
        ax.text(3.62, (inicio + fim - 1) / 2, regiao, rotation=270, ha="left", va="center", fontsize=9,
                color="#4f6769")
        inicio = fim
    barra = fig.colorbar(imagem, ax=ax, orientation="horizontal", shrink=0.6, pad=0.02, aspect=30)
    barra.set_label("Atendimento no recorte (%)")
    barra.outline.set_visible(False)
    return fig


def grafico_cobertura(municipios):
    """Composição da união municipal por região: pares válidos e registros fora da comparação."""
    tabela = cobertura(municipios).drop(index="Brasil")
    fora = tabela["municipios"] - tabela["pares"]
    y = np.arange(len(tabela))
    fig, ax = plt.subplots(figsize=(9.2, 3.6), layout="constrained")
    ax.barh(y, tabela["pares"], color=AGUA, label="Pares válidos (água e esgoto)")
    ax.barh(y, fora, left=tabela["pares"], color="#c9d5d8", label="Fora da comparação")
    for posicao, (pares, total, parcela) in enumerate(
            zip(tabela["pares"], tabela["municipios"], tabela["pares_pct"])):
        ax.text(total + 18, posicao, f"{pares:,} de {total:,} ({parcela:.1f}%)"
                .replace(",", "X").replace(".", ",").replace("X", "."), va="center", fontsize=9)
    ax.set_yticks(y, tabela.index)
    ax.invert_yaxis()
    ax.set_xlim(0, tabela["municipios"].max() * 1.32)
    ax.set_xlabel("Municípios na união das duas bases")
    ax.xaxis.set_major_formatter(lambda valor, _: f"{valor:,.0f}".replace(",", "."))
    ax.grid(axis="x", color="#dce5e7")
    ax.set_axisbelow(True)
    ax.tick_params(length=0)
    ax.spines[["top", "right", "left", "bottom"]].set_visible(False)
    ax.legend(loc="lower left", bbox_to_anchor=(0, 1), ncols=2, frameon=False)
    return fig


def grafico_distribuicao(pares):
    """Histograma da diferença municipal e diagramas de caixa por região."""
    diferenca = pares[DIFERENCA]
    fig, (hist, caixas) = plt.subplots(1, 2, figsize=(10.4, 4.3), layout="constrained",
                                       gridspec_kw={"width_ratios": [1.15, 1]})
    hist.hist(diferenca, bins=np.arange(-70, 101, 5), color="#7fb3bd", edgecolor="white", linewidth=0.6)
    for valor, rotulo, estilo in [
        (diferenca.quantile(0.25), "Q1", ":"), (diferenca.median(), "Mediana", "-"),
        (diferenca.quantile(0.75), "Q3", "--"),
    ]:
        hist.axvline(valor, color="#123e43", linestyle=estilo, linewidth=1.5,
                     label=f"{rotulo}: {valor:.1f} p.p.".replace(".", ",", 1))
    hist.set_xlabel("Diferença água − esgoto (p.p.)")
    hist.set_ylabel("Municípios")
    hist.set_title(f"Distribuição nos {len(pares):,} pares".replace(",", "."), loc="left", fontsize=12)
    hist.legend(frameon=False, loc="upper right", fontsize=9)

    grupos = [pares.loc[pares["regiao"].eq(regiao), DIFERENCA] for regiao in REGIOES]
    desenho = caixas.boxplot(grupos, vert=False, patch_artist=True, widths=0.58,
                             medianprops={"color": "#123e43", "linewidth": 1.6},
                             flierprops={"marker": "o", "markersize": 2.5, "alpha": 0.45,
                                         "markerfacecolor": "#627279", "markeredgecolor": "none"})
    for caixa, cor in zip(desenho["boxes"], CORES_REGIAO):
        caixa.set(facecolor=cor, alpha=0.55, edgecolor=cor)
    caixas.set_yticks(range(1, len(REGIOES) + 1),
                      [f"{regiao} (n = {len(grupo):,})".replace(",", ".") for regiao, grupo in zip(REGIOES, grupos)])
    caixas.invert_yaxis()
    caixas.axvline(0, color=ESGOTO, linewidth=1)
    caixas.set_xlabel("Diferença água − esgoto (p.p.)")
    caixas.set_title("Por macrorregião", loc="left", fontsize=12)
    for ax in (hist, caixas):
        ax.grid(axis="x", color="#dce5e7")
        ax.set_axisbelow(True)
        ax.tick_params(length=0)
        ax.spines[["top", "right", "left"]].set_visible(False)
    return fig


def grafico_municipios(pares):
    fig = go.Figure()
    for regiao, cor in zip(REGIOES, CORES_REGIAO):
        dados = pares.loc[pares["regiao"].eq(regiao)]
        fig.add_trace(go.Scatter(
            x=dados["agua_total_pct"].tolist(),
            y=dados["esgoto_total_pct"].tolist(),
            mode="markers",
            name=f"{regiao} ({len(dados):,})".replace(",", "."),
            customdata=dados[["municipio", "uf", DIFERENCA, "prestador_agua", "prestador_esgoto"]]
                .fillna("não informado").values.tolist(),
            marker={"color": cor, "size": 6, "opacity": 0.64},
            hovertemplate="<b>%{customdata[0]} · %{customdata[1]}</b><br>"
                          "Água: %{x:.2f}% (%{customdata[3]})<br>Esgoto: %{y:.2f}% (%{customdata[4]})<br>"
                          "Diferença: %{customdata[2]:.2f} p.p.<extra></extra>",
        ))
    fig.add_shape(type="line", x0=0, x1=100, y0=0, y1=100,
                  line={"dash": "dash", "color": "#627279", "width": 1}, layer="below")
    # Linhas das metas de 2033: quem está acima e à direita das duas cumpre ambas.
    fig.add_vline(x=META_AGUA, line={"dash": "dot", "color": AGUA, "width": 1.2}, layer="below")
    fig.add_hline(y=META_ESGOTO, line={"dash": "dot", "color": ESGOTO, "width": 1.2}, layer="below")
    fig.add_annotation(x=1, y=META_ESGOTO, text="Meta esgoto 90%", showarrow=False, xanchor="left",
                       yanchor="bottom", font={"size": 11, "color": ESGOTO})
    fig.update_layout(
        template="plotly_white", margin={"l": 58, "r": 16, "t": 13, "b": 88},
        font={"family": "system-ui, sans-serif", "color": "#223e48", "size": 12},
        separators=",.", legend={"orientation": "h", "yanchor": "top", "y": -0.22},
        xaxis={"title": "Atendimento por rede de água", "range": [-2, 102], "ticksuffix": "%"},
        yaxis={"title": "Atendimento por rede de esgoto", "range": [-2, 102], "ticksuffix": "%"},
    )
    return fig


def gravar_interativo(figura, arquivo, titulo, identificador):
    """Grava o HTML do Plotly com idioma, título e viewport para telas pequenas."""
    figura.write_html(
        arquivo, include_plotlyjs="directory", div_id=identificador, default_height="96vh",
        config={"responsive": True, "displaylogo": False, "scrollZoom": False},
    )
    html = arquivo.read_text(encoding="utf-8")
    html = html.replace('<meta charset="utf-8" />',
                        '<meta charset="utf-8" /><meta name="viewport" content="width=device-width, initial-scale=1" />'
                        f"<title>{titulo}</title>")
    arquivo.write_text(html.replace("<html>", '<html lang="pt-BR">'), encoding="utf-8")


def publicar_figuras(municipios, territorios, pasta):
    pares = pares_validos(municipios)
    pasta.mkdir(parents=True, exist_ok=True)
    for nome, figura in [
        ("regioes", grafico_regioes(territorios)),
        ("urbano-rural", grafico_urbano_rural(territorios)),
        ("calor-ufs", grafico_calor_ufs(territorios)),
        ("cobertura", grafico_cobertura(municipios)),
        ("distribuicao", grafico_distribuicao(pares)),
    ]:
        figura.savefig(pasta / f"{nome}.png", dpi=180, facecolor="white")
        plt.close(figura)
    gravar_interativo(grafico_ufs(territorios), pasta / "ufs.html", "Água e esgoto por UF", "ufs")
    gravar_interativo(grafico_municipios(pares), pasta / "municipios.html",
                      "Água e esgoto por município", "municipios")


if __name__ == "__main__":
    pd.set_option("display.width", 200)
    municipios, territorios = carregar()
    pares = pares_validos(municipios)
    publicar_figuras(municipios, territorios, ROOT / "docs/assets")
    for titulo, saida in [
        ("RQ1 · UFs", pd.Series(resumo_ufs(territorios)).round(3)),
        ("RQ2 · recorte rural nas UFs", pd.Series(resumo_rural(territorios)).round(3)),
        ("RQ3 · cobertura da amostra", cobertura(municipios).round(1)),
        ("RQ3 · resumo por região", resumo_municipal(pares).round(3)),
        ("RQ3 · diagnóstico", pd.Series(diagnostico(pares)).round(3)),
        ("RQ3 · faixas de diferença", faixas(pares)),
        ("RQ3 · prestador de água", por_prestador(pares).round(2)),
    ]:
        print(f"\n{titulo}\n{saida.to_string()}")
