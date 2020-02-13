package pack_technical;

import processing.core.PVector;

/**
 * PatternEntry class:
 * This class is to store a PVector object radius  
 * Also to calcuate the diff in radius between two PatternEntry objects
 * @author lis40
 *
 */
public class PatternEntry {
    
	//define a private variable radius
    private PVector radius;  
    
    
    //Get and set method
    public PVector getRadius() {
        return radius;
    }
    
    //Constructor of PatternEntry with one parameter (PVector type element r)
    public PatternEntry(PVector r){ 
        this.radius=r;
    }
    
    /*
     * Method: difference of this PatternEntry object with the other PatternEntry object in Radius
     * Return: the difference in radius in two PatternEntry objects
     */
    public float difference(PatternEntry other){
        float diff = PVector.dist(radius,other.getRadius());

        return diff;
    }

//For Shaling:	Using main method to test the functionalities in each class for further understanding
//    public static void main(String[] args) {
//     	
//    	System.out.println(new PatternEntry(new PVector(3,5)).getRadius());
//    	System.out.println(new PatternEntry(new PVector(3,5)).difference(new PatternEntry(new PVector(7,3))));
//    }
}
