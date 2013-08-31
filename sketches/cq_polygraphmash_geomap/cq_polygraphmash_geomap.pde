Table table;

void setup(){
	size(700, 700);
	table = loadTable("spsheet.csv", "header");
	println(table.getRowCount() + " total rows in table");
	println(table.getColumnCount() + " total cols in table");

	for (TableRow row : table.rows()) {
		String geo = row.getString("wkt_geom");
		PVector[] coords = readLineString(geo);
		drawLines(coords);
	}
	noLoop();
  // readLineString("LINESTRING(30.317922 59.968461;30.317922 59.968461;30.317922 59.968461;30.317922 59.968461;30.317922 59.968461;30.317643 59.968521;30.317643 59.968521;30.317947 59.968509;30.317922 59.968461;30.317922 59.968461)");
}

void draw(){
	background(#ffffff);
	for (TableRow row : table.rows()) {
		String geo = row.getString("wkt_geom");
		PVector[] coords = readLineString(geo);
		drawLines(coords);
	}
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

void drawLines(PVector[] coords){
	pushStyle();
	fill(#cccccc);
	noStroke();
	beginShape();
	for(int i=0; i<coords.length; i++){
		PVector c = remap(coords[i]);
		vertex(c.x, c.y);
	}
	endShape(CLOSE);
	popStyle();
}

PVector remap(PVector coord){
	// PVector t = new PVector(2752.23, 4761.3);
	PVector t = new PVector(2757000, 4770000);
	PVector screenCoord = convertGeo(coord);
	PVector out = PVector.sub(screenCoord, t);
	out.mult(0.5);
	// PVector out = screenCoord;
	println("out: "+out);
	return out;
}

PVector convertGeo(PVector coord){
	float lat = radians(coord.x);
	float lon = radians(coord.y);
	// float R = 6371;
	float R = 6383584;
	float x = R * cos(lat) * cos(lon);
	float y = R * cos(lat) * sin(lon);
	float z = R * sin(lat);

	return new PVector(x, y);
}