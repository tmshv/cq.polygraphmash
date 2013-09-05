import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.pdf.*; 
import pathfinder.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class cq_polygraphmash extends PApplet {



CQGraph graph;

Table table;

ArrayList<Building> buildings;
ArrayList<AccessPoint> accessPoints;
ArrayList<SpaceUser> users;

PVector geoCenter = new PVector(30.3166f, 59.9695f);
PVector center;

PVector tl;
PVector rb;

public void setup(){
	size(800, 800);
	
	initBuildings("buildings.csv");
	initAccessPoints("access_points.csv");
	initSpaceUsers("space_users.csv");
	
	tl = convertGeo(new PVector(30.314234f, 59.971177f));
	rb = convertGeo(new PVector(30.318649f, 59.967648f));
	center = convertGeo(geoCenter);

	PVector tlg = convertGeo(new PVector(30.314234f, 59.971177f));
	PVector rbg = convertGeo(new PVector(30.318649f, 59.967648f));
	tl = new PVector(min(tlg.x, rbg.x), min(tlg.y, rbg.y));
	rb = new PVector(max(tlg.x, rbg.x), max(tlg.y, rbg.y));

	// PGraphics pdf = createGraphics(width, height, PDF, "map.pdf");
	// renderBuildings(pdf);
	// pdf.dispose();

	PGraphics dump = createGraphics(width, height);
	renderBack(dump, 0xffffffff);
	renderBuildings(dump, 0xff000000);
	dump.save("dump.png");
	graph = new CQGraph(dump);

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
	renderBack(g, 0xffffffff);
	renderBuildings(g, 0xffdddddd);
	// graph.drawEdges(g, #cccccc);
	renderAccessPoints(g);

	AccessPoint ap1 = getAP(1);
	AccessPoint ap21 = getAP(21);
	PVector c1 = PVector.add(remap(ap1.coord), new PVector(width/2, height/2));
	PVector c21 = PVector.add(remap(ap21.coord), new PVector(width/2, height/2));
	PVector[] path = graph.calcPath(c1, c21);
	if(path != null){
		stroke(0xffaa290f);
		for(int i=1; i<path.length; i++){
			line(path[i-1].x, path[i-1].y, path[i].x, path[i].y);
		}
		println("path: "+graph.calcDist(path));	
	}

	calcAllPaths();
}

public void calcAllPaths(){
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

public void renderBack(PGraphics pg, int c){
	pg.beginDraw();
	pg.background(c);
	pg.endDraw();
}

public void renderBuildings(PGraphics pg, int buildingColor){
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

public void renderAccessPoints(PGraphics pg){
	pg.beginDraw();
	pg.pushMatrix();
	pg.translate(width/2, height/2);
	// pg.scale(1, -1);

	pg.pushStyle();
	pg.ellipseMode(CENTER);
	pg.noStroke();
	for(AccessPoint ap : accessPoints){
		pg.fill(ap.hasCarParking ? 0xffbbdd99 : 0xffdd99bb);
		PVector c = remap(ap.coord);
		pg.ellipse(c.x, c.y, 10, 10);

		pg.fill(0xff333333);
		pg.text("ap_id:"+ap.id, c.x+5, c.y);
	}
	pg.popStyle();

	pg.endDraw();
	pg.popMatrix();
}

public AccessPoint getAP(int id){
	for(AccessPoint ap : accessPoints){
		if(ap.id == id) return ap;
	}
	return null;
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
	// return out;

	PMatrix2D mtx = new PMatrix2D();
	mtx.scale(1, -1);
	return mtx.mult(out, new PVector(0, 0));
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
	public int c;
	public PVector[] geoPoly;
	public PVector[] poly;

	public Building (int id, PVector[] geoPoly) {
		this.id = id;
		this.geoPoly = geoPoly;
	}
}


public class CQGraph{
	Graph gs;
	float nodeSize = 10f;
	PImage graphImage;

	GraphNode[] gNodes;
	GraphEdge[] gEdges;

	float findNodeMaxDist = 16.0f;
	int cellnum = 200;

	GraphNode startNode;
	GraphNode endNode;

	IGraphSearch finder;

	public CQGraph (PImage img) {
		gs = new Graph();
		makeGraphFromBWimage(gs, img, null, cellnum, cellnum, true);
		gs.compact();

		gNodes =  gs.getNodeArray();
		gEdges = gs.getAllEdgeArray();
		println("gEdges: "+gEdges.length);

		finder = makePathFinder(gs, 4);
	}

	public PVector[] calcPath(PVector start, PVector end){
		PVector[] out;
		startNode = getNodeUnder(start);
		endNode = getNodeUnder(end);

		if(endNode!= null && startNode != null && startNode != endNode){
			GraphNode[] path = usePathFinder(finder);
			out = new PVector[path.length];
			for(int i=0; i<path.length; i++){
				GraphNode p = path[i];
				out[i] = new PVector(p.xf(), p.yf());
			}
			return out;
		}else{
			return null;
		}
	}

	public float calcDist(PVector start, PVector end){
		PVector[] path = calcPath(start, end);
		if(path != null){
			return calcDist(path);			
		}else{
			return -1;
		}
	}

	public float calcDist(PVector[] path){
		float di = 0;
		PVector p = path[0];
		for(int i=1; i<path.length; i++){
			PVector a = path[i];
			float d = dist(p.x, p.y, a.x, a.y);
			di += d;
		}
		return di;
	}

	private GraphNode getNodeUnder(PVector coord){
		return getNodeUnder(coord.x, coord.y);
	}

	private GraphNode getNodeUnder(float x, float y){
		// for(int i=0; i<gNodes.length; i++){
		// 	GraphNode n = gNodes[i];
		// 	float d = dist(x, y, n.xf(), n.yf());
		// 	if(d < findNodeMaxDist){
		// 		return n;
		// 	}
		// }
		return gs.getNodeAt(x, y, 0, findNodeMaxDist);
	}

	private GraphNode[] usePathFinder(IGraphSearch pf){
		int start = startNode.id();
		int end = endNode.id();
		pf.search(start, end, true);
		return pf.getRoute();
		// exploredEdges = pf.getExaminedEdges();
	}

	public IGraphSearch makePathFinder(Graph graph, int pathFinder){
		IGraphSearch pf = null;
		float f = 1.0f;
		switch(pathFinder){
			case 0:
				pf = new GraphSearch_DFS(gs);
				break;
			case 1:
				pf = new GraphSearch_BFS(gs);
				break;
			case 2:
				pf = new GraphSearch_Dijkstra(gs);
				break;
			case 3:
				pf = new GraphSearch_Astar(gs, new AshCrowFlight(f));
				break;
			case 4:
				pf = new GraphSearch_Astar(gs, new AshManhattan(f));
				break;
		}
		return pf;
	}


	public void drawRoute(GraphNode[] r, int lineCol, float sWeight){
	  if(r.length >= 2){
	    pushStyle();
	    stroke(lineCol);
	    strokeWeight(sWeight);
	    noFill();
	    for(int i = 1; i < r.length; i++)
	      line(r[i-1].xf(), r[i-1].yf(), r[i].xf(), r[i].yf());
	    // Route start node
	    strokeWeight(2.0f);
	    stroke(0,0,160);
	    fill(0,0,255);
	    ellipse(r[0].xf(), r[0].yf(), nodeSize, nodeSize);
	    // Route end node
	    stroke(160,0,0);
	    fill(255,0,0);
	    ellipse(r[r.length-1].xf(), r[r.length-1].yf(), nodeSize, nodeSize); 
	    popStyle();
	  } 
	}

	public void drawEdges(PGraphics pg, int c){
		for(int i = 0; i < gEdges.length; i++){
			GraphEdge edge = gEdges[i];
			GraphNode n1 = edge.from();
			GraphNode n2 = edge.to();
			pg.stroke(c);
			pg.strokeWeight(1);
			pg.line(n1.xf(), n1.yf(), n2.xf(), n2.yf());
		}
	}

	public void makeGraphFromBWimage(Graph g, PImage backImg, PImage costImg, int tilesX, int tilesY, boolean allowDiagonals){
	  int dx = backImg.width / tilesX;
	  int dy = backImg.height / tilesY;
	  int sx = dx / 2, sy = dy / 2;
	  // use deltaX to avoid horizontal wrap around edges
	  int deltaX = tilesX + 1; // must be > tilesX

	  float hCost = dx, vCost = dy, dCost = sqrt(dx*dx + dy*dy);
	  float cost = 0;
	  int px, py, nodeID, col;
	  GraphNode aNode;

	  py = sy;
	  for(int y = 0; y < tilesY ; y++){
	    nodeID = deltaX * y + deltaX;
	    px = sx;
	    for(int x = 0; x < tilesX; x++){
	      // Calculate the cost
	      if(costImg == null){
	        col = backImg.get(px, py) & 0xFF;
	        cost = 1;
	      }
	      else {
	        col = costImg.get(px, py) & 0xFF;
	        cost = 1.0f + (256.0f - col)/ 16.0f; 
	      }
	      // If col is not black then create the node and edges
	      // println("col: "+hex(col));
	      // if(col != 0){
	      if(col >= 128){
	        aNode = new GraphNode(nodeID, px, py);
	        g.addNode(aNode);
	        if(x > 0){
	          g.addEdge(nodeID, nodeID - 1, hCost * cost);
	          if(allowDiagonals){
	            g.addEdge(nodeID, nodeID - deltaX - 1, dCost * cost);
	            g.addEdge(nodeID, nodeID + deltaX - 1, dCost * cost);
	          }
	        }
	        if(x < tilesX -1){
	          g.addEdge(nodeID, nodeID + 1, hCost * cost);
	          if(allowDiagonals){
	            g.addEdge(nodeID, nodeID - deltaX + 1, dCost * cost);
	            g.addEdge(nodeID, nodeID + deltaX + 1, dCost * cost);
	          }
	        }
	        if(y > 0)
	          g.addEdge(nodeID, nodeID - deltaX, vCost * cost);
	          if(y < tilesY - 1)
	            g.addEdge(nodeID, nodeID + deltaX, vCost * cost);
	      }
	      px += dx;
	      nodeID++;
	    }
	    py += dy;
	  }
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
    String[] appletArgs = new String[] { "cq_polygraphmash" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
