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

		lx = (double)-10000;
		hx = (double)10000;

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
		mode = Mode.START1;
		message = startMessage1;
	}
	String startMessage1 = "First switch to the dual problem so points become lines. Now we are searching for a point which has half of the lines of each set above it. We can prove that such a point will always exist at the intersection of two lines (but it might also exist elsewhere).";
	String startMessage2 = "Red and blue lines that are thin are lines that the algorithm has ruled out. The purple and yellow line are the median levels they correspond to points with half of the lines above and below. The vertical dark green lines indicates the interval we are searching.";
	String startEndMessage = "There are very few lines left so use the brute force method to compute the answer.";
	String startIterRed = "There are more red lines left than blue lines so we will operate on the red lines.";
	String startIterBlue = "There are more blue lines left than red lines so we will operate on the blue lines.";
	String intervalMessage1 = "We want to shrink the x range but keep the property that our median levels intersect an odd number of times. We will repeatedly pick an intersection point and split based on that point.";
	String intervalMessage2 = "More splitting! When we split we always pick the side where the median levels intersect an odd number of times.";
	String intervalMessage3 = "We are done splitting. Now in this range we will construct a trapezoid.";
	String trapezoidMessage1 = "The trapezoid is constructed by picking the points on each side of the interval that are one eigth of lines above or below the median level.";
	String trapezoidMessage2 = "Now remove all of the lines entirely above or below the trapezoid. We are gaurenteed to remove at least half of the lines of the color we are working on.";
	String iterDoneMessage = "We are now done with one iteration. Click step to see the next iteration in detail or hit skip to do the next iteration in one step.";
	String endMessage = "The black point is the point with half of the lines above and below it.";
	String completeMessage = "Now we go back to the primal. The line is the dual of the point we found. As you can see half of the points are above and below the line.";

	public static Line pointToLine(Point p){
		return new Line(new Point(0,-p.y),new Point(1,2*p.x-p.y));
	}
	public enum Mode {START1,START2,INTERVALS,TRAPEZOID,ITERDONE,ENDANSCHECK, COMPLETE};
	Mode mode;
	Random rand;
	public double lx,hx;
	public ArrayList<Line> G1,G2;
	public int p1,p2;
	public boolean reversed = false;
	public boolean done = false;
	public Point ans = null;
	public String message;
	public int times;
	public boolean forceBrute = false;

	public Line[] trap = null;
	public Line[] drawTrap = null;

	public void checkAns(){
		done:
			for(int i = 0; i < G1.size()+G2.size();i++){
				for(int j = i + 1; j < G1.size()+G2.size();j++){
					Line a = i < G1.size() ? G1.get(i) : G2.get(i-G1.size());
					Line b = j < G1.size() ? G1.get(j) : G2.get(j-G1.size());
					Point p = a.interLine(b);
					if(p == null)
						continue;
					if(p.x < lx || p.x > hx)
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
		mode = Mode.COMPLETE;
	}
	}
	
	public void step(){

		System.out.println("START STEP");
		switch(mode){
		case START1:
			message = startMessage2;
			mode = Mode.START2;
			break;
		case START2:
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
			if(G1.size() + G2.size() <= 6 || G1.size() == 0 || G2.size() == 0 || forceBrute){
				message = startEndMessage;
				mode = Mode.ENDANSCHECK;
			}else{
				message = reversed? startIterBlue:startIterRed;
				mode = Mode.INTERVALS;
				times = 0;
			}
			break;
		case INTERVALS:
			if(times == 3){
				mode = Mode.TRAPEZOID;

			}
			if(times == 0){
				boolean done = false;
				int count = 0;

				while(!done && count < 25){
					count++;
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
						done = true;
					}
				}
				if(!done)
					forceBrute = true;
			}
			times++;
			if(times == 1)
				message = intervalMessage1;
			else if(times == 2)
				message = intervalMessage2;
			else{
				message = intervalMessage3;
				mode = Mode.TRAPEZOID;
			}
			System.out.format("lx=%.3f, hx = %.3f\n",lx,hx);
			break;
		case TRAPEZOID:
			if(trap == null){
				message = trapezoidMessage1;
				double yl1 = findKth(G1,p1-G1.size()/8,lx);
				double yl2 = findKth(G1,p1+G1.size()/8,lx);
				double yr1 = findKth(G1,p1-G1.size()/8,hx);
				double yr2 = findKth(G1,p1+G1.size()/8,hx);

				Line top = 		new Line(new Point(lx,yl2),new Point(hx,yr2));
				Line bottom = 	new Line(new Point(lx,yl1),new Point(hx,yr1));
				Line left = 	new Line(new Point(lx,yl1),new Point(lx,yl2));
				Line right = 	new Line(new Point(hx,yr1),new Point(hx,yr2));

				trap = new Line[]{top,bottom,left,right};
			
				
				double lx1 = lx;
				double hx1 = hx;
				
				Line top1 = 	new Line(new Point(lx1,yl2),new Point(hx1,yr2));
				Line bottom1 = 	new Line(new Point(lx1,yl1),new Point(hx1,yr1));

				drawTrap = new Line[]{top1,bottom1};
				System.out.println("TRAPEZOID: "+lx+" "+hx+" "+yl1+" "+yl2+" "+yr1+" "+yr2);
			}else{
				ArrayList<Line> newG1 = new ArrayList<Line>();
				int below = 0;
				for(Line t:trap)
					System.out.println(t.valueAt(0)+" "+t.valueAt(1)+" "+t);
				for(Line l:G1){
					boolean added = false;
					for(Line t:trap){
						Point p = l.interLine(t);
						System.out.println(l+" "+t+" "+p);
						if(p == null)
							continue;						
						if(t.onSeg(p)){
							newG1.add(l);
							added = true;
							break;
						}
					}
					System.out.println(added);
					if(!added){
						Point p = l.interLine(new Line(new Point(lx,0),new Point(lx,1)));
						if(p.y < trap[1].y1)
							below++;
					}
				}
				G1 = newG1;
				p1 -= below;	
				System.out.format("END G1 size: %d p1: %d, G2 size: %d p2:%d\n",G1.size(),p1,G2.size(),p2);
				message = trapezoidMessage2;
				mode = Mode.ITERDONE;
			}
			break;			
		case ITERDONE:
			drawTrap = null;
			message = iterDoneMessage;
			mode = Mode.START2;
			break;
		case ENDANSCHECK:
			message = endMessage;
			mode = Mode.COMPLETE;
			checkAns();
			break;
		case COMPLETE:
			message = completeMessage;
			break;
		}
		System.out.println("DONE STEP");
	}
	public static double findKth(ArrayList<Line> L, int k, double x) {
		ArrayList<Line> T = (ArrayList<Line>) L.clone();
		if(x == Double.NEGATIVE_INFINITY)
			x = Long.MIN_VALUE;
		if(x == Double.POSITIVE_INFINITY)
			x = Long.MAX_VALUE;
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

