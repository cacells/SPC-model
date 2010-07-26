/*
 * This is a class that contains main and
 * will call the TAGrid simulation, also displaying the results 
 * Graphically in a window
 * TA is for Transit Amplifying
 */


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.util.*;
 
public class TAStaticBatch extends JFrame implements Runnable {
	
	int nruns = 50;
	int celltypes = 6;
	int replicates =20; // number of replicates for each run
    int maxIters = 100; // number of experiment iterations per replicate
	
    double typesSum[][] = new double[nruns][celltypes];
    double typesSqrSum[][] = new double[nruns][celltypes];
	double typesProlifSum[][] = new double[nruns][celltypes];
    double typesProlifSqrSum[][] = new double[nruns][celltypes];
	double stasisSum[][] = new double[nruns][celltypes];
    double stasisSqrSum[][] = new double[nruns][celltypes];
	double stainSum[][] = new double[nruns][celltypes];
    double stainSqrSum[][] = new double[nruns][celltypes];
	double retainSum[][] = new double[nruns][celltypes];
    double retainSqrSum[][] = new double[nruns][celltypes];
	double proliferationSum[] = new double[nruns];
	double proliferationSqrSum[] = new double[nruns];

	double frac; // calculate fraction of SC

	static String fileprefix = "test";

    TAGridStatic experiment;
    Random rand = new Random();    
	Thread runner;
    Container mainWindow;
	CAImagePanel CApicture;
	Image backImg1;
	Graphics backGr1;
	Image backImg2;
	Graphics backGr2;
	JProgressBar progressBarIt,progressBarExp,progressBarRep;
	JTextField progressMsgIt,progressMsgExp,progressMsgRep,fracMsg,fracVal;
	JPanel buttonHolderlow;
	
	int scale = 20;//beth: could set to 1. Makes the colour transitions better?
	int border = 20;
	int iterations;
	int gSize;
    Colour palette = new Colour();
	int[] colorindices = {45,1,5,4,2,54};//{0,1,2,54,4,5};

//    Color[] colours = {Color.white,Color.black,Color.green,Color.blue,Color.yellow,Color.red,Color.pink};
    Color[] javaColours;
    double[][] epsColours;
    boolean writeImages = false;


