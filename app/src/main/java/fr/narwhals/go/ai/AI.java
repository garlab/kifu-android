package fr.narwhals.go.ai;

import java.io.Serializable;

import fr.narwhals.go.domain.Stone;

public abstract class AI implements Serializable {
	public abstract Stone getMove();
}
