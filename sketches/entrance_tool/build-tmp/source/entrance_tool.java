import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class entrance_tool extends PApplet {

ITool tool;
PVector mouse;

PImage img;

public void setup(){
	size(650, 650);
	mouse = new PVector(mouseX, mouseY);
	img = loadImage("map.png");

	tool = new MoveTool(this);
}

public void draw(){
	mouse.x = mouseX;
	mouse.y = mouseY;

	if(mousePressed){
		tool.update(mouse);
	}

	image(img, 0, 0);
}

public void mousePressed(){
	tool.on(mouse);
}

public void mouseReleased(){
	tool.off(mouse);
}
public interface ITool{
	public String getName();
	
	public void update(PVector coord);
	public void on(PVector coord);
	public void off(PVector coord);
}
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
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "entrance_tool" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
