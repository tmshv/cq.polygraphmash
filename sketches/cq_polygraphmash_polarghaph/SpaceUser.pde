/*
* Make It (makeitcenter.com) > CQ Polygraphmash Workshop > Roman Timashev (roman@tmshv.ru)
* Date::2013 [late]
* This code is licensed under the MIT license. (http://opensource.org/licenses/MIT)
*/

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