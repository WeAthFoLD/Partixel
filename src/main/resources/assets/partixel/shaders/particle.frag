#version 330

uniform sampler2D uTexture;

in vec2 vUV;
in vec4 vColor;

out vec4 color;

void main() {
    color = vColor * texture2D(uTexture, vUV);
}