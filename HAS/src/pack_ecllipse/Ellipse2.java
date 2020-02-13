package pack_ecllipse;

import java.util.ArrayList;

import org.apache.commons.math3.analysis.function.Acos;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Ellipse2 {
	
	/*
	 * Define variables
	 */
    private double centerX;
    private double centerY;
    private double l1;
    private double l2;
    private double phi;
    private double ang;

    double[] eignValues = new double[2];
    private double m = 4.4721;
    
    private double[] otpimCenter = new double[2];
    /*
     * Constructor
     */

    public Ellipse2() {
    	
    }
    
    public Ellipse2(double[][] p){
    	//Step1: extract the covariance matrix from the double[][]p:  double[][] swarm = new double[m][2];
    	//Step2: extract the eignvalues and eignvectors from the covariance matrix (symmetric one)
    	//Step3: calculate the related feature values
    	
    	RealMatrix A = MatrixUtils.createRealMatrix(p);
    	RealMatrix cov = new Covariance(A).getCovarianceMatrix();//symmetric matrix 2x2
    	
        SingularValueDecomposition  svd = new SingularValueDecomposition(cov);
    	eignValues = svd.getSingularValues();
    	RealMatrix eignVectors = svd.getU();
   	
//    	for (double s : eignValues) {
//			System.out.println(s);
//		}
//    	System.out.println(eignVectors.toString());
    	
    	double[] x = A.getColumn(0);
    	double[] y = A.getColumn(1);
    	
    	double sumX = 0;
    	for (int i = 0; i < x.length; i++) {
    		sumX += x[i];
		}
    	this.centerX = sumX/x.length;
    	
    	double sumY = 0;
    	for (int i = 0; i < y.length; i++) {
    		sumY += y[i];
		}
    	this.centerY = sumY/y.length;
    	
    	
        if (eignVectors.getRow(0)[0] < 0) {
        	eignVectors.getRow(0)[0] = eignVectors.getRow(0)[0]* -1;
        	eignVectors.getRow(0)[1] = eignVectors.getRow(0)[1]* -1;
        }
        
    	l1 = Math.sqrt(eignValues[0])*m/2;
    	l2 = Math.sqrt(eignValues[1])*m/2;
    	
    	ArrayRealVector v = new ArrayRealVector(eignVectors.getRow(0));
    	ArrayRealVector vc = new ArrayRealVector(new double[] {1,0});
    	double vp = v.dotProduct(vc);
    	Acos arccos = new Acos();
    	double phi = arccos.value(vp);
    	
    	if(eignVectors.getRow(0)[1] < 0 && phi >0) {
    		phi = phi * -1;
    	}
    	if(phi>0) {
    		ang = phi*180/Math.PI;
    	}else {
    		ang = 180 +(phi*180/Math.PI);
    	}
    	
    	
    } 	
    	

    public double getCenterX() {
		return centerX;
	}

	public void setCenterX(double centerX) {
		this.centerX = centerX;
	}

	public double getCenterY() {
		return centerY;
	}

	public void setCenterY(double centerY) {
		this.centerY = centerY;
	}

	public double getL1() {
		return l1;
	}

	public void setL1(double l1) {
		this.l1 = l1;
	}

	public double getL2() {
		return l2;
	}

	public void setL2(double l2) {
		this.l2 = l2;
	}

	public double getAng() {
		return ang;
	}

	public void setAng(double ang) {
		this.ang = ang;
	}
	

	
	/*
	 * Method: linspace
	 */
	public static double[] linspace(double min, double max, int points) {  
	    double[] d = new double[points];  
	    for (int i = 0; i < points; i++){  
	        d[i] = min + i * (max - min) / (points - 1);  
	    }  
	    return d;  
	} 
   /*
    * Method: optimiseCenter()
    */
	public static void optimiseCenter(double[][] p) {//p are the all the boids location in one swarm
		//create a meshgrid for the test to iteratre through, set center of swarm on the most concentrated area
		RealMatrix A = MatrixUtils.createRealMatrix(p);
		double[] x = A.getColumn(0);
    	double[] y = A.getColumn(1);
    	DescriptiveStatistics dsx = new DescriptiveStatistics(x);
    	DescriptiveStatistics dsy = new DescriptiveStatistics(y);
    	double x_mean = dsx.getMean();
    	double y_mean = dsy.getMean();
    	double[] x_choice = linspace(x_mean-10, x_mean+10, 20);
    	double[] y_choice = linspace(y_mean-10, y_mean+10, 20);
    	ArrayList<double[]> xy_choice = new ArrayList<>();
    	for (int i = 0; i < x_choice.length; i++) {
			for (int j = 0; j < y_choice.length; j++) {
				double[] pair = new double[2];
				pair[0]=x_choice[i];
				pair[1]=y_choice[j];
				xy_choice.add(pair); 
			}
		}
    	
    	
    	double mini_ratio= 10;

		
		//return otpimCenter;
		
	}
	
}