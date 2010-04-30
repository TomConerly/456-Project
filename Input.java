import processing.core.*;
import static java.lang.Math.*;
import controlP5.*;
import java.util.*;
import java.awt.geom.*;

public class Input extends PApplet{
	final int WIDTH =800,HEIGHT=700;
	final int CONTROL = 75;
	public void setup(){
		//don't use variables for this of export fails
		size(WIDTH,HEIGHT);
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
		
		defaultPoints = controlP5.addButton("defaultPoints", 0, WIDTH-100, 15, 75, 35);
		defaultPoints.setColorActive(0xffB84860);
		defaultPoints.setColorBackground(0xff780821);
		defaultPoints.update();
		
		red = new ArrayList<Point>();
		blue = new ArrayList<Point>();
		buttonsActive = true;

		current = new AffineTransform();
		current.preConcatenate(AffineTransform.getTranslateInstance(-WIDTH/2., -HEIGHT/2.));
		current.preConcatenate(AffineTransform.getScaleInstance(10./WIDTH, 10./HEIGHT));
		original = current;		

		font = createFont("Times New Roman",16);
	}
	boolean buttonsActive = false;
	ArrayList<Point> red;
	ArrayList<Point> blue;
	ControlP5 controlP5;
	Button switchColor;
	Button start;
	Button defaultPoints;
	boolean drawMedian = true;
	Algo alg;

	int[][] DEBUGR = {{479,217},{103,331},{239,617},{373,679},{732,399},{657,332},{300,406},{382,581},{588,609},};
	int[][] DEBUGB = {{431,204},{152,258},{63,499},{115,571},{556,406},{711,463},{582,647},{294,487},};

	public enum Mode {INPUT,RUNNING,DONE};
	Mode mode = Mode.INPUT;

