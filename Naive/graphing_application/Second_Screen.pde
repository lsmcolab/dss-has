public class Poly_Grapher extends PApplet {
  
  String[] poly_lines;
  String poly_file_name;
  String[] value = {"","","","",""};
  int polynomial_to_observe;

 
  public void settings() {
    size(500, 350);
  }
  
  public void setup() {
  background(150);

  // Prepare the points for the plot
  int nPoints = poly_lines.length;
    GPointsArray poly_points = new GPointsArray(nPoints);
    //poly_lines.length
      println("POLY GRAPHING in: "+poly_lines[polynomial_to_observe]);
      int break_index = 0;
      int l = 0;
      for (l = 0 ; l < poly_lines[polynomial_to_observe].length(); l++) {
        if (poly_lines[polynomial_to_observe].charAt(l) != ',')
          value[0] = value[0] + poly_lines[polynomial_to_observe].charAt(l);
       else {
          break_index = l+1;
          break;
        }
      }
         for (l = break_index; l < poly_lines[polynomial_to_observe].length(); l++) {
        if (poly_lines[polynomial_to_observe].charAt(l) != ',')
          value[1] = value[1] + poly_lines[polynomial_to_observe].charAt(l);
       else {
          break_index = l+1;
          break;
        }
      }
        for (l = break_index; l < poly_lines[polynomial_to_observe].length(); l++) {
        if (poly_lines[polynomial_to_observe].charAt(l) != ',')
          value[2] = value[2] + poly_lines[polynomial_to_observe].charAt(l);
       else {
          break_index = l+1;
          break;
        }
      }
              for (l = break_index; l < poly_lines[polynomial_to_observe].length(); l++) {
        if (poly_lines[polynomial_to_observe].charAt(l) != ',')
          value[3] = value[3] + poly_lines[polynomial_to_observe].charAt(l);
       else {
          break_index = l+1;
          break;
        }
      }
        for (l = break_index; l < poly_lines[polynomial_to_observe].length(); l++) {
        value[4] = value[4] + poly_lines[polynomial_to_observe].charAt(l);
      }
    // plot polynomial
       for (int x = -10; x <= 10; x= x+1) {
         println();
         println("POLYNOMIAL PLOT AT TIME "+polynomial_to_observe);
         println(x+","+"value:"+value[0]+" is * x to the power of 4");
         println(x+","+"value:"+value[1]+" is * x to the power of 3");
         println(x+","+"value:"+value[2]+" is * x to the power of 2");
         println(x+","+"value:"+value[3]+" is * x");
         println(x+","+"value:"+value[4]+" is added");
         poly_points.add(x,(float(value[0])*pow(x,4))+(float(value[1])*pow(x,3)) + (float(value[2])*pow(x,2)) + (float(value[3])*x) + float(value[4]));
       }
//      poly_points.add(float(value2),float(value1));
  // Create a new plot and set its position on the screen
  GPlot plot = new GPlot(this,25, 25);

  // Set the plot title and the axis labels
  plot.setTitleText("Polynomial");
  plot.getXAxis().setAxisLabelText("Parameter");
  plot.getYAxis().setAxisLabelText("Predicted Error");

  // Add the points
  //plot.setPoints(param_points);
  plot.setPoints(poly_points);
  //plot.setPoints(poly_points);
  // Draw it!
  plot.defaultDraw();
  save(poly_file_name+"_poly.png");
} 
 
  Poly_Grapher(String[] in,String fn,int pto) {
    poly_lines = in;
    poly_file_name = fn;
    polynomial_to_observe = pto;
  }
  
}