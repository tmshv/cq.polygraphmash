// Xplication module by menhir yusupov (DY)
// made for MakeIT CQ.SPB.POLYGRAPHMASH workshop 08.2013
// makeitcenter.com menhir.y@gmail.com

class Xplication {
  
  Xplication () {
  }
  void display () {
  
  // DY UI colors of functions
  color fc1 = #7600FF;  // production
  color fc2 = #FF00F3;  // entertainment
  color fc3 = #C3FF00;  // service
  color fc4 = #FF8D00;  // art
  color fc5 = #00E3FF;  // education
  color fc6 = #0028FF;  // restobars
  color fc7 = #00FFA3;  // retail
  color fc8 = #949B98;  // emtyspaces
  color fc9 = #050505;  // roofs 
  
  int fx = 5;
  int yx = 20;
  int ffx = 107;
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
