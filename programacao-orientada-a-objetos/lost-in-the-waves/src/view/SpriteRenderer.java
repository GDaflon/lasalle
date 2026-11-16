package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import model.Dolphin;
import model.Gull;
import model.Item;
import model.Obstacle;
import model.Surfer;

/** Desenha obstaculos e itens em pixel art usando Graphics2D. */
public final class SpriteRenderer {

	private SpriteRenderer() {
	}

	public static void drawObstacle(Graphics2D g, Obstacle obstacle, int tick) {
		int left = (int) obstacle.getX();
		int width = obstacle.getWidth();
		int height = obstacle.getHeight();
		int floor = Surfer.FLOOR;
		switch (obstacle.getType()) {
		case GULL:
			drawGull(g, left, floor - Gull.FLIGHT_HEIGHT, tick, true);
			return;
		case DOLPHIN:
			drawDolphin(g, (Dolphin) obstacle, tick);
			return;
		default:
			g.setColor(new Color(0xD6F8F5));
			g.fillRect(left - 6, floor + 1, width + 14, 3);
		}
		switch (obstacle.getType()) {
		case ROCK:
			g.setColor(new Color(0x20334C));
			g.fillPolygon(new int[] { left, left + 9, left + 25, left + 38, left + width },
					new int[] { floor, floor - 23, floor - height, floor - 23, floor }, 5);
			g.setColor(new Color(0x6C8498));
			g.fillPolygon(new int[] { left + 9, left + 25, left + 20 },
					new int[] { floor - 23, floor - height, floor - 9 }, 3);
			break;
		case BUOY:
			g.setColor(new Color(0x822B40));
			g.fillRect(left + 13, floor - height, 6, 23);
			g.setColor(new Color(0xF8AF4D));
			g.fillRect(left + 8, floor - height + 4, 16, 6);
			g.setColor(new Color(0xE95B48));
			g.fillRect(left + 3, floor - 22, width - 6, 20);
			g.setColor(new Color(0xFFF2C0));
			g.fillRect(left + 3, floor - 16, width - 6, 6);
			break;
		case WAVE:
			drawWave(g, left, floor, 1.0, tick);
			break;
		case LOG:
			g.setColor(new Color(0x643C31));
			g.fillRoundRect(left, floor - 18, width, 18, 8, 8);
			g.setColor(new Color(0xBE8A59));
			g.fillOval(left + 2, floor - 16, 14, 14);
			g.drawLine(left + 20, floor - 13, left + 57, floor - 13);
			break;
		case WHIRLPOOL:
			g.setColor(new Color(0x19395E));
			g.fillOval(left, floor - 17, width, 22);
			g.setColor(new Color(0x7CCFDD));
			g.drawArc(left + 4, floor - 15, width - 8, 16, 35, 280);
			g.drawArc(left + 16, floor - 11, width - 32, 8, 215, 280);
			break;
		default: // SHARK
			drawShark(g, left, floor, tick);
		}
	}

