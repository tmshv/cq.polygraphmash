public class Building{
	public int id;
	public int brandwidth;
	public PVector geoCoord;
	public PVector coord;
	public boolean hasCarParking;

	public Building (int id, PVector geo) {
		this.id = id;
		this.geoCoord = geo;
	}
}