package ass2.spec;

import com.jogamp.opengl.GL2;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;



/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private float[] mySunlight;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        mySunlight = new float[3];
    }
    
    public Terrain(Dimension size) {
        this(size.width, size.height);
    }

    public Dimension size() {
        return mySize;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public float[] getSunlight() {
        return mySunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight[0] = dx;
        mySunlight[1] = dy;
        mySunlight[2] = dz;        
    }
    
    /**
     * Resize the terrain, copying any old altitudes. 
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];
        
        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
            }
        }
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return myAltitude[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     *
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        // Interpolate x
        int x1 = (int) x;
        int x2 = (int) Math.ceil(x);
        int z1 = (int) z;
        int z2 = (int) Math.ceil(z);

        // Check which side of the triangle the point is on

        double fx1 = myAltitude[x1][z2];
        double fx2 = myAltitude[x2][z2];
        double fz1 = myAltitude[x2][z1];
        double fz2 = myAltitude[x2][z2];

        System.out.printf("x1: %d, x2: %d, z1: %d, z2: %d\n", x1, x2, z1, z2);
        if (x1 == x2 && z1 == z2) return fx1;
        if (x1 == x2) return (z - z1) / (z2 - z1) * fz2 + (z2 - z) / (z2 - z1) * fz1;
        if (z1 == z2) return (x - x1) / (x2 - x1) * fx2 + (x2 - x) / (x2 - x1) * fx1;

        // Interpolate x
        double dx = (x - x1) / (x2 - x1) * fx2 + (x2 - x) / (x2 - x1) * fx1;

        // Interpolate z
        double dz = (x - x1) / (x2 - x1) * fx2 + (x2 - x) / (x2 - x1) * fz2;

        double dy = (z - z1) / (z2 - z1) * dz + (z2 - z) / (z2 - z1) * dx;
        System.out.printf("x %.2f y %.2fz %.2f, %.2f, %.2f\n", dx, dy, dz, (x - x1) / (x2 - x1) * fx2, fx2);

        return dy;
    }

    /**
     * Add a tree at the specified (x,z) point.
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     *
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        double y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        myTrees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param width
     * @param spine
     */
    public void addRoad(double width, double[] spine) {
        Road road = new Road(width, spine);
        myRoads.add(road);        
    }

    private void drawSelf(GL2 gl) {
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        for (int z = 0; z < myAltitude.length - 1; z++) {
            for (int x = 0; x < myAltitude[z].length - 1; x++) {
                gl.glBegin(GL2.GL_TRIANGLES);
                {
                    gl.glVertex3d(x + 1, myAltitude[x + 1][z + 1], z + 1);
                    gl.glVertex3d(x + 1, myAltitude[x + 1][z], z);
                    gl.glVertex3d(x, myAltitude[x][z + 1], z + 1);

                    gl.glVertex3d(x, myAltitude[x][z + 1], z + 1);
                    gl.glVertex3d(x + 1, myAltitude[x + 1][z], z);
                    gl.glVertex3d(x, myAltitude[x][z], z);
                }
                gl.glEnd();

            }
        }
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    public void draw(GL2 gl) {
        gl.glPushMatrix();
        drawSelf(gl);

        for (Tree t : myTrees) {
            t.draw(gl, this);
        }
        for (Road r : myRoads) {
            r.draw(gl);
        }
        gl.glPopMatrix();
    }

}