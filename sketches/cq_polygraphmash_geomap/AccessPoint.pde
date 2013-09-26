/*
* Make It (makeitcenter.com) > CQ Polygraphmash Workshop > Roman Timashev (roman@tmshv.ru)
* Date::2013 [late]
* This code is licensed under the MIT license. (http://opensource.org/licenses/MIT)
*/

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