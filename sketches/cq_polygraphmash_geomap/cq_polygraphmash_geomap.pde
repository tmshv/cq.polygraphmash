Table table;
Table accessTable;

ArrayList<PVector[]> geom;
ArrayList<PVector> accessPoints;

PVector geoCenter = new PVector(30.3166, 59.9695);
PVector center;

PVector tl;
PVector rb;

void setup(){
	size(700, 700);
	table = loadTable("spsheet.csv", "header");
	accessTable = loadTable("access_points.csv", "header");
	
	tl = convertGeo(new PVector(30.314234, 59.971177));
	rb = convertGeo(new PVector(30.318649, 59.967648));
	center = convertGeo(geoCenter);

	PVector tlg = convertGeo(new PVector(30.314234, 59.971177));
	PVector rbg = convertGeo(new PVector(30.318649, 59.967648));
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

void draw(){
	translate(width/2, height/2);

	background(#ffffff);

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
	fill(#99dd99);
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
	fill(#333333);
	ellipseMode(CENTER);
	ellipse(tlr.x, tlr.y, 15, 15);
	ellipse(rbr.x, rbr.y, 15, 15);
	println("tlr: "+tlr);	
	println("rbr: "+rbr);

	PVector rc = remap(center, 0.1);
	fill(#aa0000);
	ellipse(rc.x, rc.y, 50, 50);
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
	return remap(coord, 2);
	// float h = rb.y - tl.y;
	// float r = height / h;

	// PVector out = PVector.sub(coord, tl);
	// out.mult(r);
	// return out;
}

PVector remap(PVector coord, float scaleFactor){
	PVector out = PVector.sub(coord, center);
	out.mult(scaleFactor);
	return out;
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
	float[] xyz = getXYZfromLatLon(coord.x, coord.y, 0);
	return new PVector(xyz[0], xyz[1]);

	//4
	// float[] xy = convertLatLongToMerc(coord.x, coord.y);
	// return new PVector(xy[0], xy[1]);
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