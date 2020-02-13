package pack_technical;

//import com.sun.media.sound.AiffFileReader;
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

/**
 * ParameterSimulation class
 * This class is to
 * 
 *
 */
public class ParameterSimulation extends Thread{
	/*
	 * Define variables
	 */
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
    Integer nextWaypoint;
    PolynomialCurveFitter fitter ;

    int k =0;
    int k2=0;

    private final int howManyErrors=20;
    private  double learningRate = 1;
    private double fastLearningRate = 1;
    private double learningRateParameters=1;
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

    /**
     * Constructor with parameters
     * @param parent
     * @param defenders
     * @param pattern
     * @param currentAi
     */
    public ParameterSimulation(PApplet parent , ArrayList<Boid_generic> defenders , ArrayList<int[]> pattern, AI_type currentAi){
    	this.parent=parent;
    	this.currentAi=currentAi;
    	this.scheme= new PatrollingScheme(currentAi.getWayPointForce());
    	this.pattern=pattern;
    }

    /*
     * Method: assignBoidsToTheirSimulations()
     * Inputs: ArrayList of boids pop, Map object map
     * Function:
     * Return: void (action) 
     */
    public void assigntBoidsToTheirSimulations(ArrayList<Boid_generic> pop ,Map<Integer,ArrayList<WeightedObservedPoint>> map ){
        int couter=0;
        for(Boid_generic g : pop){
            map.put(couter,new ArrayList<WeightedObservedPoint>());
            couter++;
        }
    }
    
    /*
     * Method: run()
     * Input: none
     * Function:
     * Return: void (action)
     */
    public void run(){
         setUpCheckPoints();
         learnTheErrors(sepBoidError,1,observations.get(1));
         calculateNewParameter(1);
         learningRate*=0.9 ;
         fastLearningRate*=0.9;
         clearMapping();
//         System.out.println("I have finished with  " 
//        		 				+ currentAi.getSep_neighbourhood_size() + " " 
//        		 				+ currentAi.getAli_neighbourhood_size() + " " 
//        		 				+ currentAi.getCoh_neighbourhood_size() + " " 
//        		 				+ currentAi.getSep_weight()  + " " 
//        		 				+ currentAi.getAli_weight() + " " 
//        		 				+ currentAi.getCoh_weight()+ " " 
//        		 				+ currentAi.getWayPointForce());
    }

    /*
     * Method: setUpCheckPoints()
     * Input: none
     * Function:
     * Return: void (action)
     */
    public void setUpCheckPoints(){
        for(int[] cord : pattern){
            scheme.getWaypoints().add(new PVector(cord[0],cord[1]));
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
            float distance = PVector.dist(observations.get(1).get(0).getLocation(), checkpoint);

            if (distance < shortestDistance) {
                shortestDistance = distance;
                positionInTheList = counter;
                shortestVectorAngle = PVector.angleBetween(observations.get(1).get(0).getLocation(), checkpoint);
                nextToShortestVectorAngle = PVector.angleBetween(observations.get(1).get(0).getLocation(), nextCheckPoint);
            }
            counter++;
        }

        if (shortestVectorAngle < nextToShortestVectorAngle) {
            nextWaypoint = positionInTheList;
        }else{
            nextWaypoint = (positionInTheList + 1) % scheme.getWaypoints().size();
        }
        scheme.currentPosition = nextWaypoint;
    }
    
