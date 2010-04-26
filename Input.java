import processing.core.*;
import static java.lang.Math.*;
import controlP5.*;
import java.util.*;
import java.awt.geom.*;

public class Input extends PApplet{
	final int WIDTH =800,HEIGHT=800;
	final int CONTROL = 75;
	public void setup(){
		//don't use variables for this of export fails
		size(800,800);
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

		current = new AffineTransform();
		current.preConcatenate(AffineTransform.getTranslateInstance(-WIDTH/2., -HEIGHT/2.));
		current.preConcatenate(AffineTransform.getScaleInstance(10./WIDTH, 10./HEIGHT));
		original = current;		

		if(DEBUGR != null){
			for(int[] q:DEBUGR)
				red.add(transform(new Point(q[0],q[1])));
		}
		if(DEBUGB != null){
			for(int[] q:DEBUGB)
				blue.add(transform(new Point(q[0],q[1])));
		}
		font = createFont("Times New Roman",16);
	}
	boolean buttonsActive = false;
	ArrayList<Point> red;
	ArrayList<Point> blue;
	ControlP5 controlP5;
	Button switchColor;
	Button start;
	boolean drawMedian = true;
	Algo alg;

	int[][] DEBUGR = null;//{{475,218},{469,625},{296,534},{338,399},{454,448},{682,383},};
	int[][] DEBUGB = null;//{{408,448},{495,348},{290,276},{171,455},{571,513},};

	public enum Mode {INPUT,RUNNING,DONE};
	Mode mode = Mode.INPUT;

	AffineTransform original;
	AffineTransform current;
	AffineTransform next;
	AffineTransform first;
	long startTime;
	long totalTime = 1000000000L;



	public void scale(){

		if(alg == null)
			return;
//		System.out.println("SCALING!");
		double lx = Double.MAX_VALUE;
		double hx = Double.MIN_VALUE;
		double ly = Double.MAX_VALUE;
		double hy = Double.MIN_VALUE;

		ArrayList<Line> allLines = new ArrayList<Line>();
		allLines.addAll(alg.G1);
		allLines.addAll(alg.G2);
		for(Line a:allLines){
			for(Line b:allLines){
				if(a == b)
					continue;
				Point p = a.interLine(b);
				if(p == null)
					continue;
				if(alg.lx <= p.x && p.x <= alg.hx){
					lx = Math.min(lx,p.x);
					hx = Math.max(hx,p.x);
					ly = Math.min(ly,p.y);
					hy = Math.max(hy,p.y);
				}
			}
		}
		if(alg.trap != null){
			for(Line l:alg.trap){
				if(l.a.x != -alg.HUGE && l.a.x != alg.HUGE){
					lx = Math.min(lx,l.a.x);
					hx = Math.max(hx,l.a.x);
					ly = Math.min(ly,l.a.y);
					hy = Math.max(hy,l.a.y);
				}
				if(l.b.x != -alg.HUGE && l.b.x != alg.HUGE){
					lx = Math.min(lx,l.b.x);
					hx = Math.max(hx,l.b.x);
					ly = Math.min(ly,l.b.y);
					hy = Math.max(hy,l.b.y);
				}
			}
		}
//		System.out.println(lx+" "+hx+" "+ly+" "+hy);
		lx = Math.max(lx,alg.lx);
		hx = Math.min(hx,alg.hx);
		double dx = hx-lx;
		lx -= dx/3;
		hx += dx/3;
		double dy = hy-ly;
		ly -= dy/3;
		hy += dy/3;

//		System.out.println(lx+" "+hx+" "+ly+" "+hy);
		AffineTransform update = new AffineTransform();
		update.preConcatenate(AffineTransform.getScaleInstance((hx-lx)/WIDTH,(hy-ly)/HEIGHT));
		update.preConcatenate(AffineTransform.getTranslateInstance(lx, ly));	

//		Point2D p = getCurrentTransform().transform(new Point2D.Double(400,400),null);
//		System.out.println("\t"+p.getX()+" "+p.getY());
//		p = getCurrentTransform().transform(new Point2D.Double(0,0),null);
//		System.out.println("\t"+p.getX()+" "+p.getY());
//		p = getCurrentTransform().transform(new Point2D.Double(800,800),null);
//		System.out.println("\t"+p.getX()+" "+p.getY());
//
//		System.out.println(Math.min(1,(System.nanoTime()-startTime)/(double)totalTime));


		current = getCurrentTransform();
		if(next == null)
			first = update;
		next = update;
		startTime = System.nanoTime();
//		System.out.println(Math.min(1,(System.nanoTime()-startTime)/(double)totalTime));
//		p = getCurrentTransform().transform(new Point2D.Double(400,400),null);
//		System.out.println("\t"+p.getX()+" "+p.getY());
//		p = getCurrentTransform().transform(new Point2D.Double(0,0),null);
//		System.out.println("\t"+p.getX()+" "+p.getY());
//		p = getCurrentTransform().transform(new Point2D.Double(800,800),null);
//		System.out.println("\t"+p.getX()+" "+p.getY());
//		System.out.println(Math.min(1,(System.nanoTime()-startTime)/(double)totalTime));
	}

