package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

    private double[] myPos;
    private MyTexture trunkTexture;

    public Tree(double x, double y, double z) {
        myPos = new double[3];
        myPos[0] = x;
        myPos[1] = y;
        myPos[2] = z;
    }
    
    public double[] getPosition() {
        return myPos;
    }


    public void draw(GL2 gl, Terrain terrain) {
        gl.glPushMatrix();
        gl.glTranslated(myPos[0], myPos[1], myPos[2]);
        int slices = 32;
        double width = 0.3;
        double height = 2;
        System.out.println(terrain.altitude(2.5, 2.5));
        double z1 = 0;
        double z2 = height;
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        // Set texture for tree trunk
        gl.glBindTexture(GL2.GL_TEXTURE_2D, trunkTexture.getTextureId());

        float matAmbAndDif[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matSpec[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matShine[] = {50.0f};
        float emm[] = {0.0f, 0.0f, 0.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpec, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShine, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emm, 0);

        // Top
        gl.glBegin(GL2.GL_TRIANGLE_FAN);{
            gl.glNormal3d(0,1,0);
            gl.glTexCoord2d(width, width);
            gl.glVertex3d(0,z1,0);
            double angleStep = 2*Math.PI/slices;
            for (int i = 0; i <= slices ; i++){
                double a0 = i * angleStep;
                double x0 = Math.cos(a0) * width;
                double y0 = Math.sin(a0) * width;

                gl.glTexCoord2d(width + x0, width + y0);
                gl.glVertex3d(x0,z1,y0);
            }
        }gl.glEnd();

        // Bottom
        gl.glBegin(GL2.GL_TRIANGLE_FAN);{
            gl.glNormal3d(0,-1,0);
            gl.glTexCoord2d(width, width);
            gl.glVertex3d(0,z2,0);
            double angleStep = 2*Math.PI/slices;
            for (int i = 0; i <= slices ; i++){
                double a0 = 2*Math.PI - i * angleStep;
                double x0 = Math.cos(a0) * width;
                double y0 = Math.sin(a0) * width;

                gl.glTexCoord2d(width + x0, width + y0);
                gl.glVertex3d(x0,z2,y0);
            }
        }gl.glEnd();

        // Sides
        gl.glBegin(GL2.GL_QUAD_STRIP);
        {
            double angleStep = 2.0 * Math.PI / slices;
            for (int i = 0; i <= slices; i++) {
                double a0 = i * angleStep;

                // Calculate vertices for the quad
                double x0 = Math.cos(a0) * width;
                double y0 = Math.sin(a0) * width;
                double sCoord = 2.0 / slices * i;

                gl.glNormal3d(x0, 0, y0);

                // Bottom
                gl.glTexCoord2d(sCoord, 0);
                gl.glVertex3d(x0, z1, y0);

                // Top
                gl.glTexCoord2d(sCoord, 1);
                gl.glVertex3d(x0, z2, y0);
            }

        }
        gl.glEnd();

        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);


        double leavesRadius = width * 4;
        gl.glTranslated(0, z2 + leavesRadius -  leavesRadius * 0.25, 0);

        matAmbAndDif = new float[]{0.0f, 0.5f, 0.1f, 1.0f};
//        matAmbAndDif[] = {1.0f, 1.0f, 1.0f, 1.0f};
        matSpec = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        matShine = new float[]{50.0f};
        emm = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpec, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShine, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emm, 0);

        GLUT glut = new GLUT();
        glut.glutSolidSphere(leavesRadius, 40, 40);

        gl.glPopMatrix();
    }

    public void setTrunkTexture(MyTexture texture) {
        trunkTexture = texture;
    }
}
