#version 450

uniform sampler2D inputTex;
uniform sampler2D iterations;
uniform float resolutionMultiplier;

uniform bool use;
uniform float firstInterval;
uniform float secondInterval;
uniform float opacity;
uniform float offset;

in vec4 fColor;
out vec4 color;


double getIteration0(ivec2 iterCoord){
    vec4 iteration = texelFetch(iterations, iterCoord, 0);
    uvec2 it = uvec2(floatBitsToUint(iteration.y), floatBitsToUint(iteration.x));
    return packDouble2x32(it);
}

double getIteration(vec2 coord){
    vec2 iterCoord = coord * resolutionMultiplier;
    vec2 dec = mod(iterCoord, 1);

    double i1 = getIteration0(ivec2(iterCoord));
    double i2 = getIteration0(ivec2(iterCoord) + ivec2(1, 0));
    double i3 = getIteration0(ivec2(iterCoord) + ivec2(0, 1));
    double i4 = getIteration0(ivec2(iterCoord) + ivec2(1, 1));

    double i5 = i1 - (i1 - i2) * dec.x;
    double i6 = i3 - (i3 - i4) * dec.x;

    if(i5 == 0){
        i5 = i6;
    }
    if(i6 == 0){
        i6 = i5;
    }

    return i5 - (i5 - i6) * dec.y;
}


void main() {
    vec2 coord = gl_FragCoord.xy;

    double iteration = getIteration(coord);
    color = texelFetch(inputTex, ivec2(coord), 0);

    if(!use || iteration == 0){
        return;
    }

    float m = float(mod(iteration - offset, firstInterval)) * float(mod(iteration - offset, secondInterval)) / (firstInterval * secondInterval);

    color = vec4((color * (1 - m * opacity)).rgb, 1);
}
