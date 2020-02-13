package pack_technical;

import pack_boids.Boid_generic;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * PatrollingScheme class
 * This class is to 
 * 
 *
 */
public class PatrollingScheme {
	
	/*
	 * Define variables
	 */
	float waypointforce;
    private PVector currWaypoint = new PVector(0,0);
    private PVector currWaypointA = new PVector(0,0);
    private ArrayList<PVector> waypoints = new ArrayList<>();
    private ArrayList<PVector> waypointsA = new ArrayList<>();
    public Iterator<PVector> iterator;
    public int currentPosition = 0;
	
	/*
	 * Constructor with one parameter
	 */
    public PatrollingScheme(float waypointforce){
        this.waypointforce=waypointforce;
    }
	
	/*
	 * Get and set methods
	 */
    public float getWaypointforce() {
        return waypointforce;
    }

    public void setWaypointforce(float waypointforce) {
        this.waypointforce = waypointforce;
    }

    public ArrayList<PVector> getWaypoints() {
        return waypoints;
    }

    public void setCurrWaypointA(PVector currWaypoint) {
        this.currWaypointA = currWaypoint;
    }

    public PVector getCurrWaypointA() {
        return currWaypointA;
    }

    public ArrayList<PVector> getWaypointsA() {
        return waypointsA;
    }

    public PVector getCurrWaypoint() {
        return currWaypoint;
    }

    public Iterator<PVector> getIterator() {
        return iterator;
    }

    public void setCurrWaypoint(PVector currWaypoint) {
        this.currWaypoint = currWaypoint;
    }
    
    public void setWaypoints(ArrayList<PVector> waypoints) {
        this.waypoints = waypoints;
    }
    
    public void setIterator(Iterator<PVector> iterator) {
        this.iterator = iterator;
    }

    /*
     * Method: setup()
     */
    public void setup(){
        iterator = waypoints.iterator();
        currWaypoint = iterator.next();
        currentPosition = 0;
    }
    
    /*
     * Method: copy()
     */
    public void copy(){

    }

    /*
     * Method: restartIterator()
     */
    public void restartIterator(){
        iterator = waypoints.iterator();
    }


    /*
     * Method: patrol()
     * Inputs: location, boid
     * Function:
     * Return: PVector targer
     */
    public PVector patrol(PVector location, Boid_generic b){

        currWaypoint = waypoints.get(currentPosition);//iterator.next();
        if(Math.abs(PVector.dist(location,currWaypoint))<=5) { // was 2
            currentPosition = (currentPosition + 1) % waypoints.size(); //loop back to the beginning of the waypoints list [0:n)
        }																//currentPosition is an index of currWaypoint in waypoints PVector ArrayList

        currWaypoint = waypoints.get(currentPosition);//iterator.next();
        PVector targer = PVector.sub(currWaypoint,b.getLocation());
        targer.setMag(waypointforce); // was 0.03
        return targer;

    }


}
