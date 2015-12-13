package fr.narwhals.go.domain;

import java.util.List;

//import fr.narwhals.utils.Tree;

public class Hist {
	int round;
	List<Stone> handicaps;
	//Tree<Move> history;
	
	public final Score score;
	
	public Hist(Score score) {
		this.score = score;
	}
	
	
}
