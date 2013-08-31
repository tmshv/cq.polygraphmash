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

public class cqpolygrmash_map extends PApplet {

PVector mouse;
PVector startCoord;
PVector endCoord;

Grid grid;
Node selectedNode;
PImage img;

ArrayList<Node> path;

public void setup(){
	size(650, 650);
	// noSmooth();

	img = loadImage("map.png");
	img.resize(width, height);
	// img.resize(30, 30);
	
	grid = new Grid(img.width, img.height);

	// println(img.width);
	// println(img.height);
	// println(grid.nodes.length);

	mouse = new PVector(mouseX, mouseY);

	fillGrid();
	// grid.calculateDistanceToEndNode();
	// noLoop();
}

public void draw(){
	background(0xffeeeeee);

	mouse.x = mouseX;
	mouse.y = mouseY;

	// image(img, 0, 0);
	// image(img, 0, 0, width, height);

	// print(startCoord);
	// print(" ");
	// println(endCoord);

	if(startCoord != null && endCoord != null && path==null){
		grid.setStart(grid.getNodeAt(startCoord));
		grid.setEnd(grid.getNodeAt(endCoord));
		path = grid.calculatePath();
	}

	// PImage p = new PImage(grid.nw, grid.nh, ARGB);
	PImage dumpImg = createImage(650, 650, RGB);
	int maxX = dumpImg.width;
	int maxY = dumpImg.height;
	for(int x = 0; x < maxX; x++){
		for(int y = 0; y < maxY; y++){
			Node node = grid.getNodeAt(x, y);
			dumpImg.set(x, y, node.backgroundColor);

			// color c = img.get(x, y);
			// dumpImg.set(x, y, c);
			// dumpImg.set(x, y, color(random(255), random(255), random(255)));
		}
	}

	if(path != null){
		for(Node pnode : path){
			dumpImg.set(PApplet.parseInt(pnode.pos.y), PApplet.parseInt(pnode.pos.x), 0xffaaaa00);
		}	
	}
	

	// for (int i = 0; i < grid.nodes.length; i++) {
	// 	Node n = grid.nodes[i];
	// 	// if(n.isDistanceSet){
	// 	// 	dumpImg.set((int) n.pos.x, (int) n.pos.y, #aa0000);
	// 	// }
	// 	dumpImg.set(int(n.pos.x), int(n.pos.y), n.backgroundColor);
 //    }
    // image(img, 0, 0);//, width, height);
    image(dumpImg, 0, 0);//, width, height);

    // color cc = img.get(mouseX, mouseY);
    // text(cc, 10, 10);
    // text(brightness(cc), 10, 20);

	// grid.view();

	if(startCoord != null){
		noStroke();
		fill(0xff00aa00);
		ellipseMode(CENTER);
		ellipse(startCoord.x, startCoord.y, 10, 10);
	}

	if(endCoord != null){
		noStroke();
		fill(0xffaa0000);
		ellipseMode(CENTER);
		ellipse(endCoord.x, endCoord.y, 10, 10);
	}
}
 
public void mousePressed() {
	if(startCoord == null){
		startCoord = new PVector(mouseX, mouseY);
	}else if(endCoord == null){
		endCoord = new PVector(mouseX, mouseY);
	}else{
		startCoord = null;
		endCoord = null;
		path = null;
	}

  // selectedNode = grid.getNodeAt(mouseX, mouseY);
  // if (selectedNode != null && selectedNode != grid.startNode && selectedNode != grid.endNode) {
  //   selectedNode.swapWall();
  // }
}
 
public void keyPressed() {
  if (key == 'v') {
    grid.showValues = !grid.showValues;
    grid.showGrid = false;
  }
  else if (key == 'g') {
    grid.showGrid = !grid.showGrid;
    grid.showValues = false;
  }
}

public void fillGrid(){
	int maxX = img.width;
	int maxY = img.height;

	int count = 0;

	for(int x = 0; x < maxX; x++){
		for(int y = 0; y < maxY; y++){
			int c = img.get(x, y);
			Node node = grid.getNodeAt(x, y);
			if(node != null){
				// boolean b = (c == -1);
				boolean b = brightness(c) < 128;
				node.setWall(b);
				count += b ? 1 : 0;
				// println(hc);
			}
		}
	}
}
class Grid {
  public Node[] nodes;
  public Node startNode;
  public Node endNode;
   
  final int nw; // number of nodes (width)
  final int nh; // number of nodes (height)
   
  boolean showValues = false;
  boolean showGrid   = false;
   
  public Grid(int nw, int nh) {   
    this.nw = nw;
    this.nh = nh;
    
    nodes = new Node[nw*nh];
     
    /**
     * Set up the nodes
     */
    int count = 0;
    for(int x = 0; x < nw; x++){
      for(int y = 0; y < nh; y++){
        Node node = new Node(new PVector(x, y));
        nodes[count] = node;
        count ++;
      }
    }     
    /**
     * Define which are the adjacent nodes
     */
    for (int i = 0; i < nodes.length; i++) {
      // all except left column
      if (i%nw != 0) {
        nodes[i].addSibling(nodes[i-1]);
      }
      // all except right column
      if (i%nw != nw - 1) {
        nodes[i].addSibling(nodes[i+1]);
      }
      // all except top row
      if (i >= nw) {
        nodes[i].addSibling(nodes[i-nw]);
      }
      // all except bottom row
      if (i < (nh - 1) * nw){
        nodes[i].addSibling(nodes[i+nw]);
      }
    }
  }

