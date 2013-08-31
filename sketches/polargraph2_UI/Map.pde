class Map{
	public ArrayList<Item> items;
	public ArrayList<Link> links;

	public Map(){
		items = new ArrayList<Item>();
		links = new ArrayList<Link>();
	}

	public Item create(String name, int power) {
		return create(name, power, color(random(255), random(255), random(255)));
	}

	public Item create(String name, int power, color c) {
		Item item = new Item();
		item.name = name;
		item.power = power;
		item.c = c;
		items.add(item);
		return item;
	}

	public Link link(String first, String second, int power) {
		Item f = getItem(first);
		Item s = getItem(second);
		return link(f, s, power);
	}

	public Link link(String first, String second, int power, color c) {
		Item f = getItem(first);
		Item s = getItem(second);
		return link(f, s, power, c);
	}

	public Link link(Item first, Item second, int power) {
		return link(first, second, power, #cccccc);
	}

	public Link link(Item first, Item second, int power, color c) {
		Link l = new Link();
		l.first = first;
		l.second = second;
		l.power = power;
		l.c = c;
		links.add(l);
		return l;
	}

	public Item getItem(String name) {
		for(Item i : items){
			if(i.name == name){
				return i;
			}
		}
		return null;
	}

	public int calcTotalPower() {
		int total = 0;
		for(Item i : items){
			total += i.power;
		}
		return total;
	}

	public int calcMaxLinkPower() {
		int max = 0;
		for(Link i : links){
			if(max < i.power){
				max = i.power;	
			}
		}
		return max;
	}	
}
