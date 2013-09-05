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

public class cq_polygraphmash_polarghaph extends PApplet {




ArrayList<SpaceUser> users;
ArrayList<SpaceFunction> fu;
Table distTable;
Map map;
MapRenderer view;

float beta;
int thickness;
int lthickness;
int radius;
int rot;

ControlP5 cp5;

public void setup(){
	size(800, 800);
	int pos = 10;
	cp5 = new ControlP5(this);
	cp5.addSlider("beta")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(0, 1)
          .setValue(0);
    pos += 25;
    cp5.addSlider("thickness")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(1, 50)
          .setValue(20);
    pos += 25;
    cp5.addSlider("lthickness")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(1, 10)
          .setValue(1);
    pos += 25;
    cp5.addSlider("radius")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(100, width)
          .setValue(width*0.3f);
    pos += 25;
    cp5.addSlider("rot")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(0, 360)
          .setValue(0);

	initSpaceUsers("space_users.csv");
	distTable = loadTable("dist.csv", "header");

	// PGraphics pdf = createGraphics(width, height, PDF, "map.pdf");
	// renderBuildings(pdf);
	// pdf.dispose();

	fu = new ArrayList<SpaceFunction>();
	fu.add(new SpaceFunction("production",		0xff634673));
	fu.add(new SpaceFunction("entertainment",	0xffFF6600));
	fu.add(new SpaceFunction("service",			0xff8B9DC3));
	fu.add(new SpaceFunction("art",				0xffFC6666));
	fu.add(new SpaceFunction("education",		0xff98DAD3));
	fu.add(new SpaceFunction("restaurant",		0xffF7B3B3));
	fu.add(new SpaceFunction("retail",			0xffCC0066));
	fu.add(new SpaceFunction("empty",			0xff949B98));
	fu.add(new SpaceFunction("office",			0xff949BFF));

	map = new Map();
	view = new MapRenderer(map);
	view.center.x = width / 2;
	view.center.y = height /2;

	fillGraph2();
	// fillGraph();
	// noLoop();
}

public void initSpaceUsers(String file){
	Table table = loadTable(file, "header");

	users = new ArrayList<SpaceUser>();
	for (TableRow row : table.rows()) {
		SpaceUser su = new SpaceUser(row.getInt("id"));
		su.buildingID = row.getInt("building_id");
		su.accessPointID = row.getInt("access_point");
		su.level = row.getInt("level");
		su.func = row.getString("function");
		su.comment = row.getString("comment");
		users.add(su);
	}
}

public void fillGraph(){
	for(SpaceUser su : users){
		SpaceFunction f = getSF(su.func);
		if(f == null) println("not found function: "+su.func);
		else f.users.add(su);
	}

	for (SpaceFunction sf : fu){
		map.create(sf.name, sf.calcPower(), sf.c);
	}

	for(SpaceFunction sf1 : fu){
		for(SpaceFunction sf2 : fu){
			if(!sf1.name.equals(sf2.name)){
				if(!map.hasLink(sf1.name, sf2.name, false)){
					map.link(sf1.name, sf2.name, 1);
				}
			}
		}
	}
}

public void fillGraph2(){
	for(SpaceUser su : users){
		SpaceFunction f = getSF(su.func);
		if(f == null) println("not found function: "+su.func);
		else f.users.add(su);
	}
	users = new ArrayList<SpaceUser>();
	for(SpaceFunction sf : fu){
		for(SpaceUser su : sf.users){
			users.add(su);
		}
	}

	int i = 0;
	for(SpaceUser su : users){
		// if(i > 10) break;
		SpaceFunction sf = getSF(su.func);
		String name = str(su.id) +":"+su.comment;
		Item item = map.create(name, 1, sf.c);
		item.data = su;
		i ++;
	}
	
	for(Item item1 : map.items){
		for(Item item2 : map.items){
			String itemName1 = item1.name;
			String itemName2 = item2.name;
			if(!itemName1.equals(itemName2)){
				if(!map.hasLink(itemName1, itemName2, false)){
					SpaceUser su1 = (SpaceUser) item1.data;
					SpaceUser su2 = (SpaceUser) item2.data;
					map.link(itemName1, itemName2, (int) getDist(su1, su2));
				}
			}	
		}		
	}
	println("map.links.size(): "+map.links.size());
}

// int calcLinkPower()

public void draw(){
	view.beta = beta;
	view.itemThickness = thickness;
	view.maxLinkThickness = lthickness;
	view.radius = radius;
	view.startArcAngle = radians(rot);



	background(0xffcccccc);
	view.render();

	float ma = atan2((mouseY - view.center.y),(mouseX - view.center.x));
	view.selectItem(ma);
	text(str(degrees(ma)), 100, 10);
}

public SpaceFunction getSF(String name){
	for(SpaceFunction sf : fu){
		if(name.equals(sf.name)) return sf;
	}
	return null;
}

public float getDist(SpaceUser su1, SpaceUser su2){
	for (TableRow row : distTable.rows()) {
		int ap1 = row.getInt("access_point1");
		int ap2 = row.getInt("access_point2");

		if(su1.accessPointID == ap1 && su2.accessPointID == ap2){
			return row.getFloat("dist");
		}
	}
	return 0;
}

public void keyPressed(){
	if(key == 'u'){
		view.computeItems();
		background(0xffcccccc);
		view.render();
	}
}
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
public interface IData{
	
}
public class Item{
	public String name;
	public int power;
	public int c;

