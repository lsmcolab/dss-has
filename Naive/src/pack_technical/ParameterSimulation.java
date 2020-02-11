package pack_technical;

import com.sun.media.sound.AiffFileReader;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import pack_AI.AI_manager;
import pack_AI.AI_type;
import pack_boids.Boid_generic;
import pack_boids.Boid_standard;
import processing.core.PApplet;
import processing.core.PVector;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.lang.reflect.Array;
import java.util.*;

public class ParameterSimulation extends Thread{
    private boolean once=true;
    private ArrayList<Boid_generic> defenders;
    private ArrayList<int[]> pattern;
    private AI_type currentAi;
    private Random rand = new Random();
    private PApplet parent;
    private PatrollingScheme scheme ;
    private Map<Integer,ArrayList<Boid_generic>> observations = new HashMap<>();
    int frameCount=0;
    int innerFrameCount=0;

    PolynomialCurveFitter fitter ;

    int k =0;
    int k2=0;

    private final int howManyErrors=20;
    private  double learningRate = 0.1;
    private double learningRateParameters=0.006;
    private int begin=0;
    private int end =-1;

    Map<Integer,ArrayList<WeightedObservedPoint>> sepBoidError= new HashMap<>();
    Map<Integer,ArrayList<WeightedObservedPoint>> cohBoidError= new HashMap<>();
    Map<Integer,ArrayList<WeightedObservedPoint>> aliBoidError= new HashMap<>();
    Map<Integer,ArrayList<WeightedObservedPoint>> sepWBoidError= new HashMap<>();
    Map<Integer,ArrayList<WeightedObservedPoint>> cohWBoidError= new HashMap<>();
    Map<Integer,ArrayList<WeightedObservedPoint>> aliWBoidError= new HashMap<>();
    Map<Integer,ArrayList<WeightedObservedPoint>> wayPointForceBoidError= new HashMap<>();

    ArrayList<ArrayList<Boid_generic>>  stack = new ArrayList<>();

    Map<Integer,Double> sepEst= new HashMap<>();
    Map<Integer,Double> cohEst= new HashMap<>();
    Map<Integer,Double> aliEst= new HashMap<>();
    Map<Integer,Double> sepWEst= new HashMap<>();
    Map<Integer,Double> cohWEst= new HashMap<>();
    Map<Integer,Double> aliWEst= new HashMap<>();
    Map<Integer,Double> wayPointEst= new HashMap<>();


    ArrayList<Integer> resultsInt = new ArrayList<>();
    ArrayList<Float> resultsFloat = new ArrayList<>();

    ArrayList<Boid_generic> nextIterationObservation = new ArrayList<>();

    Map<Integer,PVector> endPositions= new HashMap<>();
    private boolean observing=true;

    private ArrayList<PVector> initialLocation = new ArrayList<>();


    int oldBegin=0;
    private ArrayList<PVector> endingLocation = new ArrayList<>();

    public ParameterSimulation(PApplet parent , ArrayList<Boid_generic> defenders , ArrayList<int[]> pattern, AI_type currentAi){
    this.parent=parent;
    this.currentAi=currentAi;
    this.scheme= new PatrollingScheme(currentAi.getWayPointForce());
    this.pattern=pattern;

    }



