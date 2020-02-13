

package pack_ecllipse;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.*;



/**
 * EllipseForest class
 * This class is to use five metrics (arrayList) from Ellipse class and do ARIMA forecasting.
 * No need to draw ellipse visually, just to focus on the ellipse metrics (1) estimation, (2) optimisation and (3) prediction
 * 
 */
public class EllipseForecast {
    
	/*
	 * Define variables
	 */
	//five elements to define a ellipse
	public ArrayList<Double> ellipseX = new ArrayList<>();
	public ArrayList<Double> ellipseY = new ArrayList<>();
	public ArrayList<Double> ellipseL1 = new ArrayList<>();
	public ArrayList<Double> ellipseL2 = new ArrayList<>();
	public ArrayList<Double> ellipseAng = new ArrayList<>();
	
	double[] X = new double[ellipseX.size()]; //create an array with the size of the ArrayList
	double[] Y = new double[ellipseY.size()];
	double[] L1 = new double[ellipseL1.size()];
	double[] L2 = new double[ellipseL2.size()];
	double[] Angle = new double[ellipseAng.size()];
	
	public ArrayList<Double> predicted_X = new ArrayList<>();
	public ArrayList<Double> predicted_Y = new ArrayList<>();
	public ArrayList<Double> predicted_L1 = new ArrayList<>();
	public ArrayList<Double> predicted_L2 = new ArrayList<>();
	public ArrayList<Double> predicted_Ang = new ArrayList<>();
	
	public String order;
	private  static boolean firstcall1 = true; 
	private  static boolean firstcall2 = true;
	private  static boolean firstcall3 = true;
	private  static boolean firstcall4 = true;
	private  static boolean firstcall5 = true;
	
	private static ArrayList<Integer> n1 = new ArrayList<>();
	private static ArrayList<Integer> n2 = new ArrayList<>();
	
	private static boolean OrderSwitch1 = false;
	private static boolean OrderSwitch2 = false;
	
	public static int arima_switch_Xcount = 0;
	public static int arima_switch_Ycount = 0;
	
	/*
	 * Constructor
	 */
	public EllipseForecast(ArrayList<Double> ellipseX,ArrayList<Double> ellipseY,
			ArrayList<Double> ellipseL1, ArrayList<Double> ellipseL2, ArrayList<Double> ellipseAng, int h, Rengine re) {
		//receive an Array of ellipse feature and transfer it to double array
		
		double[] d_X = ellipseX.stream().mapToDouble(Double::doubleValue).toArray();
		double[] d_Y = ellipseY.stream().mapToDouble(Double::doubleValue).toArray();
		double[] d_L1 = ellipseL1.stream().mapToDouble(Double::doubleValue).toArray();
		double[] d_L2 = ellipseL2.stream().mapToDouble(Double::doubleValue).toArray();
		double[] d_Ang = ellipseAng.stream().mapToDouble(Double::doubleValue).toArray();

		
		predicted_X = arimaFittingAndPrediction1(re,d_X,h);
		predicted_Y = arimaFittingAndPrediction2(re,d_Y,h);
		
		predicted_L1 = arimaFittingAndPrediction(re,d_L1,h);
		predicted_L2 = arimaFittingAndPrediction(re,d_L2,h);
		predicted_Ang = arimaFittingAndPrediction(re,d_Ang,h);
		

		
	}

	/*Method 0: arimaFittingAndPrediction() - static, for the class, not for each instance
	 * Input: double array and integer h (the number of forecast)
	 * Function: using auto.arima to determine the order (p,d,q) and coefficients for ARIMA
	 * Return: ArrayList<Double> of the forecasted doubles
	 */
	public static ArrayList<Double> arimaFittingAndPrediction(Rengine re, double[] d, int h) { 
		
		ArrayList<Double> output = new ArrayList<Double>() ;
	    String horizon = new String();
	    horizon = String.valueOf(h);
		re.assign("rVector", d);//VERY IMPORTANT!!! 
		re.assign("fh", horizon);
		
 		//ARIMA forecasting result
		re.eval("model1 <- arima(rVector, order=c(2,1,3),method = \"CSS\")");  //choice of hyper-parameters for ARIMA: method = \"CSS\"
		re.eval("y<- as.numeric(forecast(model1,fh)$mean)"); 

	    REXP fcast = re.eval("y");
	    
	    for (double m : fcast.asDoubleArray()) {
			output.add(m);
		}
	    
	    return output;
		
	}
	
