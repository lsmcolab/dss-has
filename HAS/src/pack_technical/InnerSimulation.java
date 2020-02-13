package pack_technical;

import pack_AI.AI_type;

import pack_boids.Boid_generic;
import pack_boids.Boid_standard;
import processing.core.PApplet;
import processing.core.PVector;

import javax.imageio.ImageIO;

import org.rosuda.JRI.Rengine;

import Jama.Matrix;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import pack_ecllipse.Eign;
import pack_ecllipse.Ellipse;
import pack_ecllipse.Ellipse2;
import pack_ecllipse.EllipseForecast;

/**
 * InnerSimulation class
 * This class is to implement ellipse-based inner simulation and on-line planning for attacker (in head choice based on simulation result)
 * - Instead of waypoints estimation, I will use ellipse estimation and prediction for future position
 * - Decision for collision will change to: attacker's position is on or in ellipse --> collision
 * - There is no need to Clones the defenders but to Clone the ellipse
 * 
 * Comments: the ellipse-based on inner simulation should be quicker for online decision making
 */
public class InnerSimulation  {
	/*
	 * Define variables
	 */
	PApplet parent; 
    ArrayList<Boid_generic> attackBoids; 
    ArrayList<Boid_generic> SimulationClones; 
    AI_type ai; 
	Rengine re;
    
    private int tick = 0; 
    private boolean finished = false; 
    boolean draw = true; 
    private float fall =0;

    final BufferedImage image = new BufferedImage (1000, 1000, BufferedImage.TYPE_INT_ARGB ); 
    final Graphics2D graphics2D = image.createGraphics ();
    final BufferedImage image1 = new BufferedImage (1000, 1000, BufferedImage.TYPE_INT_ARGB );
    final Graphics2D graphics2Di = image1.createGraphics ();
    
    ArrayList<PVector> historyOfMovement = new ArrayList<>();
    Map<ArrayList<Boid_generic>,PVector> attackingVectors = new HashMap<>(); 
    
    Random randG = new Random(); 
    public PVector targetVector = new PVector(0,0); 
    boolean shouldRestart = false; 
    public boolean simulating = true; 
    private boolean vicotry = false; 
//    public boolean firstSim = true;
    
    ArrayList<ArrayList<PVector>> boidsLocations = new ArrayList<>();
    ArrayList<ArrayList<PVector>> boidsLocationsClone = new ArrayList<>();
    ArrayList<PVector> currentboidsLocations = new ArrayList<>();
    
	ArrayList<Ellipse2> observedEllipses =new ArrayList<>();
	ArrayList<Ellipse2> predictedExpandedEllipses = new ArrayList<>();

	public int h=100;//Need to estimate the maximum of Horizon for InnerSimulation. It is the total steps/10 from attacker initial to target
  	
	private int startIndex = 0;
  	private int endIndex = 0;
  	
  	private int start = 0;
  	private int end = 0;
  	

	public PrintWriter writer3 = new PrintWriter ("Data/Predicted_X.txt");
	public PrintWriter writer4 = new PrintWriter ("Data/Predicted_Y.txt");
	public PrintWriter writer5 = new PrintWriter ("Data/Predicted_L1.txt");
	public PrintWriter writer6 = new PrintWriter ("Data/Predicted_L2.txt");
	public PrintWriter writer7 = new PrintWriter ("Data/Predicted_Ang.txt");
	public PrintWriter writer8 = new PrintWriter ("Data/Observed_X.txt");
	public PrintWriter writer9 = new PrintWriter ("Data/Observed_Y.txt"); 
	public PrintWriter writer10= new PrintWriter ("Data/Observed_L1.txt");
	public PrintWriter writer11= new PrintWriter ("Data/Observed_L2.txt");
	public PrintWriter writer12= new PrintWriter ("Data/Observed_Ang.txt");
	
