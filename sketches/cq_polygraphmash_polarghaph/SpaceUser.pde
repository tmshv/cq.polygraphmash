public class SpaceUser implements IData{
	public int id;
	public int buildingID;
	public int level;
	public int accessPointID;
	public String func;
	public String comment;

	public SpaceUser (int id) {
		this.id = id;
	}
}