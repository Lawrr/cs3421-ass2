package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

public class Avatar {

    private double[] translation;
    private double rotation;

    private double moveSpeed = 0.2;
    private double rotateSpeed = 5;

    public Avatar() {
        translation = new double[3];
        rotation = 45;
    }

    public void draw(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslated(translation[0], translation[1], translation[2]);
        gl.glRotated(-rotation, 0, 1, 0);

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
        glut.glutSolidTeapot(0.3);
        gl.glFrontFace(GL2.GL_CCW);

        gl.glPopMatrix();
    }

    public double[] getPos() {
        return translation;
    }

    public void translate(double x, double y, double z) {
        translation[0] += x;
        translation[1] += y;
        translation[2] += z;
    }

    public void rotate(double theta) {
        rotation = (((rotation + theta) % 360) + 360) % 360;
    }

    public double getRotation() {
        return rotation;
    }

    public double getRotateSpeed() {
        return rotateSpeed;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public void moveForward(double distance) {
        double a0 = Math.toRadians(rotation);
        double moveX = Math.cos(a0) * distance;
        double moveZ = Math.sin(a0) * distance;
        translate(moveX, 0, moveZ);
    }
}