	/**
     * Constructor with parameters (define a way how to initialise a new object instance)
     */
    public InnerSimulation(AI_type ai, ArrayList<ArrayList<PVector>> boidsLocations, 
    		ArrayList<Boid_generic> attackers,PApplet parent, Rengine re) throws IOException {
    	this.ai = ai; 
        this.parent=parent; 
        this.attackBoids=copyTheStateOfAttackBoids(attackers);
        this.re = re;//this is to make sure all other re in this class referring to the same re object passed down from other higher level class
        this.boidsLocationsClone = copyOfBoidsLocations(boidsLocations);
        this.observedEllipses=generateRealEllipses(boidsLocationsClone);
        this.predictedExpandedEllipses=generateFutureAndExpandedEllipses(observedEllipses,h,re); //number of forecasted ellipse is integer h
         
        createSimulationsAndRandomVectors(attackBoids);
        

    }
	
   
    /*
     * Method: restartTheSimulation()
     * Input: attackboids, defenders
     * Function: clear up the simulation history and re-do the simulation //very similar to innersimulation constructor
     * The Key difference between this restartTheSimulation and Simulation
     * Return: void (action)
     */
    public void restartTheSimulation(ArrayList<Boid_generic> attackBoidss, ArrayList<ArrayList<PVector>> boidsLocations) {
    	//Clear all the past simulation records, reset the attacker, defenders and predictedEllipses
        attackingVectors.clear();
        attackBoids.clear();
        this.attackBoids=copyTheStateOfAttackBoids(attackBoidss);
        predictedExpandedEllipses.clear();
        tick = 0;
        
        this.boidsLocationsClone = copyOfBoidsLocations(boidsLocations);
//        observedEllipses=generateRealEllipses(boidsLocations);//receive updated boidsLocations (additional currentBoidsLocations)
//        predictedExpandedEllipses=generateFutureAndExpandedEllipses(observedEllipses,h, re);//with updated observedEllipses, update prediction
        
        //add the attacker's location and a random position PVector to a HashMap object: attackingVectors
        createSimulationsAndRandomVectors(attackBoids);
    }
    
