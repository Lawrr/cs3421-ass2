package ass2.spec;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

    public static final String RESOURCES_DIRECTORY = "res" + File.separator;
    public static final String TEXTURES_DIRECTORY = RESOURCES_DIRECTORY + "textures" + File.separator;
    public static final String SHADERS_DIRECTORY = RESOURCES_DIRECTORY + "shaders" + File.separator;

    private Terrain myTerrain;
    private Avatar avatar;

    //Setting for sun
    private float a = 0.2f; // Ambient white light intensity.
    private float d = 0.5f; // Diffuse white light intensity
    private float s = 0.2f; // Specular white light intensity.

    //Global Settings
    private float g = 0.2f; // Global Ambient intensity.
    private int localViewer = 1; // Local viewpoint?
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
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glEnable(GL2.GL_DEPTH_TEST);
        // Cull back faces.
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);
        // Turn on OpenGL texturing.
        gl.glEnable(GL2.GL_TEXTURE_2D);

        // Lighting
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_LIGHT0);

        // Specify how texture values combine with current surface color values.
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);

        // Init textures
        MyTexture terrainTexture = new MyTexture(gl, Terrain.TEXTURE, true);
        myTerrain.setTerrainTexture(terrainTexture);

        MyTexture treeTrunkTexture = new MyTexture(gl, Tree.TEXTURE_TRUNK, true);
        myTerrain.setTreeTrunkTexture(treeTrunkTexture);

        MyTexture treeLeavesTexture = new MyTexture(gl, Tree.TEXTURE_LEAVES, true);
        myTerrain.setTreeLeavesTexture(treeLeavesTexture);

        MyTexture monsterTexture = new MyTexture(gl, Monster.TEXTURE, true);
        myTerrain.setMonsterTexture(monsterTexture);

        // Monster shader
        try {
            myTerrain.setMonsterShader(Shader.initShaders(gl, Monster.VERTEX_SHADER, Monster.FRAGMENT_SHADER));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Monster VBO
        int bufferIds[] = new int[1];
        FloatBuffer posData = Buffers.newDirectFloatBuffer(Monster.POSITIONS);
        FloatBuffer normData = Buffers.newDirectFloatBuffer(Monster.NORMALS);
        FloatBuffer texData = Buffers.newDirectFloatBuffer(Monster.TEXTURES);

        // Generate buffers
        gl.glGenBuffers(bufferIds.length, bufferIds, 0);

        // Bind buffer as array buffer
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferIds[0]);

        // Set space for data
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, // Type of buffer
                Monster.POSITIONS.length * Float.BYTES +
                Monster.NORMALS.length * Float.BYTES +
                Monster.TEXTURES.length * Float.BYTES, // Size of data
                null, // Actual data
                GL2.GL_STATIC_DRAW); // Don't intend to modify data after loading it

        // Put data into buffers
        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, 0,
                Monster.POSITIONS.length * Float.BYTES, posData);

        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, Monster.POSITIONS.length * Float.BYTES,
                Monster.NORMALS.length * Float.BYTES, normData);

        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, Monster.POSITIONS.length * Float.BYTES + Monster.NORMALS.length * Float.BYTES,
                Monster.TEXTURES.length * Float.BYTES, texData);

        myTerrain.setMonsterVbo(bufferIds, posData, normData, texData);

        // Set back to default
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
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

        // Sun
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDif, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpec, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, myTerrain.getSunlight(), 0);

        // Global light properties
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb, 0); // Global ambient light.
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, localViewer); // Enable local viewpoint
    }

    @Override
    public void keyPressed(KeyEvent ev) {
        switch (ev.getKeyCode()) {
            case KeyEvent.VK_L:
                if (localViewer == 1) localViewer = 0;
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
