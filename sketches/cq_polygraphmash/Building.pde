public class Building{
	public int id;
	public int c;
	public PVector[] geoPoly;
	public PVector[] poly;

	public Building (int id, PVector[] geoPoly) {
		this.id = id;
		this.geoPoly = geoPoly;
	}
}