  public void setStart(Node node) {  
    if(startNode != null){
      startNode.backgroundColor = -1;
    }

    startNode = node;
    startNode.backgroundColor = Node.COLOR_START;
  }

  public void setEnd(Node node) {  
    if(endNode != null){
      endNode.backgroundColor = -1;
    }

    endNode = node;
    endNode.backgroundColor = Node.COLOR_END;
  }
   
  /**
   * Calculates the distance of each node to the end node
   */
  public ArrayList<Node> calculatePath() {
    for (int i = 0; i < nodes.length; i++) {
      nodes[i].clearDistanceToEnd();
    }
    endNode.setDistanceToEnd(0);
     
    int k = 1;
    while (k > 0) {
      k = 0;
      for (int i = 0; i < nodes.length; i++) {
        if (nodes[i].isDistanceSet() || nodes[i].toBeIgnoredForDistanceCalculation) {
          continue;
        }
        for (int j = 0; j < nodes[i].siblings.length; j++) {
          if (nodes[i].siblings[j].isDistanceSet() && !nodes[i].siblings[j].toBeIgnoredForDistanceCalculation) {
            nodes[i].setDistanceToEnd(nodes[i].siblings[j].getDistanceToEnd() + 1);
            k++;
            continue;
          }
        }
      }
    }

    for (int i = 0; i < nodes.length; i++) {
      nodes[i].resetDistanceFlag();
    }

    /**
     * Returns false if the path is blocked
     */
    if (startNode.getDistanceToEnd() == -1) {
      return null;
    }

    ArrayList<Node> path = new ArrayList<Node>();
    Node path_node = startNode;
    while(true){
      if(path_node == endNode) break;

      int minDistance = -1;
      for (int i = 0; i < path_node.siblings.length; i++) {
        Node s = path_node.siblings[i];
        int d = s.getDistanceToEnd();
        if ((minDistance == -1) || (d > -1 && d < minDistance)) {
          minDistance = d;
        }
      }
      if (minDistance == -1) {
        return null; //no path (path is blocked)
      }
      
      for (int i = 0; i < path_node.siblings.length; i++) {
        Node s = path_node.siblings[i];
        if (s.getDistanceToEnd() == minDistance) {
          path.add(s);
          path_node = s;
          break;
        }
      }
    }
     
    return path;
  }
   
  public Node getNodeAt(int x, int y) {
    if (x >= nw) return null;
    if (y >= nh) return null;
    int i = y * nw + x;
    return nodes[i];
  }

  public Node getNodeAt(PVector pos) {
    return getNodeAt((int) pos.x, (int) pos.y);
  }
}
class Node {
  final static int WIDTH = 20;
  final static int HEIGHT = 20;

  final static int COLOR_START = 0xff00aa00;
  final static int COLOR_END = 0xffaa0000;
  
  PVector pos;
  Node[] siblings;
  int backgroundColor = 0xffffffff;
  int distanceToEnd = -1;

  boolean toBeIgnoredForDistanceCalculation = false;
  boolean isDistanceSet = false;
  boolean isWall = false;
   
  public Node(PVector pos) {
    this.pos = pos;
    this.siblings = new Node[0];
    this.resetColor();
  }
   
  public void addSibling(Node b) {
    this.siblings = (Node[]) append(this.siblings, b);
  }
   
  public void setDistanceToEnd(int i) {
    this.distanceToEnd = i;
    this.isDistanceSet = true;
  }
   
  public void resetDistanceFlag() {
    this.isDistanceSet = false;
  }
   
  public void clearDistanceToEnd() {
    this.distanceToEnd = -1;
  }
   
  public int getDistanceToEnd() {
    return this.distanceToEnd;
  }
   
  public boolean isDistanceSet() {
    return this.isDistanceSet;
  }
   
  public void resetColor() {
    this.backgroundColor = 0xffffffff;
  }
   
  public void setColor(int c) {
    this.backgroundColor = c;
  }
   
  /**
   * Draw this node
   */
  public void view() {
    if(isWall){
      stroke(0xff000044);
    }else{
      stroke(0xffffffff);
    }

    if(backgroundColor >= 0){
      stroke(backgroundColor); 
    }

    // rectMode(CENTER);
    // stroke(backgroundColor);
    // fill(backgroundColor);
    // point(pos.x, pos.y);
    rect(this.pos.x, this.pos.y, 1, 1);
    // if (parentGrid.showGrid) {
    //   fill(#aaaaaa); stroke(#aaaaaa);
    //   ellipse(this.pos.x, this.pos.y, 2, 2);
    // }
    // if (parentGrid.showValues) {
    //   fill(#aaaaaa);
    //   textAlign(CENTER);
    //   text(this.distanceToEnd, this.pos.x, this.pos.y + 5);
    // }
  }
   
  /**
   * Set this node as a wall, or not
   */
  public void setWall(boolean b) {
    this.isWall = b;
    if (b) {
      this.toBeIgnoredForDistanceCalculation = true;
      this.distanceToEnd = -1;
      this.setColor(0xff888888);
    }
    else {
      this.toBeIgnoredForDistanceCalculation = false;
      this.resetColor();
    }
  }
   
  public void swapWall() {
    this.setWall(!this.isWall);
  }
   
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "cqpolygrmash_map" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