	/*Method 1: arimaFittingAndPrediction1() - static, for the class, not for each instance
	 * Input: double array and integer h (the number of forecast)
	 * Function: using auto.arima to determine the order (p,d,q) and coefficients for ARIMA
	 * Return: ArrayList<Double> of the forecasted doubles
	 */
	public static ArrayList<Double> arimaFittingAndPrediction1(Rengine re, double[] data, int h) { 
		
		ArrayList<Double> output = new ArrayList<Double>() ;
	    String horizon = new String();
	    horizon = String.valueOf(h);
		re.assign("rVector", data);//VERY IMPORTANT!!! 
		re.assign("fh", horizon);
		
		if (firstcall1 ==true) {
			n1.add(0);
			n1.add(1);
			firstcall1 = false;	
			
		}


		//ARIMA forecasting result
		for (int i = 0; i < 2; i++) {
//			System.out.println(n1.size());
			//if order swtich is true, n1=[1,0]
			if(OrderSwitch1==true) {
				n1.add(0, n1.get(1));
				n1.remove(2);
				arima_switch_Xcount ++;
			} 
			
//			for (int d : n1) {
//				System.out.print(d);
//			}
//			System.out.println("Times of X series switch is: "+arima_switch_Xcount);
			
			if(n1.get(0)==0) {
				re.eval("model1 <- arima(rVector, order=c(2,1,3),method = \"CSS\")");  //choice of hyper-parameters for ARIMA: method = \"CSS\"
			}else {
				re.eval("model1 <- arima(rVector, order=c(2,0,3),method = \"CSS\")");
			}
			
			re.eval("y<- as.numeric(forecast(model1,fh)$mean)"); 
			
		    re.eval("std_obs<-sd(rVector)");
		    re.eval("std_for<-sd(y)");
		    re.eval("diff_std<- abs(std_for-std_obs)/std_obs");
		    re.eval("mean_obs<-mean(rVector)");
            re.eval("mean_for<-mean(y)");
            re.eval("diff_mean<-abs(mean_obs-mean_for)/mean_obs");
		    REXP std_d = re.eval("diff_std");
		    REXP mean_d = re.eval("diff_mean");
		    		
		    Double diff_std = std_d.asDouble(); //the percentage of the forecast std to the observed std: threshold is 70%, lower than 70%, switch
		    Double m_d = mean_d.asDouble();
		    
		    if(Math.abs(diff_std) > 0.40 || m_d > 0.15) {//could be changed from 0.7 to 0.6
		    	OrderSwitch1 = true;
		    }else {
		    	OrderSwitch1 = false;
		    	break;
		    }

		}
		
		re.eval("y<- as.numeric(forecast(model1,fh)$mean)"); 
	    REXP fcast = re.eval("y");
	    
	    for (double m : fcast.asDoubleArray()) {
			output.add(m);
		}
	    
	    return output;
	}
	
