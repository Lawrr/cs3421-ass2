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
    public static final String TEXTURE_ROAD = Game.TEXTURES_DIRECTORY + "road.jpg";

    private List<Double> myPoints;
    private double myWidth;

    private MyTexture roadTexture;

    private double[] fixPoint;
    
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
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        //Altitude added 0.001 so it doesn't overlap with terrain
        double y = terrain.altitude(controlPoint(0)[0], controlPoint(0)[1])+0.001;
        double tIncrement = 1.0/myPoints.size();

        float matAmbAndDif[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matSpec[] = {0.3f, 0.3f, 0.3f, 1.0f};
        float matShine[] = {10.0f};
        float emm[] = {0.0f, 0.0f, 0.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpec, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShine, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emm, 0);

        //Set texture for road
        gl.glBindTexture(GL2.GL_TEXTURE_2D, roadTexture.getTextureId());

        //Draw the road mesh
        for (int i = 0; i <= myPoints.size()*size()-2; i++) {
            //Point increment depending on size()
            double t = i * tIncrement;
            double t2 = (i+1) * tIncrement;

            double vx = point(t2)[0] - point(t)[0];
            double vz = point(t2)[1] - point(t)[1];

            double[] normal = normalisePoint(point(t)[0], point(t)[1], point(t2)[0], point(t2)[1]);

            double[] p1 = {point(t)[0] + (width()/2)* normal[0], y, point(t)[1] + (width()/2)* normal[1]};
            double[] p2 = {point(t)[0] - (width()/2)* normal[0], y, point(t)[1] - (width()/2)* normal[1]};

            double[] p3 = new double[3];
            double[] p4 = new double[3];

            //Prevent out of bound while drawing the last segment
            //because normal calculations are done by 3 points
            //so it doesn't produce cracks

            if (i != myPoints.size()*size()-2) {
                double t3 = (i + 2) * tIncrement;

                double[] normal2 = normalisePoint(point(t2)[0], point(t2)[1], point(t3)[0], point(t3)[1]);

                p3[0] = point(t2)[0] - (width()/2)* normal2[0];
                p3[1] = y;
                p3[2] = point(t2)[1] - (width()/2)* normal2[1];

                p4[0] = point(t2)[0] + (width()/2)* normal2[0];
                p4[1] = y;
                p4[2] = point(t2)[1] + (width()/2)* normal2[1];

            } else {
                p3[0] = p2[0]+vx;
                p3[1] = y;
                p3[2] = p2[2]+vz;

                p4[0] = p1[0]+vx;
                p4[1] = y;
                p4[2] = p1[2]+vz;
            }

            gl.glBegin(GL2.GL_TRIANGLES);

            /*
              p4---p3
              |   / |
              |  /  |
              | /   |
              p1---p2

             */

            gl.glNormal3d(0,1,0);

            gl.glTexCoord2d(0, 0);
            gl.glVertex3dv(p1, 0);

            gl.glTexCoord2d(1, 1);
            gl.glVertex3dv(p3, 0);

            gl.glTexCoord2d(1, 0);
            gl.glVertex3dv(p2, 0);

            gl.glTexCoord2d(0, 0);
            gl.glVertex3dv(p1, 0);

            gl.glTexCoord2d(0, 1);
            gl.glVertex3dv(p4, 0);

            gl.glTexCoord2d(1, 1);
            gl.glVertex3dv(p3, 0);

            gl.glTexCoord2d(0, 1);
            gl.glVertex3dv(p4, 0);

            gl.glEnd();

            //Take end point for last control point
            fixPoint = p3;
        }

        //Draw the last missing segment at last control point
        gl.glBegin(GL2.GL_TRIANGLES);

        int p = myPoints.size()*size()-1;

        double t = p * tIncrement;

        double[] normal = normalisePoint(point(t)[0], point(t)[1], controlPoint(size()*3)[0], controlPoint(size()*3)[1]);

        double vx = controlPoint(size()*3)[0] - point(t)[0];
        double vz = controlPoint(size()*3)[1] - point(t)[1];

        double[] p1 = {point(t)[0] + (width()/2)* normal[0], y, point(t)[1] + (width()/2)* normal[1]};
        double[] p2 = fixPoint;
        double[] p3 = {p2[0]+vx-0.1, y, p2[2]+vz};
        double[] p4 = {p1[0]+vx, y, p1[2]+vz};

        gl.glNormal3d(0,1,0);

        gl.glTexCoord2d(0, 0);
        gl.glVertex3dv(p1, 0);

        gl.glTexCoord2d(1, 1);
        gl.glVertex3dv(p3, 0);

        gl.glTexCoord2d(1, 0);
        gl.glVertex3dv(p2, 0);

        gl.glTexCoord2d(0, 0);
        gl.glVertex3dv(p1, 0);

        gl.glTexCoord2d(0, 1);
        gl.glVertex3dv(p4, 0);

        gl.glTexCoord2d(1, 1);
        gl.glVertex3dv(p3, 0);

        gl.glTexCoord2d(0, 1);
        gl.glVertex3dv(p4, 0);

        gl.glEnd();


        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);

        //Set back to FILL when you are finished - not needed but is a bug fix for some implementations on some platforms
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    double[] normalisePoint (double x1, double z1, double x2, double z2) {
        //For point normal dx=x2-x1 and dy=y2-y1 (VECTOR STUFF)
        //and then swap x and y and negate one(-dy, dx)
        //Used to find distance of a point in between 2 points

        double vx = x2 - x1;
        double vz = z2 - z1;

        double nx1 = x1 - vz;
        double nz1 = z1 + vx;

        //Vector from point to point normal
        double nVx = nx1 - x1;
        double nVz = nz1 - z1;

        //Normalise the vector
        double normalVectorX = nVx/Math.sqrt(Math.pow(nVx,2) + Math.pow(nVz,2));
        double normalVectorZ = nVz/Math.sqrt(Math.pow(nVx,2) + Math.pow(nVz,2));

        return new double[]{normalVectorX, normalVectorZ};
    }

    public void setRoadTexture(MyTexture texture) {
        this.roadTexture = texture;
    }
}
