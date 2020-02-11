package pack_technical;

import pack_boids.Boid_generic;
import processing.core.PVector;

import java.util.ArrayList;

public class CollisionHandler {
    private static   GameManager manager;
    ArrayList<Boid_generic> team1;
    ArrayList<Boid_generic> team2;
    private final float mass=5;

    public boolean isLose() {
        return lose;
    }

    private boolean lose=false;

    public boolean isVictory() {
        return victory;
    }

    private boolean victory=false;

    public CollisionHandler(GameManager g){
        this.manager=g;
         team1 = manager.get_team(0);
         team2 = manager.get_team(1);
    }

    public boolean doesCollide(Boid_generic boid1,Boid_generic boid2){
        float d = PVector.dist(boid1.getLocation(),boid2.getLocation() );
        if(d<6){  //
         //   System.out.println("I COLLIDE" + boid1.getId());
            return true;
        }
        return false;
    }

    public void checkCollisions(){ //Elastic collisions

        for(Boid_generic b1 : team1){
            for (Boid_generic b2 : team2){
                if(doesCollide(b1,b2)){
                    lose=true;
                } else if(PVector.dist(b2.getLocation(),new PVector(550,500f))<=10){
                    victory=true;

                }
            }
        }



    }

}
