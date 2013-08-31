class Node {
  final static int WIDTH = 20;
  final static int HEIGHT = 20;

  final static color COLOR_START = #00aa00;
  final static color COLOR_END = #aa0000;
  
  PVector pos;
  Node[] siblings;
  color backgroundColor = #ffffff;
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
    this.backgroundColor = #ffffff;
  }
   
  public void setColor(color c) {
    this.backgroundColor = c;
  }
   
  /**
   * Draw this node
   */
  public void view() {
    if(isWall){
      stroke(#000044);
    }else{
      stroke(#ffffff);
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
      this.setColor(#888888);
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