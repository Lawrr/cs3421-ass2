package ass2.spec;

public class MathUtil {

    public static void normalize(double v[]) {
        double d = Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
        if (d != 0.0) {
            v[0]/=d;
            v[1]/=d;
            v[2]/=d;
        }
    }

    public static void normCrossProd(double v1[], double v2[], double out[]) {
        out[0] = v1[1]*v2[2] - v1[2]*v2[1];
        out[1] = v1[2]*v2[0] - v1[0]*v2[2];
        out[2] = v1[0]*v2[1] - v1[1]*v2[0];
        normalize(out);
    }

    public static void billboard(float modelview[]) {
        float ix = modelview[0];
        float iy = modelview[4];
        float iz = modelview[8];
        float i = (float) Math.sqrt(ix * ix + iy * iy + iz * iz);

        float jx = modelview[1];
        float jy = modelview[5];
        float jz = modelview[9];
        float j = (float) Math.sqrt(jx * jx + jy * jy + jz * jz);

        float kx = modelview[2];
        float ky = modelview[6];
        float kz = modelview[10];
        float k = (float) Math.sqrt(kx * kx + ky * ky + kz * kz);

        // i
        modelview[0] = i;
        modelview[1] = 0;
        modelview[2] = 0;

        // j
        modelview[4] = 0;
        modelview[5] = j;
        modelview[6] = 0;

        // k
        modelview[8] = 0;
        modelview[9] = 0;
        modelview[10] = k;
    }

    public static double lerp(double p, double q, double t) {
        return p * (1 - t) + q * t;
    }
}
