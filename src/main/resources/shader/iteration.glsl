#version 450
#extension GL_ARB_gpu_shader_int64 : enable
#define NONE 0
#define NORMAL 1
#define REVERSED 2


uniform ivec2 resolution;

uniform isampler2D iterations;
uniform int[2] maxIteration;

uniform sampler2D palette;
uniform int paletteWidth;
uniform int paletteHeight;
uniform int paletteLength;
uniform float paletteOffset;
uniform float paletteInterval;

uniform int smoothing;

out vec4 color;


int64_t getMaxIteration(){
    return (int64_t(maxIteration[0]) << 32) + int64_t(maxIteration[1]);
}


double getIteration(vec2 coord){
    ivec4 iteration = texture(iterations, coord);
    uvec2 it = uvec2(iteration.y, iteration.x);
    return packDouble2x32(it);
}


vec4 getColor(double iteration, int64_t max){
    if (iteration == 0 || iteration >= max){
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
    int64_t max = getMaxIteration();

    if (iteration == 0){
        double it2 = 0;
        while (coord.y > 0){
            coord.y -= 1.0 / resolution.y;
            it2 = getIteration(coord);
            if (it2 > 0){
                break;
            }
        }
        color = getColor(it2, max);
        return;
    }

    color = getColor(iteration, max);
}