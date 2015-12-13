package fr.narwhals.go.ai;

import java.io.Serializable;

import fr.narwhals.go.domain.Stone;

public abstract class AI implements Serializable {
	private static final long serialVersionUID = -8943525867759322994L;

	public abstract Stone getMove();
}
