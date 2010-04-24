
import static java.lang.Math.*;
import java.awt.geom.Line2D;

public class Line extends Line2D.Double{
	public Point a,b;
	static double eps = 1e-6;
	public Line(Point a, Point b){
		super(a,b);
		this.a = a;
		this.b = b;
	}
	public Line(double x1, double y1, double x2, double y2){
		this(new Point(x1,y1),new Point(x2,y2));
	}
	public boolean onSeg(Point p){
		return this.ptSegDist(p) < eps;
	}
	//very numerically imprecise
	public Point interLine(Line l){
		double a1 = b.y-a.y;
		double b1 = a.x-b.x;
		double c1 = a1*a.x+b1*a.y;
		
		double a2 = l.b.y-l.a.y;
		double b2 = l.a.x-l.b.x;
		double c2 = a2*l.a.x+b2*l.a.y;
		
		double det = a1*b2-a2*b1;
		if(abs(det) < 1e-9)
		{
			// Attempt to find a point on both line segments so the interSeg code is simpler
			if(l.onSeg(a)) return a;
			if(l.onSeg(b)) return b;
			if(onSeg(l.a)) return l.a;
			if(onSeg(l.b)) return l.b;
			return a;
		}else{
			return new Point((b2*c1 - b1*c2)/det,(a1*c2 - a2*c1)/det);
		}
	}
	public Point interSeg(Line l){
		Point p = interLine(l);
		if(onSeg(p) && l.onSeg(p))
			return p;
		return null;
	}
	public Line perp(Point p){
		Point delta = b.sub(a).perp();
		return new Line(p,p.add(delta));
	}
	public double valueAt(double x){
		Point p = interLine(new Line(new Point(x,0),new Point(x,1)));
		if(p == null)
			throw new RuntimeException();
		return p.y;
	}
}