    /*
     * Method: generateRealEllipses()
     */
    public ArrayList<Ellipse2> generateRealEllipses(ArrayList<ArrayList<PVector>> boidsLocationsClone){
    	int m = boidsLocationsClone.get(0).size();//number of boids PVector in one arrayList (in one swarm)
    	endIndex = boidsLocationsClone.size();
      	ArrayList<ArrayList<PVector>> newOnes = new ArrayList<ArrayList<PVector>>();
  		
    	for (int i = startIndex; i < boidsLocationsClone.size(); i++) {
    		newOnes.add(boidsLocationsClone.get(i));
		}
    	
      	for (ArrayList<PVector> array : newOnes) {
  			double[][] swarm = new double[m][2];//2D array
  	    	for (int i = 0; i < m; i++) {
  				swarm[i][0] = (double) array.get(i).x;
  				swarm[i][1] = (double) array.get(i).y;
  			}
//     	    	Ellipse currentEll = new Ellipse(swarm,0.001);//tolerance rate = 0.001
  	    	Ellipse2 currentEll = new Ellipse2(swarm);
  	    	observedEllipses.add(currentEll);
		}
  		startIndex = endIndex;
       	return observedEllipses;
    }
    
    
	/*
     * Method: generate and Interpolate predicted Ellipses()
     * Input: observed all Ellipse feature histories
     * Function: return features from the predicted ellipses from auto.Arima models
     * Interpolate predictedEllipses 
     * Output: Predicted and expanded future ellipses
     */
    public ArrayList<Ellipse2> generateFutureAndExpandedEllipses(ArrayList<Ellipse2> observedEllipses, int h, Rengine re) {
		predictedExpandedEllipses.clear();

    	ArrayList<Double> ellipseX = new ArrayList<>();
    	ArrayList<Double> ellipseY = new ArrayList<>();
    	ArrayList<Double> ellipseL1 = new ArrayList<>();
    	ArrayList<Double> ellipseL2 = new ArrayList<>();
    	ArrayList<Double> ellipseAng = new ArrayList<>();
    	
    	ArrayList<Double> predicted_X = new ArrayList<>();
    	ArrayList<Double> predicted_Y = new ArrayList<>();
    	ArrayList<Double> predicted_L1 = new ArrayList<>();
    	ArrayList<Double> predicted_L2 = new ArrayList<>();
    	ArrayList<Double> predicted_Ang = new ArrayList<>();
    	
    	ArrayList<Double> X = new ArrayList<>();
    	ArrayList<Double> Y = new ArrayList<>();
    	ArrayList<Double> L1 = new ArrayList<>();
    	ArrayList<Double> L2 = new ArrayList<>();
    	ArrayList<Double> Angle = new ArrayList<>();
    	
    	int last1 = observedEllipses.size();
    	
    	for (Ellipse2 ellipse :observedEllipses) {
    		ellipseX.add((Double) ellipse.getCenterX());
    		ellipseY.add((Double) ellipse.getCenterY());
    		ellipseL1.add((Double) ellipse.getL1());
    		ellipseL2.add((Double) ellipse.getL2());
    		ellipseAng.add((Double) ellipse.getAng());
		}
    	
//       	writer8.println(ellipseX);
//    	writer9.println(ellipseY);  
//    	writer10.println(ellipseL1);  
//    	writer11.println(ellipseL2);  
//    	writer12.println(ellipseAng);
//    	
//    	writer8.flush();
//    	writer9.flush();  
//    	writer10.flush();  
//    	writer11.flush();  
//    	writer12.flush();
    	
    	EllipseForecast ari = new EllipseForecast(ellipseX, ellipseY, ellipseL1,  ellipseL2,  ellipseAng, h, re);
    	
    	predicted_X = ari.getPredicted_X();
    	predicted_Y = ari.getPredicted_Y();
    	predicted_L1 = ari.getPredicted_L1();
    	predicted_L2 = ari.getPredicted_L2();
    	predicted_Ang = ari.getPredicted_Ang();
    	
//    	writer3.println(predicted_X);
//    	writer4.println(predicted_Y);  
//    	writer5.println(predicted_L1);  
//    	writer6.println(predicted_L2);  
//    	writer7.println(predicted_Ang);   	
//    	
//    	writer3.flush();
//    	writer4.flush();
//    	writer5.flush();
//    	writer6.flush();
//    	writer7.flush();
    	
    	/*writer3.close();
    	writer4.close();
    	writer5.close();
    	writer6.close();
    	writer7.close();*/
    	
    	//add the last real ellipse to be the first ellipse in this predicted Ellipse list
    	predicted_X.add(0, observedEllipses.get(last1-1).getCenterX());
    	predicted_Y.add(0, observedEllipses.get(last1-1).getCenterY());
    	predicted_L1.add(0, observedEllipses.get(last1-1).getL1());
    	predicted_L2.add(0, observedEllipses.get(last1-1).getL2());
    	predicted_Ang.add(0, observedEllipses.get(last1-1).getAng());
    	

    	//Linear interpolate the 9 gaps in forecasted ellipses
    	for (int i = 0; i < predicted_X.size()-1; i++) {
			for (int j = 0; j < 10; j++) {
				X.add(predicted_X.get(i)*(10 -j)/10 + predicted_X.get(i+1)*(j)/10 );
				Y.add(predicted_Y.get(i)*(10-j)/10 + predicted_Y.get(i+1)*(j)/10 );
				L1.add(predicted_L1.get(i)*(10-j)/10 + predicted_L1.get(i+1)*(j)/10 );
				L2.add(predicted_L2.get(i)*(10-j)/10 + predicted_L2.get(i+1)*(j)/10 );
				Angle.add(predicted_Ang.get(i)*(10-j)/10 + predicted_Ang.get(i+1)*(j)/10);
			}
		}
    	//summarise information into ellipses
    	for (int i = 0; i < X.size(); i++) {
			Ellipse2 ell = new Ellipse2();
			ell.setCenterX(X.get(i));
			ell.setCenterY(Y.get(i));
			ell.setL1(L1.get(i));
			ell.setL2(L2.get(i));
			ell.setAng(Angle.get(i));
			predictedExpandedEllipses.add(ell);
			
		}
		return predictedExpandedEllipses;
	}
    

