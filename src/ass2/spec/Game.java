package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

	private Terrain myTerrain;
	private Avatar avatar;

	private boolean light0On = true; // White light on
	private boolean light1On = false; // Blue light on

	//Setting for light 0
	private float a = 0.2f; // Ambient white light intensity.
	private float d = 0.5f; // Diffuse white light intensity
	private float s = 0.2f; // Specular white light intensity.
	private int p = 1; // Positional light 1, directional 0
	private float xAngle = 0.0f, yAngle = 0.0f; // Rotation angles of white light.


	//Global Settings
	private float g = 0.2f; // Global Ambient intensity.
	private int localViewer = 0; // Local viewpoint?

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
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		GLUT glut = new GLUT();
		GLU glu = new GLU();

		// Camera
		double[] pos = avatar.getPos();
		glu.gluLookAt(pos[0], pos[1], pos[2], 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);

//		setLighting(gl);

//		avatar.draw(gl);
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

		// Turn lights off/on.
		if (light0On) gl.glEnable(GL2.GL_LIGHT0); else gl.glDisable(GL2.GL_LIGHT0);
		if (light1On) gl.glEnable(GL2.GL_LIGHT1); else gl.glDisable(GL2.GL_LIGHT1);

		// Light property vectors.
		float lightAmb[] = { a, a, a, 1.0f };
		float lightDif0[] = { d, d, d, 1.0f };
		float lightSpec0[] = { s, s, s, 1.0f };

		float light1[] = { 0.0f, 0.0f, 1.0f, 1.0f };

		float globAmb[] = { g, g, g, 1.0f };

		// Light0 properties.
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb,0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDif0,0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpec0,0);

		// Light1 properties.
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, light1,0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, light1,0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, light1,0);

		// Global light properties
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb,0); // Global ambient light.
		gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, localViewer); // Enable local viewpoint
		drawAndPositionLights(gl);
	}

	private void drawAndPositionLights(GL2 gl){
		GLUT glut = new GLUT();
		float lightPos0[] = { 0.0f, 0.0f, 3.0f, p };
		float lightPos1[] = { 1.0f, 2.0f, 0.0f, 1.0f };

		// Light0 positioned
		// sphere (positional light)
		// or arrow (directional light).
		gl.glPushMatrix();{
			//Transformations to move lights
			gl.glRotated(xAngle, 1.0, 0.0, 0.0); // Rotation about x-axis.
			gl.glRotated(yAngle, 0.0, 1.0, 0.0); // Rotation about z-axis.
			//The light pos will be subject to current transformation
			//matrix, so will be rotated
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos0,0);

			//Also translate to draw to representation of the light
			//Usually you would not do this if you did not
			//want to actually draw the light.
			gl.glTranslatef(lightPos0[0], lightPos0[1], lightPos0[2]);


			float emmL[] = {1.0f, 1.0f, 1.0f, 1.0f};
			float matAmbAndDifL[] = {0.0f, 0.0f, 0.0f, 1.0f};
			float matSpecL[] = { 0.0f, 0.0f, 0.0f, 1.0f };
			float matShineL[] = { 50.0f };

			// Material properties of sphere.
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDifL,0);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpecL,0);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShineL,0);

			//Since the actual sphere/arrow representing the light will not necessarily be hit
			//by any light and we want to, see it, we give it an emissive property.
			//The other alternative would be to temporarily turn off lighting.
			//We do this for the other light to give an example of both ways
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emmL,0);
			if (light0On)
			{
				if (p == 1) glut.glutSolidSphere(0.05, 8, 8); // Sphere at positional light source.
				else // Arrow pointing along incoming directional light.
				{
					gl.glLineWidth(3.0f);
					gl.glBegin(GL2.GL_LINES);
					gl.glVertex3d(0.0, 0.0, 0.25);
					gl.glVertex3d(0.0, 0.0, -0.25);
					gl.glVertex3d(0.0, 0.0, -0.25);
					gl.glVertex3d(0.05, 0.0, -0.2);
					gl.glVertex3d(0.0, 0.0, -0.25);
					gl.glVertex3d(-0.05, 0.0, -0.2);
					gl.glEnd();
					gl.glLineWidth(1.0f);
				}
			}
		}gl.glPopMatrix();

		//Light 1 positioned and drawn
		//Just for an example, instead of using emissive light we
		//Disable lighting temporarily so we can see the actual sphere
		//representing light 2
		gl.glDisable(GL2.GL_LIGHTING);
		// Light1 and its sphere positioned.
		gl.glPushMatrix();{
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos1,0);
			gl.glTranslated(lightPos1[0], lightPos1[1], lightPos1[2]);
			gl.glColor3f(0.0f, 0.0f, 1.0f);
			if (light1On) glut.glutWireSphere(0.05, 8, 8);
		}gl.glPopMatrix();
		//Enable it again for the rest of scene
		gl.glEnable(GL2.GL_LIGHTING);
	}

	@Override
	public void keyPressed(KeyEvent ev) {
		switch (ev.getKeyCode()) {
			case KeyEvent.VK_W:
				light0On = !light0On;

				break;
			case KeyEvent.VK_B:
				light1On = !light1On;
				break;
			case KeyEvent.VK_L:
				if(localViewer == 1) localViewer = 0;
				else localViewer = 1;
				System.out.println("Local viewer " + localViewer);
				break;
			case KeyEvent.VK_P:
				//Positional vs directional
				if(p == 1) p = 0;
				else p = 1;

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
			case KeyEvent.VK_T:
				avatar.translate(0, 1, 0);
				break;
			case KeyEvent.VK_G:
				avatar.translate(0, -1, 0);
				break;
			case KeyEvent.VK_UP:
				avatar.translate(0, 0, -1);
				break;
			case KeyEvent.VK_DOWN:
				avatar.translate(0, 0, 1);
				break;
			case KeyEvent.VK_LEFT:
				avatar.translate(-1, 0, 0);
				break;
			case KeyEvent.VK_RIGHT:
				avatar.translate(1, 0, 0);
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
