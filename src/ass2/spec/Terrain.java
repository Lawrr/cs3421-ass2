package ass2.spec;

import com.jogamp.opengl.GL2;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;



/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    public static final String TEXTURE = Game.TEXTURES_DIRECTORY + "terrain.png";

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Monster> myMonsters;
    private List<Road> myRoads;
    private float[] mySunlight;
    private boolean directionalLight = true;

    private MyTexture texture;

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
        myMonsters = new ArrayList<Monster>();
        myRoads = new ArrayList<Road>();

        mySunlight = new float[4];
        mySunlight[3] = 0.0f;
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

    public void setTerrainTexture(MyTexture texture) {
        this.texture = texture;
    }

    public void setRoadTexture(MyTexture texture) {
        for (Road r : myRoads) {
            r.setRoadTexture(texture);
        }
    }

    public void setTreeTrunkTexture(MyTexture texture) {
        for (Tree t : myTrees) {
            t.setTrunkTexture(texture);
        }
    }

    public void setTreeLeavesTexture(MyTexture texture) {
        for (Tree t : myTrees) {
            t.setLeavesTexture(texture);
        }
    }

    public void setMonsterTexture(MyTexture texture) {
        for (Monster m : myMonsters) {
            m.setTexture(texture);
        }
    }

    public void setMonsterShader(int shader) {
        for (Monster m : myMonsters) {
            m.setShader(shader);
        }
    }

    public void setMonsterVbo(int[] bufferIds, FloatBuffer posData, FloatBuffer normData, FloatBuffer texData) {
        for (Monster m : myMonsters) {
            m.setVbo(bufferIds, posData, normData, texData);
        }
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
        int x1 = (int) x;
        int x2 = (int) Math.ceil(x);
        int z1 = (int) z;
        int z2 = (int) Math.ceil(z);

        // Terrain bounds
        if (!(x1 >= 0 && x2 < mySize.getWidth() &&
            z1 >= 0 && z2 < mySize.getHeight())) {
            return 0;
        }

        /*
         y1_____ y2
           |   /|
           | L/ |
           | /R |
           |/___|
         y3      y4
         */

        // Offsets in the tile in range of 0 to 1
        double xPercent = x % 1;
        double zPercent = z % 1;

        // The z value which correlates to the x on the mid-line
        double zMidPercent = 1 - xPercent;

        // y values
        double y1 = myAltitude[x1][z1];
        double y2 = myAltitude[x2][z1];
        double y3 = myAltitude[x1][z2];
        double y4 = myAltitude[x2][z2];

        // The lerped y value which correlates to the mid-line point
        double midY = MathUtil.lerp(y3, y2, xPercent);

        // Lerped y values
        double xY;
        double zY = midY; // Set default as on the mid-line

        // Check which side of the triangle the point is on
        if (zPercent < zMidPercent) {
            // Left triangle
            xY = MathUtil.lerp(y1, y2, xPercent);
            zY = MathUtil.lerp(xY, midY, zPercent / zMidPercent);

        } else if (zPercent > zMidPercent) {
            // Right triangle
            xY = MathUtil.lerp(y3, y4, xPercent);
            zY = MathUtil.lerp(midY, xY, (zPercent - zMidPercent) / xPercent);
        }

        return zY;
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
     * Add a monster at the specified (x,z) point.
     * The monster's y coordinate is calculated from the altitude of the terrain at that point.
     *
     * @param x
     * @param z
     */
    public void addMonster(double x, double z) {
        double y = altitude(x, z);
        Monster monster = new Monster(x, y, z);
        myMonsters.add(monster);
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
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        float matAmbAndDif[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matSpec[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matShine[] = {50.0f};
        float emm[] = {0.0f, 0.0f, 0.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpec, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShine, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emm, 0);

        // Set texture for terrain
        gl.glBindTexture(GL2.GL_TEXTURE_2D, texture.getTextureId());

        for (int z = 0; z < myAltitude.length - 1; z++) {
            for (int x = 0; x < myAltitude[z].length - 1; x++) {
                double[] rTrianglePoints = {x + 1, myAltitude[x + 1][z + 1], z + 1,         //bottom right
                                            x + 1, myAltitude[x + 1][z], z,                 //top right
                                            x, myAltitude[x][z + 1], z + 1};                //bottom left

                double[] lTrianglePoints = {x, myAltitude[x][z + 1], z + 1,                 //bottom left
                                            x + 1, myAltitude[x + 1][z], z,                 //top right
                                            x, myAltitude[x][z], z};                        //top left

                double[] rNormal = MathUtil.calcSurfaceNormal(rTrianglePoints);
                double[] lNormal = MathUtil.calcSurfaceNormal(lTrianglePoints);

                gl.glBegin(GL2.GL_TRIANGLES);
                {
                    /*
                       _____
                       |   /| L = Left triangle
                       |L / | R = Right triangle
                       | /R |
                       |/___|
                     */

                    // Right triangle
                    gl.glNormal3dv(rNormal, 0);

                    // Bottom right
                    gl.glTexCoord2d(1, 0);
                    gl.glVertex3d(x + 1, myAltitude[x + 1][z + 1], z + 1);
                    // Top right
                    gl.glTexCoord2d(1, 1);
                    gl.glVertex3d(x + 1, myAltitude[x + 1][z], z);
                    // Bottom left
                    gl.glTexCoord2d(0, 0);
                    gl.glVertex3d(x, myAltitude[x][z + 1], z + 1);

                    // Left triangle
                    gl.glNormal3dv(lNormal, 0);

                    // Bottom left
                    gl.glTexCoord2d(0, 0);
                    gl.glVertex3d(x, myAltitude[x][z + 1], z + 1);
                    // Top right
                    gl.glTexCoord2d(1, 1);
                    gl.glVertex3d(x + 1, myAltitude[x + 1][z], z);
                    // Top left
                    gl.glTexCoord2d(0, 1);
                    gl.glVertex3d(x, myAltitude[x][z], z);
                }
                gl.glEnd();
            }
        }

        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
    }

    public void draw(GL2 gl) {
        gl.glPushMatrix();
        drawSelf(gl);

        for (Tree t : myTrees) {
            t.draw(gl, this);
        }
        for (Monster m : myMonsters) {
            m.draw(gl, this);
        }
        for (Road r : myRoads) {
            r.draw(gl, this);
        }
        gl.glPopMatrix();
    }

    public void setIsDirectionalLight(boolean directionalLight) {
        this.directionalLight = directionalLight;
    }

    public boolean isDirectionalLight() {
        return directionalLight;
    }
}
