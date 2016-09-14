package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

public class Avatar {

    private double[] translation;

    public Avatar() {
        translation = new double[3];
        translation[1] = 0.5;
        translation[2] = 9;
    }

    public void draw(GL2 gl) {
        //Draw Teapot
        float matAmbAndDif[] = {1.0f, 0.0f, 0.0f, 1.0f};
        float matSpec[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matShine[] = {50.0f};
        float emm[] = {0.0f, 0.0f, 0.0f, 1.0f};

        // Material properties of teapot
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpec, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShine, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emm, 0);

        // Draw Scene
        GLUT glut = new GLUT();
        gl.glFrontFace(GL2.GL_CW);
        glut.glutSolidTeapot(1.5);
        gl.glFrontFace(GL2.GL_CCW);
    }

    public double[] getPos() {
        return translation;
    }

    public void translate(double x, double y, double z) {
        translation[0] += x;
        translation[1] += y;
        translation[2] += z;
    }
}