	/*
	 * Method:createSimulationsAndRandomVectors() - keep
	 * Input: ArrayList attackBoids
	 * Function: based on two random float numbers, generate a random point PVector with magnitude as 1, 
	 * 				generate 1000 Map entry: attackers location, random number 
	 * 				for each one estimated attacker boid's location in the input list
	 * Return: void (action)
	 */
    public void createSimulationsAndRandomVectors(ArrayList<Boid_generic> attackBoids){
    	//attackingVectors.clear();//no historical record is kept in this Hashmap;
        for(int j=0;j<1000;j++){
            //Random randG;
			float rand1 = randG.nextFloat() * 1;
            float rand2 = randG.nextFloat() * 1;
            PVector MrLeandroVector = new PVector(-1+2*rand1, -1+2*rand2); //PVector(x,y) with range of [-1,1],[-1,1], 
            MrLeandroVector.setMag(0.1f);//move to any point in a circle of radius 1
            // MrLeandroVector.normalize();
            attackingVectors.put(copyTheStateOfAttackBoids(attackBoids), MrLeandroVector);  //Monte Carlo Simulation
        }
        //return attackingVectors;
    }
    
    /*
     * isCollided2()
     * Test if the attacker is collided with the ellipse
     * Input: attacker and ellipse
     * Output: boolean true or false
     */
    private boolean isCollided2(Boid_generic attacker, Ellipse2 ellipse) {
		float x = attacker.get_future_location().x;
		float y = attacker.get_future_location().y;
		float centerX = (float) ellipse.getCenterX();
		float centerY = (float) ellipse.getCenterY();
		float l1 = (float) ellipse.getL1();
		float l2 = (float) ellipse.getL2();
		float ang = (float) ellipse.getAng();
	
		float xc = x - centerX;
		float yc = y - centerY;
		float xct = (float) (xc * Math.cos(ang) - yc * Math.sin(ang));
		float yct = (float) (xc * Math.sin(ang) + yc * Math.cos(ang));
		float fall = (float) (Math.pow(xct,2)/Math.pow(l1, 2) + Math.pow(yct,2)/Math.pow(l2,2));//the Ellipse class calculate the l1 is half of weight (measure in Python ellipse)
		
		if(fall<=1) {
			return true;
		} else {
			//System.out.println("The metrics of attacker and ellipse is " + fall);
			return false;
		}
	}
    
	/*
     * Method: copyTheStateOfAttackBoids -keep
     * Input: boids
     * Function:Assign values to new boids copied
     * Return: ArrayList boidListClone
     */
    public ArrayList<Boid_generic> copyTheStateOfAttackBoids(ArrayList<Boid_generic> boids) {
        ArrayList<Boid_generic> boidListClone = new ArrayList<>();
        for(Boid_generic boid : boids){
            Boid_generic bi = new Boid_standard(parent,boid.getLocation().x,boid.getLocation().y,6,10);//inherited the int t=6, int id=10
            bi.setAi(ai);
            bi.setAcceleration(boid.getAcceleration());
            bi.setVelocity(boid.getVelocity());
            bi.setLocation(boid.getLocation());
            boidListClone.add(bi);
        }
        return boidListClone;
    }
    
    /*
     * copyOfBoidsLocations
     */
    public ArrayList<ArrayList<PVector>> copyOfBoidsLocations(ArrayList<ArrayList<PVector>> boidsLocations){
    	end = boidsLocations.size();
    	for (int i = start; i < boidsLocations.size(); i++) {
    		ArrayList<PVector> clone = new ArrayList();
    		for (PVector array : boidsLocations.get(i)) {
				clone.add(array);
			}
    		boidsLocationsClone.add(clone);
    		start = end;
		}
		return boidsLocationsClone;
    }
    
