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
        import java.util.ArrayList;
        import java.util.Iterator;
        import java.util.concurrent.TransferQueue;

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
    private PatrollingScheme attacking = new PatrollingScheme(0.04f);
    private ArrayList<PVector> waypoints = patrolling.getWaypoints();
    ParameterHandler pHandler = new ParameterHandler();
    EnviromentalSimulation sim ;
    boolean attack=true;
    FlockManager flock;
    int timer =0;
    ParameterSimulation param;
    ParameterGatherAndSetter output;

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
        // ACUTAL WAYPOINTS ____________________________________________
//       waypoints.add(new PVector(50,800));
//       waypoints.add(new PVector(450,500));
//     waypoints.add(new PVector(50,100));

            waypoints.addAll(output.returnDifficulty());



        // INNER WAYPOINTS FROM PREVIOUS ________________________________
//       File file = new File("out4.txt");
////
//       BufferedReader br = new BufferedReader(new FileReader(file));
////
//        String st;
//       String[] cords= new String[1];
//       while ((st = br.readLine()) != null){
//           cords = st.split("]");
//        }
//
//      for(String str : cords){
//           String cord = str.replace("[","");
//            String cordss[] = cord.split(",");
//            waypoints.add(new PVector(Integer.parseInt(cordss[0]),Integer.parseInt(cordss[1])));
//
//
//        }
//        waypoints.add(new PVector(1500,500));
//        for(PVector kk : waypoints){
//            System.out.println(kk);
//        }

        patrolling.getWaypointsA().add(new PVector(550,500));
        patrolling.setup();


    }
    public void run() throws InterruptedException, IOException {
        output.captureIteration();
//        if(pattern.isOnce()) {
//            sim  = new EnviromentalSimulation(40, 70, 70, 2.0f, 1.2f, 0.9f, "", boids, parent,pattern.getImg().getNewpoints(),attackBoids,handler);
//            param = new ParameterSimulation(parent,boids,pattern.getImg().getNewpoints(),sim.getSimulator());
//            pattern.setOnce(false);
//        }
//
//        if(sim!=null) {
//            int  delay1 = 0;
//         if(param.observe(boids)==1){
//             sim.setAiToInnerSimulation(param.updateAi());
//             output.sendParameters(param.updateAi());
//             attack = true;
//         }
//
//
//            if (!sim.isSimulating()) {
//                timer++;
//
//                if (timer >= 2) {
//                   // System.out.println("I went in");
//                    sim.restartTheSimulation(attackBoids, boids);
//                    //attack = false;
//                    sim.setSimulating(true);
//                    timer = 0;
//                }
//
//            }
//        }
        for(Boid_generic be1 : attackBoids){

            coutner++;
            if(coutner>=DELAY/8 && coutner <= DELAY*2) {
                //______________________unable movement of the attack
//                if(attackBoids.get(1)==be1) {
//                    if (!attack) be1.setToMove(false);
//                    be1.setLocation(new PVector(be1.getLocation().x, be1.getLocation().y));
//                    be1.setVelocity(new PVector(0, 0));
//                    be1.setAcceleration(new PVector(0, 0));
//                }
                delay2++;

            }
            //         IMITATING THE MOVEMENT OF THE DEFENDERS
            /*PVector imitate = pHandler.getCurrentResultant();

            be1.getVelocity().limit(1);
            PVector accelet = new PVector(0,0);

                System.out.println("test1 "+ imitate);
                accelet = be1.getAcceleration().add(imitate);

                    if (abs(PVector.dist(boids.get(0).getLocation(), new PVector(150, 500))) < 120) {
                        System.out.println("here");
                        accelet.add(PVector.sub(be1.getLocation(), new PVector(150, 500)));
                        accelet.setMag(1);

                }

            be1.getLocation().add(be1.getVelocity().add(accelet));
            be1.getAcceleration().mult(0);*/


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
                    s = new Simulation(copy, parent, this,handler,patrolling);
                    flag = false;
                    startTime = System.nanoTime();
                }
            }

            if(s!=null){

                s.simulate();

                long end = System.nanoTime();
                //System.out.println((end-startTime)/1000000000 + "  " + time);
                //if((float)(end-startTime)/1000000000>=time/200) attack=true;
            }

            // ATACK MODE
            if(attack) {
//                if(PVector.dist(attackBoids.get(1).getLocation(),attackBoids.get(0).getLocation())>3){
//                    PVector diff = PVector.sub(attackBoids.get(0).getLocation(),attackBoids.get(1).getLocation());
//                    diff.setMag(0.01f);
//
//                    attackBoids.get(1).getLocation().add(attackBoids.get(1).getVelocity().add(attackBoids.get(1).getAcceleration().add(diff)));
//                    attackBoids.get(1).getAcceleration().mult(0);
//                }
                if(attackBoids.get(0)==be1) {
                    be1.setToMove(true);
                    PVector acceleration = be1.getAcceleration();
                    PVector velocity = be1.getVelocity();
                    PVector location = be1.getLocation();
                    velocity.limit(1);
                    PVector attackVector = PVector.sub(new PVector(550, 500), location);//sim.reutrnTargetVecotr();
                    attackVector.setMag(0.1f);
                    for (Boid_generic bs : boids) {
                        if (PVector.dist(location, bs.getLocation()) <= AI_manager.getAi_basic().getSep_neighbourhood_size()) {
                            PVector diff = PVector.sub(location, bs.location);
                            diff.normalize();
                            diff.div(AI_manager.getAi_basic().getSep_neighbourhood_size()); // Weight by distance
                            attackVector = diff;
                            attackVector.setMag(0.5f);
                        }
                    }

                    location.add(velocity.add(acceleration.add(attackVector)));

                    acceleration.mult(0);
                }
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
                // System.out.println("I am here" + be.getId());
                //velocity.limit(3.0f);
              //  be.run(boids,true,true);
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

        }
    }

    public PVector defend(Boid_generic b){
        PVector steer = new PVector(0, 0, 0);
        PVector target = new PVector(0, 0, 0);
        int count = 0;
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
      //  System.out.println(boids);
        for(Boid_generic boid : boids){
            //nadaj im tutaj acceleration velocity etc..
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

}
