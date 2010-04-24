
import java.awt.geom.Point2D;
import static java.lang.Math.*;


public class Point extends Point2D.Double{
	static double eps = 1e-6;
	public Point(double x, double y){
		super(x,y);
	}
	public Point unit(){
		return this.scale(1./this.len());
	}
	public Point add(Point p){
		return new Point(x+p.x,y+p.y);
	}
	public Point sub(Point p){
		return new Point(x-p.x,y-p.y);
	}
	public double dot(Point p){
		return x*p.x + y*p.y;
	}
	public double cross(Point p){
		return x*p.y-p.x*y;
	}
	public Point scale(double s){
		return new Point(x*s,y*s);
	}
	public double ang(){
		return atan2(y,x);
	}
	public double len(){
		return sqrt(this.dot(this));
	}
	public Point rot(double ang){
		return new Point(x*cos(ang)-y*sin(ang),x*sin(ang)+y*cos(ang));
	}
	public Point perp(){
		return new Point(-y,x);
	}
	public double dist(Point p){
		Point d = this.sub(p);
		return sqrt(d.dot(d));
	}
	public static int ccw(Point a, Point b, Point c){
		double dot = (b.sub(a)).cross(c.sub(a));
		if(abs(dot) < eps)
			return 0;
		return (int)signum(dot);
	}	
}