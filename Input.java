import processing.core.*;
import controlP5.*;
import java.util.*;
import java.awt.geom.*;

public class Input extends PApplet{
	final int WIDTH =1280,HEIGHT=1024;
	public void setup(){
		//don't use variables for this of export fails
		size(1280,1024);
		background(255);
		controlP5 = new ControlP5(this);
		switchColor = controlP5.addButton("switchColor", 0, 95, 15, 65, 35);
		switchColor.setColorActive(0xffB84860);
		switchColor.setColorBackground(0xff780821);
		switchColor.update();

		start = controlP5.addButton("start", 0, 15, 15, 75, 35);
		start.setColorActive(0xffB84860);
		start.setColorBackground(0xff780821);
		start.update();
		red = new ArrayList<Point>();
		blue = new ArrayList<Point>();
		buttonsActive = true;
		
		trans = new AffineTransform();
		trans.scale(10./WIDTH, 10./HEIGHT);
		trans.translate(-WIDTH/2., -HEIGHT/2.);
	}
	boolean buttonsActive = false;
	ArrayList<Point> red;
	ArrayList<Point> blue;
	ControlP5 controlP5;
	Button switchColor;
	Button start;
	boolean drawMedian = true;
	Algo alg;

	public enum Mode {INPUT,RUNNING,DONE};
	Mode mode = Mode.INPUT;

	AffineTransform trans;
	
	public void draw(){
		if(mode == Mode.INPUT)
		{
			for(Point r:red)
				drawPoint(r,255,0,0);		
			for(Point b:blue)
				drawPoint(b,0,0,255);
		}else if(mode == Mode.RUNNING){
			for(Line l:alg.LA)
				drawLine(l,255,0,0,1);
			for(Line l:alg.LB)
				drawLine(l,0,0,255,1);
			for(Line l:alg.G1)
				drawLine(l,alg.reversed?0:255,0,alg.reversed?255:0,3);
			for(Line l:alg.G2)
				drawLine(l,alg.reversed?255:0,0,alg.reversed?0:255,3);
			double lx = alg.lx;
			double hx = alg.hx;
			Point pleft = new Point(lx,0);
			Point pleftT = inverseTransform(pleft);
			Line left;
			if(pleftT.x < 0)
				left = new Line(transform(new Point(3,0)),transform(new Point(3,1)));
			else
				left = new Line(lx,0,lx,1);
			drawVertLine(left,0,0,0,4);
			
			Point pright = new Point(hx,0);
			Point prightT = inverseTransform(pright);
			Line right;
			if(prightT.x >= WIDTH)
				right = new Line(transform(new Point(WIDTH-3,0)),transform(new Point(WIDTH-3,1)));
			else
				right = new Line(hx,0,hx,1);		
			drawVertLine(right,0,0,0,4);
			
			if(drawMedian){
				if(!alg.reversed){
					drawMedian(alg.G1,alg.p1,252,188,104);
					drawMedian(alg.G2,alg.p2,169,62,250);
				}else{
					drawMedian(alg.G1,alg.p1,169,62,250);
					drawMedian(alg.G2,alg.p2,252,188,104);
				}				
			}
			
			if(alg.done)
				drawPoint(alg.ans,0,0,0);			
			
		}else{
			for(Point r:red)
				drawPoint(r,255,0,0);		
			for(Point b:blue)
				drawPoint(b,0,0,255);
			Line ans = Algo.pointToLine(alg.ans);
			drawLine(ans,0,0,0,5);
		}
	}
	
	private void drawMedian(ArrayList<Line> L, int level,int r, int g, int b) {
		if(L.size() == 0)
			return;
		TreeSet<Double> X = new TreeSet<Double>();
		X.add( 100000.);
		X.add(-100000.);
		for(int i = 0; i < L.size();i++)
			for(int j = i+1; j < L.size();j++){
				Point p = L.get(i).interLine(L.get(j));
				if( p == null)
					continue;
				X.add(p.x);
			}
		double prevx = Double.NaN;
		double prevy = 0;
		for(double x:X){
			if(!Double.isNaN(prevx)){
				drawLineSegment(new Line(prevx,prevy,x,Algo.findKth(L,level,x)),r,g,b,5);
			}
			prevx = x;
			prevy = Algo.findKth(L, level, x);
		}
	}

