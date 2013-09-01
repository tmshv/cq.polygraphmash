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

public class cq_polygraphmash_geomap extends PApplet {

Table table;
Table accessTable;

Remap remap;
ArrayList<PVector[]> geom;
ArrayList<PVector> accessPoints;

PVector tl;
PVector rb;

public void setup(){
	size(700, 700);
	table = loadTable("spsheet.csv", "header");
	accessTable = loadTable("access_points.csv", "header");
	
	tl = convertGeo(new PVector(30.314234f, 59.971177f));
	rb = convertGeo(new PVector(30.318649f, 59.967648f));

	PVector tlg = convertGeo(new PVector(30.314234f, 59.971177f));
	PVector rbg = convertGeo(new PVector(30.318649f, 59.967648f));
	tl = new PVector(min(tlg.x, rbg.x), min(tlg.y, rbg.y));
	rb = new PVector(max(tlg.x, rbg.x), max(tlg.y, rbg.y));

	println("tl: "+tl);
	println("rb: "+rb);

	//fill geom
	geom = new ArrayList<PVector[]>();
	for (TableRow row : table.rows()) {
		String geo = row.getString("wkt_geom");
		PVector[] coords = readLineString(geo);
		geom.add(toScreen(coords));
	}

	//fill access
	accessPoints = new ArrayList<PVector>();
	for (TableRow row : accessTable.rows()) {
		String geo = row.getString("wkt_geom");
		PVector coord = readPointString(geo);
		accessPoints.add(convertGeo(coord));
	}

	noLoop();
}

public void draw(){
	background(0xffffffff);

	// float minY = 100000000;
	// float maxY = 0;
	// for (TableRow row : table.rows()) {
	// 	String geo = row.getString("wkt_geom");
	// 	PVector[] coords = readLineString(geo);
		
	// 	for(int i=0; i<coords.length; i++){
	// 		PVector c = convertGeo(coords[i]);
	// 		if(minY > c.y){
	// 			minY = c.y;
	// 		}
	// 		if(maxY < c.y){
	// 			maxY = c.y;
	// 		}
	// 	}
	// }
	// double ratio = height / (maxY - minY);
	// remap = new Remap(minY, maxY, ratio);

	// for (TableRow row : table.rows()) {
	// 	String geo = row.getString("wkt_geom");
	// 	PVector[] coords = readLineString(geo);
	// 	drawLines(coords);
	// }

	
	// scale(1, -1);
	// translate(width/2, height/2);
	// rotate(HALF_PI);
	
	// translate(width, height);

	for(PVector[] coords : geom){
		beginShape();
		for(int i = 0; i<coords.length; i ++){
			// PVector c = coords[i];
			PVector c = remap(coords[i]);
			vertex(c.x, c.y);
			println(c);
		}
		endShape(CLOSE);
	}

	pushStyle();
	ellipseMode(CENTER);
	noStroke();
	fill(0xff99dd99);
	for(PVector coord : accessPoints){
		PVector c = remap(coord);
		ellipse(c.x, c.y, 10, 10);
	}
	popStyle();

	strokeWeight(10);
	PVector tlr = remap(tl);
	PVector rbr = remap(rb);
	line(tlr.x, tlr.y, rbr.x, rbr.y);
	noStroke();
	fill(0xff333333);
	ellipseMode(CENTER);
	ellipse(tlr.x, tlr.y, 15, 15);
	ellipse(rbr.x, rbr.y, 15, 15);
	println("tlr: "+tlr);	
	println("rbr: "+rbr);
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

public void drawLines(PVector[] coords){
	pushStyle();
	fill(0xffcccccc);
	noStroke();
	beginShape();
	for(int i=0; i<coords.length; i++){
		PVector sc = convertGeo(coords[i]);
		PVector c = remap.remap(sc);
		vertex(c.x, c.y);
	}
	endShape(CLOSE);
	popStyle();
}

public PVector remap(PVector coord){
	float h = rb.y - tl.y;
	float r = height / h;

	PVector out = PVector.sub(coord, tl);
	out.mult(r);
	// out.mult(0.1);
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
	float[] xy = convertLatLongToMerc(coord.x, coord.y);
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
public class Remap{
	double mny;
	double mxy;
	double r;
	public Remap (double mny, double mxy, double r) {
		this.mny = mny;
		this.mxy = mxy;
		this.r = r;
	}

	public PVector remap(PVector coord){
		PVector a = PVector.sub(coord, new PVector(0, (float) mny));
		a.mult((float) r);
		return a;
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
