package model;

import java.awt.geom.Rectangle2D;

/**
 * Personagem controlada pelo jogador. A coordenada y representa a linha dos pes.
 */
public class Surfer extends GameObject {

	public static final int WIDTH = 68;
	public static final int HEIGHT = 72;
	public static final int FLOOR = 414;
	public static final int CROUCH_HEIGHT = 50;
	/** Altura da area de colisao em pe (ate o topo da cabeca). */
	public static final int STAND_HEIGHT = 72;
	/** Quadros que a animacao leva para descer (ou subir) da agachada. */
	public static final int CROUCH_FRAMES = 8;
	private static final int MIN_X = 35;
	private static final int MAX_X = 310;
	private static final double STEP = 4.4;
	private static final double JUMP_FORCE = -13.8;
	private static final double GRAVITY = 0.67;

	private double velocity;
	private boolean landed;
	private boolean crouching;
	private int crouchFrame;

	public Surfer() {
		super(132, FLOOR, WIDTH, HEIGHT);
		reset();
	}

	public void reset() {
		x = 132;
		land();
	}

	public void land() {
		y = FLOOR;
		velocity = 0;
		landed = true;
		crouching = false;
		crouchFrame = 0;
	}

	public void jump() {
		if (landed && !crouching) {
			velocity = JUMP_FORCE;
			landed = false;
		}
	}

	/** Move na horizontal (direcao -1, 0 ou 1) e aplica a gravidade do pulo. */
	public void move(int direction) {
		if (crouching && crouchFrame < CROUCH_FRAMES) {
			crouchFrame++;
		} else if (!crouching && crouchFrame > 0) {
			crouchFrame--;
		}
		push(direction * STEP);
		if (!landed) {
			y += velocity;
			velocity += GRAVITY;
			if (y >= FLOOR) {
				land();
			}
		}
	}

	/** Empurra a surfista na horizontal respeitando os limites da tela. */
	public void push(double dx) {
		x = Math.max(MIN_X, Math.min(MAX_X, x + dx));
	}

	@Override
	public Rectangle2D getBounds() {
		int h = crouching ? CROUCH_HEIGHT : STAND_HEIGHT;
		return new Rectangle2D.Double(x + 8, y - h, WIDTH - 16, h - 3);
	}

	public boolean isLanded() {
		return landed;
	}

	public boolean isCrouching() {
		return crouching;
	}

	/** Etapa da animacao de agachar: 0 = em pe ... CROUCH_FRAMES = agachada. */
	public int getCrouchFrame() {
		return crouchFrame;
	}

	public void setCrouching(boolean crouching) {
		this.crouching = crouching;
	}
}
