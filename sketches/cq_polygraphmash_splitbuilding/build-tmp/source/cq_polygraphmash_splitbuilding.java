import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.pdf.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class cq_polygraphmash_splitbuilding extends PApplet {



ArrayList<Building> buildings;
ArrayList<SpaceUser> users;

PVector geoCenter = new PVector(30.3166f, 59.9695f);
PVector center;

public void setup(){
	size(700, 700);
	
	initBuildings("buildings.csv");
	initSpaceUsers("spsheet.csv");
	
	center = convertGeo(geoCenter);

	noLoop();
}

public void initBuildings(String file){
	Table table = loadTable(file, "header");
	buildings = new ArrayList<Building>();
	for (TableRow row : table.rows()) {
		String geo = row.getString("wkt_geom");
		PVector[] coords = readLineString(geo);
		Building b = new Building(row.getInt("id"), coords);
		b.poly = toScreen(coords);
		buildings.add(b);
	}
}

public void initSpaceUsers(String file){
	Table table = loadTable(file, "header");

	users = new ArrayList<SpaceUser>();
	for (TableRow row : table.rows()) {
		SpaceUser su = new SpaceUser(row.getInt("id"));
		su.buildingID = row.getInt("building_id");
		su.level = row.getInt("level");
		
		users.add(su);
	}
}

public void draw(){
	for(int i=0; i<8; i++){
		PGraphics pdf = createGraphics(width, height, PDF, "level"+i+".pdf");
		renderBack(pdf, 0xffffffff);
		for(SpaceUser su : users){
			if(su.level == i){
				Building b = getBuilding(su.buildingID);
				renderBuilding(pdf, b, 0xffdddddd);
			}
		}
		pdf.dispose();
	}
}

public Building getBuilding(int id){
	for(Building b : buildings){
		if(b.id == id) return b;
	}
	return null;
}

public void renderBack(PGraphics pg, int c){
	pg.beginDraw();
	pg.background(c);
	pg.endDraw();
}

public void renderBuilding(PGraphics pg, Building b, int buildingColor){
	pg.beginDraw();
	pg.pushMatrix();
	pg.translate(width/2, height/2);
	
	pg.pushStyle();
	pg.noStroke();
	pg.fill(buildingColor);
	PVector[] coords = b.poly;
	pg.beginShape();
	for(int i = 0; i<coords.length; i ++){
		PVector c = remap(coords[i]);
		pg.vertex(c.x, c.y);
	}
	pg.endShape(CLOSE);
	pg.popStyle();

	pg.endDraw();
	pg.popMatrix();
}

public PVector[] readLineString(String raw){
	String[] m = match(raw, "LINESTRING\\(([0123456789. ;]+)\\)");
	String digits = m[1];
	String[] geo = split(digits, ";");
	PVector[] coords = new PVector[geo.length];
	for(int i=0;i<geo.length;i++){
		String[] p = split(geo[i], " ");
		float x = PApplet.parseFloat(p[0]);
		float y = PApplet.parseFloat(p[1]);
		coords[i] = new PVector(x, y);
	}
	return coords;
}

public PVector remap(PVector coord){
	return remap(coord, 1);
}

public PVector remap(PVector coord, float scaleFactor){
	PVector out = PVector.sub(coord, center);
	out.mult(scaleFactor);
	// return out;

	PMatrix2D mtx = new PMatrix2D();
	mtx.scale(1, -1);
	return mtx.mult(out, new PVector(0, 0));
}

public PVector convertGeo(PVector coord){
	float[] xy = convertLatLongSpherToMerc(coord.x, coord.y);
	return new PVector(xy[0], xy[1]);
}

public PVector[] toScreen(PVector[] source){
	PVector[] out = new PVector[source.length];
	for(int i=0; i<source.length; i++){
		out[i] = convertGeo(source[i]);
	}
	return out;
}

public float[] convertLatLongSpherToMerc(float lon, float lat){
	if (lat > 89.5f) lat = 89.5f;
    if (lat < -89.5f) lat=-89.5f;

    float rLat = radians(lat);
    float rLong = radians(lon);
 
    float a = 6378137.0f;
    float x = a * rLong;
    float y = a * log(tan(PI/4+rLat/2));
    return new float[]{x, y};
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
public class SpaceUser{
	public int id;
	public int buildingID;
	public int level;

	public SpaceUser (int id) {
		this.id = id;
	}
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "cq_polygraphmash_splitbuilding" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
