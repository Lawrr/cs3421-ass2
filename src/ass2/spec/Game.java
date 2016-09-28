package ass2.spec;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

    public static final String TEXTURE_FILENAME_TERRAIN = "terrain.png";
    public static final String TEXTURE_EXT_TERRAIN = "png";

    // TODO find texture which is size of power of 2
    public static final String TEXTURE_FILENAME_TREE_TRUNK = "tree_trunk.png";
    public static final String TEXTURE_EXT_TREE_TRUNK = "png";

    private Terrain myTerrain;
    private Avatar avatar;

    //Setting for light 0
    private float a = 0.2f; // Ambient white light intensity.
    private float d = 0.5f; // Diffuse white light intensity
    private float s = 0.2f; // Specular white light intensity.

    //Global Settings
    private float g = 0.2f; // Global Ambient intensity.
    private int localViewer = 0; // Local viewpoint?
    private boolean thirdPerson = true;

    public Game(Terrain terrain) {
        super("Assignment 2");
        myTerrain = terrain;
        avatar = new Avatar();
    }

    /**
     * Run the game.
     *
     */
    public void run() {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLJPanel panel = new GLJPanel();
        panel.addGLEventListener(this);
        panel.addKeyListener(this);

        // Add an animator to call 'display' at 60fps
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(panel);
        animator.start();

        getContentPane().add(panel);
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        Game game = new Game(terrain);
        game.run();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.7f, 0.9f, 1, 1);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU glu = new GLU();

        // Camera
        double[] pos = avatar.getPos();
        pos[1] = myTerrain.altitude(pos[0], pos[2]);

        double a0 = Math.toRadians(avatar.getRotation());
        double eyeX = pos[0];
        double eyeY = pos[1] + 1;
        double eyeZ = pos[2];
        double towardsX = Math.cos(a0);
        double towardsZ = Math.sin(a0);
        double centerX = eyeX + towardsX;
        double centerY = eyeY;
        double centerZ = eyeZ + towardsZ;

        if (thirdPerson) {
            glu.gluLookAt(eyeX - towardsX * 2, eyeY + 1, eyeZ - towardsZ * 2, centerX, centerY, centerZ, 0.0, 1.0, 0.0);
        } else {
            glu.gluLookAt(eyeX - towardsX * 0.25, eyeY + 0.25, eyeZ - towardsZ * 0.25, centerX, centerY, centerZ, 0.0, 1.0, 0.0);
        }

        // Directional light
        setLighting(gl);

        if (thirdPerson) avatar.draw(gl);
        myTerrain.draw(gl);

        // Draw axises
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        gl.glBegin(GL2.GL_LINES);
        gl.glColor4d(1, 0, 0, 1);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(100, 0, 0);
        gl.glColor4d(0, 1, 0, 1);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(0, 100, 0);
        gl.glColor4d(0, 0, 1, 1);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(0, 0, 100);
        gl.glEnd();

        //Set back to FILL when you are finished - not needed but is a bug fix for some implementations on some platforms
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        // Cull back faces.
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);
        // Turn on OpenGL texturing.
        gl.glEnable(GL2.GL_TEXTURE_2D);

        // Enable lights
        gl.glEnable(GL2.GL_LIGHT0);

        // Specify how texture values combine with current surface color values.
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);

        // Init textures
        MyTexture terrainTexture = new MyTexture(gl, TEXTURE_FILENAME_TERRAIN, TEXTURE_EXT_TERRAIN, true);
        myTerrain.setTerrainTexture(terrainTexture);

        MyTexture treeTrunkTexture = new MyTexture(gl, TEXTURE_FILENAME_TREE_TRUNK, TEXTURE_EXT_TREE_TRUNK, true);
        myTerrain.setTreeTrunkTexture(treeTrunkTexture);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
                        int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU glu = new GLU();
        glu.gluPerspective(60.0, (float) width / (float) height, 1.0, 20.0);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    public void setLighting(GL2 gl) {
        // Sun property vectors
        float lightAmb[] = { a, a, a, 1.0f };
        float lightDif[] = { d, d, d, 1.0f };
        float lightSpec[] = { s, s, s, 1.0f };

        float globAmb[] = { g, g, g, 1.0f };

        // Sun properties
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDif, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpec, 0);

        // Global light properties
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb, 0); // Global ambient light.
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, localViewer); // Enable local viewpoint

        // Draw light
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, myTerrain.getSunlight(), 0);
    }

    @Override
    public void keyPressed(KeyEvent ev) {
        switch (ev.getKeyCode()) {
            case KeyEvent.VK_L:
                if(localViewer == 1) localViewer = 0;
                else localViewer = 1;
                System.out.println("Local viewer " + localViewer);
                break;
            case KeyEvent.VK_D:
                if (ev.isShiftDown()) {
                    if (d < 1.0) d += 0.05;
                } else {
                    if (d > 0.0) d -= 0.05;
                }

                break;
            case KeyEvent.VK_A:
                if (ev.isShiftDown()) {
                    if (a < 1.0) a += 0.05;
                } else {
                    if (a > 0.0) a -= 0.05;
                }
                break;
            case KeyEvent.VK_S:
                if (ev.isShiftDown()) {
                    if (s < 1.0) s += 0.05;
                } else {
                    if (s > 0.0) s -= 0.05;
                }
                break;
            case KeyEvent.VK_UP:
                avatar.moveForward(avatar.getMoveSpeed());
                break;
            case KeyEvent.VK_DOWN:
                avatar.moveForward(-avatar.getMoveSpeed());
                break;
            case KeyEvent.VK_LEFT:
                avatar.rotate(-avatar.getRotateSpeed());
                break;
            case KeyEvent.VK_RIGHT:
                avatar.rotate(avatar.getRotateSpeed());
                break;
            case KeyEvent.VK_SPACE:
                thirdPerson = !thirdPerson;
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
}
