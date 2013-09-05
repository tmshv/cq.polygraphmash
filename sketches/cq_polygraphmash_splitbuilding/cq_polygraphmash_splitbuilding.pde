import processing.pdf.*;

ArrayList<Building> buildings;
ArrayList<SpaceUser> users;

PVector geoCenter = new PVector(30.3166, 59.9695);
PVector center;

void setup(){
	size(700, 700);
	
	initBuildings("buildings.csv");
	initSpaceUsers("spsheet.csv");
	
	center = convertGeo(geoCenter);

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
	for(int i=0; i<8; i++){
		PGraphics pdf = createGraphics(width, height, PDF, "level"+i+".pdf");
		renderBack(pdf, #ffffff);
		for(SpaceUser su : users){
			if(su.level == i){
				Building b = getBuilding(su.buildingID);
				renderBuilding(pdf, b, #dddddd);
			}
		}
		pdf.dispose();
	}
}

Building getBuilding(int id){
	for(Building b : buildings){
		if(b.id == id) return b;
	}
	return null;
}

void renderBack(PGraphics pg, color c){
	pg.beginDraw();
	pg.background(c);
	pg.endDraw();
}

void renderBuilding(PGraphics pg, Building b, color buildingColor){
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

PVector remap(PVector coord){
	return remap(coord, 1);
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