    /*
     * Method: clearMapping()
     * Input: none
     * Return: void (action)
     */
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
        this.scheme = new PatrollingScheme(currentAi.getWayPointForce());
        scheme.setWaypointforce(currentAi.getWayPointForce());
        frameCount=0;
    }


    /*
     * Method: calculateDistance()
     * Input: none
     * Function:
     * Return: void (action)
     */
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

    /*
     * Method: generatePopulationAndMpasForPoints()
     * Input: ArrayList<Boid_generic> attacker
     * Function:
     * Result: void (action)
     */
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

    /*
     * Method: calculateNewParameter()
     * Input: mode
     * Function:
     * Return: void (action)
     */
    public void calculateNewParameter(int mode){
        int counter=0;
        float averageSep=0;
        switch (mode){
            case 1:
                for(Map.Entry<Integer,ArrayList<WeightedObservedPoint>> boid: sepBoidError.entrySet()){
                    fitter=  PolynomialCurveFitter.create(2);
                    int erased = 0;
                    double estimation = 0;
                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;

                    for (int e = 1; e < 3; e++) {
                           // power of zero can be ignored for calculating the gradient
                            terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getSep_neighbourhood_size());
                            //   System.out.println("terms_total " + terms_toal);
                        }

                    estimation = currentAi.getSep_neighbourhood_size() - terms_toal * 2;

                    if (estimation >= AI_manager.neighbourhoodSeparation_upper_bound || estimation <= AI_manager.neighbourhoodSeparation_lower_bound) {
                            estimation = currentAi.getSep_neighbourhood_size();
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
                    fitter=  PolynomialCurveFitter.create(2);
                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 3; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getAli_neighbourhood_size());
                    }
                    double estimation = currentAi.getAli_neighbourhood_size()-terms_toal*fastLearningRate;
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
                    fitter=  PolynomialCurveFitter.create(2);
                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 3; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getCoh_neighbourhood_size());
                    }
                    double estimation = currentAi.getCoh_neighbourhood_size()-terms_toal*fastLearningRate;
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
                    fitter=  PolynomialCurveFitter.create(2);
                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 3; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getSep_weight());
                    }
                    double estimation = currentAi.getSep_weight()-terms_toal*learningRateParameters;
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
                    fitter=  PolynomialCurveFitter.create(2);
                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 3; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getAli_weight());
                    }
                    double estimation = currentAi.getAli_weight()-terms_toal*learningRateParameters;
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
                    fitter=  PolynomialCurveFitter.create(2);
                    double[] coefincients = fitter.fit(boid.getValue());
                    double terms_toal = 0;
                    for (int e = 1; e < 3; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getCoh_weight());
                    }
                    double estimation = currentAi.getCoh_weight()-terms_toal*learningRateParameters;
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
                    fitter=  PolynomialCurveFitter.create(2);
                    double[] coefincients = fitter.fit(boid.getValue());
                   // System.out.println("my coefficents " + Arrays.toString(coefincients));
                    double terms_toal = 0;
                    for (int e = 1; e < 3; e++) {
                        // power of zero can be ignored for calculating the gradient
                        terms_toal = terms_toal + create_new_term(e, coefincients[e], currentAi.getWayPointForce());
                    }
                    double estimation = currentAi.getWayPointForce()-terms_toal*10;

                    if(estimation > 0.1f || estimation < 0.01f){
                        estimation=currentAi.getWayPointForce();
                    }
                    wayPointEst.put(boid.getKey(),estimation);
                }
                averageSep=0;
                for(Map.Entry<Integer,Double> sep : wayPointEst.entrySet()){
                    averageSep+=sep.getValue();
                }
                //System.out.println("average sep " + averageSep + " size  " +  sepEst.size() + " together " + (int)averageSep/sepEst.size() );
                resultsFloat.add((float) averageSep/wayPointEst.size());
                currentAi.setWayPointForce((float) averageSep/wayPointEst.size());
                break;
        }
    }
    
    /*
     * Method: create_new_term()
     * Input: exponent, coeff, param_x
     * Function: defined by a math formula
     * Return: double term
     */
    private double create_new_term(int exponent, double coeffs, double param_x) {
        // term format... e.g. 5x^4 becomes 5(4x^5);
        // is is the point of the derivative
        // so becomes coeff(exponent*x^exponent)
        double term = coeffs * (exponent * Math.pow(param_x, exponent-1)); // doe abs work?
            return term;
    }

    /*
     * Method: randFloat()
     * Input: min and max
     * Function: a random float between min and max
     * Result: static float result
     */
    public static float randFloat(float min, float max) {
        Random rand = new Random();
        float result = rand.nextFloat() * (max - min) + min;
        return result;
    }
    // Mode 1 sep 2 ali 3 coh 4 sepWeight 5 aliW 6 cohW

    /*
     * Method: learnTheErrors()
     * Input: map, mode, defenders
     * Result: void (action)
     */
    public void learnTheErrors(Map<Integer,ArrayList<WeightedObservedPoint>> map,int mode,ArrayList<Boid_generic> defenders ){
        calculateDistance();
        PatrollingScheme schemeCopy = new PatrollingScheme(currentAi.getWayPointForce());
        for(PVector k : scheme.getWaypoints()){
            schemeCopy.getWaypoints().add(new PVector(k.x,k.y));
        }
        //float values[] = {19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40};

        float values[] = new float[howManyErrors];
        float lowerBound = AI_manager.neighbourhoodSeparation_lower_bound;
        float higherBound = AI_manager.neighbourhoodSeparation_upper_bound;

        values[0] = lowerBound - 1;
        values[howManyErrors-1] = higherBound + 1;
        float separation = (higherBound - lowerBound + 2)/(howManyErrors -2);

        for(int z = 1; z < howManyErrors -1; z++) {
            values[z] = values[z-1] + separation;
        }

        for(int j=0;j<howManyErrors;j++) {
            schemeCopy.currentPosition = nextWaypoint;
        //    System.out.println("j " + j);
            float xValue =0;
            if (mode == 1)
            {
                xValue = randFloat(AI_manager.neighbourhoodSeparation_lower_bound,AI_manager.neighbourhoodSeparation_upper_bound);
            }
            else if(mode>1 && mode <=3){
                /*if (j == 0) {
                    xValue = 19;
                }
                else if (j == howManyErrors - 1) {
                    xValue = 101;
                }
                else
                    xValue = randFloat(20,100) ;*/
                xValue = randFloat(AI_manager.getNeighbourhoodLowerBound(),AI_manager.getNeighbourhoodUpperBound());
                //xValue = values[j];
            }else if (mode==7) {
                xValue=randFloat(0.01f,0.1f);
               // System.out.println("my random " + xValue);
            }else {
                xValue = randFloat(0.01f,5);
            }
            
            AI_type sepAi = currentAi;
            int save = frameCount;
            ArrayList<Boid_generic> simulationBoids = copyTheStateOfAttackBoids(defenders,0);
            
            switch (mode) {
                case 1:
                    sepAi = new AI_type( xValue, currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(), currentAi.getSep_weight(), currentAi.getAli_weight(), currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                    break;
                case 2:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(),  xValue, currentAi.getCoh_neighbourhood_size(),currentAi.getSep_weight(), currentAi.getAli_weight(), currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                    break;
                case 3:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(),  xValue,currentAi.getSep_weight(), currentAi.getAli_weight(), currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                    break;
                case 4:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(), (float) xValue, currentAi.getAli_weight(), currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                    break;
                case 5:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(), currentAi.getSep_weight(), (float) xValue, currentAi.getCoh_weight(),currentAi.getWayPointForce(), ":(");
                    break;
                case 6:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(),currentAi.getSep_weight(), currentAi.getAli_weight(), (float) xValue,currentAi.getWayPointForce(), ":(");
                    break;
                case 7:
                    sepAi = new AI_type(currentAi.getSep_neighbourhood_size(), currentAi.getAli_neighbourhood_size(), currentAi.getCoh_neighbourhood_size(),currentAi.getSep_weight(), currentAi.getAli_weight(), currentAi.getCoh_weight(),xValue, ":(");
                    break;
            }

            int numberOfBoidsErrors=0;
            for (int i = 0; i < frameCount; i++) {
                int counter=0;
                for (Boid_generic def : simulationBoids) {
                     def.setAi(sepAi);
                     PVector acceleration =def.getAcceleration();
                     PVector velocity = def.getVelocity();
                     PVector location = def.getLocation();
                     def.run(simulationBoids, true, true); //Alex Part where he applies all forces
                     velocity.limit(1);
                     //My force
                     location.add(velocity.add(acceleration.add(schemeCopy.patrol(def.getLocation(), def)/*patrolling.patrol(be.getLocation(),be)*/)));
                        if(i==frameCount-1) {
                             WeightedObservedPoint point = new WeightedObservedPoint(1, xValue, (double) PVector.dist(endPositions.get(counter), location));
                             map.get(counter).add(point);
                            }
                            acceleration.mult(0);
                            counter++;
                        }
            }
        }
    }

    /*
     * Method: calculateErrors()
     * Input: none
     * Return: void
     */
    public void calculateErrors(){}

    /*
     * Method: observe()
     * Input: defenders
     * Function:
     * Return: integer 
     */
    public int observe(ArrayList<Boid_generic> defenders){
        ArrayList<Boid_generic>  initialState = new ArrayList<>();
        int numFrames = 20;
        if(stack.size()<numFrames){
            stack.add(copyTheStateOfAttackBoids(defenders,0));
            end = (end + 1) % numFrames;
        }else{
            begin = (begin + 1) % numFrames;
            initialState = copyTheStateOfAttackBoids(stack.get(begin),0);
            end = (end + 1) % numFrames;
            stack.set(end,copyTheStateOfAttackBoids(defenders,0));
        }

        if(stack.size()== numFrames && once) {
            frameCount=numFrames;
            ArrayList<Boid_generic>   initialStateForCalculation = stack.get(begin);
            ArrayList<Boid_generic>  endStateForCalculation = stack.get(end);
            //System.out.println("   " + initialStateForCalculation.size() + endStateForCalculation.size());
            observations.put(1, initialStateForCalculation); //initial state
            observations.put(2, endStateForCalculation);//end state
            observing = false;
            new Thread(this).start();
            //System.out.println("thread started");
            once=false;
        }

        if ( observations.size()==0  &&!once) {
            k++;
            once=true;
            /*if(learningRate>=0.5){
                learningRate+=2;
                learningRateParameters+=0.5;
            } else {
                learningRate+=0.5;
            }*/
            return 1;
        }
        return 0;
    }
    
    /*
     * Method: updateAi()
     * Input: none
     * Return: AI_type
     */
    public AI_type updateAi(){
        return currentAi;
    }

    /*
     * Method: copyTheStateOfAttackBoids()
     * Input:boids, mode
     * Function:for each member of boids, assign values to members and get each member's initialLocation and endingLocation
     * Return: ArrayList boidListClone
     */
    public ArrayList<Boid_generic> copyTheStateOfAttackBoids(ArrayList<Boid_generic> boids,int mode) {
        ArrayList<Boid_generic> boidListClone = new ArrayList<>();
        for(Boid_generic boid : boids){
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