	AffineTransform original;
	AffineTransform current;
	AffineTransform next;
	AffineTransform first;
	long startTime;
	long totalTime = 1000000000L;
	long zoomTime = 1000000000L;



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
		if(alg.drawTrap != null){
			for(Line l:alg.drawTrap){
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
		System.out.println(lx+" "+hx+" "+ly+" "+hy);
		lx = Math.max(lx,alg.lx);
		hx = Math.min(hx,alg.hx);
		double dx = hx-lx;
		lx -= dx/3;
		hx += dx/3;
		double dy = hy-ly;
		ly -= dy/3;
		hy += dy/3;

		AffineTransform update = new AffineTransform();
		update.preConcatenate(AffineTransform.getScaleInstance((hx-lx)/WIDTH,(hy-ly)/HEIGHT));
		update.preConcatenate(AffineTransform.getTranslateInstance(lx, ly));	

		current = getCurrentTransform();
		if(next == null)
			first = update;
		next = update;
		startTime = System.nanoTime();
		zoomTime = totalTime;
	}

	private AffineTransform getCurrentTransform() {
		if(next == null)
			return current;
		return weightedAverage(current,next,Math.min(1,(System.nanoTime()-startTime)/(double)zoomTime));
	}
	private AffineTransform weightedAverage(AffineTransform A, AffineTransform B, double w) {
		double a = 1-w;
		double b = w;
		return new AffineTransform((a*A.getScaleX()+b*B.getScaleX()),0.0,0.0,(a*A.getScaleY()+b*B.getScaleY()),
				(a*A.getTranslateX()+b*B.getTranslateX()),(a*A.getTranslateY()+b*B.getTranslateY()));
	}
	final int DEADLINEWIDTH=1;
	final int LINEWIDTH=3;
	final int MEDIANWIDTH = 8;
	final int MEDIANREDR = 166;
	final int MEDIANREDG = 13;
	final int MEDIANREDB = 46;
	final int MEDIANBLUER = 23;
	final int MEDIANBLUEG = 43;
	final int MEDIANBLUEB = 133;
	
	final int REDR = 255;
	final int REDG = 54;
	final int REDB = 64;
	final int BLUER = 43;
	final int BLUEG = 82;
	final int BLUEB = 255;
	
	final int XBOUNDARYWIDTH = 5;
	final int XBOUNDARYR = 4;
	final int XBOUNDARYG = 89;
	final int XBOUNDARYB = 12;
	
	final double GLOWTIME = 0.45;
	final int GLOWSIZE = 10;
	
	int frames = 0;
	public int glowWidth(double time){
		int width;
		if(time < GLOWTIME)
			width = LINEWIDTH + (int)(time/(GLOWTIME/GLOWSIZE));
		else
			width = LINEWIDTH + (int)(GLOWSIZE) - (int)((time-GLOWTIME)/(GLOWTIME/GLOWSIZE));
		width = max(width,DEADLINEWIDTH);
		return width;
	}
	public void draw(){
		clear();
		if(zoomTime == totalTime*25 && System.nanoTime() - startTime > totalTime*5){
			current = getCurrentTransform();
			startTime = System.nanoTime();
			zoomTime = totalTime*1;
		}
		if(mode == Mode.INPUT)
		{
			for(Point r:red)
				drawPoint(r,REDR,REDG,REDB,1);
			for(Point b:blue)
				drawPoint(b,BLUER,BLUEG,BLUEB,1);
		}else if(mode == Mode.RUNNING){
			for(Line l:alg.LA){
				int width = glowWidth((System.nanoTime()-l.remTime)/(double)totalTime);				
				drawLine(l,REDR,REDG,REDB,width);
			}
			for(Line l:alg.LB){
				int width = glowWidth((System.nanoTime()-l.remTime)/(double)totalTime);	
				drawLine(l,BLUER,BLUEG,BLUEB,width);
			}
			for(Line l:alg.G1)
				drawLine(l,alg.reversed?BLUER:REDR,alg.reversed?BLUEG:REDG,alg.reversed?BLUEB:REDB,LINEWIDTH);
			for(Line l:alg.G2)
				drawLine(l,alg.reversed?REDR:BLUER,alg.reversed?REDG:BLUEG,alg.reversed?REDB:BLUEB,LINEWIDTH);
			double lx = alg.lx;
			double hx = alg.hx;
			Point pleft = new Point(lx,0);
			Point pleftT = inverseTransform(pleft);
			Line left;
			if(pleftT.x < 0)
				left = new Line(transform(new Point(4,0)),transform(new Point(4,1)));
			else
				left = new Line(lx,0,lx,1);
			drawVertLine(left,XBOUNDARYR,XBOUNDARYG,XBOUNDARYB,XBOUNDARYWIDTH);

			Point pright = new Point(hx,0);
			Point prightT = inverseTransform(pright);
			Line right;
			if(prightT.x >= WIDTH)
				right = new Line(transform(new Point(WIDTH-4,0)),transform(new Point(WIDTH-4,1)));
			else
				right = new Line(hx,0,hx,1);		
			drawVertLine(right,XBOUNDARYR,XBOUNDARYG,XBOUNDARYB,XBOUNDARYWIDTH);

			if(drawMedian){
				if(!alg.reversed){
					drawMedian(alg.G1,alg.p1,MEDIANREDR,MEDIANREDG,MEDIANREDB,MEDIANWIDTH);
					drawMedian(alg.G2,alg.p2,MEDIANBLUER,MEDIANBLUEG,MEDIANBLUEB,MEDIANWIDTH);
					drawMedian(alg.G1,alg.p1,REDR,REDG,REDB,LINEWIDTH);
					drawMedian(alg.G2,alg.p2,BLUER,BLUEG,BLUEB,LINEWIDTH);
				}else{
					drawMedian(alg.G1,alg.p1,MEDIANBLUER,MEDIANBLUEG,MEDIANBLUEB,MEDIANWIDTH);
					drawMedian(alg.G2,alg.p2,MEDIANREDR,MEDIANREDG,MEDIANREDB, MEDIANWIDTH);
					drawMedian(alg.G1,alg.p1,BLUER,BLUEG,BLUEB,LINEWIDTH);
					drawMedian(alg.G2,alg.p2,REDR,REDG,REDB,LINEWIDTH);
				}				
			}
			
			if(alg.drawTrap != null){
				stroke(61,191,0);
				strokeWeight(5);
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
			if(alg.splitPoint != null){
				drawPoint(alg.splitPoint,0,0,0,3);
			}
			if(alg.done)
				drawPoint(alg.ans,0,0,0,3);		

		}else{
			for(Point r:red)
				drawPoint(r,255,0,0,1);		
			for(Point b:blue)
				drawPoint(b,0,0,255,1);
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

		if(alg != null && mode != Mode.DONE){
			drawText(alg.message,0,0,0,200,5);
		}else if(mode == Mode.DONE){
			drawText("Going back to the original points we get the ham sandwich cut.",0,0,0,200,5);
		}else if(mode == Mode.INPUT){
			drawText("Click to create points.",0,0,0,200,5);
		}
	}

	private void drawMedian(ArrayList<Line> L, int level,int r, int g, int b,int width) {
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
				drawLineSegment(new Line(prevx,prevy,x,Algo.findKth(L,level,x)),r,g,b,width);
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
	public void drawPoint(Point p, int r, int g, int b, int weight){
		Point pT = inverseTransform(p);
		stroke(r,g,b);
		strokeWeight(weight);
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
			if(defaultPoints.isInside())
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
	public void defaultPoints(int value){
		if(!buttonsActive)
			return;
		System.out.println("DEFAULT");
		if(DEBUGR != null){
			for(int[] q:DEBUGR)
				red.add(transform(new Point(q[0],q[1])));
			DEBUGR = null;
		}
		if(DEBUGB != null){
			for(int[] q:DEBUGB)
				blue.add(transform(new Point(q[0],q[1])));
			DEBUGB = null;
		}
		buttonsActive = false;
		defaultPoints.setVisible(false);
		defaultPoints.update();
		clear();
		buttonsActive = true;
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
			
			System.out.println("STEP");
			clear();
			try{
				alg.step();
			}catch(Exception e){
				e.printStackTrace();
			}
			if(alg.mode == Algo.Mode.COMPLETE){
				current = getCurrentTransform();
				next = first;
				startTime = System.nanoTime();
				zoomTime = totalTime*25;
			}else
				scale();
		}else if(mode == Mode.RUNNING && alg.done){			
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
