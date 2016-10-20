package ass2.spec;

import com.jogamp.opengl.GL2;

import java.util.ArrayList;
import java.util.List;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    private List<Double> myPoints;
    private double myWidth;
    
    /** 
     * Create a new road starting at the specified point
     */
    public Road(double width, double x0, double y0) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        myPoints.add(x0);
        myPoints.add(y0);
    }

    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(double width, double[] spine) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        for (int i = 0; i < spine.length; i++) {
            myPoints.add(spine[i]);
        }
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return myWidth;
    }

    /**
     * Add a new segment of road, beginning at the last point added and ending at (x3, y3).
     * (x1, y1) and (x2, y2) are interpolated as bezier control points.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     */
    public void addSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
        myPoints.add(x1);
        myPoints.add(y1);
        myPoints.add(x2);
        myPoints.add(y2);
        myPoints.add(x3);
        myPoints.add(y3);        
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return myPoints.size() / 6;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public double[] controlPoint(int i) {
        double[] p = new double[2];
        p[0] = myPoints.get(i*2);
        p[1] = myPoints.get(i*2+1);
        return p;
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public double[] point(double t) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 6;
        
        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i++);
        
        double[] p = new double[2];

        p[0] = b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3;
        p[1] = b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3;        
        
        return p;
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private double b(int i, double t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }


    public void draw(GL2 gl, Terrain terrain) {
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);

        double y0 = terrain.altitude(controlPoint(0)[0], controlPoint(0)[1]);
        double tIncrement = 1.0/myPoints.size();

        gl.glLineWidth(2);

        gl.glBegin(GL2.GL_LINE_STRIP);

        //Draw the road mesh
        for (int i = 0; i < myPoints.size()*size()-2; i++) {
            //Point increment depending on size()
            double t = i * tIncrement;
            double t2 = (i+1) *tIncrement;
            double t3 = (i+2) *tIncrement;

            double normal[] = normalisePoint(t, t2);
            double normal2[] = normalisePoint(t2, t3);

            double[] p1 = {point(t)[0] + (width()/2)* normal[0], y0, point(t)[1] + (width()/2)* normal[1]};
            double[] p2 = {point(t)[0] - (width()/2)* normal[0], y0, point(t)[1] - (width()/2)* normal[1]};

            double[] p3 = {point(t2)[0] - (width()/2)* normal2[0], y0, point(t2)[1] - (width()/2)* normal2[1]};
            double[] p4 = {point(t2)[0] + (width()/2)* normal2[0], y0, point(t2)[1] + (width()/2)* normal2[1]};


            /*
              p4---p3
              |   / |
              |  /  |
              | /   |
              p1---p2

             */

            gl.glVertex3dv(p1, 0);
            gl.glVertex3dv(p3, 0);
            gl.glVertex3dv(p2, 0);
            gl.glVertex3dv(p1, 0);
            gl.glVertex3dv(p4, 0);
            gl.glVertex3dv(p3, 0);
            gl.glVertex3dv(p4, 0);

        }

        gl.glEnd();

        //Set back to FILL when you are finished - not needed but is a bug fix for some implementations on some platforms
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    double[] normalisePoint (double t, double nexT) {
        //For point normal dx=x2-x1 and dy=y2-y1 (VECTOR STUFF)
        //and then swap x and y and negate one(-dy, dx)

        double vx = point(nexT)[0] - point(t)[0];
        double vz = point(nexT)[1] - point(t)[1];

        double nx1 = point(t)[0] - vz;
        double nz1 = point(t)[1] + vx;

        //Vector from point to point normal
        double nVx = nx1 - point(t)[0];
        double nVz = nz1 - point(t)[1];

        //Normalise the vector
        double normalVectorX = nVx/Math.sqrt(Math.pow(nVx,2) + Math.pow(nVz,2));
        double normalVectorZ = nVz/Math.sqrt(Math.pow(nVx,2) + Math.pow(nVz,2));

        double normalPoint[] = {normalVectorX, normalVectorZ};

        return normalPoint;
    }
}
