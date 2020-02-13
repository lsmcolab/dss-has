package pack_technical;

import pack_1.ParameterGatherAndSetter;
import pack_AI.AI_manager;
import pack_boids.Boid_generic;
        import pack_boids.Boid_standard;
        import processing.core.PApplet;
        import processing.core.PShape;
        import processing.core.PVector;

        import java.io.BufferedReader;
        import java.io.File;
        import java.io.FileReader;
        import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
        import java.util.Iterator;
        import java.util.concurrent.TransferQueue;

import org.rosuda.JRI.Rengine;

import static java.lang.Math.abs;

public class ZoneDefence implements Cloneable{
    private static  BaseManager base;
    private static   GameManager manager;
    private boolean victory =false;
    private boolean defend=true;
    private ArrayList<Boid_generic> boids;
//    private ArrayList<Boid_generic> defenders;
    private ArrayList<Boid_generic> attackBoids;
    private PApplet parent;
    private boolean isZoneAttacked=false;
    static  ArrayList<Boid_generic> clones;
    static int coutner=0;
    boolean flag = true;
    int DELAY=200;
    int delay2=0;
    Simulation s;
    CollisionHandler handler;
    PatternHandler pattern;

    //timing simulation/real world
    float time=0;
    double startTime;
    double startTimeWithoutwait;
    double endTime=0;
    float circumfence;
    private PatrollingScheme patrolling = new PatrollingScheme(0.04f);
    private PatrollingScheme attacking = new PatrollingScheme(0.04f);
    private ArrayList<PVector> waypoints = patrolling.getWaypoints();
    ParameterHandler pHandler = new ParameterHandler();
    EnviromentalSimulation sim ;
    boolean attack=false;
    FlockManager flock;
    int timer =0;
    ParameterSimulation param;
    ParameterGatherAndSetter output;
    public ArrayList<ArrayList<PVector>> boidsLocations = new ArrayList<>(); //Nested ArrayList and the most inner data type is PVector
    Rengine re ;
    
    public PVector movingVector = new PVector();
    public PrintWriter writer1 = new PrintWriter ("Data/Boidslocation.txt");
	public PrintWriter writer2 = new PrintWriter ("Data/Attacker.txt");
	public PrintWriter writer13 = new PrintWriter ("Data/MovingVector.txt");
	public PrintWriter writer14 = new PrintWriter ("Data/AttackingAndUpdatingTime.txt");
    
    /**
     * Constructor for ZoneDefence
     */
    public ZoneDefence(BaseManager b, GameManager g, PApplet p, CollisionHandler collision, FlockManager flock, ParameterGatherAndSetter output, Rengine re) throws IOException {
        this.flock=flock;
        this.handler=collision;
        this.parent=p;
        this.base=b;
        this.manager=g;
        this.re = re;
        boids = manager.get_team(0);
        attackBoids = manager.get_team(1);
        pattern = new PatternHandler();
        this.output =output;
        waypoints.addAll(output.returnDifficulty()); //add "hard", "medium", "easy"
        patrolling.getWaypointsA().add(new PVector(550,500));
        patrolling.setup();
        startTime=System.nanoTime();

    }
    