	/*
	 * Method2: arimaFittingAndPrediction2
	 */
	public static ArrayList<Double> arimaFittingAndPrediction2(Rengine re, double[] data, int h) { 
		ArrayList<Double> output = new ArrayList<Double>() ;
	    String horizon = new String();
	    horizon = String.valueOf(h);
		re.assign("rVector", data);//VERY IMPORTANT!!! 
		re.assign("fh", horizon);
		
		if (firstcall2==true) {
			n2.add(0);
			n2.add(1);
			firstcall2 = false;	
			
		}
		//ARIMA forecasting result
		for (int i = 0; i < 2; i++) {
			//if order swtich is true, n1=[1,0]
			if(OrderSwitch2==true) {
				n2.add(0, n2.get(1));
				n2.remove(2);
				arima_switch_Ycount ++;
			} 
			
//			System.out.println("Times of Y series switch is: "+arima_switch_Ycount);
			
			if(n2.get(0)==0) {
				re.eval("model1 <- arima(rVector, order=c(2,1,3),method = \"CSS\")");  //choice of hyper-parameters for ARIMA: method = \"CSS\"
			}else {
				re.eval("model1 <- arima(rVector, order=c(2,0,3),method = \"CSS\")");
			}
			
		    re.eval("std_obs<-sd(rVector)");
		    re.eval("std_for<-sd(y)");
		    re.eval("diff_std<- abs(std_for-std_obs)/std_obs");
		    re.eval("mean_obs<-mean(rVector)");
            re.eval("mean_for<-mean(y)");
            re.eval("diff_mean<-abs(mean_obs-mean_for)/mean_obs");
		    REXP std_d = re.eval("diff_std");
		    REXP mean_d = re.eval("diff_mean");
		    		
		    Double diff_std = std_d.asDouble(); //the percentage of the forecast std to the observed std: threshold is 70%, lower than 70%, switch
		    Double m_d = mean_d.asDouble();//the percentage of the forecast std to the observed std: threshold is 70%, lower than 70%, switch
		    
		    if(Math.abs(diff_std) > 0.40 || m_d > 0.15) {//changed from 0.7 to 0.6
		    	OrderSwitch2 = true;
		    }else {
		    	OrderSwitch2 = false;
		    	break;
		    }

		}
		
		
	
		re.eval("y<- as.numeric(forecast(model1,fh)$mean)"); 
	    REXP fcast = re.eval("y");
	    
	    for (double m : fcast.asDoubleArray()) {
			output.add(m);
		}
	    
	    return output;
	}
	
	/*
	 * Method3: arimaFittingAndPrediction3
	 */
	public static ArrayList<Double> arimaFittingAndPrediction3(Rengine re, double[] data, int h) { 
		
		ArrayList<Double> output = new ArrayList<Double>() ;
	    
	    String horizon = new String();
	    horizon = String.valueOf(h);
	    
		re.assign("rVector", data);//VERY IMPORTANT!!! 
		re.assign("fh", horizon);

		if (firstcall3==true) {
			re.eval("model1 <- forecast::auto.arima(rVector)");
			
			re.eval("p3 <-model1$arma[1]");
			re.eval("d3 <-model1$arma[6]");
			re.eval("q3 <-model1$arma[2]");
		
			firstcall3 = false;
			
		}else {
			re.eval("model1 <- forecast::Arima(rVector, order=c(p3,d3,q3))"); //choice of hyper-parameters for ARIMA
		}
			
		//Non-parametric forecasting result
	    re.eval("x1<-ssa(rVector, force.decompose = TRUE) ");
	    re.eval("x2 <- forecast(x1, groups = list(1:6), method = \"recurrent\",  len = 100, drop=TRUE)");//vector, recurrent; bootstrap = TRUE
	    re.eval("x<- x2$mean");
	    
	    //ARIMA forecasting result
	    re.eval("model1 <- arima(rVector, order=c(2,1,3),method = \"CSS\")");  //choice of hyper-parameters for ARIMA: method = \"CSS\"
	    re.eval("y<- as.numeric(forecast(model1,fh)$mean)"); 
	    
	    //Combined forecasting result
	    re.eval("z<- (x+y)/2"); //equally weighted on both forecast results
	    
	    
	    REXP fcast = re.eval("z");
	    
	    for (double m : fcast.asDoubleArray()) {
			output.add(m);
		}
	    
	    return output;
		
	}
	
