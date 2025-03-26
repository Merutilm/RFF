#version 450

uniform float time;

void main(){

    float value = (sin(time / 4.0) + 1) / 2;
    color = vec4(value, value, value, 1);
}