	private AffineTransform getCurrentTransform() {
		if(next == null)
			return current;
		return weightedAverage(current,next,Math.min(1,(System.nanoTime()-startTime)/(double)totalTime));
	}
	private AffineTransform weightedAverage(AffineTransform A, AffineTransform B, double w) {
		double a = 1-w;
		double b = w;
		return new AffineTransform((a*A.getScaleX()+b*B.getScaleX()),0.0,0.0,(a*A.getScaleY()+b*B.getScaleY()),
				(a*A.getTranslateX()+b*B.getTranslateX()),(a*A.getTranslateY()+b*B.getTranslateY()));
	}

	int frames = 0;
	public void draw(){
		clear();
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
			drawVertLine(left,4,89,12,4);

			Point pright = new Point(hx,0);
			Point prightT = inverseTransform(pright);
			Line right;
			if(prightT.x >= WIDTH)
				right = new Line(transform(new Point(WIDTH-2,0)),transform(new Point(WIDTH-2,1)));
			else
				right = new Line(hx,0,hx,1);		
			drawVertLine(right,4,89,12,4);

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

			if(alg.drawTrap != null){
				stroke(61,191,0);
				strokeWeight(6);
				fill(61,191,0);

				Line top = new Line(inverseTransform(alg.drawTrap[0].a),inverseTransform(alg.drawTrap[0].b));
				Line bot = new Line(inverseTransform(alg.drawTrap[1].a),inverseTransform(alg.drawTrap[1].b));
				if(top.a.x < 0){
					Point p = top.interLine(new Line(0,0,0,1));
					top = new Line(p,top.b);
				}
				if(top.b.x >= WIDTH){
					Point p = top.interLine(new Line(WIDTH,0,WIDTH,1));
					top = new Line(top.a,p);
				}
				if(bot.a.x < 0){
					Point p = bot.interLine(new Line(0,0,0,1));
					bot = new Line(p,bot.b);
				}
				if(bot.b.x >= WIDTH){
					Point p = bot.interLine(new Line(WIDTH,0,WIDTH,1));
					bot = new Line(bot.a,p);
				}
				line((int)top.a.x,(int)top.a.y,(int)top.b.x,(int)top.b.y);
				line((int)bot.a.x,(int)bot.a.y,(int)bot.b.x,(int)bot.b.y);
				line((int)bot.a.x,(int)bot.a.y,(int)top.a.x,(int)top.a.y);
				line((int)top.b.x,(int)top.b.y,(int)bot.b.x,(int)bot.b.y);
			}

		}else{
			for(Point r:red)
				drawPoint(r,255,0,0);		
			for(Point b:blue)
				drawPoint(b,0,0,255);
			Line ans = Algo.pointToLine(alg.ans);
			drawLine(ans,0,0,0,5);
		}

		fill(255,255,255);
		stroke(255,255,255);
		strokeWeight(0);
		rect(0,0,WIDTH,CONTROL);

		stroke(0,0,0);
		strokeWeight(7);
		fill(0,0,0);
		line(0,CONTROL,WIDTH,CONTROL);

		if(alg != null){
			drawText(alg.message,0,0,0,200,5);
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
		smooth();
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
	public void drawText(String s, int r, int g, int b, int x, int y){
		textFont(font);
		fill(r,g,b);
		text(s,x,y,WIDTH-200,200);
	}
	PFont font;
	boolean drawingRed = true;
	public void mousePressed(){
		if(mode == Mode.INPUT){
			if(switchColor.isInside())
				return;
			if(start.isInside())
				return;
			if(mouseY <= CONTROL)
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
		Point2D temp = getCurrentTransform().transform(p, null);
		return new Point(temp.getX(),temp.getY());
	}
	private Point inverseTransform(Point p){
		Point2D temp;
		try {
			temp = getCurrentTransform().inverseTransform(p, null);
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
			System.out.println("****************************************");
			System.out.print("{");
			for(Point p:red){
				Point q = inverseTransform(p);
				System.out.print("{"+(int)q.x+","+(int)q.y+"},");
			}
			System.out.println("}");

			System.out.print("{");
			for(Point p:blue){
				Point q = inverseTransform(p);
				System.out.print("{"+(int)q.x+","+(int)q.y+"},");
			}
			System.out.println("}");
			System.out.println("****************************************");
		}else if(mode == Mode.RUNNING && !alg.done){
			scale();
			System.out.println("STEP");
			clear();
			try{
				alg.step();
			}catch(Exception e){
				e.printStackTrace();
			}
			if(alg.mode == Algo.Mode.COMPLETE){
				next = first;
				current = getCurrentTransform();
				startTime = System.nanoTime();
			}
		}else if(mode == Mode.RUNNING && alg.done){
			scale();
			System.out.println("DONE!");
			mode = Mode.DONE;
			buttonsActive = false;
			start.setVisible(false);
			start.update();
			clear();
			buttonsActive = true;
			current = original;
			next = null;
		}

		System.out.println("RETURN START");
	}
	public void clear(){
		stroke(255,255,255);
		fill(255,255,255);
		rect(0, 0, WIDTH, HEIGHT);
	}
}
