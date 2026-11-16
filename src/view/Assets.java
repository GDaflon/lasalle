package view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/** Carrega as imagens da pasta src/assets (empacotadas junto com as classes). */
public final class Assets {

	public static final BufferedImage OCEAN = load("sea_day");
	public static final BufferedImage BEACH = load("beach");
	public static final BufferedImage SURFER = load("surfer");
	/** Quadros da agachada: meio caminho e agachada por completo. */
	public static final BufferedImage SURFER_CROUCH_1 = load("surfer_crouch1");
	public static final BufferedImage SURFER_CROUCH_2 = load("surfer_crouch2");
	/** Caminhada na areia (4 quadros) e comemoracao no final, sem a prancha. */
	public static final BufferedImage[] SURFER_WALK = { load("surfer_walk0"), load("surfer_walk1"),
			load("surfer_walk2"), load("surfer_walk3") };
	public static final BufferedImage SURFER_CHEER = load("surfer_cheer");
	/** Onda quebrando: 8 quadros lado a lado, na paleta do mar (grande para o prologo, pequena na fase). */
	public static final BufferedImage WAVE_BIG = load("wave_big");
	public static final BufferedImage WAVE_SMALL = load("wave_small");
	public static final BufferedImage BOARD = load("board");
	/** Caranguejo em pixel art: 2 quadros (pernas alternando, garras abrindo e fechando). */
	public static final BufferedImage[] CRAB = { load("crab_0"), load("crab_1") };
	/** Pecas recortadas da imagem da praia final, usadas tambem na praia da ultima fase. */
	public static final BufferedImage BEACH_ROCK = load("beach_rock");
	public static final BufferedImage BEACH_HILL = load("beach_hill");
	public static final BufferedImage BEACH_UMBRELLA = load("beach_umbrella");
	public static final BufferedImage BEACH_SAND = load("beach_sand");
	public static final BufferedImage BALL = load("ball");

	private Assets() {
	}

	private static BufferedImage load(String name) {
		URL url = Assets.class.getResource("/assets/" + name + ".png");
		if (url == null) {
			throw new IllegalStateException("Imagem nao encontrada: assets/" + name + ".png");
		}
		try {
			return ImageIO.read(url);
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao ler assets/" + name + ".png", e);
		}
	}
}
