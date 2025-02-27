#version 450
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;


out vec4 fColor;
out vec3 vPos;

void main(){
    fColor = aColor;
    gl_Position = vec4(aPos, 1.0);
    vPos = aPos;
}
