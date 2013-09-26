/*
* Make It (makeitcenter.com) > CQ Polygraphmash Workshop > Roman Timashev (roman@tmshv.ru)
* Date::2013 [late]
* This code is licensed under the MIT license. (http://opensource.org/licenses/MIT)
*/

public interface ITool{
	String getName();
	
	void update(PVector coord);
	void on(PVector coord);
	void off(PVector coord);
}