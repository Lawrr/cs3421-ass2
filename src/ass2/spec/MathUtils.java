package ass2.spec;

public class MathUtils {

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
}
