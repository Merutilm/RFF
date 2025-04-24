#version 450
#define PI 3.141592653589793238

uniform sampler2D inputTex;
uniform sampler2D iterations;

uniform float depth;
uniform float reflectionRatio;
uniform float opacity;
uniform float zenith;
uniform float azimuth;

in vec4 fColor;
out vec4 color;

double getIteration(ivec2 coord){
    vec4 iteration = texelFetch(iterations, coord, 0);
    uvec2 it = uvec2(floatBitsToUint(iteration.y), floatBitsToUint(iteration.x));
    return packDouble2x32(it);
}


void main() {

    ivec2 coord = ivec2(gl_FragCoord.xy);

    if(depth <= 0 || reflectionRatio >= 1){
        color = texelFetch(inputTex, coord, 0);
        return;
    }


    float aRad = radians(azimuth);
    float zRad = radians(zenith);

    double ld = getIteration(coord + ivec2(-1, -1));
    double d = getIteration(coord + ivec2(0, -1));
    double rd = getIteration(coord + ivec2(1, -1));
    double l = getIteration(coord + ivec2(-1, 0));
    double r = getIteration(coord + ivec2(1, 0));
    double lu = getIteration(coord + ivec2(-1, 1));
    double u = getIteration(coord + ivec2(0, 1));
    double ru = getIteration(coord + ivec2(1, 1));

    float dzDx = float((rd + 2 * r + ru) - (ld + 2 * l + lu)) * depth;
    float dzDy = float((lu + 2 * u + ru) - (ld + 2 * d + rd)) * depth;
    float slope = atan(radians(length(vec2(dzDx, dzDy))), 1);
    float aspect = atan(dzDy, -dzDx);
    float shade = max(reflectionRatio, cos(zRad) * cos(slope) + sin(zRad) * sin(slope) * cos(aRad + aspect));
    float fShade = 1 - opacity * (1 - shade);


    color = vec4(texelFetch(inputTex, coord, 0).rgb * fShade, 1);
}
