/*
* Make It (makeitcenter.com) > CQ Polygraphmash Workshop > Roman Timashev (roman@tmshv.ru)
* Date::2013 [late]
* This code is licensed under the MIT license. (http://opensource.org/licenses/MIT)
*/

ITool tool;
PVector mouse;

PImage img;

void setup(){
	size(650, 650);
	mouse = new PVector(mouseX, mouseY);
	img = loadImage("map.png");

	tool = new MoveTool(this);
}

void draw(){
	mouse.x = mouseX;
	mouse.y = mouseY;

	if(mousePressed){
		tool.update(mouse);
	}

	image(img, 0, 0);
}

void mousePressed(){
	tool.on(mouse);
}

void mouseReleased(){
	tool.off(mouse);
}