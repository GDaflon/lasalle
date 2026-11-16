package model;

import java.awt.geom.Rectangle2D;

/**
 * Golfinho: vem nadando com so a barbatana de fora e, ao chegar perto da surfista,
 * salta num arco que passa por cima dela e mergulha do outro lado. O ponto mais
 * baixo do arco fica rente a cabeca: em pe (ou pulando) ela e atingida; agachada,
 * passa por baixo.
 */
public class Dolphin extends Obstacle {

	/** Metade do comprimento do salto (da saida da agua ate o topo do arco). */
	public static final int HALF_JUMP = 220;
	/** Altura (acima da agua) da barriga do golfinho no topo do arco. */
	public static final int TOP_ALTITUDE = 70;
	/** Profundidade de onde ele sai e para onde volta. */
	public static final int DEPTH = 28;
	public static final int BODY_HEIGHT = 20;
	public static final int LENGTH = 96;

	private boolean jumping;
	private double apexX; // centro do arco na tela (posicao da surfista na hora do salto)

	public Dolphin(double x) {
		super(x, LENGTH, BODY_HEIGHT);
	}

	@Override
	public ObstacleType getType() {
		return ObstacleType.DOLPHIN;
	}

	@Override
	public double getSpeedBonus() {
		return 1.2;
	}

	/** Comeca o salto quando a surfista esta a meio salto de distancia. */
	@Override
	public void interact(Surfer surfer) {
		double surferCenter = surfer.getX() + Surfer.WIDTH / 2.0;
		if (!jumping && getCenterX() - surferCenter <= HALF_JUMP) {
			jumping = true;
			apexX = getCenterX() - HALF_JUMP;
		}
	}

	public double getCenterX() {
		return x + width / 2.0;
	}

	public boolean isJumping() {
		return jumping;
	}

	/** Posicao no arco: 1 = saindo da agua, 0 = topo, -1 = entrando na agua. */
	private double arcPosition(double centerX) {
		return (centerX - apexX) / HALF_JUMP;
	}

	/** Altura da barriga acima da agua em um ponto do arco (negativa = submerso). */
	private double altitudeAt(double centerX) {
		if (!jumping) {
			return -DEPTH;
		}
		double u = Math.abs(arcPosition(centerX));
		if (u >= 1) {
			return -DEPTH;
		}
		// curva com topo arredondado e laterais ingremes, como um salto de verdade
		return -DEPTH + (TOP_ALTITUDE + DEPTH) * (1 - u * u * u * u);
	}

	public double getAltitude() {
		return altitudeAt(getCenterX());
	}

	/** Inclinacao do corpo em radianos (positivo = nariz para cima). */
	public double getTilt() {
		double c = getCenterX();
		double slope = (altitudeAt(c - 4) - altitudeAt(c + 4)) / 8.0;
		return Math.max(-1.1, Math.min(1.1, Math.atan(slope)));
	}

	/** Ja passou: mergulhou depois do salto. */
	public boolean hasDived() {
		return jumping && arcPosition(getCenterX()) <= -1;
	}

	/** So machuca enquanto esta no ar. */
	public boolean isDangerous() {
		return jumping && !hasDived() && getAltitude() > 0;
	}

	@Override
	public Rectangle2D getBounds() {
		if (!isDangerous()) {
			return new Rectangle2D.Double(-1000, -1000, 0, 0);
		}
		double bottom = Surfer.FLOOR - getAltitude();
		return new Rectangle2D.Double(x + 10, bottom - BODY_HEIGHT, width - 20, BODY_HEIGHT);
	}
}
