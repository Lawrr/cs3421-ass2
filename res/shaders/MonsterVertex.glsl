#version 130

out vec3 N;
out vec4 v;

out vec2 texCoordV;

in vec4 vertexPos;
in vec3 vertexNorm;
in vec2 vertexTex;

void main (void) {
    v = gl_ModelViewMatrix * vertexPos;
    N = vec3(normalize(gl_NormalMatrix * normalize(vertexNorm)));

	gl_Position = gl_ModelViewProjectionMatrix * vertexPos;
	texCoordV= vec2(vertexTex);

}

