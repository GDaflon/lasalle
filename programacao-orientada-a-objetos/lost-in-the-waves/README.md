# Lost in the Waves 🏄‍♀️

Jogo retrô de surfe em **Java Swing**, organizado em **MVC**, com a janela principal construída no **WindowBuilder** (Eclipse).

Uma onda derruba a surfista, que perde a costa de vista e precisa atravessar o oceano, por três fases cada vez mais difíceis, até encontrar a areia.

<p align="center">
  <img src="docs/gameplay.gif" alt="Trecho de uma partida" width="560">
</p>

| Trabalho | G2 de Programação Orientada a Objetos |
| --- | --- |
| **Disciplina** | Programação Orientada a Objetos (SII1P0504N0001) |
| **Curso** | Sistemas de Informação · Unilasalle-RJ |
| **Professor** | Jota Pê |
| **Aluno** | Guilherme Daflon Goulart Costa |

**Vídeos:** [partida completa, 3 fases (~2min40)](docs/videos/demo_lost_in_the_waves.mp4) · [destaques em 40 s](docs/videos/destaques_40s.mp4)<br>
**Documentos:** [documento de entrega](docs/entrega/documento-de-entrega.pdf) · [apresentação](docs/entrega/apresentacao.pdf)

## Como executar

### Eclipse (com WindowBuilder)

1. `File > Import > General > Existing Projects into Workspace` e selecione a pasta `lost-in-the-waves`.
2. Execute `src/app/Main.java` (`Run As > Java Application`).
3. Para editar a tela no WindowBuilder: botão direito em `src/view/TelaPrincipal.java` >
   `Open With > WindowBuilder Editor` > aba **Design**.

### Terminal (JDK 17 ou superior)

```bash
javac -d bin $(find src -name "*.java") && cp -R src/assets bin/ && java -cp bin app.Main
```

## Controles

| Tecla | Ação |
| --- | --- |
| Enter | Começar, avançar a história, próxima fase, tentar novamente |
| ← / → (ou A / D) | Mover |
| Espaço / ↑ (ou W) | Pular |
| ↓ (ou S) | Segurar para agachar e passar sob as gaivotas e os golfinhos |
| P | Pausar ou continuar |

O menu **Jogo** tem Novo jogo, Pausar/Continuar, Menu principal e Sair.
O menu **Ajuda** mostra os controles e os créditos.

## Regras

- 3 fases de 600 m: **Mar Aberto**, **Tempestade** e **A Última Onda**.
  Cada fase é mais rápida que a anterior e a praia aparece no fim da última.
- Colidir com um obstáculo tira 25 de energia. Com energia 0 é **game over**; ao tentar
  de novo, a fase recomeça com energia cheia e a pontuação do início da fase.
- Chegar à praia na última fase é a **vitória**: na cena final animada ela surfa do horizonte
  até a beira, pula da prancha para a areia, caminha até a toalha e comemora.
  A caminhada usa 4 quadros próprios da surfista andando.
- Na reta final da última fase a praia surge no mesmo estilo da cena final (mesmas peças da
  imagem da praia): água rasa, espuma em degraus indo e voltando, areia molhada e seca, morro com
  grama, rocha, guarda-sol com toalha, bola de praia e caranguejo. A beira para logo à frente da
  surfista: ela só desembarca na cena final.
- Caranguejo em pixel art (2 quadros): anda de lado mexendo as pernas e abrindo e fechando as garras.
- Pontuação: distância percorrida, desvios (com bônus de combo) e moedas (100).
- Itens (boiando no mar, com um brilho em volta): **coco** (+25 energia), **garrafa de água**
  (+15 energia), **moeda** (gira) e **escudo** (absorve um impacto ou expira após ~6 s).
