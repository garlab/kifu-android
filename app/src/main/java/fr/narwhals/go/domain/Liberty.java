package fr.narwhals.go.domain;


public class Liberty extends Section {
	private static final long serialVersionUID = -8409620248088827028L;
	
	private Territory territory;

	public Liberty(SColor color, Point point, Goban goban) {
		super(color, point, goban);
		territory = null;
	}
	
	public Territory getTerritory() {
		return territory;
	}
	
	public boolean hasTerritory() {
		return territory != null;
	}
	
	public void setTerritory(Territory territory) {
		this.territory = territory;
	}
}
