package ass2.spec;

import com.jogamp.opengl.GL2;

public class RainSystem {

    public static final int NUM_DROPLETS = 10000;

    public static final String TEXTURE = Game.TEXTURES_DIRECTORY + "rain.bmp";

    private static final float GRAVITY = -0.0001f;

    private Droplet droplets[] = new Droplet[NUM_DROPLETS];
    private Avatar avatar;

    public RainSystem(Avatar avatar) {
        this.avatar = avatar;

        for (int i = 0; i < droplets.length; i++) {
            droplets[i] = new Droplet();
        }
    }

    public void draw(GL2 gl) {
        // Enable Blending
        gl.glEnable(GL2.GL_BLEND);
        // Creates an additive blend, which looks spectacular on a black background
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);

        for (Droplet d : droplets) {
            d.draw(gl);
            d.evolve();
        }

        // Disable/set default
        gl.glDisable(GL2.GL_BLEND);
    }

    public void setTexture(MyTexture texture) {
        for (Droplet d : droplets) {
            d.setTexture(texture);
        }
    }

    class Droplet {
        private MyTexture texture;
        private float pos[] = new float[3];
        private float speedX;
        private float speedY;
        private float speedZ;
        private float gravity;

        public Droplet() {
            reset();
        }

        private void reset() {
            double[] avatarPos = avatar.getPos();
            pos[0] = (float) avatarPos[0] - 25 + (float) Math.random() * 50;
            pos[1] = (float) avatarPos[1] + 7 + (float) Math.random() * 25;
            pos[2] = (float) avatarPos[2] - 25 + (float) Math.random() * 50;
            speedX = -0.005f + (float) Math.random() * 0.01f;
            speedY = 0;
            speedZ = -0.005f + (float) Math.random() * 0.01f;
            gravity = GRAVITY + (GRAVITY * (float) Math.random() * 3);
        }

        void draw(GL2 gl) {
            gl.glPushMatrix();
            gl.glTranslated(pos[0], pos[1], pos[2]);
            gl.glScaled(0.1, 0.1, 0.1);

            float matAmbAndDif[] = {1.0f, 1.0f, 1.0f, 1.0f};
            float matSpec[] = {0.0f, 0.0f, 0.0f, 1.0f};
            float matShine[] = {0.0f};
            float emm[] = {0.0f, 0.0f, 0.0f, 1.0f};
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif, 0);
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpec, 0);
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShine, 0);
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emm, 0);

            gl.glBindTexture(GL2.GL_TEXTURE_2D, texture.getTextureId());
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);

            // Billboard
            float modelview[] = new float[16];
            gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelview, 0);
            MathUtil.billboard(modelview);
            gl.glLoadMatrixf(modelview, 0);

            // Draw
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2d(1, 1);
            gl.glVertex3f(0.5f, 0.5f, 0); // Top Right
            gl.glTexCoord2d(0, 1);
            gl.glVertex3f(-0.5f, 0.5f, 0); // Top Left
            gl.glTexCoord2d(0, 0);
            gl.glVertex3f(-0.5f, -0.5f, 0); // Bottom Left
            gl.glTexCoord2d(1, 0);
            gl.glVertex3f(0.5f, -0.5f, 0); // Bottom Right
            gl.glEnd();

            gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);

            gl.glPopMatrix();
        }

        void evolve() {
            if (pos[1] < 0) {
                reset();
            } else {
                pos[0] += speedX;
                pos[1] += speedY;
                pos[2] += speedZ;
                speedY += gravity;
            }
        }

        void setTexture(MyTexture texture) {
            this.texture = texture;
        }

    }
}
