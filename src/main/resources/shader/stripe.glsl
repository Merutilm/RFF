#version 450

uniform sampler2D inputTex;
uniform sampler2D iterations;

uniform bool use;
uniform float firstInterval;
uniform float secondInterval;
uniform float opacity;
uniform float offset;

in vec4 fColor;
out vec4 color;


double getIteration(ivec2 coord){
    vec4 iteration = texelFetch(iterations, coord, 0);
    uvec2 it = uvec2(floatBitsToUint(iteration.y), floatBitsToUint(iteration.x));
    return packDouble2x32(it);
}

void main() {
    ivec2 coord = ivec2(gl_FragCoord.xy);

    double iteration = getIteration(coord);
    color = texelFetch(inputTex, coord, 0);

    if(!use || iteration == 0){
        return;
    }

    float m = float(mod(iteration - offset, firstInterval)) * float(mod(iteration - offset, secondInterval)) / (firstInterval * secondInterval);

    color = vec4((color * (1 - m * opacity)).rgb, 1);
}
