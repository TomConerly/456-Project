import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class Algo{
	ArrayList<Point> PA,PB;
	ArrayList<Line> LA,LB;	
	
	public Algo(ArrayList<Point> A, ArrayList<Point> B){
		PA = A;
		PB = B;
		LA = new ArrayList<Line>();
		LB = new ArrayList<Line>();
		for(int i = 0; i < A.size();i++)
			LA.add(pointToLine(A.get(i)));
		for(int i = 0; i < B.size();i++)
			LB.add(pointToLine(B.get(i)));
		
		lx = Double.NEGATIVE_INFINITY;
		hx = Double.POSITIVE_INFINITY;
		
		G1 = new ArrayList<Line>();
		for(Line l:LA)
			G1.add(l);
		G2 = new ArrayList<Line>();
		for(Line l:LB)
			G2.add(l);
		if(G1.size() %2 == 0)
			G1.remove(0);
		if(G2.size() %2 == 0)
			G2.remove(0);
		p1 = G1.size()/2;
		p2 = G2.size()/2;
		rand = new Random(234234L);
	}
	public static Line pointToLine(Point p){
		return new Line(new Point(0,-p.y),new Point(1,2*p.x-p.y));
	}
	Random rand;
	public double lx,hx;
	public ArrayList<Line> G1,G2;
	public int p1,p2;
	public boolean reversed = false;
	public boolean done = false;
	public Point ans = null;
	public void step(){
		if(done == true)
			return;
		System.out.println("START STEP");
		if(G1.size() < G2.size()){
			ArrayList<Line> temp = G2;
			G2 = G1;
			G1 = temp;
			int tempp = p2;
			p2 = p1;
			p1 = tempp;
			reversed = !reversed;
		}
		System.out.format("START G1 size: %d p1: %d, G2 size: %d p2:%d\n",G1.size(),p1,G2.size(),p2);
		if(G1.size() + G2.size() <= 6 || G1.size() == 0 || G2.size() == 0){
			done:
			for(int i = 0; i < G1.size()+G2.size();i++){
				for(int j = i + 1; j < G1.size()+G2.size();j++){
					Line a = i < G1.size() ? G1.get(i) : G2.get(i-G1.size());
					Line b = j < G1.size() ? G1.get(j) : G2.get(j-G1.size());
					Point p = a.interLine(b);
					if(p == null)
						continue;
					if((G1.size() == 0 ||Math.abs(findKth(G1,p1,p.x)-p.y) < 1e-4) && (G2.size() == 0 || Math.abs(findKth(G2,p2,p.x)-p.y) < 1e-4)){
						done = true;
						ans = p;
						break done;
					}
				}
			}
			if(ans == null){
				throw new RuntimeException();
			}else{
				int belowa=0,abovea = 0, belowb=0,aboveb=0;
				for(Line l:LA){
					double y = l.valueAt(ans.x);
					if(Math.abs(y-ans.y) < 1e-4)
						continue;
					if(y < ans.y)
						belowa++;
					else
						abovea++;
				}
				for(Line l:LB){
					double y = l.valueAt(ans.x);
					if(Math.abs(y-ans.y) < 1e-4)
						continue;
					if(y < ans.y)
						belowb++;
					else
						aboveb++;
				}
				System.out.format("FOUND ANS: belowa=%d abovea=%d totala=%d belowb=%d aboveb=%d totalb=%d\n",belowa,abovea,LA.size(),belowb,aboveb,LB.size());
			}
		}
		int times = 0;
		do{
			Line a = G1.get(rand.nextInt(G1.size()));
			Line b = G1.get(rand.nextInt(G1.size()));
			if(a == b)
				continue;
			
			Point inter = a.interLine(b);
			if(inter == null)
				continue;
			if(lx < inter.x && inter.x < hx){
				double yl1 = findKth(G1,p1,lx);
				double yl2 = findKth(G2,p2,lx);
				double ymid1 = findKth(G1,p1,inter.x);
				double ymid2 = findKth(G2,p2,inter.x);
				
				if((yl1 < yl2 && ymid1 > ymid2) || (yl1 > yl2 && ymid1 < ymid2))
					hx = inter.x;
				else
					lx = inter.x;				
			}
			times++;
		}while(times < 3);
		System.out.format("lx=%.3f, hx = %.3f\n",lx,hx);
		
		double yl1 = findKth(G1,p1-G1.size()/8,lx);
		double yl2 = findKth(G1,p1+G1.size()/8,lx);
		double yr1 = findKth(G1,p1-G1.size()/8,hx);
		double yr2 = findKth(G1,p1+G1.size()/8,hx);
		
		Line top = 		new Line(new Point(lx,yl2),new Point(hx,yr2));
		Line bottom = 	new Line(new Point(lx,yl1),new Point(hx,yr1));
		Line left = 	new Line(new Point(lx,yl1),new Point(lx,yl2));
		Line right = 	new Line(new Point(hx,yr1),new Point(hx,yr2));
		
		ArrayList<Line> newG1 = new ArrayList<Line>();
		Line[] trapezoid = {top,bottom,left,right};
		
		int below = 0;
		for(Line l:G1){
			boolean added = false;
			for(Line t:trapezoid){
				Point p = l.interLine(t);
				if(p == null)
					continue;
				if(t.onSeg(p)){
					newG1.add(l);
					added = true;
					break;
				}
			}
			if(!added){
				Point p = l.interLine(new Line(new Point(lx,0),new Point(lx,1)));
				if(p.y < yl1)
					below++;
			}
		}
		G1 = newG1;
		p1 -= below;	
		System.out.format("END G1 size: %d p1: %d, G2 size: %d p2:%d\n",G1.size(),p1,G2.size(),p2);
		System.out.println("DONE STEP");
	}
	public static double findKth(ArrayList<Line> L, int k, double x) {
		ArrayList<Line> T = (ArrayList<Line>) L.clone();
		if(x == Double.NEGATIVE_INFINITY)
			x = Long.MIN_VALUE;
		Collections.sort(T,new SelectionComparator(x));
		return T.get(k).interLine(new Line(new Point(x,0),new Point(x,1))).y;
	}
	
	private static class SelectionComparator implements Comparator<Line>{
		double x;
		public SelectionComparator(double x){
			this.x = x;
		}
		@Override
		public int compare(Line a, Line b) {
			Point ai = a.interLine(new Line(new Point(x,0),new Point(x,1)));
			Point bi = b.interLine(new Line(new Point(x,0),new Point(x,1)));
			if(ai == null)
				return -1;
			if(bi == null)
				return 1;
			if(ai == null && bi == null)
				return 0;
			return Double.valueOf(ai.y).compareTo(bi.y);
		}
	}
}











