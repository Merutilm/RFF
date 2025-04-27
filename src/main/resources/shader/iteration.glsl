#version 450
#extension GL_ARB_gpu_shader_int64 : enable
#define NONE 0
#define NORMAL 1
#define REVERSED 2


uniform ivec2 resolution;

uniform sampler2D iterations;
uniform double maxIteration;

uniform sampler2D palette;
uniform int paletteWidth;
uniform int paletteHeight;
uniform int paletteLength;
uniform float paletteOffset;
uniform float paletteInterval;

uniform int smoothing;

out vec4 color;


double getIteration(ivec2 coord){
    vec4 iteration = texelFetch(iterations, coord, 0);
    uvec2 it = uvec2(floatBitsToUint(iteration.y), floatBitsToUint(iteration.x));
    return packDouble2x32(it);
}

vec4 getColor(double iteration){

    if (iteration == 0 || iteration >= maxIteration){
        return vec4(0, 0, 0, 1);
    }
    switch (smoothing){
        case NONE :
        iteration = int64_t(iteration);
        break;
        case NORMAL :
        break;
        case REVERSED :
        iteration = 2 * int64_t(iteration) + 1 - iteration;
        break;
    }
   
    
    float hSquare = paletteHeight - 1;
    float hRemainder = (paletteLength - paletteWidth * hSquare) / paletteWidth;
    float hLength = hSquare + hRemainder;

    float offset = mod(float(iteration / paletteInterval + paletteOffset), 1) * hLength;
    
    float ox = mod(offset, 1);
    float oy = (floor(offset) + 0.5) / paletteHeight;
    
    return texture(palette, vec2(ox ,oy));
}

void main(){

    ivec2 coord = ivec2(gl_FragCoord.xy);

    int x = coord.x;
    int y = coord.y;

    double iteration = getIteration(coord);

    if (iteration == 0){

        double it2 = 0;
        while (coord.y > 0){
            coord.y -= 1;
            it2 = getIteration(coord);
            if (it2 > 0){
                break;
            }
        }
        color = getColor(it2);
        return;
    }

    color = getColor(iteration);
}