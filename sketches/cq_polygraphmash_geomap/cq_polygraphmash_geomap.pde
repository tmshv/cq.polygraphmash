Table table;
Table accessTable;

Remap remap;
ArrayList<PVector[]> geom;
ArrayList<PVector> accessPoints;

PVector tl;
PVector rb;

void setup(){
	size(700, 700);
	table = loadTable("spsheet.csv", "header");
	accessTable = loadTable("access_points.csv", "header");
	
	tl = convertGeo(new PVector(30.314234, 59.971177));
	rb = convertGeo(new PVector(30.318649, 59.967648));

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
	background(#ffffff);

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

void drawLines(PVector[] coords){
	pushStyle();
	fill(#cccccc);
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

PVector remap(PVector coord){
	float h = rb.y - tl.y;
	float r = height / h;

	PVector out = PVector.sub(coord, tl);
	out.mult(r);
	return out;
}

PVector convertGeo(PVector coord){
	float lat = radians(coord.x);
	float lon = radians(coord.y);
	float R = 6383584;
	float x = R * cos(lat) * cos(lon);
	float y = R * cos(lat) * sin(lon);
	float z = R * sin(lat);

	return new PVector(x, y);
	// return coord;
}

PVector[] toScreen(PVector[] source){
	PVector[] out = new PVector[source.length];
	for(int i=0; i<source.length; i++){
		out[i] = convertGeo(source[i]);
	}
	return out;
}