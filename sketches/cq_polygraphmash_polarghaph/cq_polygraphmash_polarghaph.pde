import controlP5.*;
import processing.pdf.*;

ArrayList<SpaceUser> users;
ArrayList<SpaceFunction> fu;
Table distTable;
Map map;
MapRenderer view;

float beta;
int thickness;
int lthickness;
int radius;
int rot;

ControlP5 cp5;

void setup(){
	size(800, 800);
	int pos = 10;
	cp5 = new ControlP5(this);
	cp5.addSlider("beta")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(0, 1)
          .setValue(0);
    pos += 25;
    cp5.addSlider("thickness")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(1, 50)
          .setValue(20);
    pos += 25;
    cp5.addSlider("lthickness")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(1, 10)
          .setValue(1);
    pos += 25;
    cp5.addSlider("radius")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(100, width)
          .setValue(width*0.3);
    pos += 25;
    cp5.addSlider("rot")
    .setPosition(10, pos)
      .setSize(200, 20)
        .setRange(0, 360)
          .setValue(0);

	initSpaceUsers("space_users.csv");
	distTable = loadTable("dist.csv", "header");

	// PGraphics pdf = createGraphics(width, height, PDF, "map.pdf");
	// renderBuildings(pdf);
	// pdf.dispose();

	fu = new ArrayList<SpaceFunction>();
	fu.add(new SpaceFunction("production",		#634673));
	fu.add(new SpaceFunction("entertainment",	#FF6600));
	fu.add(new SpaceFunction("service",			#8B9DC3));
	fu.add(new SpaceFunction("art",				#FC6666));
	fu.add(new SpaceFunction("education",		#98DAD3));
	fu.add(new SpaceFunction("restaurant",		#F7B3B3));
	fu.add(new SpaceFunction("retail",			#CC0066));
	fu.add(new SpaceFunction("empty",			#949B98));
	fu.add(new SpaceFunction("office",			#949BFF));

	map = new Map();
	view = new MapRenderer(map);
	view.center.x = width / 2;
	view.center.y = height /2;

	fillGraph2();
	// fillGraph();
	// noLoop();
}

void initSpaceUsers(String file){
	Table table = loadTable(file, "header");

	users = new ArrayList<SpaceUser>();
	for (TableRow row : table.rows()) {
		SpaceUser su = new SpaceUser(row.getInt("id"));
		su.buildingID = row.getInt("building_id");
		su.accessPointID = row.getInt("access_point");
		su.level = row.getInt("level");
		su.func = row.getString("function");
		su.comment = row.getString("comment");
		users.add(su);
	}
}

void fillGraph(){
	for(SpaceUser su : users){
		SpaceFunction f = getSF(su.func);
		if(f == null) println("not found function: "+su.func);
		else f.users.add(su);
	}

	for (SpaceFunction sf : fu){
		map.create(sf.name, sf.calcPower(), sf.c);
	}

	for(SpaceFunction sf1 : fu){
		for(SpaceFunction sf2 : fu){
			if(!sf1.name.equals(sf2.name)){
				if(!map.hasLink(sf1.name, sf2.name, false)){
					map.link(sf1.name, sf2.name, 1);
				}
			}
		}
	}
}

void fillGraph2(){
	for(SpaceUser su : users){
		SpaceFunction f = getSF(su.func);
		if(f == null) println("not found function: "+su.func);
		else f.users.add(su);
	}
	users = new ArrayList<SpaceUser>();
	for(SpaceFunction sf : fu){
		for(SpaceUser su : sf.users){
			users.add(su);
		}
	}

	int i = 0;
	for(SpaceUser su : users){
		// if(i > 10) break;
		SpaceFunction sf = getSF(su.func);
		String name = str(su.id) +":"+su.comment;
		Item item = map.create(name, 1, sf.c);
		item.data = su;
		i ++;
	}
	
	for(Item item1 : map.items){
		for(Item item2 : map.items){
			String itemName1 = item1.name;
			String itemName2 = item2.name;
			if(!itemName1.equals(itemName2)){
				if(!map.hasLink(itemName1, itemName2, false)){
					SpaceUser su1 = (SpaceUser) item1.data;
					SpaceUser su2 = (SpaceUser) item2.data;
					map.link(itemName1, itemName2, (int) getDist(su1, su2));
				}
			}	
		}		
	}
	println("map.links.size(): "+map.links.size());
}

// int calcLinkPower()

void draw(){
	view.beta = beta;
	view.itemThickness = thickness;
	view.maxLinkThickness = lthickness;
	view.radius = radius;
	view.startArcAngle = radians(rot);



	background(#cccccc);
	view.render();

	float ma = atan2((mouseY - view.center.y),(mouseX - view.center.x));
	view.selectItem(ma);
	text(str(degrees(ma)), 100, 10);
}

SpaceFunction getSF(String name){
	for(SpaceFunction sf : fu){
		if(name.equals(sf.name)) return sf;
	}
	return null;
}

float getDist(SpaceUser su1, SpaceUser su2){
	for (TableRow row : distTable.rows()) {
		int ap1 = row.getInt("access_point1");
		int ap2 = row.getInt("access_point2");

		if(su1.accessPointID == ap1 && su2.accessPointID == ap2){
			return row.getFloat("dist");
		}
	}
	return 0;
}

void keyPressed(){
	if(key == 'u'){
		view.computeItems();
		background(#cccccc);
		view.render();
	}
}