- A bola de praia enviada pelo grupo enfeita a areia, na praia da última fase e na cena final.
- Obstáculos (desenhados em pixel art no SpriteRenderer): rocha, boia, tronco, gaivota (voa baixo, com sombra na água: agache para
  passar por baixo), tubarão (mais rápido, com barbatana e marola), golfinho (nada com a barbatana de fora e,
  perto da surfista, salta em arco por cima dela, rente à cabeça: agache para passar por baixo), onda (quebrando, desenhada na paleta do mar: parede em faixas, tubo escuro,
  crista de espuma, cortina d'água e espuma na base, animada em 8 quadros) e redemoinho (puxa a surfista para o centro).
- A surfista mantém o rosto e o cabelo da arte original. Na prancha ela fica na postura de surfe
  (pés abertos ao longo da prancha, joelhos dobrados, braços abertos para frente e para trás);
  agachada, o quadril desce, o joelho da frente fica sobre o pé da frente e o de trás desce em direção
  à prancha. A perna e o braço de trás têm um tom mais escuro para as pernas ficarem fáceis de ler.
  Na areia ela anda de perfil (4 quadros) e comemora com os braços para cima.
  Ao segurar ↓ ela desce passando por um quadro intermediário e sobe pelo mesmo caminho.
  A colisão agachada é mais baixa, e a de pé vai até o topo da cabeça.
- Prólogo animado em 3 cenas: a surfista no mar, a onda gigante que a derruba (ela gira
  no ar e a prancha voa longe) e ela nadando de volta e subindo na prancha.

## Telas

<p align="center">
  <img src="docs/menu.png" alt="Menu inicial" width="420">
  <img src="docs/print_tela_principal.png" alt="Partida em andamento" width="420">
</p>

## Estrutura (MVC)

```
src/
├── app/
│   └── Main.java              monta model + view + controller e inicia o jogo
├── model/                     regras do jogo (não conhece Swing)
│   ├── GameModel.java         estado da partida, game loop, fases, energia, vitória/derrota
│   ├── GameState.java         TITLE, INTRO, PLAYING, PAUSED, LOST, ROUTE, WON
│   ├── GameObject.java        classe abstrata base (posição, tamanho, colisão)
│   ├── Surfer.java            personagem do jogador (movimento, pulo, agachar)
│   ├── Obstacle.java          obstáculo abstrato + fábrica
│   ├── Rock, Buoy, Log, Gull, Shark, Dolphin, Wave, Whirlpool   obstáculos concretos
│   ├── ObstacleType.java      tipos de obstáculo
│   ├── Item.java / ItemType.java   itens coletáveis e seus efeitos
│   ├── Stage.java             dados de cada fase
│   ├── ScoreManager.java      pontos, combo e recorde
│   └── CollisionManager.java  regras de colisão
├── view/                      interface gráfica
│   ├── TelaPrincipal.java     JFrame do WindowBuilder: menu, HUD e painel do jogo
│   ├── GamePanel.java         JPanel que desenha o jogo (paintComponent)
│   ├── SpriteRenderer.java    desenho dos obstáculos e itens
│   └── Assets.java            carregamento das imagens
├── controller/
│   └── GameController.java    ActionListener (Timer e menus) + KeyListener (teclado)
└── assets/                    imagens do jogo
```

**Fluxo:** o `javax.swing.Timer` do `GameController` dispara a cada 16 ms →
o controller repassa as teclas para o `GameModel` e chama `update()` →
o model move objetos, verifica colisões e pontuação → o controller atualiza o HUD
da `TelaPrincipal` e pede `repaint()` ao `GamePanel`, que desenha o estado atual do model.

Conceitos de POO aplicados: **herança** (`GameObject` → `Surfer`, `Item`, `Obstacle` → obstáculos),
**polimorfismo** (`getSpeedBonus()`, `interact()` e `getBounds()` sobrescritos),
**classes abstratas**, **encapsulamento** (atributos privados com getters),
**enums** e separação de responsabilidades com MVC.

## Créditos da arte

A prancha (`board.png`) vem do **High Tides Basic Pack**,
de **Kanpelle** (https://kanpelle.itch.io/). Termos em `LICENCA_HIGH_TIDES.txt`.
