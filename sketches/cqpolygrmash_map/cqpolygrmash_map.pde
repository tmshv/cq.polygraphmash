PVector mouse;
PVector startCoord;
PVector endCoord;

Grid grid;
Node selectedNode;
PImage img;

ArrayList<Node> path;

void setup(){
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

void draw(){
	background(#eeeeee);

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
			dumpImg.set(int(pnode.pos.y), int(pnode.pos.x), #aaaa00);
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
		fill(#00aa00);
		ellipseMode(CENTER);
		ellipse(startCoord.x, startCoord.y, 10, 10);
	}

	if(endCoord != null){
		noStroke();
		fill(#aa0000);
		ellipseMode(CENTER);
		ellipse(endCoord.x, endCoord.y, 10, 10);
	}
}
 
void mousePressed() {
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
 
void keyPressed() {
  if (key == 'v') {
    grid.showValues = !grid.showValues;
    grid.showGrid = false;
  }
  else if (key == 'g') {
    grid.showGrid = !grid.showGrid;
    grid.showValues = false;
  }
}

void fillGrid(){
	int maxX = img.width;
	int maxY = img.height;

	int count = 0;

	for(int x = 0; x < maxX; x++){
		for(int y = 0; y < maxY; y++){
			color c = img.get(x, y);
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