import processing.core.*;
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

		trans = new AffineTransform();
		trans.scale(10./WIDTH, 10./HEIGHT);
		trans.translate(-WIDTH/2., -HEIGHT/2.);


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

	int[][] DEBUGR = null;//{{640,379},{1026,521},{788,797},{512,755},{382,516},{705,375},{671,663},{368,674},{648,884},{977,569},{673,415},{547,370},};
	int[][] DEBUGB = null;//{{635,210},{319,405},{462,659},{731,738},{539,498},{406,481},{667,480},{244,362},{619,474},{485,636},{850,779},{1111,572},{811,410},{555,425},{361,627},};

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
