public class AccessPoint{
	public int id;
	public int brandwidth;
	public PVector geoCoord;
	public PVector coord;
	public boolean hasCarParking;

	public AccessPoint (int id, PVector geo) {
		this.id = id;
		this.geoCoord = geo;
	}
}