	public TAStaticBatch(int size) {
		//if size ne 64 we are in trouble
	    gSize=size;
		setVisible(true);
		backImg1 = createImage(scale * size, scale * size);
		backGr1 = backImg1.getGraphics();
		backImg2 = createImage(scale * size, scale * size);
		backGr2 = backImg2.getGraphics();
		setpalette();
		
	    int wscale = 6;//scale for main panel
	    int btnHeight = 480-384;//found by trial and error - must be a better way!
	    //although no buttons yet
	    int wh = (gSize*1)*wscale + 2*border;// +btnHeight;//mainWindow height
	    int ww = (gSize*2)*wscale + 3*border;//mainWindow width   
	    
		mainWindow = getContentPane();
		mainWindow.setLayout(new BorderLayout());
		setSize(ww,wh+btnHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
        CApicture = new CAImagePanel(ww,wh);
        CApicture.setBorder(border);
        CApicture.rowstoShow = gSize;
        mainWindow.add(CApicture,BorderLayout.CENTER);
		setVisible(true);
		
		buttonHolderlow = new JPanel();
		buttonHolderlow.setLayout(new GridLayout(2,4,10,1));
		
	    progressBarExp = new JProgressBar(0,nruns);
	    progressBarExp.setValue(0);
	    progressBarExp.setStringPainted(true);
	    
	    progressBarIt = new JProgressBar(0,maxIters);
	    progressBarIt.setValue(0);
	    progressBarIt.setStringPainted(true);   
	    
	    progressBarRep = new JProgressBar(0,replicates);
	    progressBarRep.setValue(0);
	    progressBarRep.setStringPainted(true);
	    
        progressMsgIt = new JTextField("Iterations % of "+maxIters);
        progressMsgIt.setEditable(false);
	    
        progressMsgExp = new JTextField("Experiments % of "+nruns);
        progressMsgExp.setEditable(false);
	    
        progressMsgRep = new JTextField("Replicates % of "+replicates);
        progressMsgRep.setEditable(false);
	    
        fracMsg = new JTextField("SC fraction");
        fracMsg.setEditable(false);

        
        frac = 1.0/(double)maxIters;
        fracVal = new JTextField(""+frac);
        fracVal.setEditable(false);

	    
        progressMsgRep = new JTextField("Replicates finished out of "+replicates);
        progressMsgRep.setEditable(false);
        
	    buttonHolderlow.add(progressMsgIt);
	    buttonHolderlow.add(progressMsgRep);
	    buttonHolderlow.add(progressMsgExp);
	    buttonHolderlow.add(fracMsg);

	    buttonHolderlow.add(progressBarIt);
	    buttonHolderlow.add(progressBarRep);
	    buttonHolderlow.add(progressBarExp);
	    buttonHolderlow.add(fracVal);
	    
        fracMsg.setVisible(false);
        fracVal.setVisible(false);
	    
	    mainWindow.add(buttonHolderlow, BorderLayout.SOUTH);
		setVisible(true);
		
		
	}
	//new ones
	public void drawCA() {
		int a;
		for (TACell c : experiment.tissue){
			a = c.type;
			CApicture.drawCircleAt(c.home.x, c.home.y, javaColours[a], 1);
		}
	    CApicture.updateGraphic();
	}
	public void drawCAstain() {
		double cstain;
		for (TACell c : experiment.tissue){
			//if ((c.stain < minstain) && (c.type>0)) minstain = c.stain;
			cstain = c.stain;
			if (c.type==1) 
				CApicture.drawCircleAt2(c.home.x, c.home.y, palette.Javashades(cstain), 2);
			else 
				CApicture.drawCircleAt(c.home.x, c.home.y, palette.Javashades(cstain), 2);
		}
	    //outputImage();
	    CApicture.updateGraphic();
	}


	public void initialise(){
			CApicture.setScale(gSize,gSize,scale,gSize,gSize,scale);
      	    CApicture.clearCAPanel(1);
      	    CApicture.clearCAPanel(2);
      	    CApicture.clearParent();
		    iterations=0;
	}
	
	
	public void start() {
		initialise();
		if (runner == null) {
			runner = new Thread(this);
		}
		runner.start();
	}


	public void run() {
		if (runner == Thread.currentThread()) {
			for(int exp=0; exp<nruns; exp++){// 50 sets of  experiments
				System.out.print(exp);
				for(int r=0; r<replicates; r++){
					progressBarRep.setValue(r+1);
					setOfIterations(exp,r); 
					System.out.print("["+r+"]");
				}
				progressBarExp.setValue(exp+1);
		        fracMsg.setVisible(true);
		        fracVal.setVisible(true);
				drawCA();
				drawCAstain();
				fracVal.setText(""+frac);
				if (writeImages) CApicture.writeImage(exp);
			}
			outputData();
		}
	}
	

	public void setOfIterations(int exp,int rep){
		double  avProliferations;
		double avTypes;
		double avTypeProliferations;
		double avStains;
		

		frac = (double)(exp+1)/(double)(maxIters); // calculate fraction of SC


		experiment = new TAGridStatic(64, celltypes-2, frac,false);//new experiment
		
		progressBarIt.setValue(0);
		for(iterations=0; iterations<(maxIters-1); iterations++){
			progressBarIt.setValue(iterations+1);
			if(iterations==89)experiment.stain();// stain all cells 10 iterations before end
			experiment.iterate();

		}
        //count things on the last iteration
		experiment.iterateandcount();
		//finished counting for all cells
		avProliferations = (double) TACell.totalproliferations/(64.0*64.0);
		proliferationSum[exp]+=avProliferations; // add average to the array of experiments
		proliferationSqrSum[exp]+=(avProliferations*avProliferations); // add standard deviations to the array of experiments

		for(int i=0; i<celltypes; i++){
			if(TACell.cellcounts[i]>0){
				avTypeProliferations = (double)(TACell.prolifcounts[i])/(TACell.cellcounts[i]*1.0);
			}else{
				avTypeProliferations=0.0;
			}
			avTypes = (double)(TACell.cellcounts[i])/(64.0*64.0);
			avStains = (TACell.stainsums[i])/(64.0*64.0);
			// again add results to the array of experiments
			stainSum[exp][i]+=avStains;
			stainSqrSum[exp][i]+=(avStains*avStains);
			
			typesSum[exp][i]+=avTypes;
			typesSqrSum[exp][i]+=(avTypes*avTypes);
			
			typesProlifSum[exp][i]+=avTypeProliferations;
			typesProlifSqrSum[exp][i]+=(avTypeProliferations*avTypeProliferations);
		}

		
	}//end set of iterations

    public double getMean(double sum) {
		 return sum / replicates;
	}
	
	
	public double getStandardDeviationGvnMean(double mean, double sqrSum) {
			double dev = (sqrSum / replicates) - (mean * mean);
			if(dev>0){
	       	return Math.sqrt(dev);
			}
			return 0.0;
	   }
	
	public void outputData(){
		double mean;
		// Loop through all of the array of experiments and print them out 
		try{
			//creates the data files for the experiments
			BufferedWriter bufProlif = new BufferedWriter(new FileWriter(fileprefix+"Proliferation.txt"));
			BufferedWriter bufTypes = new BufferedWriter(new FileWriter(fileprefix+"Types.txt"));
			BufferedWriter bufTypeProlif = new BufferedWriter(new FileWriter(fileprefix+"TypeProliferation.txt"));
			BufferedWriter bufStain = new BufferedWriter(new FileWriter(fileprefix+"StainProliferation.txt"));
			String[] headers = {"Space","SC","TA1","TA2","TA3","TA4","TA5"};
			double frac=0.0;
			bufProlif.write("SCFraction Av_proliferations stdev");
			bufProlif.newLine();
			for(int i=0; i< nruns; i++){
				frac = (double)((i+1)/(1.0*maxIters));
				mean = getMean(proliferationSum[i]);
				bufProlif.write(frac+" "+mean+" "+getStandardDeviationGvnMean(mean, proliferationSqrSum[i])+" ");
				bufProlif.newLine();	
			}
			bufProlif.newLine();
			bufProlif.newLine();
			bufProlif.close();
			for(int i=0; i<6; i++){
				bufTypes.write("SCFraction "+headers[i]+" sdtev");
				bufTypes.newLine();	
				bufTypeProlif.write("SCFraction "+headers[i]+" sdtev");
				bufTypeProlif.newLine();	
				bufStain.write("SCFraction "+headers[i]+" sdtev");
				bufStain.newLine();	
				for(int j=0; j< nruns; j++){
					frac = (double)((j+1)/(1.0*maxIters));
					mean = getMean(typesSum[j][i]);
					bufTypes.write(frac+" "+mean+" "+getStandardDeviationGvnMean(mean,typesSqrSum[j][i])+" ");
					bufTypes.newLine();	
					mean = getMean(typesProlifSum[j][i]);
					bufTypeProlif.write(frac+" "+mean+" "+getStandardDeviationGvnMean(mean,typesProlifSqrSum[j][i])+" ");
					bufTypeProlif.newLine();
					mean = getMean(stainSum[j][i]);
					bufStain.write(frac+" "+mean+" "+getStandardDeviationGvnMean(mean,stainSqrSum[j][i])+" ");
					bufStain.newLine();	
				}
				bufTypes.newLine();	
				bufTypes.newLine();
				bufTypeProlif.newLine();	
				bufTypeProlif.newLine();
				bufStain.newLine();	
				bufStain.newLine();
			}
			bufTypes.close();
			bufTypeProlif.close();
			bufStain.close();
		}catch(IOException e){
		}
	}
	
	public void postscriptPrint(String fileName) {
		int xx;
		int yy;
		int state;
		boolean flag;
		try {
			java.io.FileWriter file = new java.io.FileWriter(fileName);
			java.io.BufferedWriter buffer = new java.io.BufferedWriter(file);
			System.out.println(fileName);
			buffer.write("%!PS-Adobe-2.0 EPSF-2.0");
			buffer.newLine();
			buffer.write("%%Title: test.eps");
			buffer.newLine();
			buffer.write("%%Creator: gnuplot 4.2 patchlevel 4");
			buffer.newLine();
			buffer.write("%%CreationDate: Thu Jun  4 14:16:00 2009");
			buffer.newLine();
			buffer.write("%%DocumentFonts: (atend)");
			buffer.newLine();
			buffer.write("%%BoundingBox: 0 0 300 300");
			buffer.newLine();
			buffer.write("%%EndComments");
			buffer.newLine();
			for (TACell c : experiment.tissue){
				if(c.type>0){
					xx = (c.home.x * 4) + 20;
					yy = (c.home.y * 4) + 20;
					if (c.proliferated) {
						buffer.write("newpath " + xx + " " + yy + " 1.5 0 360 arc fill\n");
						buffer.write("0 setgray\n");
						buffer.write("newpath " + xx + " " + yy + " 1.5 0 360 arc  stroke\n");
					} else {
						buffer.write("0.75 setgray\n");
						buffer.write("newpath " + xx + " " + yy + " 1.5 0 360 arc fill\n");
					}
				}
			}
			buffer.write("showpage");
			buffer.newLine();
			buffer.write("%%Trailer");
			buffer.newLine();
			buffer.write("%%DocumentFonts: Helvetica");
			buffer.newLine();
			buffer.close();
		} catch (java.io.IOException e) {
			System.out.println(e.toString());
		}
	}
    public void setpalette(){
    	int ind = colorindices.length;
    	javaColours = new Color[ind];
    	epsColours = new double[ind][3];
    	for (int i=0;i<ind;i++){
    		//System.out.println("color index "+colorindices[i]);
    		javaColours[i] = palette.chooseJavaColour(colorindices[i]);
    		epsColours[i] = palette.chooseEPSColour(colorindices[i]);
    	}
    }

	public static void main(String args[]) {
		double initalSeed = 0.1;
		if(args.length>0){
			fileprefix=args[0];
			TAStaticBatch s = new TAStaticBatch(64);
			s.start();
		}else{
			TAStaticBatch s = new TAStaticBatch(64);
			s.start();
		}
	}
}