	/*
	 * Method4: arimaFittingAndPrediction4
	 */
	public static ArrayList<Double> arimaFittingAndPrediction4(Rengine re, double[] data, int h) { 
		
		ArrayList<Double> output = new ArrayList<Double>() ;
	    
	    String horizon = new String();
	    horizon = String.valueOf(h);
	    
		re.assign("rVector", data);//VERY IMPORTANT!!! 
		re.assign("fh", horizon);

		if (firstcall4==true) {
			re.eval("model1 <- forecast::auto.arima(rVector)");
			
			re.eval("p4 <-model1$arma[1]");
			re.eval("d4 <-model1$arma[6]");
			re.eval("q4 <-model1$arma[2]");
			
			firstcall4 = false;
			
		}else {
			re.eval("model1 <- forecast::Arima(rVector, order=c(p4,d4,q4))"); //choice of hyper-parameters for ARIMA
		}
			
		//Non-parametric forecasting result
	    re.eval("x1<-ssa(rVector, force.decompose = TRUE) ");
	    re.eval("x2 <- forecast(x1, groups = list(1:6), method = \"recurrent\",  len = 100, drop=TRUE)");//vector, recurrent; bootstrap = TRUE
	    re.eval("x<- x2$mean");
	    
	    //ARIMA forecasting result
	    re.eval("model1 <- arima(rVector, order=c(2,1,3),method = \"CSS\")");  //choice of hyper-parameters for ARIMA: method = \"CSS\"
	    re.eval("y<- as.numeric(forecast(model1,fh)$mean)"); 
	    
	    //Combined forecasting result
	    re.eval("z<- (x+y)/2"); //equally weighted on both forecast results
	    
	    
	    REXP fcast = re.eval("z");
	    
	    for (double m : fcast.asDoubleArray()) {
			output.add(m);
		}
	    
	    return output;
		
	}
	
	/*
	 * Method5: arimaFittingAndPrediction5
	 */
	public static ArrayList<Double> arimaFittingAndPrediction5(Rengine re, double[] data, int h) { 
		
		ArrayList<Double> output = new ArrayList<Double>() ;
	    
	    String horizon = new String();
	    horizon = String.valueOf(h);
	    
		re.assign("rVector", data);//VERY IMPORTANT!!! 
		re.assign("fh", horizon);


		if (firstcall5==true) {
			re.eval("model1 <- forecast::auto.arima(rVector)");
			
			re.eval("p5 <-model1$arma[1]");
			re.eval("d5 <-model1$arma[6]");
			re.eval("q5 <-model1$arma[2]");
			
			firstcall5 = false;
			
		}else {
			re.eval("model1 <- forecast::Arima(rVector, order=c(p5,d5,q5))");  //choice of hyper-parameters for ARIMA
		}
			
		//Non-parametric forecasting result
	    re.eval("x1<-ssa(rVector, force.decompose = TRUE) ");
	    re.eval("x2 <- forecast(x1, groups = list(1:6), method = \"recurrent\",  len = 100, drop=TRUE)");//vector, recurrent; bootstrap = TRUE
	    re.eval("x<- x2$mean");
	    
	    //ARIMA forecasting result
	    re.eval("model1 <- arima(rVector, order=c(2,1,3),method = \"CSS\")");  //choice of hyper-parameters for ARIMA: method = \"CSS\"
	    re.eval("y<- as.numeric(forecast(model1,fh)$mean)"); 
	    
	    //Combined forecasting result
	    re.eval("z<- (x+y)/2"); //equally weighted on both forecast results
	    
	    
	    REXP fcast = re.eval("y"); //only Saa result
	    
	    for (double m : fcast.asDoubleArray()) {
			output.add(m);
		}
	    
	    return output;
		
	}

	/*
	 * Get and set methods
	 */


	public ArrayList<Double> getPredicted_X() {
		return predicted_X;
	}


	public void setPredicted_X(ArrayList<Double> predicted_X) {
		this.predicted_X = predicted_X;
	}


	public ArrayList<Double> getPredicted_Y() {
		return predicted_Y;
	}


	public void setPredicted_Y(ArrayList<Double> predicted_Y) {
		this.predicted_Y = predicted_Y;
	}


	public ArrayList<Double> getPredicted_L1() {
		return predicted_L1;
	}


	public void setPredicted_L1(ArrayList<Double> predicted_L1) {
		this.predicted_L1 = predicted_L1;
	}


	public ArrayList<Double> getPredicted_L2() {
		return predicted_L2;
	}


	public void setPredicted_L2(ArrayList<Double> predicted_L2) {
		this.predicted_L2 = predicted_L2;
	}


	public ArrayList<Double> getPredicted_Ang() {
		return predicted_Ang;
	}


	public void setPredicted_Ang(ArrayList<Double> predicted_Ang) {
		this.predicted_Ang = predicted_Ang;
	}


	
	
}
