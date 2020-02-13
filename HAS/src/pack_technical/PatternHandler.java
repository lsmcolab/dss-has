package pack_technical;

import pack_boids.Boid_generic;
import processing.core.PVector;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.PrintWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

/**
 * PatternHandler class:
 * 
 * @author lis40
 *
 */

public class PatternHandler {
	/*
	 * Define variables
	 */
    //public PrintWriter xywriter = new PrintWriter ("Boids_location.txt");// construct a writer with its output file name defined in the working directory
    ArrayList<PatternEntry> observations = new ArrayList<>();
    public float radius;
    private boolean once = false;//For testing Environmental simulation once delete later
    public float ERROR= 0.2f;
    public PatternImage img = new PatternImage();
    
    /*
     * Constructor without parameter
     */
    public PatternHandler() throws FileNotFoundException {}
    
    /*
     * Get and set methods
     */
    public ArrayList<PatternEntry> getObservations() {
        return observations;
    }
    public boolean isOnce() {
        return once;
    }
    public void setOnce(boolean once) {
        this.once = once;
    }
    public float getRadius() {
        return radius;
    }
    public PatternImage getImg() {
        return img;
    }

    /*
     * Method: newObservation
     * Function: (1) Calculate the SumOfMasses of all boids, MiddleOfTheMass of all boids (in one swarm)
     * 			(2) Add the MiddleOfTheMass to an PatternImage object img and an ArrayList observations
     * 			(3) Save boids location into an ArrayList object boidsLocations
     * Return: action void; 
     */

    public void newObservation(ArrayList<Boid_generic> boids,int coutner){
        //ArrayList<PVector> boidsLocations = new ArrayList<PVector>();  //define a list with PVector as the type of elements

        if(coutner%10==0) { //if countner staisfies some condition, %10 == 0
            PVector SumOfTheMasses = new PVector(0, 0);
            int counter = 0;
            for (Boid_generic b : boids) {
                SumOfTheMasses = PVector.add(SumOfTheMasses, b.getLocation());
                counter++;
                //b.getLocation() leads to a pointer which the content can change each time.
                //boidsLocations.add(new PVector(b.getLocation().x, b.getLocation().y));//create a new PVector copying the content of the most recent b.location();
            }
            //xywriter.println(boidsLocations);

            PVector MiddleOfTheMass = PVector.div(SumOfTheMasses, counter);
            img.getPoints().add(new int[]{(int)MiddleOfTheMass.x,(int)MiddleOfTheMass.y});
            observations.add(new PatternEntry(MiddleOfTheMass));
        }
        
//        System.out.println(boidsLocations.size());

    }

    /*
     * Method: analyze()
     * Function: (1) Set the condition of getting the first middle point when swarms moves 50 time steps
     * 			(2) if the observation size is 100, draw the pattern of observation
     * Return: an integer 1 or 0
     */
    public int  analyze() throws IOException {
    	//if the middle point of swarm size is larger than 150, this arraylist is cleared.
        if (observations.size() >= 150) 
        	observations.clear();
        if (observations.size() < 150 && observations.size() > 50) {
            PatternEntry circle = observations.get(0);//return the element at index 0 in the arraylist observation.
            PVector base = new PVector(550,500);
          //distance between the first element of the swarm middle points (centroid) and A point(150,500)
            float exact = PVector.dist(circle.getRadius(), new PVector(150, 500)); 

            for (PatternEntry entry : observations) {
                float error = Math.abs(entry.difference(new PatternEntry(base)) / circle.difference(new PatternEntry(base)));
             }
            // when to draw
            if(observations.size()==100) {
                img.drawPattern();
                //img.clearMe();
                radius = circle.difference(new PatternEntry(base)); 
                once=true;
                return 1;
            }
        }
        return 0;
    }
    
//    public static void main(String[] args) throws FileNotFoundException {
// 	
//    	System.out.println(new PatternHandler().getImg());
//   // 	System.out.println(new PatternEntry(new PVector(3,5)).difference(new PatternEntry(new PVector(7,3))));
//}


}
