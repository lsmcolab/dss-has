package pack_technical;

import pack_AI.AI_manager;
import pack_AI.AI_type;
import pack_boids.Boid_generic;
import pack_boids.Boid_standard;
import pack_ecllipse.Ellipse;
import processing.core.PApplet;
import processing.core.PVector;

import java.io.IOException;
import java.util.*;
import org.rosuda.JRI.Rengine;

/**
 * EnvironmentalSimulation class
 * - Combination of parameter-simulation and Inner-Simulatoin2
 */
public class EnviromentalSimulation extends Thread { //*A Thread is applied here!
	/*
	 * Define variables
	 */
    AI_type ai;
    AI_type simulator;
    PApplet parent;
    ArrayList<Boid_generic> defenders;
    ArrayList<Boid_generic> SimulationClones;
    ArrayList<Boid_generic> attackBoids;
    ArrayList<InnerSimulation> simulations = new ArrayList<>();
    ArrayList<InnerSimulation> historyOfSimulations = new ArrayList<>();
    ArrayList<PVector> historyOfMovement = new ArrayList<>();
    
    ArrayList<ArrayList<PVector>> boidsLocations = new ArrayList<>();
    ArrayList<Ellipse> observedEllipses = new ArrayList<>(); 
	ArrayList<Ellipse> predictedEllipses = new ArrayList<>(); 
	
    boolean draw = true;
    int tick = 0;
    FlockManager flock;
    static int simulationCounter =0;
    double startTime=0;
    boolean simulating;

    Random rand = new Random();
    PVector currentAimVector = new PVector(0,0);
    Rengine re;
    
    /**
     * EnviromentalSimulation constructor
     */

    public EnviromentalSimulation(int sns, int ans, int cns, double sw, double aw, double cw, String name, ArrayList<Boid_generic> attackers,ArrayList<Boid_generic> defenders, ArrayList<ArrayList<PVector>> boidsLocations,PApplet parent, Rengine re) throws IOException {
        this.parent=parent;
        simulator = new AI_type(randFloat(30, AI_manager.neighbourhoodSeparation_upper_bound), 70, 70, 2.0, 1.2, 0.9f,0.04f,"Simulator2000");
        SimulationClones = copyTheStateOfAttackBoids(defenders);
        this.attackBoids = copyTheStateOfAttackBoids(attackers);
        this.flock= new FlockManager(parent,true,true);
        this.boidsLocations = boidsLocations;
        this.re = re;
        for(Boid_generic g : SimulationClones){
            g.setAi(simulator);
        }

        startTime=System.nanoTime(); //start TIMING!

        for(int i=0;i<1;i++){//single simulation; code: simulations(a list of innerSimulation) = new InnerSimulation(para...)
            simulations.add(new InnerSimulation(simulator, boidsLocations, attackers, parent, re));
        }
        
        new Thread(this).start(); //start a Thread of (ENVIROMENTALSIMULATION)!
    }

 
    /*
     * Method: run()
     * Input: no
     * Function:run inner simulations
     * Return: void(action)
     */
    public void run(){
        while(true){
            try {
                 for(InnerSimulation s : simulations){
                     s.run1();//call the run1() in InnerSimulatioin class
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    
    /*
     * Method: restartTheSimulation()
     */
    public void restartTheSimulation(ArrayList<Boid_generic> attackBoids, ArrayList<ArrayList<PVector>> boidsLocations){
        simulations.get(0).restartTheSimulation(attackBoids,boidsLocations);
    }

    public void setSimulating(boolean k){
        simulations.get(0).setSimulating(k);

    }
    
    /*
     * Method: returnTargetVector()
     * Function: collecting the best moving choices in simulation
     * Return: a PVector - the recommended forward steps
     */
    public PVector returnTargetVector(){
        return simulations.get(0).returnTargetVector();
    }
    
    /*
     * Method: randFloat
     * Input: two float number: min and max
     * Function: generate a random float type number between min and max FOR the AI_manager.neighbourhoodSeparation_upper_bound
     * Randomise one parameter for the AI swarm
     * Return: a static float: result
     */
    public static float randFloat(float min, float max) {
        Random rand = new Random();
        float result = rand.nextFloat() * (max - min) + min;
        return result;
    }


    /*
     * Method: copyTheStateOfAttackBoids
     * Input: boids
     * Function: for each boid in boids, copy the location and acceleration, velocity
     * Return: a new ArrayList of boidListClone
     */
    public ArrayList<Boid_generic> copyTheStateOfAttackBoids(ArrayList<Boid_generic> boids) {
        ArrayList<Boid_generic> boidListClone = new ArrayList<>();

        for(Boid_generic boid : boids){
            Boid_generic bi = new Boid_standard(parent,boid.getLocation().x,boid.getLocation().y,6,10);
            bi.setAcceleration(boid.getAcceleration());
            bi.setVelocity(boid.getVelocity());
            bi.setLocation(boid.getLocation());
            boidListClone.add(bi);
        }
        return boidListClone;
    }

    /*
     * Get and Set methods
     */
    public void setAiToInnerSimulation(AI_type t){
        simulations.get(0).setAii(t);
    }

    public AI_type getSimulator() {
        return simulator;
    }
    
    public boolean isSimulating(){
        return simulations.get(0).isSimulating();
    }
}
