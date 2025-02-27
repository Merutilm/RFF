#version 450

uniform double x;
uniform double y;
uniform double zoom;
uniform int maxIteration;
uniform double time;
uniform vec2 power;
uniform vec2 resolution;

out vec4 color;

in vec4 fColor;
in vec3 vPos;

void main(){

    int iteration = 0;

    vec2 c = vec2(x + vPos.x * zoom, y + vPos.y * zoom * resolution.x / resolution.y);
    vec2 z = c;
    while (distance(z, vec2(0, 0)) < 2 && iteration < maxIteration){
        z = vec2(z.x * z.x - z.y * z.y + c.x, 2 * z.x * z.y + c.y);
        iteration++;
    }

    if (iteration == maxIteration){
        color = vec4(0, 0, 0, 1);
        return;
    }

    float value = (sin(iteration / 50.0 + float(time) / 4.0) + 1) / 2;
    color = vec4(value, value, value, 1);

}