#version 450
#define PI 3.141592653589793238

uniform sampler2D inputTex;
uniform isampler2D iterations;
uniform ivec2 resolution;

uniform float depth;
uniform float reflectionRatio;
uniform float opacity;
uniform float zenith;
uniform float azimuth;

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

    if(depth <= 0 || reflectionRatio >= 1){
        color = texture(inputTex, coord);
        return;
    }

    if (x < 0 || y < 0){
        discard;
    }
    if (x >= 1 || y >= 1){
        discard;
    }

    float aRad = radians(azimuth);
    float zRad = radians(zenith);
    float ix = 1.0 / resolution.x;
    float iy = 1.0 / resolution.y;

    double ld = getIteration(vec2(x - ix, y - iy));
    double d = getIteration(vec2(x, y - iy));
    double rd = getIteration(vec2(x + ix, y - iy));
    double l = getIteration(vec2(x - ix, y));
    double r = getIteration(vec2(x + ix, y));
    double lu = getIteration(vec2(x - ix, y + iy));
    double u = getIteration(vec2(x, y + iy));
    double ru = getIteration(vec2(x + ix, y + iy));

    float dzDx = float((rd + 2 * r + ru) - (ld + 2 * l + lu)) * depth;
    float dzDy = float((lu + 2 * u + ru) - (ld + 2 * d + rd)) * depth;
    float slope = atan(radians(length(vec2(dzDx, dzDy))), 1);
    float aspect = atan(dzDy, -dzDx);
    float shade = max(reflectionRatio, cos(zRad) * cos(slope) + sin(zRad) * sin(slope) * cos(aRad + aspect));
    float fShade = 1 - opacity * (1 - shade);


    color = vec4(texture(inputTex, coord).rgb * fShade, 1);
}
