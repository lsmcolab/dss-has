
import grafica.*;

void settings() {
  size(500, 350);
}
void setup() {
  background(150);
  final int TIME_TO_OBSERVE = 1; // change this to view different time intervals at
  String run_moment = "77";
  String file_name = run_moment+" camera observing flee";
  String param = "sw";
  String poly_str = "poly_";
  String param_str = "param_";
  String param_file_name = file_name+" "+param_str+param;
  String poly_file_name = file_name+" "+poly_str+param;
  String[] param_lines = loadStrings(param_file_name+".txt");
  String[] poly_lines = loadStrings(poly_file_name+".txt");
  frame.setLocation(0,0);
  int polynomial_interval = 40;
  
  // second screen
  String[] args = {"Poly Grapher"};
  PApplet.runSketch(args, new Poly_Grapher(poly_lines,poly_file_name,TIME_TO_OBSERVE));
  PApplet.runSketch(args, new Error_Grapher(param_lines,param_file_name,polynomial_interval));

  // Prepare the points for the plot
  int nPoints = param_lines.length;
  String[] values = {"",""};
  GPointsArray param_points = new GPointsArray(nPoints);
    //points.add(0,0);
    int line_start_read = TIME_TO_OBSERVE*polynomial_interval;
    for (int i = line_start_read; i < (line_start_read+polynomial_interval); i++) {
      int break_index = 0;
      println("PARAM GRAPHING in: "+param_lines[i]);
      for (int l = 0 ; l < param_lines[i].length(); l++) {
        if (param_lines[i].charAt(l) != ',')
          values[0] = values[0] + param_lines[i].charAt(l);
       else {
          break_index = l+1;
          break;
        }
      }
      for (int l2 = break_index; l2 < param_lines[i].length(); l2++) {
          values[1] = values[1] + param_lines[i].charAt(l2);
      }
      param_points.add(float(values[0]),float(values[1]));
      values[0] = "";
      values[1] = "";
    }

  // Create a new plot and set its position on the screen
  GPlot plot = new GPlot(this,25,25);

  // Set the plot title and the axis labels
  plot.setTitleText("Parameter vs Error");
  plot.getXAxis().setAxisLabelText("Parameter");
  plot.getYAxis().setAxisLabelText("Observation Error");

  // Add the points
  plot.setPoints(param_points);
  // Draw it!
  plot.defaultDraw();
  //save(param_file_name+".png");
}