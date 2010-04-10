import processing.core.*;
import controlP5.*;
import java.util.*;
import java.awt.geom.*;

public class Input extends PApplet{
	public void setup(){
		size(500,500);
		background(255);
		controlP5 = new ControlP5(this);
		switchColor = controlP5.addButton("switchColor", 0, 15, 15, 65, 35);
		switchColor.valueLabel().setControlFontSize(200);
		switchColor.setColorActive(0xffB84860);
		switchColor.setColorBackground(0xff780821);
		switchColor.update();
		red = new ArrayList<Point2D>();
		blue = new ArrayList<Point2D>();

	}
	ArrayList<Point2D> red;
	ArrayList<Point2D> blue;
	ControlP5 controlP5;
	Button switchColor;
		
	public void draw(){
		for(Point2D r:red){
			stroke(255,0,0);
			fill(255,0,0);
			ellipseMode(CENTER);
			ellipse((int)r.getX(),(int)r.getY(),10,10);
		}
		for(Point2D b:blue){
			stroke(0,0,255);
			fill(0,0,255);
			ellipseMode(CENTER);
			ellipse((int)b.getX(),(int)b.getY(),10,10);
		}
	}
	boolean drawingRed = true;
	public void mousePressed(){
		if(switchColor.isInside())
			return;
		
		int r = drawingRed ? 255:0;
		int b = drawingRed ? 0:255;
		stroke(r,0,b);
		fill(r,0,b);
		ellipseMode(CENTER);
		ellipse(mouseX,mouseY,10,10);
		if(drawingRed)
			red.add(new Point2D.Double(mouseX,mouseY));
		else
			blue.add(new Point2D.Double(mouseX,mouseY));
	}
	public void switchColor(int value){
		drawingRed = !drawingRed;
		if(drawingRed){
			switchColor.setColorActive(0xffB84860);
			switchColor.setColorBackground(0xff780821);
		}else{
			switchColor.setColorActive(0xff525CAB);
			switchColor.setColorBackground(0xff051173);
		}
	}
}
