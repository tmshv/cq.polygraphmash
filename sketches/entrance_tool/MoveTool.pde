/*
* Make It (makeitcenter.com) > CQ Polygraphmash Workshop > Roman Timashev (roman@tmshv.ru)
* Date::2013 [late]
* This code is licensed under the MIT license. (http://opensource.org/licenses/MIT)
*/

public class MoveTool implements ITool{
	private PVector startCoord;
	private PApplet context;

	public String getName(){
		return "move";
	}

	public MoveTool(PApplet context) {
		startCoord = new PVector();
		this.context = context;
	}

	public void update(PVector coord) {
		// PVector a = PVector.sub(startCoord, coord);
		PVector a = PVector.sub(coord, startCoord);

		context.translate(a.x, a.y);
	}

	public void on(PVector coord) {
		startCoord.x = coord.x;
		startCoord.y = coord.y;
	}

	public void off(PVector coord) {
		
	}
}