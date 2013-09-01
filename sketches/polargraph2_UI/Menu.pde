// Menu module by menhir yusupov (DY)
// made for MakeIT CQ.SPB.POLYGRAPHMASH workshop 08.2013
// makeitcenter.com menhir.y@gmail.com

class Menu {
  
  Menu () {
  }
  void display () {
    
    Group GUI = cp5.addGroup("GUI")//,15,25,35)
                    .setPosition(500,25)
                    .setWidth(210)
                    .setBackgroundHeight(75)
                    .setBackgroundColor(#416E83)
                    .setColorBackground(#416E83) 
                    .setColorForeground(#416E83) 
                    .setLabel("MENU")
                    .close()
                    ;
                    
    cp5.setColorCaptionLabel(#FF7300);

    cp5.addSlider("track")
       .setPosition(10,10)
       .setSize(150,15)
       .setValue(track)
       .setRange(0,255)
       .setColorBackground(#416E83)
       .setColorForeground(#FF7300)
       .setColorValueLabel(#416E83)
       .setColorActive(#FF7300)
       .setGroup(GUI)
       ;
    
     /* cp5.addSlider("beta")
    .setPosition(10, 10)
      .setSize(200, 20)
        .setRange(0, 1)
          .setValue(0);
          */
    
  }
}
