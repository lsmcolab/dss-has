package pack_technical;

//import javafx.scene.shape.Ellipse;
import processing.core.PApplet;

/**
 * BaseManager class
 * set up a PApplet; call a constructor of this class; define a method of draw()
 * 
 *
 */
public class BaseManager {
    private PApplet app;

    /*
     * Constructor with one parameter
     */
    public BaseManager(PApplet p){
        this.app = p;
    }
    
    /*
     * Method: draw()
     * 
     */
    public void draw(){

        app.fill(255,0,0);
        app.rect(550,500f,10f,10f);
        app.fill(105,105,105);
        app.line(450,800,850,500);
        app.line(850,500,450,100);
        app.line(450,100,450,800);
    }
    

}
