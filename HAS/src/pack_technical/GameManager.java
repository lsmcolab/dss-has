package pack_technical;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import pack_1.Launcher;
import pack_AI.AI_manager;
import pack_AI.AI_type;
import pack_boids.Boid_standard;
import pack_boids.Boid_generic;
import processing.core.PApplet;
import processing.core.PVector;

/*
 * does game logic systems, such as holding teams, team colours and can perform higher level game
 * functions such as telling the flock to spawn groups of boids on a team, or start a round.
 * 
 * The game manager also creates an AI manager which creates all the AI profiles, which the game
 * manager will load into each team so that they have a common AI type.
 */
public class GameManager {

	static Boid_generic selected_boid = null;
	static PApplet parent; // the processing app (allows access to its functions)
	private static int team_number = (int) (Launcher.getClient_dimensions().x / 75); // not all of these will be used
	private FlockManager flock_ref;
	private DisplayManager display_sys_ref;

	@SuppressWarnings("unchecked")
	static ArrayList<Boid_generic>[] team = new ArrayList[getTeam_number()];
	static Color[] team_cols = new Color[getTeam_number()]; // Array for the teams, index is team, colour is held
	static AI_type[] team_ai = new AI_type[getTeam_number()]; // Array for the teams, index is team, colour is held
	static boolean isSwitched = false;

	/*
	 * Constructor GameManager with one parameter (itself)
	 * 
	 */
	public GameManager(GameManager g){
		this.selected_boid = g.get_select_boid();
		this.team_number = g.getTeam_number();
		this.parent = g.getParent();
		this.flock_ref = g.getFlock_ref();

	}
	
	/*
	 * Constructor GameManager with different parameters
	 * Initiate or instantise a few classes
	 */
	public GameManager(PApplet p, FlockManager f, DisplayManager d) {
		parent = p;
		flock_ref = f;
		display_sys_ref = d;
		// assign team colours and ai's
		for (int i = 0; i < getTeam_number(); i++) { // for every team
			team_cols[i] = generate_teamcolour(i);
			team_ai[i] = AI_manager.get_team_ai(i);
			team[i] = new ArrayList<Boid_generic>();
			team[i].clear(); // teams start empty
		}

	}

	/*
	 * Method: generate_teamcolour
	 * Function: generate random color for 3 basic color to form a new color (format sRGB space)
	 * Return: a color object
	 */
	Color generate_teamcolour(int seed) {
		Random rand = new Random(seed);
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		return new Color(r, g, b);
	}

	/*
	 * Method: get_team_colour
	 * Input: integer i
	 * Function: Assign colours: if i is in [0, team_number), random color; if i == total team_number + 1, a fixed color; else report error message.
	 * Return: ??
	 */
	public static Color get_team_colour(int i) {
		if ((i >= 0) && (i < getTeam_number()))
			return team_cols[i]; // for every normal team fetch it's colour
		else if (i == getTeam_number()+1) // for the camera team
			return new Color(210, 210, 210);
		else {
			PApplet.print(" error: attempted to access team colour array out of bounds");
			return new Color(255, 0, 0);

		}

	}
	
	/*
	 * Method: spawn_boids (three input parameters)
	 * Function:(1) generate boids: Boid_generic is a parent class, Boid_standard is a child class
	 * 			(2) add boids to a flock_ref object and a team object	 * 
	 */
	public void spawn_boids(int team_n, int amount, PVector pos) {
		for (int i = 0; i < amount; i++) {
			Boid_generic b = new Boid_standard(parent, pos.x, pos.y, team_n,i);
			flock_ref.add_boid(b);
			team[team_n].add(b);
		}
	}

	/*
	 * Method: delete and assign null value to the selected_boid
	 * Need to define the selected_boid!! ??
	 * Return: void
	 */
	public void delete_selected() {
		if (selected_boid != null)
			selected_boid.kill();
		selected_boid = null;

	}

	/*
	 * Method: process_selected()
	 * Function: display and other drawing functions for the selected_boid
	 * Return: void
	 */
	public void process_selected() {
		// if (!boid_selected && selected_boid != null) {
		if (selected_boid != null) {
			display_sys_ref.super_highlight_boid(selected_boid);
		}
	}

	/*
	 * Method: run()
	 * Function: run the method process_selected()
	 * Return: void
	 */
	public void run() {
		process_selected();
		if (parent.frameCount % 300 == 0) {
			// spawn_group((int) (parent.random(team_number)), (int) (parent.random(6) +
			// 3));
		}
	}

	

	/*
	 * Get and set methods for all related objects
	 */
	public static ArrayList<Boid_generic> get_team(int i) {
		return team[i];
	}

	public Boid_generic get_select_boid() {
		return selected_boid;
	}

	public static Color[] getTeam_cols() {
		return team_cols;
	}

	
	public static void setTeam_cols(Color[] team_cols) {
		GameManager.team_cols = team_cols;  //setting call
	}

	public static AI_type[] getTeam_ai() {
		return team_ai;
	}

	public static void setTeam_ai(AI_type[] team_ai) {
		GameManager.team_ai = team_ai;//setting call
	}

	public static int getTeam_number() {
		return team_number;
	}

	public static Boid_generic getSelected_boid() {
		return selected_boid;
	}

	public void setSelected_boid(Boid_generic selected_boid) {
		GameManager.selected_boid = selected_boid; //setting call
	}

	public static int get_random_team() {
		//return (int) parent.random(getTeam_number()); previous Alex version
		if (isSwitched){
			return 1;
		}
		isSwitched=true;
		return 0;
	}
	public static PApplet getParent() {
		return parent;
	}

	public FlockManager getFlock_ref() {
		return flock_ref;
	}

	public DisplayManager getDisplay_sys_ref() {
		return display_sys_ref;
	}

	public static ArrayList<Boid_generic>[] getTeam() {
		return team;
	}

	public static boolean isSwitched() {
		return isSwitched;
	}
	
	

}
