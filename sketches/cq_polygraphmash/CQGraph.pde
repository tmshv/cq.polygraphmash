/*
* Make It (makeitcenter.com) > CQ Polygraphmash Workshop > Roman Timashev (roman@tmshv.ru)
* Date::2013 [late]
* This code is licensed under the MIT license. (http://opensource.org/licenses/MIT)
*/

import pathfinder.*;

public class CQGraph{
	Graph gs;
	float nodeSize = 10f;
	PImage graphImage;

	GraphNode[] gNodes;
	GraphEdge[] gEdges;

	float findNodeMaxDist = 16.0;
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

	PVector[] calcPath(PVector start, PVector end){
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

	float calcDist(PVector start, PVector end){
		PVector[] path = calcPath(start, end);
		if(path != null){
			return calcDist(path);			
		}else{
			return -1;
		}
	}

	float calcDist(PVector[] path){
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

	IGraphSearch makePathFinder(Graph graph, int pathFinder){
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


	void drawRoute(GraphNode[] r, int lineCol, float sWeight){
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

	void drawEdges(PGraphics pg, color c){
		for(int i = 0; i < gEdges.length; i++){
			GraphEdge edge = gEdges[i];
			GraphNode n1 = edge.from();
			GraphNode n2 = edge.to();
			pg.stroke(c);
			pg.strokeWeight(1);
			pg.line(n1.xf(), n1.yf(), n2.xf(), n2.yf());
		}
	}

	void makeGraphFromBWimage(Graph g, PImage backImg, PImage costImg, int tilesX, int tilesY, boolean allowDiagonals){
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