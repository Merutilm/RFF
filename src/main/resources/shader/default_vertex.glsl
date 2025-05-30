#version 450
#define PI 3.1415926535897932

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;



out vec4 fColor;

void main() {
    fColor = aColor;
    float far = 1024;
    gl_Position = vec4(aPos.xy / aPos.z, -aPos.z / far, 1);

}
