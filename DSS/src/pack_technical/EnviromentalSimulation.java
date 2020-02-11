package pack_technical;

import pack_1.Launcher;
import pack_AI.AI_manager;
import pack_AI.AI_type;
import pack_boids.Boid_generic;
import pack_boids.Boid_standard;
import processing.core.PApplet;
import processing.core.PVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class EnviromentalSimulation extends Thread {
    AI_type ai;
    ArrayList<Boid_generic> defenders;
    ArrayList<Boid_generic> SimulationClones;
    ArrayList <Boid_generic> attackBoids;

    public AI_type getSimulator() {
        return simulator;
    }

    AI_type simulator;
    PApplet parent;
    PatrollingScheme scheme ;

    ArrayList<InnerSimulation> simulations = new ArrayList<>();
    ArrayList<InnerSimulation> historyOfSimulations = new ArrayList<>();

    ArrayList<int[]> historyOfMovement = new ArrayList<>();
    boolean draw = true;
    int tick = 0;
    FlockManager flock;
    static int simulationCounter =0;
    double startTime=0;
    CollisionHandler handler;
    Random rand = new Random();





    PVector currentAimVector = new PVector(0,0);

    public EnviromentalSimulation(int sns, int ans, int cns, double sw, double aw, double cw, String name, ArrayList<Boid_generic> defenders,PApplet parent,ArrayList<int[]> cords,ArrayList<Boid_generic> attackers,CollisionHandler handler) throws IOException {
        this.parent=parent;
        this.handler=handler;


        simulator = new AI_type(randFloat(AI_manager.neighbourhoodSeparation_lower_bound, AI_manager.neighbourhoodSeparation_upper_bound), 70, 70, 2.0, 1.2, 0.9f,0.04f,"Simulator2000");

        SimulationClones = copyTheStateOfAttackBoids(defenders);
        this.attackBoids = copyTheStateOfAttackBoids(attackers);

        this.flock=new FlockManager(parent,true,true);
        this.scheme=  new PatrollingScheme(simulator.getWayPointForce());
        for(Boid_generic g : SimulationClones){
            g.setAi(simulator);

        }

       for(int[] cord : cords){
       scheme.getWaypoints().add(new PVector(cord[0],cord[1]));

    }
        //FOLLOW THE SIMILLAR WAYPOINT AS DEFENDERS _____________________________________________________________
        PVector theClosestOne = new PVector(2000,2000);
        float shortestDistance=3000;
        int counter =0;
        int positionInTheList =0;
        for(PVector checkpoint : scheme.getWaypoints()){
            float distance = PVector.dist(SimulationClones.get(0).getLocation(),checkpoint);
            counter++;
           // System.out.println(distance);
            if(distance<shortestDistance){
                shortestDistance=distance;
                positionInTheList=counter;

            }


        }

        scheme.setup();

        for(int i=0;i<positionInTheList+1;i++){
            if (!scheme.getIterator().hasNext()){   // the ! is important
                scheme.setIterator(scheme.getWaypoints().iterator());
            }
            scheme.setCurrWaypoint( scheme.getIterator().next());
        }
        startTime=System.nanoTime();

        for(int i=0;i<1;i++){
            simulations.add(new InnerSimulation(simulator, defenders, cords, attackers, handler,parent));
        }
        new Thread(this).start();
    }

    public void setAiToInnerSimulation(AI_type t){
        simulations.get(0).setAii(t);
    }


    public boolean isSimulating(){

        return simulations.get(0).isSimulating();

    }

    public static float randFloat(float min, float max) {

        Random rand = new Random();

        float result = rand.nextFloat() * (max - min) + min;

        return result;

    }

    public void restartTheSimulation(ArrayList<Boid_generic> attackBoids,ArrayList<Boid_generic> defenders){
        simulations.get(0).restartTheSimulation(attackBoids,defenders);
    }

    public void setSimulating(boolean k){
        simulations.get(0).setSimulating(k);

    }
    public PVector returnTargetVector(){

        return simulations.get(0).reutrnTargetVecotr();
    }

    public void run(){
        int kor=0;

        while(true){

            kor++;

                try {
                    for(InnerSimulation s : simulations){

//
                        s.run1();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


        }

    }

    public ArrayList<Boid_generic> copyTheStateOfAttackBoids(ArrayList<Boid_generic> boids) {
        ArrayList<Boid_generic> boidListClone = new ArrayList<>();
       // System.out.println(boids);

        for(Boid_generic boid : boids){
            //nadaj im tutaj acceleration velocity etc..
            Boid_generic bi = new Boid_standard(parent,boid.getLocation().x,boid.getLocation().y,6,10);
            //bi.setAi(simulator);
            bi.setAcceleration(boid.getAcceleration());
            bi.setVelocity(boid.getVelocity());
            bi.setLocation(boid.getLocation());
            boidListClone.add(bi);
        }

        return boidListClone;

    }



}
