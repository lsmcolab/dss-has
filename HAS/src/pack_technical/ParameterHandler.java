package pack_technical;

import processing.core.PVector;

/**
 * ParameterHandler class
 * This class is to
 * @author franc
 *
 */
public class ParameterHandler {
	/*
	 * Define variables
	 */
    static PVector currentSum = new PVector(0,0);
    public static PVector[] estimations = new PVector[3];
    
    /*
     * Constructor:
     */
    public ParameterHandler() {}
        
    /*
     * Get and Set methods
     */
    public static PVector[] getEstimations() {
        return estimations;
    }
    
    public PVector getCurrentResultant(){
        return currentSum;
    }
    
    /*
     * Methods:createResultant()
     * Input: sep, ali, coh
     * Function: add all sep, ali, coh together as vector, assign 
     * Return: void (action)
     */

    public void createResultant(PVector sep, PVector ali, PVector coh){
        currentSum = PVector.add(sep,PVector.add(ali,coh)); //Vector addition: sep+ali+coh
        estimations[0]=sep;
        estimations[1]=ali;
        estimations[2]=coh;
    }

}