	/**
	 * Gaivota voando para a esquerda, com bater de asas e sombra na agua
	 * (a sombra ajuda o jogador a perceber que ela voa baixo e que e preciso agachar).
	 */
	public static void drawGull(Graphics2D g, int left, int top, int tick, boolean shadow) {
		Graphics2D b = (Graphics2D) g.create();
		b.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		double flap = 0.3 + 0.7 * Math.sin(tick * 0.38); // asa desce pouco: nao encosta em quem agacha
		int bob = (int) Math.round(Math.sin(tick * 0.19) * 2);
		int y = top + bob;
		if (shadow) {
			b.setColor(new Color(0x10263C).darker());
			b.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.30f));
			b.fillOval(left + 10, Surfer.FLOOR - 4, 40, 7);
			b.setComposite(java.awt.AlphaComposite.SrcOver);
		}
		Color outline = new Color(0x17344E);
		// asa de tras (cinza)
		Path2D far = new Path2D.Double();
		far.moveTo(left + 26, y + 11);
		far.lineTo(left + 40, y + 11);
		far.lineTo(left + 46, y + 11 - 13 * flap);
		far.lineTo(left + 33, y + 10 - 11 * flap);
		far.closePath();
		b.setColor(new Color(0xA9B7C2));
		b.fill(far);
		// cauda
		b.setColor(new Color(0xE9EEF1));
		b.fillPolygon(new int[] { left + 44, left + 57, left + 55, left + 44 },
				new int[] { y + 10, y + 7, y + 15, y + 15 }, 4);
		// corpo e cabeca
		b.setColor(new Color(0xFBFDFE));
		b.fillOval(left + 12, y + 7, 36, 12);
		b.fillOval(left + 5, y + 3, 14, 13);
		b.setColor(new Color(0xC8D3DB));
		b.fillOval(left + 20, y + 7, 26, 5);
		b.setColor(outline);
		b.setStroke(new BasicStroke(1.4f));
		b.drawOval(left + 12, y + 7, 36, 12);
		b.drawArc(left + 5, y + 3, 14, 13, 40, 240);
		// bico e olho
		b.setColor(new Color(0xF7A23B));
		b.fillPolygon(new int[] { left - 3, left + 7, left + 7 }, new int[] { y + 10, y + 8, y + 12 }, 3);
		b.setColor(new Color(0x10263C));
		b.fillRect(left + 9, y + 7, 3, 3);
		// asa da frente (branca com ponta escura)
		Path2D near = new Path2D.Double();
		near.moveTo(left + 22, y + 12);
		near.lineTo(left + 38, y + 12);
		near.lineTo(left + 44, y + 12 - 19 * flap);
		near.lineTo(left + 28, y + 11 - 16 * flap);
		near.closePath();
		b.setColor(new Color(0xFFFFFF));
		b.fill(near);
		b.setColor(outline);
		b.draw(near);
		b.setColor(new Color(0x2B3A47));
		b.fillOval((int) (left + 39), (int) (y + 9 - 18 * flap), 7, 6);
		b.dispose();
	}

	// ------------------------------------------------------------------
	// Sprites em pixel art (cada letra e um "pixel" de 3x3)
	// ------------------------------------------------------------------

	private static final int PX = 3;

	/** Tubarao nadando para a esquerda, com barbatana, olho, dentes e barriga clara. */
	private static final String[] SHARK = {
			"............D.............",
			"...........DD.............",
			"..........DGD..........D..",
			".........DGGD.........DD..",
			"........DGGGD........DGD..",
			"....DDDDGGGGGDDDDDDDDGGD..",
			"..DDGGGGGGGGGGGGGGGGGGGD..",
			".DGGEGGDGDGGGGGGGGGGGGGGD.",
			"DGGGGGGDGDGGGGGGGGGGGGDGD.",
			"RTRTRBBBBBBBBBBBBBBBBGD.DD",
			".RBBBBBBBBBBBBBBBBBBBD....", };

	/**
	 * Golfinho virado para a esquerda: bico curto, testa arredondada, olho, sorriso,
	 * barbatana curvada para tras, barriga clara, nadadeira e cauda em forquilha.
	 */
	private static final String[] DOLPHIN = {
			"...................DD...........",
			".................DDGD...........",
			"...............DDGGD............",
			"........DDDDDDDGGGGDDDD.........",
			"......DDLLLLLLLLLLLLLLLDDD...DD.",
			".....DLLGGGGGGGGGGGGGGGGGGDD.DGD",
			"....DGGGGGGGGGGGGGGGGGGGGGGGDGD.",
			"...DGGEWGGGGGGGGGGGGGGGGGGGGGD..",
			"DDDGGGEEGGGGGGGGGGGGGGGGGDDDGD..",
			"DBBDDBBBGGGGGGGGGGGGGGGDD...DD..",
			".DDBBBBBBBBBBDGGDBBBBDD.........",
			"...DDDDDDDDDDDDGGDDDD...........",
			"..............DDD...............", };

	/** Linha do sprite que fica na base do corpo (usada para alinhar com a colisao). */
	private static final int DOLPHIN_BODY_BOTTOM = 11;

	/** Paleta propria do golfinho. */
	private static Color dolphinColor(char c) {
		switch (c) {
		case 'D':
			return new Color(0x1B3550);
		case 'G':
			return new Color(0x6A9AC2);
		case 'L':
			return new Color(0xA8CCE4);
		case 'B':
			return new Color(0xEAF4F8);
		case 'E':
			return new Color(0x0A1420);
		case 'W':
			return new Color(0xFFFFFF);
		default:
			return null;
		}
	}

	/**
	 * Golfinho: antes do salto so a barbatana corta a agua; depois ele sai num arco,
	 * inclinado conforme a curva (nariz para cima subindo, para baixo descendo), com
	 * sombra na agua avisando para agachar, gotas caindo e respingo na saida e na
	 * entrada. A parte abaixo da linha d'agua nao aparece.
	 */
	public static void drawDolphin(Graphics2D g, Dolphin dolphin, int tick) {
		int floor = Surfer.FLOOR;
		double x = dolphin.getX();
		double cx = dolphin.getCenterX();
		Color foam = new Color(0xF4FFFD);

		if (!dolphin.isJumping()) {
			// nadando submerso: vulto escuro e barbatana com marola
			Graphics2D s = (Graphics2D) g.create();
			s.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.25f));
			s.setColor(new Color(0x0B2740));
			s.fillOval((int) x + 6, floor + 1, 84, 9);
			s.dispose();
			int fx = (int) x + 46;
			g.setColor(new Color(0x1B3550));
			g.fillPolygon(new int[] { fx, fx + 9, fx + 16 }, new int[] { floor, floor - 15, floor }, 3);
			g.setColor(new Color(0x6A9AC2));
			g.fillPolygon(new int[] { fx + 4, fx + 9, fx + 12 }, new int[] { floor - 1, floor - 11, floor - 1 }, 3);
			g.setColor(foam);
			g.fillRect(fx - 6, floor - 1, 30, 2);
			int phase = tick % 12;
			g.fillRect(fx - 8 - phase, floor - 3, 4, 2);
			return;
		}
		if (dolphin.hasDived()) {
			return; // ja voltou para o fundo
		}

		double alt = dolphin.getAltitude();
		double angle = dolphin.getTilt();
		boolean rising = angle > 0;

		// sombra na agua embaixo dele
		if (alt > 0) {
			Graphics2D s = (Graphics2D) g.create();
			s.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.30f));
			s.setColor(new Color(0x0B2740));
			s.fillOval((int) cx - 34, floor - 3, 68, 7);
			s.dispose();
		}

		// corpo girado em torno do centro, escondendo o que esta debaixo d'agua
		int sw = DOLPHIN[0].length() * PX;
		double bottom = floor - alt;
		double pivotY = bottom - 12;
		int left = (int) Math.round(cx - sw / 2.0);
		int top = (int) Math.round(bottom - DOLPHIN_BODY_BOTTOM * PX);
		Graphics2D d = (Graphics2D) g.create();
		d.clipRect(-400, -400, 1600, floor + 400);
		d.rotate(angle, cx, pivotY);
		for (int r = 0; r < DOLPHIN.length; r++) {
			for (int c = 0; c < DOLPHIN[r].length(); c++) {
				Color color = dolphinColor(DOLPHIN[r].charAt(c));
				if (color != null) {
					d.setColor(color);
					d.fillRect(left + c * PX, top + r * PX, PX, PX);
				}
			}
		}
		// gotas escorrendo da cauda (acompanham a inclinacao)
		if (alt > 10) {
			d.setColor(foam);
			for (int i = 0; i < 4; i++) {
				int drop = (tick * 3 + i * 9) % 24;
				d.fillRect(left + sw - 8 + i * 3, (int) pivotY + drop - 6, 2, 3);
			}
		}
		d.dispose();

		// respingo na saida (atras dele, subindo) e na entrada (na frente, descendo)
		if (alt < 26) {
			double strength = (26 - Math.max(alt, -Dolphin.DEPTH)) / (26.0 + Dolphin.DEPTH);
			int sx = (int) (rising ? cx + 30 : cx - 30);
			int h = (int) (6 + 22 * Math.min(1, 1.6 - strength));
			g.setColor(foam);
			g.fillRect(sx - 22, floor - 2, 44, 3);
			for (int i = 0; i < 6; i++) {
				int dx = (i - 3) * 7 + 3;
				int dy = h - Math.abs(dx) / 2 + (tick + i) % 4;
				g.fillRect(sx + dx, floor - Math.max(3, dy), 3, 3);
			}
		}
	}

	private static Color pixelColor(char c) {
		switch (c) {
		case 'D':
			return new Color(0x22374B);
		case 'G':
			return new Color(0x587389);
		case 'B':
			return new Color(0xE4ECF0);
		case 'E':
			return new Color(0x0B1520);
		case 'R':
			return new Color(0x8E2F3F);
		case 'T':
		case 'W':
			return new Color(0xF4FFFD);
		case 'L':
			return new Color(0x8BE8EE);
		case 'M':
			return new Color(0x2AA8C4);
		case 'K':
			return new Color(0x0B4664);
		default:
			return null;
		}
	}

	private static void drawPixels(Graphics2D g, String[] rows, int x, int y, int px) {
		for (int r = 0; r < rows.length; r++) {
			for (int c = 0; c < rows[r].length(); c++) {
				Color color = pixelColor(rows[r].charAt(c));
				if (color != null) {
					g.setColor(color);
					g.fillRect(x + c * px, y + r * px, px, px);
				}
			}
		}
	}

	/** Tubarao meio submerso avancando rapido, com marola na frente do focinho. */
	public static void drawShark(Graphics2D g, int left, int floor, int tick) {
		int bob = (tick / 10) % 2;
		int top = floor - 31 + bob;
		drawPixels(g, SHARK, left - 6, top, PX);
		// espuma na linha d'agua
		g.setColor(new Color(0xF4FFFD));
		g.fillRect(left - 4, floor - 1, 78, 2);
		g.fillRect(left + 4, floor + 1, 60, 2);
		// marola e respingos no focinho (ele vem mais rapido que o mar)
		for (int i = 0; i < 4; i++) {
			int phase = (tick + i * 5) % 20;
			g.fillRect(left - 10 - phase, floor - 8 - (i % 2) * 3 + phase / 4, 3, 3);
		}
		g.fillRect(left - 12, floor - 6, 8, 3);
		// esteira atras da cauda
		for (int i = 0; i < 3; i++) {
			int dx = (i * 9 + tick * 2) % 27;
			g.fillRect(left + 76 + dx, floor - 4, 5 - i, 2);
		}
	}

	/** Quantidade de quadros da animacao da onda (lado a lado na imagem). */
	private static final int WAVE_FRAMES = 8;

	/**
	 * Onda quebrando para a esquerda (na direcao da surfista), desenhada na mesma paleta
	 * e no mesmo tamanho de pixel do mar: parede em faixas que seguem a curva, tubo escuro,
	 * crista de espuma com bolhas, cortina d'agua caindo da ponta e espuma na base.
	 * scale >= 2 usa a versao grande (prologo); senao, a do obstaculo.
	 */
	public static void drawWave(Graphics2D g, int x, int y, double scale, int tick) {
		int px = Math.max(PX, (int) Math.round(PX * scale));
		int left = x - 2 * px;
		int top = y - 15 * px;
		java.awt.image.BufferedImage strip = scale >= 2 ? Assets.WAVE_BIG : Assets.WAVE_SMALL;
		int fw = strip.getWidth() / WAVE_FRAMES;
		int fh = strip.getHeight();
		int frame = (tick / 5) % WAVE_FRAMES;
		g.drawImage(strip, left, top, left + 28 * px, top + 16 * px, frame * fw, 0, frame * fw + fw, fh, null);
	}

	/**
	 * Itens desenhados como objetos reconheciveis boiando na agua (sem caixa escura em volta):
	 * coco, garrafa de agua, moeda de ouro girando e bolha de escudo.
	 */
	public static void drawItem(Graphics2D g, Item item, int tick) {
		int left = (int) item.getX();
		int y = (int) item.getY() + (int) Math.round(Math.sin(tick * 0.12 + left * 0.05) * 2);
		int size = Item.SIZE;
		// brilho suave em volta para destacar o item no mar (inclusive na tempestade)
		g.setColor(new Color(0x55FFF6C8, true));
		g.fillOval(left - 5, y - 5, size + 10, size + 10);
		switch (item.getType()) {
		case WATER:
			g.setColor(new Color(0x1E4E8C));
			g.fillRect(left + 9, y + 1, 6, 4); // tampa
			g.setColor(new Color(0xBFEFFF));
			g.fillRect(left + 10, y + 5, 4, 3); // gargalo
			g.fillRoundRect(left + 6, y + 8, 12, 15, 4, 4); // garrafa
			g.setColor(new Color(0x3FB6F0));
			g.fillRoundRect(left + 7, y + 13, 10, 9, 3, 3); // agua dentro
			g.setColor(new Color(0xFFFFFF));
			g.fillRect(left + 8, y + 9, 2, 6); // reflexo
			g.setColor(new Color(0xE84855));
			g.fillRect(left + 6, y + 12, 12, 2); // rotulo
			break;
		case SHIELD:
			g.setColor(new Color(0x7FA0F1AA, true));
			g.fillOval(left, y, size, size);
			g.setColor(new Color(0xA0F1AA));
			g.setStroke(new BasicStroke(2));
			g.drawOval(left, y, size, size);
			g.setStroke(new BasicStroke(1));
			g.setColor(new Color(0x2E7D4F));
			g.fillPolygon(new int[] { left + 6, left + 18, left + 17, left + 12, left + 7 },
					new int[] { y + 6, y + 6, y + 14, y + 19, y + 14 }, 5);
			g.setColor(new Color(0xFFFFFF));
			g.fillRect(left + 5, y + 4, 3, 3);
			break;
		case COIN:
			int w = Math.max(3, (int) Math.round(Math.abs(Math.cos(tick * 0.09)) * 18));
			int cx = left + size / 2;
			g.setColor(new Color(0xB9771F));
			g.fillOval(cx - w / 2, y + 3, w, 18);
			g.setColor(new Color(0xFFD166));
			g.fillOval(cx - w / 2 + 1, y + 3, Math.max(2, w - 3), 16);
			if (w > 8) {
				g.setColor(new Color(0xB9771F));
				g.fillRect(cx - 1, y + 7, 3, 9);
				g.setColor(new Color(0xFFF4C2));
				g.fillRect(cx - w / 2 + 3, y + 6, 2, 4);
			}
			break;
		default: // COCO
			g.setColor(new Color(0x6E4426));
			g.fillOval(left + 3, y + 5, 18, 17);
			g.setColor(new Color(0x9C6A3E));
			g.fillOval(left + 6, y + 7, 9, 7);
			g.setColor(new Color(0x2E1B10));
			g.fillRect(left + 9, y + 10, 2, 2);
			g.fillRect(left + 13, y + 10, 2, 2);
			g.fillRect(left + 11, y + 14, 2, 2);
			g.setColor(new Color(0x4CAF50)); // folhinha
			g.fillPolygon(new int[] { left + 12, left + 20, left + 15 }, new int[] { y + 5, y, y + 6 }, 3);
		}
	}

	/** Bola de praia (imagem fornecida pelo grupo) parada na areia, com sombra. */
	public static void drawBeachBall(Graphics2D g, int x, int base, int size) {
		g.setColor(new Color(0x45A0752F, true));
		g.fillOval(x - 2, base - 4, size + 4, 8);
		g.drawImage(Assets.BALL, x, base - size, size, size, null);
	}
}
