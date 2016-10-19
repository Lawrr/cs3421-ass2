package ass2.spec;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import java.nio.FloatBuffer;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Monster {

    public static final String TEXTURE = Game.TEXTURES_DIRECTORY + "monster.png";

    public static final String VERTEX_SHADER = Game.SHADERS_DIRECTORY + "MonsterVertex.glsl";
    public static final String FRAGMENT_SHADER = Game.SHADERS_DIRECTORY + "MonsterFragment.glsl";

    public static final float POSITIONS[] = {
            // Front
            0, 0, 1,
            1, 0, 1,
            1, 1, 1,
            0, 1, 1,

            // Back
            1, 0, 0,
            0, 0, 0,
            0, 1, 0,
            1, 1, 0,

            // Left
            0, 0, 0,
            0, 0, 1,
            0, 1, 1,
            0, 1, 0,

            // Right
            1, 0, 1,
            1, 0, 0,
            1, 1, 0,
            1, 1, 1,

            // Top
            0, 1, 1,
            1, 1, 1,
            1, 1, 0,
            0, 1, 0,

            // Bottom
            0, 0, 0,
            1, 0, 0,
            1, 0, 1,
            0, 0, 1
    };

    public static final float NORMALS[] = {
            // Front
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,

            // Back
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,

            // Left
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,

            // Right
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,

            // Top
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,

            // Bottom
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0
    };

    public static final float TEXTURES[] = {
            0, 0, 1, 0, 1, 1, 0, 1,
            0, 0, 1, 0, 1, 1, 0, 1,
            0, 0, 1, 0, 1, 1, 0, 1,
            0, 0, 1, 0, 1, 1, 0, 1,
            0, 0, 1, 0, 1, 1, 0, 1,
            0, 0, 1, 0, 1, 1, 0, 1
    };

    private double[] myPos;

    private MyTexture texture;

    private int bufferIds[];
    private FloatBuffer posData;
    private FloatBuffer normData;
    private FloatBuffer texData;

    private int shader;

    public Monster(double x, double y, double z) {
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
        gl.glScaled(0.5, 0.5, 0.5);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        // Set colours
        float matAmbAndDif[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matSpec[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matShine[] = {10.0f};
        float emm[] = {0.0f, 0.0f, 0.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpec, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShine, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emm, 0);

        // Enable shader
        gl.glUseProgram(shader);

        // Set texture
        gl.glBindTexture(GL2.GL_TEXTURE_2D, texture.getTextureId());

        // Tell the shader our texUnit is the 0th one
        int texUnitLoc = gl.glGetUniformLocation(shader, "texUnit");
        gl.glUniform1i(texUnitLoc, 0);

        // Set sun/torch
        int isDirectionalLoc = gl.glGetUniformLocation(shader, "isDirectional");
        gl.glUniform1i(isDirectionalLoc, terrain.isDirectionalLight() ? 1 : 0);

        // Bind the buffer we want to use
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferIds[0]);

        // Give location of the arrays in the buffer
        int vertexPosLoc = gl.glGetAttribLocation(shader, "vertexPos");
        int vertexNormLoc = gl.glGetAttribLocation(shader, "vertexNorm");
        int vertexTexLoc = gl.glGetAttribLocation(shader, "vertexTex");

        gl.glEnableVertexAttribArray(vertexPosLoc);
        gl.glVertexAttribPointer(vertexPosLoc,
                3, // 3 coordinates per vertex
                GL.GL_FLOAT, // Type of a coordinate
                false,
                0, // Stride
                0); // Offset in array

        gl.glEnableVertexAttribArray(vertexNormLoc);
        gl.glVertexAttribPointer(vertexNormLoc, 3, GL.GL_FLOAT, true, 0, POSITIONS.length * Float.BYTES);

        gl.glEnableVertexAttribArray(vertexTexLoc);
        gl.glVertexAttribPointer(vertexTexLoc, 2, GL.GL_FLOAT, false, 0, POSITIONS.length * Float.BYTES + NORMALS.length * Float.BYTES);

        // Set wrap mode for texture in S direction
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
        // Set wrap mode for texture in T direction
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);

        // Draw
        gl.glDrawArrays(GL2.GL_QUADS, 0, POSITIONS.length / 3);

        // Disable
        gl.glDisableVertexAttribArray(vertexPosLoc);
        gl.glDisableVertexAttribArray(vertexNormLoc);
        gl.glDisableVertexAttribArray(vertexTexLoc);

        gl.glUseProgram(0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);

        gl.glPopMatrix();
    }

    public void setTexture(MyTexture texture) {
        this.texture = texture;
    }

    public void setShader(int shader) {
        this.shader = shader;
    }

    public void setVbo(int bufferIds[], FloatBuffer posData, FloatBuffer normData, FloatBuffer texData) {
        this.bufferIds = bufferIds;
        this.posData = posData;
        this.normData = normData;
        this.texData = texData;
    }
}
