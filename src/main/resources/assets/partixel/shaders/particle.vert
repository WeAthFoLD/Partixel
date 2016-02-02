#version 330

uniform mat4 uWVPMatrix;
uniform vec2 uUVSpan;

// vertex attribute
layout (location = 0) in vec3 gPosition;
layout (location = 1) in vec2 gUV;

// per-instance
layout (location = 2) in vec2 iUVBegin;
layout (location = 3) in vec4 iColor;
layout (location = 4) in mat4 iTransform;

out vec2 vUV;
out vec4 vColor;

void main() {
    gl_Position = uWVPMatrix * iTransform * vec4(gPosition, 1);

    vUV = gUV;
    vColor = iColor;
}