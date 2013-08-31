import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 
import processing.pdf.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class polargraph2 extends PApplet {




ControlP5 cp5;

Map map;
MapRenderer view;

float beta;

public void setup(){
  size(650, 650);
  smooth();

  cp5 = new ControlP5(this);

  map = new Map();
  map.create("n1", 10, 0xff236721);
  map.create("n2", 50, 0xff582679);
  map.create("n3", 25, 0xff578294);
  map.create("n4", 25, 0xffeabd46);
  map.create("n5", 25, 0xfff901be);

  for(int i = 0; i<50; i++){
      map.create("n", (int) random(1, 100));
  }

  for(int i = 0; i<50; i++){
      map.create("n", (int) random(100, 1000));
  }

  map.link("n1", "n2", 23);
  map.link("n1", "n3", 5);
  map.link("n1", "n5", 65);

  map.link("n2", "n3", 46);
  map.link("n2", "n4", 46);
  map.link("n2", "n5", 46);

  view = new MapRenderer(map);
  view.center.x = width / 2;
  view.center.y = height /2;

  cp5.addSlider("beta")
    .setPosition(10, 10)
      .setSize(200, 20)
        .setRange(0, 1)
          .setValue(0);
}

public void draw(){
  view.beta = beta;

  background(0xffeeeeee);
  view.render();
}

public void keyPressed(){
  if(key == 'q'){
    noLoop();
    beginRecord(PDF, "Record.pdf");
    view.render();
    endRecord();
    loop();
  }
}
public class Item{
	public String name;
	public int power;
	public int c;

	public Item () {
		
	}
}
public class Link{
	public Item first;
	public Item second;
	public int power;
	public int c;

	public Link () {
		
	}
}
class Map{
	public ArrayList<Item> items;
	public ArrayList<Link> links;

	public Map(){
		items = new ArrayList<Item>();
		links = new ArrayList<Link>();
	}

	public Item create(String name, int power) {
		return create(name, power, color(random(255), random(255), random(255)));
	}

	public Item create(String name, int power, int c) {
		Item item = new Item();
		item.name = name;
		item.power = power;
		item.c = c;
		items.add(item);
		return item;
	}

	public Link link(String first, String second, int power) {
		Item f = getItem(first);
		Item s = getItem(second);
		return link(f, s, power);
	}

	public Link link(String first, String second, int power, int c) {
		Item f = getItem(first);
		Item s = getItem(second);
		return link(f, s, power, c);
	}

	public Link link(Item first, Item second, int power) {
		return link(first, second, power, 0xffcccccc);
	}

	public Link link(Item first, Item second, int power, int c) {
		Link l = new Link();
		l.first = first;
		l.second = second;
		l.power = power;
		l.c = c;
		links.add(l);
		return l;
	}

	public Item getItem(String name) {
		for(Item i : items){
			if(i.name == name){
				return i;
			}
		}
		return null;
	}

	public int calcTotalPower() {
		int total = 0;
		for(Item i : items){
			total += i.power;
		}
		return total;
	}

	public int calcMaxLinkPower() {
		int max = 0;
		for(Link i : links){
			if(max < i.power){
				max = i.power;	
			}
		}
		return max;
	}	
}
public class MapRenderer{
	private Map map;

	public int itemThickness = 8;
	public int maxLinkThickness = 4;
	public int radius = 200;
	public float startArcAngle = 0;
	public PVector center = new PVector();

	public float beta = 0;
	public float bezierStepT = 0.025f;

	private ArrayList<ItemView> itemsView;

	public MapRenderer (Map map) {
		this.map = map;
		computeItems();
	}

	public void render() {
		if(itemsView.size() != map.items.size()){
			computeItems();
		}

		pushMatrix();
		translate(center.x, center.y);

		pushMatrix();
		renderLinks();
		popMatrix();

		pushMatrix();
		renderItems();
		popMatrix();

		ellipseMode(CENTER);
		fill(0xffffffff);
		noStroke();
		for(ItemView v : itemsView){
			PVector c = v.calcCoord(center, radius);
			// ellipse(c.x, c.y, 10, 10);
		}

		popMatrix();
	}

	public void renderItems() {
		strokeCap(PROJECT);
		int w = radius * 2;
		noFill();

		int i = 0;
		// int total = map.calcTotalPower();

		for(ItemView view : itemsView){
			Item item = view.item;
			pushMatrix();
			stroke(item.c);
			strokeWeight(itemThickness);

			// arc(center.x, center.y, w, w, view.angleStart, view.angleStop);
			arc(0, 0, w, w, view.angleStart, view.angleStop);

			popMatrix();
			i += 1;
		}
	}

	public void renderLinks() {
		strokeCap(ROUND);
		int maxPower = map.calcMaxLinkPower();
		int i = 0;
		for(Link link : map.links){
			Item firstItem = link.first;
			Item secondItem = link.second;
			ItemView firstView = getView(firstItem);
			ItemView secondView = getView(secondItem);

			PVector firstCoord = firstView.calcCoord(center, radius);
			PVector secondCoord = secondView.calcCoord(center, radius);

			float ratio = link.power / (float) maxPower;
			int w = (int) (ratio * maxLinkThickness);
			w = w < 1 ? 1 : w;
			stroke(link.c);
			strokeWeight(w);
			// line(firstCoord.x, firstCoord.y, secondCoord.x, secondCoord.y);
			qubic(firstCoord, secondCoord);
			i += 1;
		}		
	}

	private void qubic(PVector start, PVector end){
		float a0 = start.x;
		float b0 = start.y;
		// float a1 = center.x;
		// float b1 = center.y;
		// float a2 = center.x;
		// float b2 = center.y;
		float a1 = 0;
		float b1 = 0;
		float a2 = 0;
		float b2 = 0;
		float a3 = end.x;
		float b3 = end.y;

		float xPrev;
		float yPrev;
		float xNext;
		float yNext;

		xPrev = beta * a0 + (1 - beta) * start.x;
	    yPrev = beta * b0 + (1 - beta) * start.y;

		float t = 0;
		while(t <= 1){
			float bX = bezierPoint(a0, a1, a2, a3, t);
			float bY = bezierPoint(b0, b1, b2, b3, t);

			xNext = beta * bX + (1 - beta) * (start.x + (end.x - start.x) * t);
			yNext = beta * bY + (1 - beta) * (start.y + (end.y - start.y) * t);

			// xNext = beta * (a3 * t * t * t + a2 * t * t + a1 * t + a0) + (1 - beta) * (start.x + (end.x - start.x) * t);
			// yNext = beta * (b3 * t * t * t + b2 * t * t + b1 * t + b0) + (1 - beta) * (start.y + (end.y - start.y) * t);	

			line(xPrev, yPrev, xNext, yNext);

			xPrev = xNext;
			yPrev = yNext;
			t += bezierStepT;	
		}
	}

	private ItemView getView(Item item) {
		for(ItemView v : itemsView){
			if(v.item == item) return v;
		}
		return null;
	}

	private void computeItems() {
		itemsView = new ArrayList<ItemView>();

		int i = 0;
		int total = map.calcTotalPower();

		float start_angle = startArcAngle;
		for(Item item : map.items){
			float circ_ratio = item.power / (float) total;
			float angle = circ_ratio * TWO_PI;
			float stop_angle = start_angle + angle;
			float center_angle = start_angle + angle/2;
			
			ItemView view = new ItemView(item);
			view.angleStart = start_angle;
			view.angleCenter = center_angle;
			view.angleStop = stop_angle;
			itemsView.add(view);

			start_angle = stop_angle;
			i += 1;
		}	
	}
}

class ItemView{
	public Item item;
	public float angleStart;
	public float angleCenter;
	public float angleStop;

	private ItemView(Item item){
		this.item = item;
	}

	public PVector calcCoord(PVector center, int radius) {
		PVector coord = PVector.fromAngle(angleCenter);
		coord.mult(radius);
		// coord.add(center);
		return coord;
	}
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "polargraph2" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
