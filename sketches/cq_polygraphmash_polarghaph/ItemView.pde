class ItemView{
	public Item item;
	public float angleStart;
	public float angleCenter;
	public float angleStop;

	private boolean select;

	private ItemView(Item item){
		this.item = item;
	}

	public PVector calcCoord(PVector center, int radius) {
		PVector coord = PVector.fromAngle(angleCenter);
		coord.mult(radius);
		// coord.add(center);
		return coord;
	}

	public void select(){
		select = true;
	}

	public void deselect(){
		select = false;
	}

	public boolean idSelected(){
		return select;
	}
}