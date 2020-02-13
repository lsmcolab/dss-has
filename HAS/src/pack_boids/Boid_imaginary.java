package pack_boids;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Boid_imaginary class
 * a fake boid running inside the 'heads' of existing boids,
*/
public class Boid_imaginary extends Boid_generic {

	/*
	 * Define variables
	 */
	// technically always a boid standard
	Boid_generic original; // the real boid that this fake one imitates

	
	/*
	 * Constructor
	 */
	public Boid_imaginary(PApplet p, float x, float y, int t, Boid_generic b) {
		super(p, x, y, t,b.getId());
		original = b;
	}
	
	/*
	 * Get and set methods
	 */

	public Boid_generic getOriginal() {
		return original;
	}

	/*
	 * Method: run()
	 * Input: boids, real_step, simulation
	 * Function:
	 * Return: void
	 */
	public void run(ArrayList<Boid_generic> boids, boolean real_step,boolean simulation) {
		if (boids.get(0).isMoveable()) {
			record_history();
			isalone = true; // is boid uninteracted with?
			move(boids); // unsets isalone if interacted with
			record_acceleration();
			update();
		}
		if(!simulation) {
			move_borders(false);
			if (real_step) {
				render_trails(1,simulation);
				render();
			}
		}

	}

	/*
	 * Method: render()
	 * Function:
	 */
	public void render() {
		parent.fill(fillcol.getRGB());
		parent.stroke(linecol.getRGB());
		parent.pushMatrix();
		parent.translate(location.x, location.y);
		parent.rotate(velocity.heading());
		parent.beginShape(PConstants.TRIANGLES);
		parent.vertex(size, 0);
		parent.vertex(-size, size / 2);
		parent.vertex(-size, -size / 2);
		parent.endShape();
		parent.popMatrix();
	}
}
