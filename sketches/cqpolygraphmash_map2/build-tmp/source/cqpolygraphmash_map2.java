import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import pathfinder.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class cqpolygraphmash_map2 extends PApplet {



Graph gs = new Graph();
float nodeSize = 10f;
PImage graphImage;

GraphNode[] gNodes;
GraphEdge[] gEdges;

float findNodeMaxDist = 16.0f;

GraphNode startNode;
GraphNode endNode;

IGraphSearch finder;
GraphNode[] path;
GraphEdge[] exploredEdges;

public void setup(){
	size(700, 700);
	graphImage = loadImage("map3.png");
	graphImage.resize(width, height);

	makeGraphFromBWimage(gs, graphImage, null, 200, 200, true);
	gs.compact();

	gNodes =  gs.getNodeArray();
	gEdges = gs.getAllEdgeArray();

	finder = makePathFinder(gs, 4);
}

public void draw(){
	image(graphImage, 0, 0);

	GraphNode startNode = gs.getNodeAt(mouseX, mouseY, 0, findNodeMaxDist);
	println("mouse at "+startNode);

	// drawEdges();

	if(path != null){
		// for(int i=0; i<path.length; i++){
		// 	GraphNode n = path[i];
		// 	stroke(#ff0000);
		// 	point(n.xf(), n.yf());
		// }
		drawRoute(path, 0xffff0000, 3f);
	}
}

// void drawEdges(){
// 	drawEdges(gEdges);
// }

// void drawEdges(){
// 	for(int i = 0; i < gEdges.length; i++){
// 		GraphEdge edge = gEdges[i];
// 		GraphNode n1 = edge.from();
// 		GraphNode n2 = edge.to();
// 		stroke(#cccccc);
// 		line(n1.xf(), n1.yf(), n2.xf(), n2.yf());
// 	}
// }

// void mousePressed(){
// 	GraphNode node = gs.getNodeAt(mouseX, mouseY, 0, findNodeMaxDist);
// 	if(node != null){
// 		int nodeID = 123123 + (int) random(10000);
// 		GraphNode n = new GraphNode(nodeID, mouseX, mouseY);
//         gs.addNode(n);
//         gs.addEdge(node.id(), nodeID, random(10020));//, hCost * cost);
// 	}
// }

public void mousePressed(){
	startNode = getNodeUnderMouse();

}

public void mouseReleased(){
	endNode = getNodeUnderMouse();

	if(endNode!= null && startNode != null && startNode != endNode){
	    usePathFinder(finder);
	}
}

public GraphNode getNodeUnderMouse(){
	return gs.getNodeAt(mouseX, mouseY, 0, findNodeMaxDist);
}

public void usePathFinder(IGraphSearch pf){
	int start = startNode.id();
	int end = endNode.id();
	    
  // time = System.nanoTime();
	pf.search(start, end, true);
  // time = System.nanoTime() - time;
	path = pf.getRoute();
	exploredEdges = pf.getExaminedEdges();
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
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "cqpolygraphmash_map2" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
