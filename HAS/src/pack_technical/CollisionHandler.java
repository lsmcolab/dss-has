package pack_technical;

import pack_boids.Boid_generic;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * CollisionHandler class
 * This class is to 
 * 
 *
 */
public class CollisionHandler {
    /*
     * define variables
     */
    ArrayList<Boid_generic> team1;
    ArrayList<Boid_generic> team2;
    private final float mass=5;
    private static GameManager manager;
    private boolean lose=false;
    private boolean victory=false;
    public float collision_distance= - 100;
    
    /*
     * Get and set methods
     */
    public boolean isLose() {
        return lose;
    }

    public boolean isVictory() {
        return victory;
    }

    
    /*
     * Constructor CollisionHandler with one parameter
     * initialise a manager g; get two teams from manager and assign to team1 and team2
     */
    public CollisionHandler(GameManager g){
        CollisionHandler.manager=g;
        team1 = GameManager.get_team(0);
        team2 = GameManager.get_team(1);
    }

    /*
     * Method: doesCollide( boid1, boid2): judgement if two boids are collided or not
     * Input: boid1 and boid2
     * Function: calculate the distance between two boids
     * Return: a boolean return: true or false, conditional on if the distance is <6 pixels or not
     */
    public boolean doesCollide(Boid_generic boid1,Boid_generic boid2){
        float d = PVector.dist(boid1.getLocation(),boid2.getLocation() );
        if(d<6){  //
         //   System.out.println("I COLLIDE" + boid1.getId());
            return true;
        }
        return false;
    }
    
    /*
     * Method: checkCollisions()
     * Function: (1) check if any member in team1 and any member in team2 collide: if true, lose
     * 			 (2) if team2 (only one member-> the attacker) is very close to target without collide: victory
     * Return: void; the process produce the result of simulation (lose or victory)
     */
    public void checkCollisions(){ //Elastic collisions
        for(Boid_generic b1 : team1){
            for (Boid_generic b2 : team2){ //only has the attacker, one boid
                if(doesCollide(b1,b2)){
                    lose=true;
                    collision_distance = PVector.dist(b2.getLocation(),new PVector(550,500f));
                } else if(PVector.dist(b2.getLocation(),new PVector(550,500f))<=10){ //target is PVector (550, 500f)
                    victory=true;
                }
            }
        }

    }

}
