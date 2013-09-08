public class MapRenderer{
	private Map map;

	public int itemThickness = 8;
	public int maxLinkThickness = 4;
	public int radius = 200;
	public float startArcAngle = 0;
	public PVector center = new PVector();

	public float beta = 0;
	public float bezierStepT = 0.025;

	private ArrayList<ItemView> itemsView;

	private ItemView selected;
	private ItemView highlighted;

	public MapRenderer (Map map) {
		this.map = map;
		computeItems();
	}

	public void selectItem (float angle){
		// if(selected != null) selected.deselect();
		for(ItemView iv : itemsView){
			if(iv.angleStart < angle && iv.angleStop > angle){
				selected = iv;
			}
		}
	}

	public void highlightItem (float angle){
		for(ItemView iv : itemsView){
			if(iv.angleStart < angle && iv.angleStop > angle){
				highlighted = iv;
			}
		}
	}

	public void render() {
		// computeItems();
		if(itemsView.size() != map.items.size()){
			computeItems();
		}

		pushMatrix();
		translate(center.x, center.y);

		pushMatrix();
		renderLinks();
		popMatrix();

		pushMatrix();
		renderItems();
		popMatrix();

		ellipseMode(CENTER);
		fill(#ffffff);
		noStroke();
		for(ItemView v : itemsView){
			PVector c = v.calcCoord(center, radius);
			// ellipse(c.x, c.y, 10, 10);
		}

		popMatrix();
	}

	public void renderItems() {
		strokeCap(SQUARE);
		int w = radius * 2;
		noFill();

		int i = 0;
		// int total = map.calcTotalPower();

		for(ItemView view : itemsView){
			pushMatrix();
			Item item = view.item;
			
			strokeWeight(itemThickness);
			stroke(item.c);
			if(view == selected){
				stroke(#ffffff);
			}
			
			arc(0, 0, w, w, view.angleStart, view.angleStop);

			PVector tcoord = view.calcCoord(new PVector(), radius+20+itemThickness/2);
			translate(tcoord.x, tcoord.y);
			rotate(view.angleCenter);
			stroke(#ffffff);
			text(item.name, 0, 0);

			i += 1;
			popMatrix();
		}
	}

	public void renderLinks() {
		strokeCap(ROUND);
		int maxPower = map.calcMaxLinkPower();
		int i = 0;
		for(Link link : map.links){
			Item firstItem = link.first;
			Item secondItem = link.second;
			ItemView firstView = getView(firstItem);
			ItemView secondView = getView(secondItem);

			if(firstView != selected && secondView != selected) continue;

			PVector firstCoord = firstView.calcCoord(center, radius);
			PVector secondCoord = secondView.calcCoord(center, radius);

			float ratio = link.power / (float) maxPower;
			// float ratio = (1 / link.power) * (float) maxPower;
			int w = (int) (ratio * maxLinkThickness);
			w = w < 1 ? 1 : w;
			// w = 1 - w;
			// println("w: "+w);

			// int cc = lerpColor(#cccccc, link.c, ratio);
			// stroke(cc, 200);
			stroke(link.c, 200);

			// stroke(link.c);
			strokeWeight(w);
			// line(firstCoord.x, firstCoord.y, secondCoord.x, secondCoord.y);
			// qubic(firstCoord, secondCoord);
			noFill();
			PVector f2 = interpolate(firstCoord, new PVector(), beta);
			PVector s2 = interpolate(secondCoord, new PVector(), beta);
			bezier(firstCoord.x, firstCoord.y, f2.x, f2.y, s2.x, s2.y, secondCoord.x, secondCoord.y);

			i += 1;
		}		
	}

	private void qubic(PVector start, PVector end){
		float a0 = start.x;
		float b0 = start.y;
		// float a1 = center.x;
		// float b1 = center.y;
		// float a2 = center.x;
		// float b2 = center.y;
		float a1 = 0;
		float b1 = 0;
		float a2 = 0;
		float b2 = 0;
		float a3 = end.x;
		float b3 = end.y;

		float xPrev;
		float yPrev;
		float xNext;
		float yNext;

		xPrev = beta * a0 + (1 - beta) * start.x;
	    yPrev = beta * b0 + (1 - beta) * start.y;

		float t = 0;
		while(t <= 1){
			float bX = bezierPoint(a0, a1, a2, a3, t);
			float bY = bezierPoint(b0, b1, b2, b3, t);

			xNext = beta * bX + (1 - beta) * (start.x + (end.x - start.x) * t);
			yNext = beta * bY + (1 - beta) * (start.y + (end.y - start.y) * t);

			// xNext = beta * (a3 * t * t * t + a2 * t * t + a1 * t + a0) + (1 - beta) * (start.x + (end.x - start.x) * t);
			// yNext = beta * (b3 * t * t * t + b2 * t * t + b1 * t + b0) + (1 - beta) * (start.y + (end.y - start.y) * t);	

			line(xPrev, yPrev, xNext, yNext);

			xPrev = xNext;
			yPrev = yNext;
			t += bezierStepT;	
		}
	}

	private ItemView getView(Item item) {
		for(ItemView v : itemsView){
			if(v.item == item) return v;
		}
		return null;
	}

	private PVector interpolate (PVector v1, PVector v2, float t){
		PVector out = new PVector();
		out.x = v1.x + (v2.x-v1.x)*t;
		out.y = v1.y + (v2.y-v1.y)*t;
		return out;
	}

	public void computeItems() {
		// float a = PI * 0.001;
		float a = 0;
		itemsView = new ArrayList<ItemView>();
		int i = 0;
		int total = map.calcTotalPower();

		float start_angle = startArcAngle;
		for(Item item : map.items){
			float circ_ratio = (float) item.power / (float) total;
			float angle = circ_ratio * TWO_PI;
			float stop_angle = start_angle + angle;
			float center_angle = start_angle + angle/2;
			
			ItemView view = new ItemView(item);
			view.angleStart = start_angle + a;
			view.angleCenter = center_angle;
			view.angleStop = stop_angle - a;
			itemsView.add(view);

			start_angle = stop_angle;
			i += 1;
		}	
	}
}