public class Error_Grapher extends PApplet {
  
  String[] err_lines;
  String param_file_name;
  String[] value = {"","","","",""};
  int polynomial_to_observe;
  int polynomial_interval;

 
  public void settings() {
    size(500, 350);
  }
  
  public void setup() {
  background(150);

   // Prepare the points for the plot
  int nPoints = err_lines.length;
  String[] values = {"",""};
  GPointsArray param_points = new GPointsArray(nPoints);
    //points.add(0,0);
    for (int i = 0; i < (err_lines.length); i = i+polynomial_interval*4) { // plots every data reset interval
      int break_index = 0;
      for (int l = 0 ; l < err_lines[i].length(); l++) {
        if (err_lines[i].charAt(l) != ',')
          values[0] = values[0] + err_lines[i].charAt(l);
       else {
          break_index = l+1;
          break;
        }
      }
      for (int l2 = break_index; l2 < err_lines[i].length(); l2++) {
          values[1] = values[1] + err_lines[i].charAt(l2);
      }
      param_points.add(float(i),float(values[1]));
      values[0] = "";
      values[1] = "";
    }
    // Create a new plot and set its position on the screen
    GPlot plot = new GPlot(this,25,25);

    // Set the plot title and the axis labels
    plot.setTitleText("Error over number of observatinos");
    plot.getXAxis().setAxisLabelText("Number of Observations");
    plot.getYAxis().setAxisLabelText("Observation Error");

    // Add the points
    plot.setPoints(param_points);
    // Draw it!
    plot.defaultDraw();
    save(param_file_name+"_error.png");
  }
 
  Error_Grapher(String[] in,String fn,int pni) {
    err_lines = in;
    param_file_name = fn;
    polynomial_interval = pni;
  }
  
}