    public void assigntBoidsToTheirSimulations(ArrayList<Boid_generic> pop ,Map<Integer,ArrayList<WeightedObservedPoint>> map ){
        int couter=0;
        for(Boid_generic g : pop){
            map.put(couter,new ArrayList<WeightedObservedPoint>());
            couter++;
        }
        //System.out.println(" Population before applying " + Arrays.toString(map.get(0).toArray()));
    }
    public void run(){
            setUpCheckPoints();
           // StringBuilder sb = new StringBuilder();
        System.out.println("I have start with these parameters " + currentAi.getSep_neighbourhood_size() + " " + currentAi.getAli_neighbourhood_size() + " " + currentAi.getCoh_neighbourhood_size() + " " + currentAi.getSep_weight()  + " " + currentAi.getAli_weight() + " " + currentAi.getCoh_weight() + " " + currentAi.getWayPointForce());
           learnTheErrors(sepBoidError,1,observations.get(1));
       // System.out.println("1");
            calculateNewParameter(1);
           learnTheErrors(aliBoidError,2,observations.get(1));
            calculateNewParameter(2);
           learnTheErrors(cohBoidError,3,observations.get(1));
            calculateNewParameter(3);
           learnTheErrors(sepWBoidError,4,observations.get(1));
            calculateNewParameter(4);
           learnTheErrors(aliWBoidError,5,observations.get(1));
            calculateNewParameter(5);
           learnTheErrors(cohWBoidError,6,observations.get(1));
            calculateNewParameter(6);
           learnTheErrors(wayPointForceBoidError,7,observations.get(1));
            calculateNewParameter(7);
            clearMapping();

        System.out.println("I have finished with  " + currentAi.getSep_neighbourhood_size() + " " + currentAi.getAli_neighbourhood_size() + " " + currentAi.getCoh_neighbourhood_size() + " " + currentAi.getSep_weight()  + " " + currentAi.getAli_weight() + " " + currentAi.getCoh_weight()+ " " + currentAi.getWayPointForce());

    }


    public void setUpCheckPoints(){

            for (int[] cord : pattern) {
                scheme.getWaypoints().add(new PVector(cord[0], cord[1]));
//
            }
            //FOLLOW THE SIMILLAR WAYPOINT AS DEFENDERS _____________________________________________________________
            PVector theClosestOne = new PVector(2000, 2000);
            float shortestDistance = 3000;
            int counter = 0;
            int positionInTheList = 0;
            for (PVector checkpoint : scheme.getWaypoints()) {
                float distance = PVector.dist(observations.get(1).get(0).getLocation(), checkpoint);
                counter++;
                // System.out.println(distance);
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    positionInTheList = counter;

                }
            }
            scheme.setup();

