import controlP5.*;
import processing.pdf.*;

ControlP5 cp5;

Map map;
MapRenderer view;

float beta;

void setup(){
  size(650, 650);
  smooth();

  cp5 = new ControlP5(this);

  map = new Map();
  map.create("n1", 10, #236721);
  map.create("n2", 50, #582679);
  map.create("n3", 25, #578294);
  map.create("n4", 25, #eabd46);
  map.create("n5", 25, #f901be);

  for(int i = 0; i<50; i++){
      map.create("n", (int) random(1, 100));
  }

  for(int i = 0; i<50; i++){
      map.create("n", (int) random(100, 1000));
  }

  map.link("n1", "n2", 23);
  map.link("n1", "n3", 5);
  map.link("n1", "n5", 65);

  map.link("n2", "n3", 46);
  map.link("n2", "n4", 46);
  map.link("n2", "n5", 46);

  view = new MapRenderer(map);
  view.center.x = width / 2;
  view.center.y = height /2;

  cp5.addSlider("beta")
    .setPosition(10, 10)
      .setSize(200, 20)
        .setRange(0, 1)
          .setValue(0);
}

void draw(){
  view.beta = beta;

  background(#eeeeee);
  view.render();
}

void keyPressed(){
  if(key == 'q'){
    noLoop();
    beginRecord(PDF, "Record.pdf");
    view.render();
    endRecord();
    loop();
  }
}