    /*
     * Method: run1()
     * Input: none
     * Function:
     * Return: void (action)
     */
    public void run1() throws IOException {
//    	if (!simulating)
//    	{
//    		int x = 0;
//    	}
    	boolean firstSim = true;
        if (simulating) { //initial value is True AND later situation can change this boolean variable; situational control
           //loop over each entry in the Map object attackingVectors (for all the number of (attacker's location * 1000) entry in the map
            if(firstSim) {
                observedEllipses=generateRealEllipses(boidsLocationsClone);//receive updated boidsLocations (additional currentBoidsLocations)
                predictedExpandedEllipses=generateFutureAndExpandedEllipses(observedEllipses,h, re);//w
                firstSim = false;
            }
        	tick++;
            //System.out.println("current tick number is " + tick);
            PVector theClosest = new PVector(0,0);
            float ClosestD = Float.MAX_VALUE;
            
            outerloop:
        	for (Map.Entry<ArrayList<Boid_generic>, PVector> pair : attackingVectors.entrySet()) { 
                Ellipse2 ellipse = new Ellipse2();
                Boid_generic attacker = pair.getKey().get(0);
                
                for (int i = 0; i < 1000; i++) {
                	//for (Boid_generic attacker : pair.getKey()) {//only one Boid object here
                	PVector acceleration = attacker.getAcceleration();
                	PVector velocity = attacker.getVelocity();
                	PVector location = attacker.getLocation();
 
                    //assign value to attacker's property and move it by the velocity and acceleration and the .getValue of the randomly direction and position in a circle 
                    velocity.limit(1);
                    location.add(velocity.add(acceleration.add(pair.getValue()))); //attacker boid has moved in a random choice
                    acceleration.mult(0); //re-set acceleration to zero;
   
                    // CHECK COLLISION after the move for the 1000 possible locations: 
                    ellipse = predictedExpandedEllipses.get(i);//can be empty!!
                    
                    if(isCollided2(attacker, ellipse)) {
                        attacker.setHasFailed(true);//add a label to the possible location which will lead to a collision
                        break;
                    }                        
                        
                    if((PVector.dist(location, new PVector(550,500))<10)){ //can be a tick function, depends on how far the attacker still need to travel
                        //distance =(Float) PVector.dist(location, new PVector(550,500));
                        theClosest = pair.getValue(); 
                        //simulating = false;
                        break outerloop;
                    }
 
                }
            
            if (!attacker.isHasFailed()) {
                Float distance = (Float) PVector.dist(attacker.getLocation(), new PVector(550,500));
                if(distance < ClosestD) {
                	ClosestD = distance;
                	theClosest = pair.getValue();
                }
             }
				
         } 
            
        targetVector = theClosest;
        simulating = false;  //stop run1() as the attacker finished its job
        

        }
    }


    
	/*
	 * Get and Set methods
	 */
    public boolean isFinished() {
        return finished;
    }

    public void setAii(AI_type ai) {
        this.ai = ai;
    }
	
    public boolean isVicotry() {
        return vicotry;
    }
    
    public boolean isSimulating() {
        return simulating;
    }

    public void setSimulating(boolean simulating) {
        this.simulating = simulating;
    }


	public ArrayList<Ellipse2> getObservedEllipses() {
		return observedEllipses;
	}


	public void setObservedEllipses(ArrayList<ArrayList<PVector>> boidsLocationsClone) {
		this.observedEllipses=generateRealEllipses(boidsLocationsClone);
	}

	public void setPredictedEllipses(ArrayList<Ellipse2> observedEllipses,int h,Rengine re) {
		this.predictedExpandedEllipses=generateFutureAndExpandedEllipses(observedEllipses,h,re); 
	}


	public ArrayList<Ellipse2> getPredictedExpandedEllipses() {
		return predictedExpandedEllipses;
	}
	
	public PVector returnTargetVector() {
		return targetVector;
	}
	

}