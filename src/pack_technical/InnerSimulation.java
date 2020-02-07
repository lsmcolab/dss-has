package pack_technical;

import pack_AI.AI_type;
import pack_boids.Boid_generic;
import pack_boids.Boid_standard;
import processing.core.PApplet;
import processing.core.PVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InnerSimulation  {
    public boolean isFinished() {
        return finished;
    }
    ArrayList<Boid_generic> attackBoids;
    ArrayList<Boid_generic> SimulationClones;

    public void setAii(AI_type ai) {
        this.ai = ai;
    }

    AI_type ai;
    private int tick =0;
    private boolean finished=false;
    PApplet parent;
    CollisionHandler handler;
    ArrayList<int[]> cords ;
    public boolean isVicotry() {
        return vicotry;
    }
    boolean draw=true;
    final BufferedImage image = new BufferedImage ( 1000, 1000, BufferedImage.TYPE_INT_ARGB );
    final Graphics2D graphics2D = image.createGraphics ();
   // before history draw
   final BufferedImage image1 = new BufferedImage ( 1000, 1000, BufferedImage.TYPE_INT_ARGB );
    final Graphics2D graphics2Di = image1.createGraphics ();
    ArrayList<int[]> historyOfMovement = new ArrayList<>();
    private boolean vicotry=false;
    PatrollingScheme scheme ;

    Integer nextWaypoint;

    Map<ArrayList<Boid_generic>,PVector> attackingVectors = new HashMap<>();
    Random randG = new Random();
    PVector targetVector = new PVector(0,0);
    boolean shouldRestart=false;

    int counter = 0;

    public boolean isSimulating() {
        return simulating;
    }

    public void setSimulating(boolean simulating) {
        this.simulating = simulating;
    }

    boolean simulating=true;

    public void createSimulationsAndRandomVectors(ArrayList<Boid_generic> attackBoids){
        for(int j=0;j<1000;j++){
            float rand = randG.nextFloat() * 1;
            float rand2 = randG.nextFloat() * 1;
            PVector AllDirectionVectors = new PVector(-1+2*rand, -1+2*rand2);
            AllDirectionVectors.setMag(0.1f);

            attackingVectors.put(copyTheStateOfAttackBoids(attackBoids),AllDirectionVectors);

        }
    }
    public void restartTheSimulation(ArrayList<Boid_generic> attackBoidss,ArrayList<Boid_generic> defenders ) {
        attackingVectors.clear();
        attackBoids.clear();
        SimulationClones.clear();
        this.attackBoids=copyTheStateOfAttackBoids(attackBoidss);
        this.SimulationClones = copyTheStateOfAttackBoids(defenders);
        scheme.setWaypointforce(ai.getWayPointForce());
        for(Boid_generic g : SimulationClones){
            g.setAi(ai);

            //flock.getReal_boids().add(g);
        }
        scheme.restartIterator();
        PVector theClosestOne = new PVector(2000, 2000);
        float shortestDistance = 3000;
        int counter = 0;
        int positionInTheList = 0;
        float shortestVectorAngle=0;
        float nextToShortestVectorAngle=0;
        for(int i=0;i<scheme.getWaypoints().size();i++) {
            PVector checkpoint = scheme.getWaypoints().get(i);
            PVector nextCheckPoint = scheme.getWaypoints().get((i+1)%scheme.getWaypoints().size());

            float distance = PVector.dist(SimulationClones.get(0).getLocation(), checkpoint);

            // System.out.println(distance);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                positionInTheList = counter;
                shortestVectorAngle = PVector.angleBetween(SimulationClones.get(0).getLocation(), checkpoint);
                nextToShortestVectorAngle = PVector.angleBetween(SimulationClones.get(0).getLocation(), nextCheckPoint);
            }
            counter++;
        }
        //scheme.setup();

        if (shortestVectorAngle < nextToShortestVectorAngle) {
            nextWaypoint = positionInTheList;
        }
        else{
            nextWaypoint = (positionInTheList + 1) % scheme.getWaypoints().size();
        }


        scheme.currentPosition = nextWaypoint;

        createSimulationsAndRandomVectors(attackBoids);

    }
    public InnerSimulation(AI_type ai, ArrayList<Boid_generic> defenders, ArrayList<int[]> cords, ArrayList<Boid_generic> attackers,CollisionHandler handler,PApplet parent) throws IOException {
        this.ai = ai;
        this.parent=parent;
        this.cords= new ArrayList<>(cords);
        this.parent=parent;
        this.attackBoids=copyTheStateOfAttackBoids(attackers);
        this.SimulationClones=copyTheStateOfAttackBoids(defenders);
        this.handler=handler;
        scheme = new PatrollingScheme(ai.getWayPointForce());

        for(int[] cord : cords){
            scheme.getWaypoints().add(new PVector(cord[0],cord[1]));
;
        }
        //FOLLOW THE SIMILLAR WAYPOINT AS DEFENDERS _____________________________________________________________
        PVector theClosestOne = new PVector(2000, 2000);
        float shortestDistance = 3000;
        int counter = 0;
        int positionInTheList = 0;
        float shortestVectorAngle=0;
        float nextToShortestVectorAngle=0;
        for(int i=0;i<scheme.getWaypoints().size();i++) {
            PVector checkpoint = scheme.getWaypoints().get(i);
            PVector nextCheckPoint = scheme.getWaypoints().get((i+1)%scheme.getWaypoints().size());

            float distance = PVector.dist(SimulationClones.get(0).getLocation(), checkpoint);

            // System.out.println(distance);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                positionInTheList = counter;
                shortestVectorAngle = PVector.angleBetween(SimulationClones.get(0).getLocation(), checkpoint);
                nextToShortestVectorAngle = PVector.angleBetween(SimulationClones.get(0).getLocation(), nextCheckPoint);
            }
            counter++;
        }
        //scheme.setup();

        if (shortestVectorAngle < nextToShortestVectorAngle) {
            nextWaypoint = positionInTheList;
        }
        else{
            nextWaypoint = (positionInTheList + 1) % scheme.getWaypoints().size();
        }


        scheme.currentPosition = nextWaypoint;


        createSimulationsAndRandomVectors(attackBoids);

    }
    public PVector reutrnTargetVecotr(){




        return targetVector;
    }

    public void run1() throws IOException {
        //flock.run(1);
        if (simulating) {


            boolean willContinueSimulation = true;
            tick++;
            PVector sumOfMassCentres = new PVector(0, 0);
            int counter = 0;
            //System.out.println("attackers" + attackBoids);
            // Map<ArrayList<Boid_generic>,PVector> newMap = new HashMap<>();
            PVector theClosest = new PVector(0,0);
            float theClosetDistance = 2000;
            float distance = 150;
            boolean CheckVector = false ;
            for (Map.Entry<ArrayList<Boid_generic>, PVector> pair : attackingVectors.entrySet()) {
                ArrayList<Boid_generic> newStatus = new ArrayList<>();
                PVector theirPvector = new PVector(0, 0);

                for (Boid_generic attacker : pair.getKey()) {
                    PVector acceleration = attacker.getAcceleration();
                    PVector velocity = attacker.getVelocity();
                    PVector location = attacker.getLocation();
                    // CHECK COLLISION _______________________________________________________
                    for (Boid_generic b1 : SimulationClones) {

                        if (Math.abs(PVector.dist(b1.getLocation(), location)) < 16) {  // was 3
                            attacker.setHasFailed(true);

                        }

                    }
                    if((PVector.dist(location,new PVector(550,500))<=10 || PVector.dist(attackBoids.get(0).getLocation(),location)>=distance /*location.x-50<=0*/) && !attacker.isHasFailed()){
                        //targetVector=theClosest;
                        //simulating=false;
                        willContinueSimulation = false;
                    }

                    velocity.limit(1);
                    location.add(velocity.add(acceleration.add(pair.getValue())));
                    acceleration.mult(0);


                }

            }

                for (Map.Entry<ArrayList<Boid_generic>, PVector> pair : attackingVectors.entrySet()) {


                    for (Boid_generic attacker : pair.getKey()) {

                        PVector location = attacker.getLocation();
                        float currentDistance = Math.abs(PVector.dist(location,new PVector(550,500)));
                        if (currentDistance < theClosetDistance && !attacker.isHasFailed()) {
                            theClosest = pair.getValue();
                            theClosetDistance = currentDistance;
                        }
                        if(!attacker.isHasFailed())
                            CheckVector = true;


                    }
                }
                if(CheckVector) {
                    if(!willContinueSimulation)
                        targetVector = theClosest;
                } else {

                    willContinueSimulation = false;
                }

            if (!willContinueSimulation)
                simulating = false;


            // attackingVectors = newMap;
            if (simulating) {
                for (Boid_generic b : SimulationClones) {

                    PVector acceleration = b.getAcceleration();
                    PVector velocity = b.getVelocity();
                    PVector location = b.getLocation();
                    b.run(SimulationClones, true, true);

                    velocity.limit(1);
                    location.add(velocity.add(acceleration.add(scheme.patrol(b.getLocation(), b)/*patrolling.patrol(be.getLocation(),be)*/)));
                    acceleration.mult(0);



                    sumOfMassCentres = PVector.add(sumOfMassCentres, b.getLocation());
                    counter++;
                    //System.out.println(b.getLocation());
                }

                PVector mean = PVector.div(sumOfMassCentres, counter);
                if (tick % 10 == 0) {
                    historyOfMovement.add(new int[]{(int) mean.x + 50, (int) mean.y});
                }

            }
        }
    }

    public ArrayList<Boid_generic> copyTheStateOfAttackBoids(ArrayList<Boid_generic> boids) {
        ArrayList<Boid_generic> boidListClone = new ArrayList<>();


        for(Boid_generic boid : boids){
            Boid_generic bi = new Boid_standard(parent,boid.getLocation().x,boid.getLocation().y,6,10);
            bi.setAi(ai);
            bi.setAcceleration(boid.getAcceleration());
            bi.setVelocity(boid.getVelocity());
            bi.setLocation(boid.getLocation());
            boidListClone.add(bi);
        }

        return boidListClone;

    }

}