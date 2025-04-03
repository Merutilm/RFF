#version 450

uniform sampler2D inputTex;
uniform isampler2D iterations;
uniform ivec2 resolution;

uniform bool use;
uniform float firstInterval;
uniform float secondInterval;
uniform float opacity;
uniform float offset;

in vec4 fColor;
out vec4 color;


double getIteration(vec2 coord){
    ivec4 iteration = texture(iterations, coord);
    uvec2 it = uvec2(iteration.y, iteration.x);
    return packDouble2x32(it);
}

void main() {
    vec2 coord = gl_FragCoord.xy / resolution;

    float x = coord.x;
    float y = coord.y;

    if (x < 0 || y < 0){
        discard;
    }
    if (x >= 1 || y >= 1){
        discard;
    }

    double iteration = getIteration(coord);
    color = texture(inputTex, coord);

    if(!use || iteration == 0){
        return;
    }

    float m = float(mod(iteration - offset, firstInterval)) * float(mod(iteration - offset, secondInterval)) / (firstInterval * secondInterval);

    color = vec4((color * (1 - m * opacity)).rgb, 1);
}
