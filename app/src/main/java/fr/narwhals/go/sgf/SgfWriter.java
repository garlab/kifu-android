package fr.narwhals.go.sgf;

import java.io.IOException;
import java.io.Writer;

import fr.narwhals.go.domain.Point;

public class SgfWriter {

	private Writer writer;

	public SgfWriter(Writer out) {
		this.writer = out;
	}
	
	public SgfWriter beginGameTree() throws IOException {
		writer.write("(");
		return this;
	}
	
	public SgfWriter endGameTree() throws IOException {
		writer.write(")");
		return this;
	}
	
	public SgfWriter beginNode() throws IOException {
		writer.write(";");
		return this;
	}
	
	public SgfWriter write(String key, Point value) throws IOException {
		return write(key, getString(value));
	}
	
	public SgfWriter write(String key, double value) throws IOException {
		return write(key, String.valueOf(value));
	}
	
	public SgfWriter write(String key, int value) throws IOException {
		return write(key, String.valueOf(value));
	}
	
	public SgfWriter write(String key, String value) throws IOException {
		writer.write(key);
		writer.write(String.format("[%s]", value));
		return this;
	}
	
	public SgfWriter write(String key, Point[] values) throws IOException {
		writer.write(key);
		for (Point value : values) {
			writer.write(String.format("[%s]", getString(value)));
		}
		return this;
	}
	
	public static String getString(Point point) {
		char[] value = { (char) (point.getX() - 1 + 'a'), (char) (point.getY() - 1 + 'a')};
		return String.valueOf(value);
	}
}
