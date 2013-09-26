/*
* Make It (makeitcenter.com) > CQ Polygraphmash Workshop > Roman Timashev (roman@tmshv.ru)
* Date::2013 [late]
* This code is licensed under the MIT license. (http://opensource.org/licenses/MIT)
*/

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