/*
* Make It (makeitcenter.com) > CQ Polygraphmash Workshop > Roman Timashev (roman@tmshv.ru)
* Date::2013 [late]
* This code is licensed under the MIT license. (http://opensource.org/licenses/MIT)
*/

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