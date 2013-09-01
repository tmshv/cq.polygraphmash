// Xplication module by menhir yusupov (DY)
// made for MakeIT CQ.SPB.POLYGRAPHMASH workshop 08.2013
// makeitcenter.com menhir.y@gmail.com

class Xplication {
  
  Xplication () {
  }
  void display () {
  
  // colors of functions
  color fc1 = #634673;  // production
  color fc2 = #FF6600;  // entertainment
  color fc3 = #8B9DC3;  // service
  color fc4 = #FC6666;  // art
  color fc5 = #98DAD3;  // education
  color fc6 = #F7B3B3;  // restobars
  color fc7 = #CC0066;  // retail
  color fc8 = #949B98;  // emtyspaces
  color fc9 = #050505;  // roofs 
  
  int fx = 5;
  int yx = 20;
  int ffx = 157;
  int add = 15;
  
  noStroke();
  // textFont(dyFont);

  fill(0); text ("ЭКСПЛИКАЦИЯ : ФУНКЦИИ",      fx,yx-5);

  fill(fc1); rect (fx-2,  yx+3,      ffx,yx);
  fill(255); text ("PRODUCTION",      fx, yx + add);
  
  fill(fc2); rect (fx-2,  yx+add+3,  ffx,yx);
  fill(255); text ("ENTERTAINMENT",   fx, yx + 2*add);
  
  fill(fc3); rect (fx-2,  yx+2*add+3,ffx,yx-3);
  fill(255); text ("SERVICE",         fx, yx + 3*add);
  
  fill(fc4); rect (fx-2,  yx+3*add+3,ffx,yx-3);
  fill(255); text ("ART",             fx, yx + 4*add);
  
  fill(fc5); rect (fx-2,  yx+4*add+3,ffx,yx-3);
  fill(255); text ("EDUCATION",       fx, yx + 5*add);
  
  fill(fc6); rect (fx-2,  yx+5*add+3,ffx,yx-3);
  fill(255); text ("RESTOBARS",       fx, yx + 6*add);

  fill(fc7); rect (fx-2,  yx+6*add+3,ffx,yx-3);
  fill(255); text ("RETAIL",          fx, yx + 7*add);

  fill(fc8); rect (fx-2,  yx+7*add+3,ffx,yx-3);
  fill(255); text ("EMPTYSPACES",     fx, yx + 8*add);

  fill(fc9); rect (fx-2,  yx+8*add+3,ffx,yx-3);
  fill(255); text ("ROOFSPACES",      fx, yx + 9*add);
    
  }
}
