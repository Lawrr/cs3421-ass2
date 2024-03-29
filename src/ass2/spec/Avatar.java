package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_COLOR_MATERIAL;

public class Avatar {

    private double[] translation;
    private double rotation;

    private double moveSpeed = 0.2;
    private double rotateSpeed = 5;
    private double velocity = 0;
    private int rotationLimit = 0;
    private boolean up = true;

    private Terrain terrain;

    public Avatar(Terrain terrain) {
        this.terrain = terrain;

        rotation = 45;

        translation = new double[3];
        // Set avatar altitude
        translation[1] = terrain.altitude(translation[0], translation[2]);
    }

    public void draw(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslated(translation[0], translation[1], translation[2]);
        gl.glRotated(-rotation, 0, 1, 0);
        gl.glEnable(GL_COLOR_MATERIAL);

        float matAmbAndDif[] = {1.0f, 0.0f, 0.0f, 1.0f};
        float matSpec[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matShine[] = {50.0f};
        float emm[] = {0.0f, 0.0f, 0.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpec, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShine, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emm, 0);

        // Draw Scene
        GLUT glut = new GLUT();

        //For altitude fix
        gl.glTranslated(0,-0.05,0);

        //Main body (white)
        gl.glColor3f(2.0f, 2.0f, 2.0f);
        gl.glTranslated(-0.5,0.5,0);
        gl.glScaled(1.2,0.5,0.8);
        glut.glutSolidSphere(0.25,8,8);

        //Front body (black)
        gl.glColor3f(0f, 0f, 0f);
        gl.glTranslated(0.2,-0.05,0);
        glut.glutSolidSphere(0.25,5,5);


        //LegR (black)
        gl.glTranslated(0,-0.4,0.15);
        gl.glRotated(90,1,0,0);
        gl.glScaled(0.8,1.4,1/0.8);

        //LegR animation
        gl.glPushMatrix();
        gl.glRotated(rotationLimit*4,0,1,0);
        glut.glutSolidCylinder(0.1,0.3,8,8);
        gl.glPopMatrix();


        //LegL
        gl.glTranslated(0,-0.2,0.05);

        //LegL animation
        gl.glPushMatrix();
        gl.glRotated(-rotationLimit*4,0,1,0);
        glut.glutSolidCylinder(0.1,0.3,8,8);
        gl.glPopMatrix();

        //Head animation start
        gl.glPushMatrix();
        gl.glRotated(rotationLimit*4/5,0,1,0);

        //Head (white)
        gl.glColor3f(2.0f, 2.0f, 2.0f);
        gl.glTranslated(0.72,0.1,0.1);
        gl.glRotated(-90,1,0,0);
        gl.glScaled(1.1,0.7,0.7);
        glut.glutSolidSphere(0.25,12,12);

        //EarR
        gl.glColor3f(0f, 0f, 0f);
        gl.glTranslated(0.1,0.2,0.15);
        gl.glScaled(0.3,0.3,0.3);
        glut.glutSolidSphere(0.25,10,10);

        //EarL
        gl.glTranslated(0,0,-1);
        glut.glutSolidSphere(0.25,10,10);

        //Head animation end
        gl.glPopMatrix();

        gl.glDisable(GL_COLOR_MATERIAL);

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

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void moveForward(double distance) {
        double a0 = Math.toRadians(rotation);
        double moveX = Math.cos(a0) * distance;
        double moveZ = Math.sin(a0) * distance;
        translate(moveX, 0, moveZ);

        //Animation stuff
        //Rotation animation limit
        if (rotationLimit % 10 == 0 && rotationLimit != 0) {
            up = !up;
        }

        if (rotationLimit == 10) {
            rotationLimit--;
        } else if (distance > 0 && up || distance < 0 && !up || rotationLimit == -10) {
            rotationLimit++;
        } else {
            rotationLimit--;
        }
    }
}