	private void drawVertLine(Line l, int r, int g, int b, int weight) {
		stroke(r,g,b);
		strokeWeight(weight);
		fill(r,g,b);
		Point p = l.interLine(new Line(0,0,1,0));
		double x = p.x;
		Point la = new Point(x,-HEIGHT);
		Point lb = new Point(x,HEIGHT);
		Point laT = inverseTransform(la);
		Point lbT = inverseTransform(lb);
		line((int)laT.x,(int)laT.y,(int)lbT.x,(int)lbT.y);		
	}
	public void drawLine(Line l,int r, int g, int b, int weight){
		stroke(r,g,b);
		strokeWeight(weight);
		fill(r,g,b);
		Point la = new Point(-WIDTH,l.valueAt(-WIDTH));
		Point lb = new Point(WIDTH,l.valueAt(WIDTH));
		Point laT = inverseTransform(la);
		Point lbT = inverseTransform(lb);
		line((int)laT.x,(int)laT.y,(int)lbT.x,(int)lbT.y);
	}
	private void drawLineSegment(Line l, int r, int g, int b, int weight) {
		stroke(r,g,b);
		strokeWeight(weight);
		fill(r,g,b);
		Point la = l.a;
		Point lb = l.b;
		Point laT = inverseTransform(la);
		Point lbT = inverseTransform(lb);
		line((int)laT.x,(int)laT.y,(int)lbT.x,(int)lbT.y);
		
	}
	public void drawPoint(Point p, int r, int g, int b){
		Point pT = inverseTransform(p);
		stroke(r,g,b);
		strokeWeight(1);
		fill(r,g,b);
		ellipseMode(CENTER);
		ellipse((int)pT.x,(int)pT.y,10,10);
	}

	boolean drawingRed = true;
	public void mousePressed(){
		if(mode == Mode.INPUT){
			if(switchColor.isInside())
				return;
			if(start.isInside())
				return;

			System.out.println("("+mouseX+","+mouseY+")");
			
			Point p = transform(new Point(mouseX,mouseY));
			System.out.format("(%.3f,%.3f)\n",p.x,p.y);
			if(drawingRed)
				red.add(p);
			else
				blue.add(p);
		}else{
			
		}
	}
	private Point transform(Point p) {
		Point2D temp = trans.transform(p, null);
		return new Point(temp.getX(),temp.getY());
	}
	private Point inverseTransform(Point p){
		Point2D temp;
		try {
			temp = trans.inverseTransform(p, null);
			return new Point(temp.getX(),temp.getY());
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return null;
	}
	public void switchColor(int value){
		if(!buttonsActive)
			return;
		System.out.println("SWITCH");
		drawingRed = !drawingRed;
		if(drawingRed){
			switchColor.setColorActive(0xffB84860);
			switchColor.setColorBackground(0xff780821);
		}else{
			switchColor.setColorActive(0xff525CAB);
			switchColor.setColorBackground(0xff051173);
		}
	}
	public void start(int value){
		System.out.println("START CLICK");
		if(!buttonsActive)
			return;
		System.out.println("START");
		if(mode == Mode.INPUT){
			System.out.println("END OF INPUT");
			mode = Mode.RUNNING;
			buttonsActive = false;
			start.setLabel("Step algorithm");
			switchColor.setVisible(false);
			switchColor.update();

			alg = new Algo(red,blue);
			clear();
			buttonsActive = true;
		}else if(mode == Mode.RUNNING && !alg.done){
			System.out.println("STEP");
			clear();
			try{
				alg.step();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}else if(mode == Mode.RUNNING && alg.done){
			System.out.println("DONE!");
			mode = Mode.DONE;
			buttonsActive = false;
			start.setVisible(false);
			start.update();
			clear();
			buttonsActive = true;
		}
		System.out.println("RETURN START");
	}
	public void clear(){
		stroke(255,255,255);
		fill(255,255,255);
		rect(0, 0, WIDTH, HEIGHT);
	}
}
