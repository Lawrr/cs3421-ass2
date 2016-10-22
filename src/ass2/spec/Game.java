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
    private RainSystem rainSystem;

    // Setting for sunlight
    private static final float aSun = 0.1f; // Ambient white light intensity.
    private static final float dSun = 0.5f; // Diffuse white light intensity
    private static final float sSun = 0.2f; // Specular white light intensity.
    private static final float gSun = 0.2f; // Global Ambient intensity.

    // Setting for torch
    private static final float aTorch = 0.4f; // Ambient white light intensity.
    private static final float dTorch = 0.8f; // Diffuse white light intensity
    private static final float sTorch = 0.2f; // Specular white light intensity.
    private static final float gTorch = 0.1f; // Global Ambient intensity.
    private static final float torchAngle = 45; // Half angle
    private static final float torchExponent = 4;

    // Global Settings
    private boolean thirdPerson = true;
    private boolean raining = false;
    private boolean darkMode = false;

    public Game(Terrain terrain) {
        super("Assignment 2");

        myTerrain = terrain;
        avatar = new Avatar(myTerrain);
        rainSystem = new RainSystem(avatar);
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

        // Background colour
        if (!darkMode) {
            gl.glClearColor(0.7f, 0.9f, 1, 1);
        } else {
            gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
        }

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU glu = new GLU();

        double[] pos = avatar.getPos();

        double a0 = Math.toRadians(avatar.getRotation());
        double towardsX = Math.cos(a0);
        double towardsZ = Math.sin(a0);
        double eyeX = pos[0];
        double eyeY = pos[1] + 1;
        double eyeZ = pos[2];
        double centerX = eyeX + towardsX;
        double centerY = eyeY;
        double centerZ = eyeZ + towardsZ;

        // Set camera position
        if (thirdPerson) {
            glu.gluLookAt(eyeX - towardsX * 3, eyeY + 1, eyeZ - towardsZ * 3, centerX, centerY, centerZ, 0.0, 1.0, 0.0);
        } else {
            glu.gluLookAt(eyeX - towardsX, eyeY + 0.1, eyeZ - towardsZ, centerX, centerY, centerZ, 0.0, 1.0, 0.0);
        }

        setLighting(gl);

        if (thirdPerson) avatar.draw(gl);

        myTerrain.draw(gl);

        if (raining) rainSystem.draw(gl);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // TODO Auto-generated method stub
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glEnable(GL2.GL_DEPTH_TEST);
        // Cull back faces
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);
        // Turn on OpenGL texturing
        gl.glEnable(GL2.GL_TEXTURE_2D);

        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

        // Lighting
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_LIGHT0);

        // Specify how texture values combine with current surface color values.
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);

        // Init textures
        setTextures(gl);

        // Monster shader
        setShaders(gl);

        // Monster VBO
        setVbos(gl);
    }

    private void setTextures(GL2 gl) {
        MyTexture terrainTexture = new MyTexture(gl, Terrain.TEXTURE, true);
        myTerrain.setTerrainTexture(terrainTexture);

        MyTexture roadTexture = new MyTexture(gl, Road.TEXTURE_ROAD, true);
        myTerrain.setRoadTexture(roadTexture);

        MyTexture treeTrunkTexture = new MyTexture(gl, Tree.TEXTURE_TRUNK, true);
        myTerrain.setTreeTrunkTexture(treeTrunkTexture);

        MyTexture treeLeavesTexture = new MyTexture(gl, Tree.TEXTURE_LEAVES, true);
        myTerrain.setTreeLeavesTexture(treeLeavesTexture);

        MyTexture monsterTexture = new MyTexture(gl, Monster.TEXTURE, true);
        myTerrain.setMonsterTexture(monsterTexture);

        MyTexture rainTexture = new MyTexture(gl, RainSystem.TEXTURE, true);
        rainSystem.setTexture(rainTexture);
    }

    private void setShaders(GL2 gl) {
        try {
            myTerrain.setMonsterShader(Shader.initShaders(gl, Monster.VERTEX_SHADER, Monster.FRAGMENT_SHADER));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void setVbos(GL2 gl) {
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
        float amb[];
        float dif[];
        float spec[];
        float globAmb[];
        float pos[];

        if (!darkMode) {
            // Sunlight
            amb = new float[]{ aSun, aSun, aSun, 1 };
            dif = new float[]{ dSun, dSun, dSun, 1 };
            spec = new float[]{ sSun, sSun, sSun, 1 };
            globAmb = new float[]{ gSun, gSun, gSun, 1 };
            pos = myTerrain.getSunlight();

            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 180);
        } else {
            // Dark mode: torch
            amb = new float[]{ aTorch, aTorch, aTorch, 1 };
            dif = new float[]{ dTorch, dTorch, dTorch, 1 };
            spec = new float[]{ sTorch, sTorch, sTorch, 1 };
            globAmb = new float[]{ gTorch, gTorch, gTorch, 1 };

            pos = new float[4];
            double avatarPos[] = avatar.getPos();
            for (int i = 0; i < 3; i++) {
                pos[i] = (float) avatarPos[i];
            }
            // Set as point light
            pos[3] = 1;

            double a0 = Math.toRadians(avatar.getRotation());
            float towardsX = (float) Math.cos(a0);
            float towardsY = 0;
            float towardsZ = (float) Math.sin(a0);

            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, torchAngle);
        	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, new float[]{towardsX, towardsY, towardsZ}, 0);
        	gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_EXPONENT, torchExponent);
        }

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, amb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, dif, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, spec, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);

        // Global light properties
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb, 0); // Global ambient light.
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, 1); // Enable local viewpoint
    }

    @Override
    public void keyPressed(KeyEvent ev) {
        switch (ev.getKeyCode()) {
            case KeyEvent.VK_T:
                myTerrain.setIsDirectionalLight(darkMode);
                darkMode = !darkMode;
                break;
            case KeyEvent.VK_R:
                raining = !raining;
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
            case KeyEvent.VK_V:
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