            for (int i = 0; i < positionInTheList + 1; i++) {
                if (!scheme.getIterator().hasNext()) {   // the ! is important
                    scheme.setIterator(scheme.getWaypoints().iterator());
                }
                scheme.setCurrWaypoint(scheme.getIterator().next());
            }

    }
    public void clearMapping(){
        sepBoidError.clear();
        cohBoidError.clear();
        aliBoidError.clear();
        sepWBoidError.clear();
        cohWBoidError.clear();
        aliWBoidError.clear();
        wayPointForceBoidError.clear();

        sepEst.clear();
        cohEst.clear();
        aliEst.clear();
        sepWEst.clear();
        cohWEst.clear();
        aliWEst.clear();
        wayPointEst.clear();
        observations.clear();

        endPositions.clear();
        initialLocation.clear();
        endingLocation.clear();
        scheme.setWaypointforce(currentAi.getWayPointForce());
        frameCount=0;
    }



    public void calculateDistance(){
        for (Map.Entry<Integer, ArrayList<Boid_generic>> entry : observations.entrySet()) {
            if (entry.getKey() == 2) {
                int counter =0;
                for (Boid_generic def : entry.getValue()) {
                    endPositions.put(counter, def.getLocation());
                    counter++;

                }
            }
            if (entry.getKey() == 1) {
           //     System.out.println("defenders " + Arrays.toString(entry.getValue().toArray()));
                generatePopulationAndMapsForPoints(entry.getValue());
            }
        }
    }

    public void generatePopulationAndMapsForPoints(ArrayList<Boid_generic> attacker){
        ArrayList<Boid_generic> sep = copyTheStateOfAttackBoids(attacker,0);
      //  System.out.println(" Population before applying " + Arrays.toString(attacker.toArray()));
        assigntBoidsToTheirSimulations(sep,sepBoidError);

        ArrayList<Boid_generic> ali = copyTheStateOfAttackBoids(attacker,0);
        assigntBoidsToTheirSimulations(ali,aliBoidError);

        ArrayList<Boid_generic> coh = copyTheStateOfAttackBoids(attacker,0);
        assigntBoidsToTheirSimulations(coh,cohBoidError);

        ArrayList<Boid_generic> sepW = copyTheStateOfAttackBoids(attacker,0);
        assigntBoidsToTheirSimulations(sepW,sepWBoidError);

        ArrayList<Boid_generic> aliW = copyTheStateOfAttackBoids(attacker,0);
        assigntBoidsToTheirSimulations(aliW,aliWBoidError);

        ArrayList<Boid_generic> cohW = copyTheStateOfAttackBoids(attacker,0);
        assigntBoidsToTheirSimulations(cohW,cohWBoidError);

        ArrayList<Boid_generic> waypoints = copyTheStateOfAttackBoids(attacker,0);
        assigntBoidsToTheirSimulations(waypoints,wayPointForceBoidError);
    }
    // Mode 1 sep 2 ali 3 coh 4 sepWeight 5 aliW 6 cohW

    public void calculateNewParameter(int mode){
        int counter=0;

        float averageSep=0;

        switch (mode){

            case 1:
                for(Map.Entry<Integer,ArrayList<WeightedObservedPoint>> boid: sepBoidError.entrySet()){
                    fitter=  PolynomialCurveFitter.create(9);

                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;

                    for (int e = 1; e < 9; e++) {
                      //  System.out.println(" coef " + Arrays.toString(coefincients));
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getSep_neighbourhood_size());
                     //   System.out.println("terms_total " + terms_toal);
//                        if(terms_toal>= AI_manager.getNeighbourhoodUpperBound() || terms_toal <= AI_manager.getNeighbourhoodLowerBound()){
//                            System.out.println("kappa");
//                            terms_toal=currentAi.getSep_neighbourhood_size();
//                            learningRate*=0.5;
//                            learningRateParameters*=0.1;
//                        } else {
//                            learningRateParameters*=1.25;
//                            learningRate *=10;
//                        }

                    }
                    double estimation = currentAi.getSep_neighbourhood_size()-terms_toal*learningRate*0.3;
                    if(estimation>= AI_manager.getNeighbourhoodUpperBound() || estimation <= AI_manager.getNeighbourhoodLowerBound()){
                           // System.out.println("kappa");
                        estimation=currentAi.getSep_neighbourhood_size();

                       }

                    sepEst.put(boid.getKey(),estimation);

                }
                for(Map.Entry<Integer,Double> sep : sepEst.entrySet()){
                    averageSep+=sep.getValue();
                }

                resultsInt.add((int)averageSep/sepEst.size());
                currentAi.setSep_neighbourhood_size((float)averageSep/sepEst.size());
                break;
            case 2:
                for(Map.Entry<Integer,ArrayList<WeightedObservedPoint>> boid: aliBoidError.entrySet()){
                    fitter=  PolynomialCurveFitter.create(9);

                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 9; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getAli_neighbourhood_size());
                    }
                    double estimation = currentAi.getAli_neighbourhood_size()-terms_toal*learningRate;
                    if(estimation>= AI_manager.getNeighbourhoodUpperBound() || estimation <= AI_manager.getNeighbourhoodLowerBound()){
                        estimation=currentAi.getAli_neighbourhood_size();
                    }
                    aliEst.put(boid.getKey(),estimation);

                }
                 averageSep=0;
                for(Map.Entry<Integer,Double> sep : aliEst.entrySet()){
                    averageSep+=sep.getValue();
                }
                resultsInt.add((int)averageSep/aliEst.size());
                currentAi.setAli_neighbourhood_size((float)averageSep/aliEst.size());
                break;
            case 3:
                for(Map.Entry<Integer,ArrayList<WeightedObservedPoint>> boid: cohBoidError.entrySet()){
                    fitter=  PolynomialCurveFitter.create(9);

                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 9; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getCoh_neighbourhood_size());
                    }
                    double estimation = currentAi.getCoh_neighbourhood_size()-terms_toal*learningRate*0.25;
                    if(estimation>= AI_manager.getNeighbourhoodUpperBound() || estimation <= AI_manager.getNeighbourhoodLowerBound()){
                        estimation=currentAi.getCoh_neighbourhood_size();
                    }
                    cohEst.put(boid.getKey(),estimation);

                }
                averageSep=0;
                for(Map.Entry<Integer,Double> sep : cohEst.entrySet()){
                    averageSep+=sep.getValue();
                }
                resultsInt.add((int)averageSep/cohEst.size());
                currentAi.setCoh_neighbourhood_size((float)averageSep/cohEst.size());
                break;

            case 4:
                for(Map.Entry<Integer,ArrayList<WeightedObservedPoint>> boid: sepWBoidError.entrySet()){

                    fitter=  PolynomialCurveFitter.create(9);
                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 9; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getSep_weight());
                    }
                    double estimation = currentAi.getSep_weight()-terms_toal*learningRateParameters*0.25;
                    if(estimation>= 5 || estimation <= 0){
                        estimation=currentAi.getSep_weight();
                    }
                    sepWEst.put(boid.getKey(),estimation);

                }
                averageSep=0;
                for(Map.Entry<Integer,Double> sep : sepWEst.entrySet()){
                    averageSep+=sep.getValue();
                }
                resultsFloat.add((float) averageSep/sepWEst.size());
                currentAi.setSep_weight((float) averageSep/sepWEst.size());
                break;

            case 5:
                for(Map.Entry<Integer,ArrayList<WeightedObservedPoint>> boid: aliWBoidError.entrySet()){
                    fitter=  PolynomialCurveFitter.create(9);
                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 9; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getAli_weight());
                    }
                    double estimation = currentAi.getAli_weight()-terms_toal*learningRateParameters*0.001;
                    if(estimation>= 5 || estimation <= 0){
                        estimation=currentAi.getAli_weight();
                    }
                    aliWEst.put(boid.getKey(),estimation);

                }
                averageSep=0;
                for(Map.Entry<Integer,Double> sep : aliWEst.entrySet()){
                    averageSep+=sep.getValue();
                }
                resultsFloat.add((float) averageSep/aliWEst.size());
                currentAi.setAli_weight((float) averageSep/aliWEst.size());
                break;

            case 6:
                for(Map.Entry<Integer,ArrayList<WeightedObservedPoint>> boid: cohWBoidError.entrySet()){

                    fitter=  PolynomialCurveFitter.create(9);
                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 9; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getCoh_weight());
                    }
                    double estimation = currentAi.getCoh_weight()-terms_toal*learningRateParameters*0.01;
                    if(estimation>= 5 || estimation <= 0){
                        estimation=currentAi.getCoh_weight();
                    }
                    cohWEst.put(boid.getKey(),estimation);

                }
                averageSep=0;
                for(Map.Entry<Integer,Double> sep : cohWEst.entrySet()){
                    averageSep+=sep.getValue();
                }
                resultsFloat.add((float) averageSep/cohWEst.size());
                currentAi.setCoh_weight((float) averageSep/cohWEst.size());
                break;

            case 7:
                for(Map.Entry<Integer,ArrayList<WeightedObservedPoint>> boid: wayPointForceBoidError.entrySet()){

                    fitter=  PolynomialCurveFitter.create(9);
                    double[] coefincients = fitter.fit(boid.getValue());
                   // System.out.println("my coefficents " + Arrays.toString(coefincients));
                    double terms_toal = 0;
                    for (int e = 1; e < 9; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getWayPointForce());
                    }
                    double estimation = currentAi.getWayPointForce()-terms_toal*10;

                    if(estimation> 1 || estimation < 0){
                        estimation=currentAi.getWayPointForce();
                    }
                    wayPointEst.put(boid.getKey(),estimation);

                }

                averageSep=0;
                for(Map.Entry<Integer,Double> sep : wayPointEst.entrySet()){
                    averageSep+=sep.getValue();
                }
                System.out.println("average sep " + averageSep + " size  " +  sepEst.size() + " together " + (int)averageSep/sepEst.size() );
                resultsFloat.add((float) averageSep/wayPointEst.size());
                currentAi.setWayPointForce((float) averageSep/wayPointEst.size());
                break;

        }
    }
    private double create_new_term(int exponent, double coeffs, double param_x) {
        // term format... e.g. 5x^4 becomes 5(4x^5);
        // is is the point of the derivative
        // so becomes coeff(exponent*x^exponent)
        double term = coeffs * (exponent * Math.pow(param_x, exponent-1)); // doe abs work?

            return term;
    }

    public static float randFloat(float min, float max) {

        Random rand = new Random();

        float result = rand.nextFloat() * (max - min) + min;

        return result;

    }
    // Mode 1 sep 2 ali 3 coh 4 sepWeight 5 aliW 6 cohW

    public void learnTheErrors(Map<Integer,ArrayList<WeightedObservedPoint>> map,int mode,ArrayList<Boid_generic> defenders ){

        calculateDistance();
      //  System.out.println("3");

        for(int j=0;j<howManyErrors;j++) {
        //    System.out.println("j " + j);
            float xValue =0;
            if(mode>0 && mode <=3){
               xValue = randFloat(20,100) ;
            }else if (mode==7) {
                xValue=rand.nextFloat()*5;
               // System.out.println("my random " + xValue);
            }else {
                xValue = randFloat(0,5);
            }
            AI_type sepAi = currentAi;
            int save = frameCount;
            ArrayList<Boid_generic> simulationBoids = copyTheStateOfAttackBoids(defenders,0);
            switch (mode) {
                case 1:
                    sepAi = new AI_type( xValue, currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(), currentAi.getSep_weight(), currentAi.getAli_weight(), currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                  //  System.out.println("cSepPop " + Arrays.toString(cSepPop.toArray()));
                   // simulationBoids = cSepPop;
                    break;
                case 2:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(),  xValue, currentAi.getCoh_neighbourhood_size(),currentAi.getSep_weight(), currentAi.getAli_weight(), currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                   // simulationBoids = cAliPop;
                    break;
                case 3:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(),  xValue,currentAi.getSep_weight(), currentAi.getAli_weight(), currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                   // simulationBoids=cCohPop;
                    break;
                case 4:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(), (float) xValue, currentAi.getAli_weight(), currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                  //  simulationBoids=cSepWPop;
                    break;
                case 5:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(), currentAi.getSep_weight(), (float) xValue, currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                   // simulationBoids=cAliWPop;

                    break;
                case 6:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(),currentAi.getSep_weight(), currentAi.getAli_weight(), (float) xValue,currentAi.getWayPointForce(), ":(");
                  //  simulationBoids=cCohWPop;
                    break;
                case 7:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(),currentAi.getSep_weight(), currentAi.getAli_weight(), currentAi.getCoh_weight(),xValue, ":(");
                    //  simulationBoids=cCohWPop;
                    //frameCount=40;
                    break;
            }
            int numberOfBoidsErrors=0;
          //  System.out.println("simulation " + Arrays.toString(simulationBoids.toArray()));
            //ArrayList<Boid_generic> simulationBoids = copyTheStateOfAttackBoids(observations.get(1),0);
             //test
            for (int i = 0; i < frameCount; i++) {
                                //   System.out.println(" before i " + i);

                                int counter=0;
                                for (Boid_generic def : simulationBoids) {
                                    // System.out.println(" after counter " + counter);


                                    def.setAi(sepAi);
                                    PVector acceleration =def.getAcceleration();
                                    PVector velocity = def.getVelocity();
                                    PVector location = def.getLocation();
                                    def.run(simulationBoids, true, true); //Alex Part where he applies all forces


                                    velocity.limit(1);
                                    //My force
                                    location.add(velocity.add(acceleration.add(scheme.patrol(def.getLocation(), def)/*patrolling.patrol(be.getLocation(),be)*/)));
                            if(i==frameCount-1) {
                                //if(mode==7)
                               // System.out.println("error " + (double)PVector.dist(endPositions.get(counter), location));

                                        WeightedObservedPoint point = new WeightedObservedPoint(1, xValue, (double) PVector.dist(endPositions.get(counter), location));


                                if(mode==7 && counter==10){
                                       // System.out.println("Hello " + xValue +  " "+  (double)PVector.dist(endPositions.get(counter), location));
                                }
                                map.get(counter).add(point);
                            }

                            acceleration.mult(0);
                            counter++;

                        }

            }

        }

    }

    public void calculateErrors(){

    }

    public int observe(ArrayList<Boid_generic> defenders){

        ArrayList<Boid_generic>  initialState = new ArrayList<>();



//                observations.put(1, copyTheStateOfAttackBoids(defenders,1)); //initial state
//
//                observations.put(2, copyTheStateOfAttackBoids(defenders,2));//end state
            if(stack.size()<20){
                stack.add(copyTheStateOfAttackBoids(defenders,0));



                end = end + 1;
            }

            else
            {
                //initialState = copyTheStateOfAttackBoids(stack.get(begin),0);

                oldBegin = begin;

                end = (end + 1) %20;

                stack.toArray()[end] = copyTheStateOfAttackBoids(defenders,0);

                begin = (begin + 1) % 20;
            }



        if(stack.size()== 20 && once) {
            frameCount=20;
         ArrayList<Boid_generic>   initialStateForCalculation = copyTheStateOfAttackBoids(stack.get(oldBegin),0);
         ArrayList<Boid_generic>  endStateForCalculation = copyTheStateOfAttackBoids(stack.get(end),0);
            System.out.println("   " + initialStateForCalculation.size() + endStateForCalculation.size());
            observations.put(1, initialStateForCalculation); //initial state
//
            observations.put(2, endStateForCalculation);//end state
            observing = false;
            new Thread(this).start();
            System.out.println("thread started");

            once=false;
        }


        //10
        //0.1
        //0.01
        //0.02

        if ( observations.size()==0  &&!once) {
               // observing=true;
                k++;
            //System.out.println(k);
                once=true;

                if(learningRate>=0.5){
                    learningRate+=0.5;
                    learningRateParameters+=0.5;
                } else {
                    learningRate+=0.02;
                }
                return 1;

        }

            return 0;

    }
    public AI_type updateAi(){
        return currentAi;
    }

    public ArrayList<Boid_generic> copyTheStateOfAttackBoids(ArrayList<Boid_generic> boids,int mode) {
        ArrayList<Boid_generic> boidListClone = new ArrayList<>();
        //System.out.println(boids);

        for(Boid_generic boid : boids){
            //nadaj im tutaj acceleration velocity etc..
            Boid_generic bi = new Boid_standard(parent,boid.getLocation().x,boid.getLocation().y,6,10);
            bi.setAi(currentAi);


            bi.setAcceleration(boid.getAcceleration());
            bi.setVelocity(boid.getVelocity());
            bi.setLocation(boid.getLocation());
            if(mode==1){
                initialLocation.add(bi.getLocation());
            } else if (mode==2){
                endingLocation.add(bi.getLocation());
            }
            boidListClone.add(bi);
        }

        return boidListClone;

    }

}