	public IData data;

	public Item () {
		
	}
}
class ItemView{
	public Item item;
	public float angleStart;
	public float angleCenter;
	public float angleStop;

	private boolean select;

	private ItemView(Item item){
		this.item = item;
	}

	public PVector calcCoord(PVector center, int radius) {
		PVector coord = PVector.fromAngle(angleCenter);
		coord.mult(radius);
		// coord.add(center);
		return coord;
	}

	public void select(){
		select = true;
	}

	public void deselect(){
		select = false;
	}

	public boolean idSelected(){
		return select;
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
		return link(first, second, power, 0xffffffff);
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

	public boolean hasLink(String first, String second, boolean strongDir){
		Item f = getItem(first);
		Item s = getItem(second);
		return hasLink(f, s, strongDir);
	}

	public boolean hasLink(Item first, Item second, boolean strongDir){
		for(Link link : links){
			boolean direct = (link.first == first) && (link.second == second);
			if(strongDir && direct){
				return true;
			}

			boolean d1 = (link.first == first) || (link.first == second);
			boolean d2 = (link.second == first) || (link.second == second);
			if(d1 && d2){
				return true;
			}
		}
		return false;
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

	private ItemView selected;
	private ItemView highlighted;

	public MapRenderer (Map map) {
		this.map = map;
		computeItems();
	}

	public void selectItem (float angle){
		// if(selected != null) selected.deselect();
		for(ItemView iv : itemsView){
			if(iv.angleStart < angle && iv.angleStop > angle){
				selected = iv;
			}
		}
	}

	public void highlightItem (float angle){
		for(ItemView iv : itemsView){
			if(iv.angleStart < angle && iv.angleStop > angle){
				highlighted = iv;
			}
		}
	}

	public void render() {
		// computeItems();
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
		strokeCap(SQUARE);
		int w = radius * 2;
		noFill();

		int i = 0;
		// int total = map.calcTotalPower();

		for(ItemView view : itemsView){
			pushMatrix();
			Item item = view.item;
			stroke(item.c);
			strokeWeight(itemThickness);

			arc(0, 0, w, w, view.angleStart, view.angleStop);

			PVector tcoord = view.calcCoord(new PVector(), radius+20+itemThickness/2);
			translate(tcoord.x, tcoord.y);
			rotate(view.angleCenter);
			stroke(0xffffffff);
			text(item.name, 0, 0);

			i += 1;
			popMatrix();
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

			if(firstView != selected) continue;

			PVector firstCoord = firstView.calcCoord(center, radius);
			PVector secondCoord = secondView.calcCoord(center, radius);

			float ratio = link.power / (float) maxPower;
			int w = (int) (ratio * maxLinkThickness);
			w = w < 1 ? 1 : w;
			stroke(link.c, 200);
			// stroke(link.c);
			strokeWeight(w);
			// line(firstCoord.x, firstCoord.y, secondCoord.x, secondCoord.y);
			qubic(firstCoord, secondCoord);
			// bezier(firstCoord.x, firstCoord.y, 0, 0, 0, 0, secondCoord.x, secondCoord.y);
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

	public void computeItems() {
		itemsView = new ArrayList<ItemView>();

		int i = 0;
		int total = map.calcTotalPower();

		float start_angle = startArcAngle;
		for(Item item : map.items){
			float circ_ratio = (float) item.power / (float) total;
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
public class SpaceFunction{
	public String name;
	public ArrayList<SpaceUser> users;
	public int c;

	public SpaceFunction (String name, int c) {
		this.name = name;
		this.c = c;
		this.users = new ArrayList<SpaceUser>();
	}

	public int calcPower(){
		int s = users.size();
		return s == 0 ? 1 : s;
		// int total = 0;
		// for(SpaceUser su : users){
		// 	total += su.power;
		// }
		// return total;
	}
}
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
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "cq_polygraphmash_polarghaph" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
