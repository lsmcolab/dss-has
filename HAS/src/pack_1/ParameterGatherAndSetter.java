package pack_1;

/**
 * ParameterGatherAndSetter class
 */
import pack_AI.AI_manager;
import pack_AI.AI_type;
import pack_ecllipse.EllipseForecast;
import pack_technical.CollisionHandler;
import pack_technical.GameManager;
import pack_technical.ZoneDefence;
import processing.core.PVector;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

public class ParameterGatherAndSetter {
    // 0-1 : parameters for attackers
    //2 counter
    //
	
	/*
	 * Define the variables
	 */
    GameManager game;
    CollisionHandler col;
    private double startTime;
    private int counter = 0;
    static ArrayList<String> history_of_learning = new ArrayList<>();
    double startTimeWithoutwait;
    public int iterations = 0;
    boolean once=true;
    float x;
    float y;
    ArrayList<PVector> hard = new ArrayList<>();
    ArrayList<PVector> medium = new ArrayList<>();
    ArrayList<PVector> easy = new ArrayList<>();
    int amountOfBoids=0;
    String difficulty;
    String[] args;
    
        
    /*
     * Constructor with parameters
     */
    public ParameterGatherAndSetter(int PosX, int PosY, int difficulty, int mode, GameManager game , CollisionHandler col, String[] args)  {
        this.game=game;
        this.args=args;
        this.difficulty=args[3]; //a string from a String list (including 3 elements)
        this.col=col;
        this.x=Float.parseFloat(args[0]);
        this.y=Float.parseFloat(args[1]);
        this.counter=Integer.parseInt(args[2]);
        amountOfBoids=Integer.parseInt(args[4]);
        startTime=System.nanoTime();
        game.spawn_boids(0,amountOfBoids,new PVector(450,510)); //team0 from initial PVector point
        game.spawn_boids(1,1,new PVector(Float.parseFloat(args[0]),Float.parseFloat(args[1])));//team1 with 1 member

        createDifficulties();
    }
    
    /*
     * Method: createDifficulties()
     * Function: adding PVector points to three ArrayList named as medium, hard and easy
     * Return: void
     */
    public void createDifficulties(){
  	
    	
    	
    	medium.add(new PVector(530,525));
        medium.add(new PVector(650,425));
        medium.add(new PVector(530,330));

        hard.add(new PVector(530,500));
        hard.add(new PVector(530,405));

        easy.add(new PVector(450,600));
        easy.add(new PVector(650,500));
        easy.add(new PVector(450,405));
        easy.add(new PVector(590,305));
    }
    
    
    
    /*
     * Method: returnDifficulty()
     * Function:
     * Return: An ArrayList
     */
    public ArrayList<PVector> returnDifficulty(){
        if(difficulty.equals("hard")){
            return hard;
        }
        if(difficulty.equals("medium")){
            return medium;
        }
        return easy;
            
    }

    /*
     * Method: gather()
     * Function: print the simulation information and exit the system for two cases: lose and victory
     * Return: void
     */
    public void gather(ZoneDefence zone) throws IOException {
        if(col.isLose()){
        	System.out.println("Times of (X,Y) series switch is: "+EllipseForecast.arima_switch_Xcount+", "+EllipseForecast.arima_switch_Ycount);
        	System.out.println("Simulation took " + Math.round((System.nanoTime()-startTime)/1000000000) + " s and was a failure");
            //System.out.println("The attacker failed with its distance to target "+ col.collision_distance);
            generateEndingStatement(0);
            
//    		System.out.println("The times of arima swtich for x and y in simulation is " + 
//    				"("+ arima_switch_Xcount +"),"
//    				+"("+ arima_switch_Ycount+")");
            zone.stop();
            
            if(Math.round((System.nanoTime()-startTime)/1000000000)==300){//timeout after 300 s
                generateEndingStatement(2);
                System.out.println("Timeout");
                System.exit(0);
            }
            
            System.exit(0);
        } else if(col.isVictory()){
            generateEndingStatement(1);
        	System.out.println("Times of (X,Y) series switch is: "+EllipseForecast.arima_switch_Xcount+", "+EllipseForecast.arima_switch_Ycount);
            System.out.println("Simulation took " + Math.round((System.nanoTime()-startTime)/1000000000) + " s and was a victory");
//    		System.out.println("The times of arima swtich for x and y in simulation is " + 
//    				"("+ arima_switch_Xcount +"),"
//    				+"("+ arima_switch_Ycount+")");
            zone.stop();
            
            if(Math.round((System.nanoTime()-startTime)/1000000000)==300){//timeout after 300 s
                generateEndingStatement(2);
                System.out.println("Timeout");
                System.exit(0);
            }
            
            //zone.stop();
            System.exit(0);
        }
    }

    /*
     * Method: sendParameters with one parameter
     * Function: add information to currentAi class object: size, weight and calculated information??
     * Return: void
     */
    public void sendParameters(AI_type currentAi){
        if(once){
            startTimeWithoutwait=System.nanoTime();
            once=false;
        }
        history_of_learning.add(currentAi.getSep_neighbourhood_size() + "," 
        						+ currentAi.getAli_neighbourhood_size() + "," 
        						+ currentAi.getCoh_neighbourhood_size() + "," 
        						+ currentAi.getSep_weight()  + "," 
        						+ currentAi.getAli_weight() + "," 
        						+ currentAi.getCoh_weight() + "," 
        						+ Math.pow(currentAi.getSep_neighbourhood_size()-30,2)+","
        						+ Math.pow(currentAi.getAli_neighbourhood_size()-70,2) + "," 
        						+ Math.pow(currentAi.getCoh_neighbourhood_size()-70,2) + "," 
        						+ Math.pow(currentAi.getSep_weight()-2,2) + "," 
        						+ Math.pow(currentAi.getAli_weight()-1.2,2)  + "," 
        						+ Math.pow(currentAi.getCoh_weight()-0.9f,2) +  "," 
        						+ Math.pow(currentAi.getWayPointForce()-0.04,2)+"\n");

    }

    /*
     * Method: generatingEndingStatemetn with one parameter
     * Function: (1) add ending statement information to an arraylist lines
     * 			 (2) write the lines to a .txt file in a defined directory
     * Return: void
     */
    public void generateEndingStatement(int v) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(AI_manager.getAi_basic().getSep_neighbourhood_size() + "," + AI_manager.getAi_basic().getAli_neighbourhood_size() + "," + AI_manager.getAi_basic().getCoh_neighbourhood_size() + "," + AI_manager.getAi_basic().getSep_weight()  + "," + AI_manager.getAi_basic().getAli_weight() + "," + AI_manager.getAi_basic().getCoh_weight() );
        lines.add(v+","+ Math.round((System.nanoTime()-startTime)/1000000000)+","+ Math.round((System.nanoTime()-startTimeWithoutwait)/1000000000)+","+ iterations + "," + difficulty+","+amountOfBoids+","+x+","+y);
        lines.add(EllipseForecast.arima_switch_Xcount+", "+EllipseForecast.arima_switch_Ycount+"\n");
        lines.addAll(history_of_learning);

        Path file = Paths.get(args[5]+counter+".txt");
        Files.write(file, lines, Charset.forName("UTF-8"));

    }
    
    /*
     * Method: generatingEndingStatemetn with ellipse information
     * Function: (1) add ending statement information to an arraylist lines
     * 			 (2) write the lines to a .txt file in a defined directory
     * Return: void
     */
    public void generateEndingStatement2() throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        //lines.add(1);


        Path file = Paths.get(args[5]+counter*20 +".txt");
        Files.write(file, lines, Charset.forName("UTF-8"));

    }


}