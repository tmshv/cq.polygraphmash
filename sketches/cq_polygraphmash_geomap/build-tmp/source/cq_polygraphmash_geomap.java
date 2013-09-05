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

public class cq_polygraphmash_geomap extends PApplet {



Table table;

ArrayList<PVector[]> geom;
ArrayList<AccessPoint> accessPoints;

PVector geoCenter = new PVector(30.3166f, 59.9695f);
PVector center;

PVector tl;
PVector rb;

public void setup(){
	size(700, 700);
	smooth();

	table = loadTable("spsheet.csv", "header");
	initAccessPoints("access_points.csv");
	
	tl = convertGeo(new PVector(30.314234f, 59.971177f));
	rb = convertGeo(new PVector(30.318649f, 59.967648f));
	center = convertGeo(geoCenter);

	PVector tlg = convertGeo(new PVector(30.314234f, 59.971177f));
	PVector rbg = convertGeo(new PVector(30.318649f, 59.967648f));
	tl = new PVector(min(tlg.x, rbg.x), min(tlg.y, rbg.y));
	rb = new PVector(max(tlg.x, rbg.x), max(tlg.y, rbg.y));

	//fill geom
	geom = new ArrayList<PVector[]>();
	for (TableRow row : table.rows()) {
		String geo = row.getString("wkt_geom");
		PVector[] coords = readLineString(geo);
		geom.add(toScreen(coords));
	}

	PGraphics pdf = createGraphics(width, height, PDF, "map.pdf");
	renderGeom(pdf);
	pdf.dispose();
	println("pdf: "+pdf);

	noLoop();
}

public void initAccessPoints(String file){
	Table table = loadTable(file, "header");
	accessPoints = new ArrayList<AccessPoint>();
	for (TableRow row : table.rows()) {
		String geo = row.getString("wkt_geom");
		PVector coord = readPointString(geo);

		int car = row.getInt("car");

		AccessPoint ap = new AccessPoint(row.getInt("id"), coord);
		ap.hasCarParking = car != 0; 
		ap.coord = convertGeo(coord);
		accessPoints.add(ap);
	}
}

public void draw(){
	renderGeom(g);
}

public void renderGeom(PGraphics pg){
	pg.beginDraw();
	pg.pushMatrix();
	pg.translate(width/2, height/2);
	// pg.rotate(TWO_PI);
	pg.scale(1, -1);

	pg.background(0xffffffff);

	//draw geom
	pg.pushStyle();
	noStroke();
	fill(0xff000000);
	for(PVector[] coords : geom){
		// fill(color(random(255), random(255), random(255)));
		pg.beginShape();
		for(int i = 0; i<coords.length; i ++){
			PVector c = remap(coords[i]);
			pg.vertex(c.x, c.y);
		}
		pg.endShape(CLOSE);
	}
	pg.popStyle();

	//draw entrance
	pg.pushStyle();
	pg.ellipseMode(CENTER);
	pg.noStroke();
	for(AccessPoint ap : accessPoints){
		pg.fill(ap.hasCarParking ? 0xffbbdd99 : 0xffdd99bb);
		PVector c = remap(ap.coord);
		pg.ellipse(c.x, c.y, 10, 10);
	}
	pg.popStyle();

	// pg.strokeWeight(10);
	// PVector tlr = remap(tl);
	// PVector rbr = remap(rb);
	// pg.line(tlr.x, tlr.y, rbr.x, rbr.y);
	// pg.noStroke();
	// pg.fill(#333333);
	// pg.ellipseMode(CENTER);
	// pg.ellipse(tlr.x, tlr.y, 15, 15);
	// pg.ellipse(rbr.x, rbr.y, 15, 15);
	pg.endDraw();
	pg.popMatrix();
}

public void renderAccessPoints(PGraphics pg){
	pg.beginDraw();
	pg.pushMatrix();
	pg.translate(width/2, height/2);
	pg.scale(1, -1);

	pg.pushStyle();
	pg.ellipseMode(CENTER);
	pg.noStroke();
	for(AccessPoint ap : accessPoints){
		pg.fill(ap.hasCarParking ? 0xffbbdd99 : 0xffdd99bb);
		PVector c = remap(ap.coord);
		pg.ellipse(c.x, c.y, 10, 10);
	}
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

public PVector readPointString(String raw){
	String[] m = match(raw, "POINT\\(([0123456789. ]+)\\)");
	String digits = m[1];
	String[] p = split(digits, " ");
	float x = PApplet.parseFloat(p[0]);
	float y = PApplet.parseFloat(p[1]);
	return new PVector(x, y);
}

public PVector remap(PVector coord){
	return remap(coord, 1);
	// float h = rb.y - tl.y;
	// float r = height / h;

	// PVector out = PVector.sub(coord, tl);
	// out.mult(r);
	// return out;
}

public PVector remap(PVector coord, float scaleFactor){
	PVector out = PVector.sub(coord, center);
	out.mult(scaleFactor);
	return out;
}

public PVector convertGeo(PVector coord){
	//1
	// return coord;

	//2
	// float lat = radians(coord.x);
	// float lon = radians(coord.y);
	// float R = 6383584;
	// float x = R * cos(lat) * cos(lon);
	// float y = R * cos(lat) * sin(lon);
	// float z = R * sin(lat);
	// return new PVector(x, y);
	
	//3
	// float[] xyz = getXYZfromLatLon(coord.x, coord.y, 0);
	// return new PVector(xyz[0], xyz[1]);

	//4
	// float[] xy = convertLatLongToMerc(coord.x, coord.y);
	// return new PVector(xy[0], xy[1]);

	//5
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

public float[] getXYZfromLatLon(float latitudeDeg, float longitudeDeg, float height){
	float latitude = radians(latitudeDeg);
	float longitude = radians(longitudeDeg);

    float a = 6378137.0f; //semi major axis
    float b = 6356752.3142f; //semi minor axis
    float cosLat = cos(latitude);
    float sinLat = sin(latitude);
	
    float rSubN = (a*a) / sqrt(((a*a) * (cosLat*cosLat) + ((b*b) * (sinLat*sinLat))));
	
    float X = (rSubN + height) * cosLat * cos(longitude);
    float Y = (rSubN + height) * cosLat * sin(longitude);
    float Z = ((((b*b) / (a*a)) * rSubN) + height) * sinLat;
	
    return new float[] {X, Y, Z};
}

public float[] convertLatLongToMerc(float lon, float lat){
    if (lat > 89.5f) lat = 89.5f;
    if (lat < -89.5f) lat=-89.5f;

    float rLat = radians(lat);
    float rLong = radians(lon);

    float a = 6378137.0f;
    float b = 6356752.3142f;
    float f = (a-b)/a;
    float e = sqrt(2*f - f*f);
    float x = a*rLong;
    float yy = tan(PI/4+rLat/2) * ((1-e*sin(rLat)) / (1+e*sin(rLat)));
    float y = a * log(pow(yy, (e/2)));
    return new float[]{x, y};
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
	public int brandwidth;
	public PVector geoCoord;
	public PVector coord;
	public boolean hasCarParking;

	public Building (int id, PVector geo) {
		this.id = id;
		this.geoCoord = geo;
	}
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "cq_polygraphmash_geomap" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
