/*
 * This is a class that contains main and
 * will call the TAGrid simulation, also displaying the results 
 * Graphically in a window
 * TA is for Transit Amplifying
 */


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.util.*;
 
public class TAStatic extends JFrame implements Runnable {

    TAGridStatic experiment;
    Random rand = new Random();    
	Thread runner;
    Container mainWindow;
	CAImagePanel CApicture;
	Image backImg1;
	Graphics backGr1;
	Image backImg2;
	Graphics backGr2;
	JProgressBar progressBar;
	int scale = 20;//beth: could set to 1. Makes the colour transitions better?
	int border = 20;
	int iterations;
	int gSize;
    Colour palette = new Colour();
	int[] colorindices = {45,1,5,4,2,54};//{0,1,2,54,4,5};
	int nnw = colorindices.length-1;
//    Color[] colours = {Color.white,Color.black,Color.green,Color.blue,Color.yellow,Color.red,Color.pink};
    Color[] javaColours;
    double[][] epsColours;
    Color[] colours = {Color.black,Color.white,Color.green,Color.blue,Color.yellow,Color.red,Color.pink};
    boolean writeImages = false;
    int maxIters = 100;

	public TAStatic(int size, int maxC, double frac) {
	    gSize=size;
		experiment = new TAGridStatic(size, maxC, frac,false);
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
		setSize(ww,wh);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
        CApicture = new CAImagePanel(ww,wh);
        CApicture.setBorder(border);
        CApicture.rowstoShow = gSize;
        mainWindow.add(CApicture,BorderLayout.CENTER);
		setVisible(true);
	    progressBar = new JProgressBar(JProgressBar.VERTICAL,0,maxIters-1);
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    mainWindow.add(progressBar, BorderLayout.EAST);
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
			for(iterations=0; iterations<100; iterations++){
				if(iterations==89)experiment.stain();// stain all cells 10 iterations before end
				experiment.iterate();
				progressBar.setValue(iterations);
				if (iterations%2==0){
					drawCA();
					drawCAstain();
					if (writeImages) CApicture.writeImage(iterations);
				}
				//if((iterations%5)==0)postscriptPrint("TA"+iterations+".eps");
				// This will produce a postscript output of the tissue
			}
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
			initalSeed = Double.parseDouble(args[0]);
			TAStatic s = new TAStatic(64, 4, initalSeed);
			s.start();
		}else{
			TAStatic s = new TAStatic(64, 4, 0.10);
			s.start();
		}
	}
}

