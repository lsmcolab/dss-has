package pack_technical;

        import pack_1.ParameterGatherAndSetter;
        import pack_boids.Boid_generic;
        import processing.core.PApplet;
        import processing.core.PVector;

        import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

        import static java.lang.Math.abs;

public class ZoneDefence implements Cloneable{
    private static  BaseManager base;
    private static   GameManager manager;

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

    private boolean victory =false;
    private boolean defend=true;
    private ArrayList<Boid_generic> boids;
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
    long startTime=0;
    float circumfence;
    private PatrollingScheme patrolling = new PatrollingScheme(0.04f);

    private ArrayList<PVector> waypoints = patrolling.getWaypoints();

    EnviromentalSimulation sim ;
    boolean attack=false;
    FlockManager flock;
    int timer =0;
    ParameterSimulation param;
    ParameterGatherAndSetter output;
    
	public PrintWriter writer14 = new PrintWriter ("output/AttackingAndUpdatingTime.txt");

    public ZoneDefence(BaseManager b, GameManager g, PApplet p, CollisionHandler collision, FlockManager flock, ParameterGatherAndSetter output) throws IOException {
        this.flock=flock;
        this.handler=collision;
        this.parent=p;
        this.base=b;
        this.manager=g;
        boids = manager.get_team(0);
        attackBoids = manager.get_team(1);
        pattern = new PatternHandler();
        this.output =output;


            waypoints.addAll(output.returnDifficulty());


        patrolling.getWaypointsA().add(new PVector(550,500));
        patrolling.setup();


    }
    public void run() throws InterruptedException, IOException {
        if(pattern.isOnce()) {
            sim  = new EnviromentalSimulation(40, 70, 70, 2.0f, 1.2f, 0.9f, "", boids, parent,pattern.getImg().getNewpoints(),attackBoids,handler);
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

                    sim.restartTheSimulation(attackBoids, boids);

                    sim.setSimulating(true);
                    timer = 0;
                }

            }
        }
        for(Boid_generic be1 : attackBoids){

            coutner++;
            if(coutner>=DELAY/8 && coutner <= DELAY*2) {
                //______________________unable movement of the attack
                if(!attack)  be1.setToMove(false);
                be1.setLocation(new PVector(be1.getLocation().x, be1.getLocation().y));
                be1.setVelocity(new PVector(0, 0));
                be1.setAcceleration(new PVector(0, 0));

                delay2++;

            }
            //         IMITATING THE MOVEMENT OF THE DEFENDERS



            if(delay2>=200) {

                pattern.newObservation(boids,coutner);

                if (attackBoids != null && flag && pattern.analyze()==1) {
                    long delay = 0;
                    circumfence = (float) (3.14 * 2 * pattern.getRadius());

                    System.out.println(attackBoids);
                    ArrayList<Boid_generic> copy = copyTheStateOfAttackBoids(manager.get_team(1));
                    //System.out.println(copy+"bef");

                    time =  (circumfence/boids.get(0).getVelocity().mag());
                    System.out.println(boids.get(0).getVelocity().mag() + "   " + circumfence + "   " + time + "  " + (float) startTime);
                    //s = new Simulation(copy, parent, this,handler,patrolling);
                    flag = false;
                    startTime = System.nanoTime();
                }
            }


            // ATACK MODE
            if(attack) {

                be1.setToMove(true);
                PVector acceleration = be1.getAcceleration();
                PVector velocity = be1.getVelocity();
                PVector location = be1.getLocation();
                velocity.limit(1);
                PVector attackVector = sim.returnTargetVector();

                location.add(velocity.add(acceleration.add(attackVector)));

                acceleration.mult(0);
            }else if(!attack){
                be1.setLocation(new PVector(be1.getLocation().x, be1.getLocation().y));
                be1.setVelocity(new PVector(0, 0));
                be1.setAcceleration(new PVector(0, 0));
            }




        }
        for( Boid_generic be : boids){
            if(defend) {
                PVector acceleration = be.getAcceleration();
                PVector velocity = be.getVelocity();
                PVector location = be.getLocation();

                velocity.limit(1);
                location.add(velocity.add(patrolling.patrol(be.getLocation(),be)));
                acceleration.mult(0);
            } else {

                PVector location = be.getLocation();

                be.setLocation(location);
                be.setVelocity(new PVector(0,0));
                be.setAcceleration(new PVector(0,0));
            }

        }
        output.iterations++;
    }

    public PVector defend(Boid_generic b){

        PVector target = new PVector(0, 0, 0);

        // For every boid in the system, check if it's too close
        for (Boid_generic other : boids) {
            float d = PVector.dist(b.getLocation(),new PVector(550,500) );
            // If the distance is greater than 0 and less than an arbitrary amount (0 when
            // you are yourself)

            if (abs(d)>120) {
                target = PVector.sub(new PVector(550,500),b.getLocation());
                target.setMag((float) 0.04);
            } else {
                target.setMag((float) 0.00);
            }
        }

        //target.limit((float) 0.02);
        return target;
    }
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
    public PVector attack(Boid_generic b1,int boidType)  {
            PVector target = new PVector(0, 0, 0);

            for (Boid_generic b2 : boids){
                double radius = PVector.dist(new PVector(550,500),b2.getLocation());
                double circumfence = 3.14 * 2 * radius;

                //x(t) = 0.5 a*t^2 + v*t + x_0

                float d = PVector.dist(b1.getLocation(),b2.getLocation() );

                    target = PVector.sub(new PVector(550,500),b1.getLocation());
                    if(boidType==1) target.setMag((float) 0.09);
                    if(boidType==2) target.setMag((float) 0.01);
                //}

        }
        return  target;
    }

}