    /*
     * run() method
     */
    public void run() throws InterruptedException, IOException {
    	
        if(pattern.isOnce()) {
            sim  = new EnviromentalSimulation(40, 70, 70, 2.0f, 1.2f, 0.9f, "", attackBoids, boids, boidsLocations,parent,re);
            param = new ParameterSimulation(parent,boids,pattern.getImg().getNewpoints(),sim.getSimulator());
            pattern.setOnce(false);
        }

        if(sim!=null) {
            int  delay1 = 0;
            if(param.observe(boids)==1){
            	sim.setAiToInnerSimulation(param.updateAi());
            	output.sendParameters(param.updateAi());
            	attack = true;
            	//time is measured by System.nanoTime()/10-e6; 1/1000 of one second
            	writer14.write("I started to attack "+ ","+ Math.round((System.nanoTime()-startTime)/1000000)+","+coutner+"\n");
            	writer14.flush();
            }

            if (!sim.isSimulating()) {
            	if (timer == 0) {
            		writer14.write("Updating attack vector " + ","+ Math.round((System.nanoTime()-startTime)/1000000)+","+coutner+"\n");
            		writer14.flush();
            	}	
                timer++;
                if (timer >= 3) {
                    sim.restartTheSimulation(attackBoids, boidsLocations);//pass information to EnviornmentalSimulation class; 
                    sim.setSimulating(true);
                    timer = 0;
                }
            }
        }
        
//        for (PVector vec: waypoints){
//            parent.fill(255,120,10);
//            parent.rect(vec.x,vec.y,10f,10f);
//
//        }
        
        PVector attk = new PVector();
        for(Boid_generic be1 : attackBoids){
            coutner++;
          //take away (discard) the first 50 steps as ellipse forming stage
            if(coutner>=5*DELAY/20 && coutner <= DELAY*2) {
                //______________________unable movement of the attack
                if(!attack)  be1.setToMove(false);
                be1.setLocation(new PVector(be1.getLocation().x, be1.getLocation().y));
                be1.setVelocity(new PVector(0, 0));
                be1.setAcceleration(new PVector(0, 0));
                delay2++;//delay2 ==0 to start till 200
            }

            if(delay2>=200) {//after waiting for 200 ticks (coutner ++)// remember to change it back to 200
                pattern.newObservation(boids,coutner);//The first obs is when coutner =219 (200+200/10-1 )   and delay2 >=200
                if (attackBoids != null && flag && pattern.analyze()==1) {
                    long delay = 0;
                    circumfence = (float) (3.14 * 2 * pattern.getRadius());
//                    System.out.println(attackBoids);
                    ArrayList<Boid_generic> copy = copyTheStateOfAttackBoids(manager.get_team(1));
                    time =  (circumfence/boids.get(0).getVelocity().mag());
                    System.out.println(boids.get(0).getVelocity().mag() + "   " + circumfence + "   " + time + "  " + (float) startTime);
                    flag = false;
                    startTime = System.nanoTime();
                }
            }
            
            if(s!=null){
                long end = System.nanoTime();
            }

            // ATTACK MODE
          
            if(attack) {
                be1.setToMove(true);
                PVector acceleration = be1.getAcceleration();
                PVector velocity = be1.getVelocity();
                PVector location = be1.getLocation();
                velocity.limit(1);
                PVector attackVector = sim.returnTargetVector(); //this is the Monte Carlo choice for the shortest vector to target
//                attackVector.setMag(0.3f);//why?
                location.add(velocity.add(acceleration.add(attackVector)));
                acceleration.mult(0);
                
                movingVector = attackVector;
                
            }else {
                be1.setLocation(new PVector(be1.getLocation().x, be1.getLocation().y));
                be1.setVelocity(new PVector(0, 0));
                be1.setAcceleration(new PVector(0, 0));
            }
            attk = be1.getLocation();

        }
   
        //take away (discard) the first 50 steps as ellipse forming stage
        if(coutner>=5*DELAY/20 && coutner %10 ==0  ) {
//        	System.out.println(attk.x+','+attk.y);
        	writer2.println(attk);
        	writer2.flush();
            writer13.println(movingVector);
            writer13.flush();
        }
        
        //Set up an ArrayList to collect the boidsLocation
        ArrayList<PVector> loca = new ArrayList<>();
        for(Boid_generic be : boids){
            if(defend) {
                PVector acceleration = be.getAcceleration();
                PVector velocity = be.getVelocity();
                PVector location = be.getLocation();
                velocity.limit(1);
                location.add(velocity.add(/*acceleration.add(defend(be)*/patrolling.patrol(be.getLocation(),be)));
                acceleration.mult(0);
            } else {
                PVector acceleration = be.getAcceleration();
                PVector velocity = be.getVelocity();
                PVector location = be.getLocation();

                be.setLocation(location);
                be.setVelocity(new PVector(0,0));
                be.setAcceleration(new PVector(0,0));
            }
         
			//Add boidsLocation ArrayList(ArrayList)
            loca.add(new PVector(be.getLocation().x, be.getLocation().y));//collecting all boids in the swarm at one time tick
            
        }
        
        //Nested ArrayList
        //DELAY =200, DELAY/10 = 20, discard the first 50 steps
        if(coutner>=5*DELAY/20 && coutner %10 ==0  ) {//exclude the forming stage boidsLocation data (300 STEPS)and collect loca every 10 steps. 
        	boidsLocations.add(loca); //add the one swarm (group of boids) into an ArrayList
        	writer1.println(loca);//print out each loca which leads to the one set 
        	writer1.flush();
        }
 
		output.iterations++;
    }


    
    /*
     * Method: copyTheStateOfAttackBoids()
     * Input: boid
     * Function:
     * Return: an ArrayList boidListClone
     */
    public ArrayList<Boid_generic> copyTheStateOfAttackBoids(ArrayList<Boid_generic> boids) {
        ArrayList<Boid_generic> boidListClone = new ArrayList<>();

        for(Boid_generic boid : boids){
            Boid_generic bi = new Boid_generic(parent,boid.getLocation().x,boid.getLocation().y,6,10);
            bi.setAcceleration(boid.getAcceleration());
            bi.setVelocity(boid.getVelocity());
            bi.setLocation(boid.getLocation());
            boidListClone.add(bi);
        }
        return boidListClone;

    }
    
    /*
     * Method: attack()
     * Input: boid, boidType
     * Function:
     * Return: PVector target
     */
    public PVector attack(Boid_generic b1,int boidType)  {
            PVector target = new PVector(0, 0, 0);

            for (Boid_generic b2 : boids){
                double radius = PVector.dist(new PVector(550,500),b2.getLocation());
                double circumfence = 3.14 * 2 * radius;

                //x(t) = 0.5 a*t^2 + v*t + x_0

                float d = PVector.dist(b1.getLocation(),b2.getLocation() );
               /* if(d<200){
                    target = PVector.sub(new PVector(-b2.getLocation().x,-b2.getLocation().y),b1.getLocation());
                    target.setMag((float) 0.09);
                } else  { */
                    target = PVector.sub(new PVector(550,500),b1.getLocation());
                    if(boidType==1) target.setMag((float) 0.09);
                    if(boidType==2) target.setMag((float) 0.01);
                //}
        }
        return  target;
    }
    
   
    /*
     * Get and set methods
     * 
     */
    public ArrayList<Boid_generic> getBoids() {
        return boids;
    }

    public boolean isDefend() {
        return defend;
    }

    public void setDefend(boolean defend) {
        this.defend = defend;
    }

    public boolean isVictory() {
        return victory;
    }
    
	public ArrayList<ArrayList<PVector>> getBoidsLocations() {
		return boidsLocations;
	}
	
	public void setBoidsLocations(ArrayList<ArrayList<PVector>> boidsLocations) {
		this.boidsLocations = boidsLocations;
	}
    
    public void stop() {
    	sim.setSimulating(false);
    }

}
