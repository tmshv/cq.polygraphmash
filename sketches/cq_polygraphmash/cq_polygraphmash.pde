/*
* Make It (makeitcenter.com) > CQ Polygraphmash Workshop > Roman Timashev (roman@tmshv.ru)
* Date::2013 [late]
* This code is licensed under the MIT license. (http://opensource.org/licenses/MIT)
*/

import processing.pdf.*;

CQGraph graph;

Table table;

ArrayList<Building> buildings;
ArrayList<AccessPoint> accessPoints;
ArrayList<SpaceUser> users;

PVector geoCenter = new PVector(30.3166, 59.9695);
PVector center;

PVector tl;
PVector rb;

void setup(){
	size(800, 800);
	
	// initBuildings("buildings.csv");
	initBuildings("buildings_arcs.csv");
	initAccessPoints("access_points.csv");
	initSpaceUsers("space_users.csv");
	
	tl = convertGeo(new PVector(30.314234, 59.971177));
	rb = convertGeo(new PVector(30.318649, 59.967648));
	center = convertGeo(geoCenter);

	PVector tlg = convertGeo(new PVector(30.314234, 59.971177));
	PVector rbg = convertGeo(new PVector(30.318649, 59.967648));
	tl = new PVector(min(tlg.x, rbg.x), min(tlg.y, rbg.y));
	rb = new PVector(max(tlg.x, rbg.x), max(tlg.y, rbg.y));

	// PGraphics pdf = createGraphics(width, height, PDF, "map.pdf");
	// renderBuildings(pdf);
	// pdf.dispose();

	PGraphics dump = createGraphics(width, height);
	renderBack(dump, #ffffff);
	renderBuildings(dump, #000000);
	dump.save("dump.png");
	graph = new CQGraph(dump);

	noLoop();
}

void initBuildings(String file){
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

void initAccessPoints(String file){
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

void initSpaceUsers(String file){
	Table table = loadTable(file, "header");

	users = new ArrayList<SpaceUser>();
	for (TableRow row : table.rows()) {
		SpaceUser su = new SpaceUser(row.getInt("id"));
		su.buildingID = row.getInt("building_id");
		su.level = row.getInt("level");
		
		users.add(su);
	}
}

void draw(){
	renderBack(g, #ffffff);
	renderBuildings(g, #dddddd);
	// graph.drawEdges(g, #cccccc);
	renderAccessPoints(g);

	AccessPoint ap1 = getAP(1);
	AccessPoint ap21 = getAP(21);
	PVector c1 = PVector.add(remap(ap1.coord), new PVector(width/2, height/2));
	PVector c21 = PVector.add(remap(ap21.coord), new PVector(width/2, height/2));
	PVector[] path = graph.calcPath(c1, c21);
	if(path != null){
		stroke(#aa290f);
		for(int i=1; i<path.length; i++){
			line(path[i-1].x, path[i-1].y, path[i].x, path[i].y);
		}
		println("path: "+graph.calcDist(path));	
	}

	// calcAllPaths();
}

void calcAllPaths(){
	for(AccessPoint aps : accessPoints){
		for(AccessPoint ape : accessPoints){
			PVector s = PVector.add(remap(aps.coord), new PVector(width/2, height/2));
			PVector e = PVector.add(remap(ape.coord), new PVector(width/2, height/2));
			float dist = graph.calcDist(s, e);
			if(dist > -1){
				println(aps.id + "," + ape.id + "," + dist);
			}
		}	
	}
}

void renderBack(PGraphics pg, color c){
	pg.beginDraw();
	pg.background(c);
	pg.endDraw();
}

void renderBuildings(PGraphics pg, color buildingColor){
	pg.beginDraw();
	pg.pushMatrix();
	pg.translate(width/2, height/2);
	
	pg.pushStyle();
	pg.noStroke();
	pg.fill(buildingColor);
	for(Building b : buildings){
		PVector[] coords = b.poly;
		pg.beginShape();
		for(int i = 0; i<coords.length; i ++){
			PVector c = remap(coords[i]);
			pg.vertex(c.x, c.y);
		}
		pg.endShape(CLOSE);
	}
	pg.popStyle();

	pg.endDraw();
	pg.popMatrix();
}

void renderAccessPoints(PGraphics pg){
	pg.beginDraw();
	pg.pushMatrix();
	pg.translate(width/2, height/2);
	// pg.scale(1, -1);

	pg.pushStyle();
	pg.ellipseMode(CENTER);
	pg.noStroke();
	for(AccessPoint ap : accessPoints){
		pg.fill(ap.hasCarParking ? #bbdd99 : #dd99bb);
		PVector c = remap(ap.coord);
		pg.ellipse(c.x, c.y, 10, 10);

		pg.fill(#333333);
		pg.text("ap_id:"+ap.id, c.x+5, c.y);
	}
	pg.popStyle();

	pg.endDraw();
	pg.popMatrix();
}

AccessPoint getAP(int id){
	for(AccessPoint ap : accessPoints){
		if(ap.id == id) return ap;
	}
	return null;
}

PVector[] readLineString(String raw){
	String[] m = match(raw, "LINESTRING\\(([0123456789. ;]+)\\)");
	String digits = m[1];
	String[] geo = split(digits, ";");
	PVector[] coords = new PVector[geo.length];
	for(int i=0;i<geo.length;i++){
		String[] p = split(geo[i], " ");
		float x = float(p[0]);
		float y = float(p[1]);
		coords[i] = new PVector(x, y);
	}
	return coords;
}

PVector readPointString(String raw){
	String[] m = match(raw, "POINT\\(([0123456789. ]+)\\)");
	String digits = m[1];
	String[] p = split(digits, " ");
	float x = float(p[0]);
	float y = float(p[1]);
	return new PVector(x, y);
}

PVector remap(PVector coord){
	return remap(coord, 1);
	// float h = rb.y - tl.y;
	// float r = height / h;

	// PVector out = PVector.sub(coord, tl);
	// out.mult(r);
	// return out;
}

PVector remap(PVector coord, float scaleFactor){
	PVector out = PVector.sub(coord, center);
	out.mult(scaleFactor);
	// return out;

	PMatrix2D mtx = new PMatrix2D();
	mtx.scale(1, -1);
	return mtx.mult(out, new PVector(0, 0));
}

PVector convertGeo(PVector coord){
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

PVector[] toScreen(PVector[] source){
	PVector[] out = new PVector[source.length];
	for(int i=0; i<source.length; i++){
		out[i] = convertGeo(source[i]);
	}
	return out;
}

float[] getXYZfromLatLon(float latitudeDeg, float longitudeDeg, float height){
	float latitude = radians(latitudeDeg);
	float longitude = radians(longitudeDeg);

    float a = 6378137.0; //semi major axis
    float b = 6356752.3142; //semi minor axis
    float cosLat = cos(latitude);
    float sinLat = sin(latitude);
	
    float rSubN = (a*a) / sqrt(((a*a) * (cosLat*cosLat) + ((b*b) * (sinLat*sinLat))));
	
    float X = (rSubN + height) * cosLat * cos(longitude);
    float Y = (rSubN + height) * cosLat * sin(longitude);
    float Z = ((((b*b) / (a*a)) * rSubN) + height) * sinLat;
	
    return new float[] {X, Y, Z};
}

float[] convertLatLongToMerc(float lon, float lat){
    if (lat > 89.5) lat = 89.5;
    if (lat < -89.5) lat=-89.5;

    float rLat = radians(lat);
    float rLong = radians(lon);

    float a = 6378137.0;
    float b = 6356752.3142;
    float f = (a-b)/a;
    float e = sqrt(2*f - f*f);
    float x = a*rLong;
    float yy = tan(PI/4+rLat/2) * ((1-e*sin(rLat)) / (1+e*sin(rLat)));
    float y = a * log(pow(yy, (e/2)));
    return new float[]{x, y};
}

 
float[] convertLatLongSpherToMerc(float lon, float lat){
	if (lat > 89.5) lat = 89.5;
    if (lat < -89.5) lat=-89.5;

    float rLat = radians(lat);
    float rLong = radians(lon);
 
    float a = 6378137.0;
    float x = a * rLong;
    float y = a * log(tan(PI/4+rLat/2));
    return new float[]{x, y};
}
