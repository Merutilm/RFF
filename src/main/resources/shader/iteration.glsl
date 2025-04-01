#version 450
#extension GL_ARB_gpu_shader_int64 : enable

uniform ivec2 resolution;

uniform isampler2D iterations;
uniform int[2] maxIteration;

uniform sampler1D palette;
uniform float paletteOffset;
uniform float paletteInterval;

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
    if(iteration == 0 || iteration >= max){
        return vec4(0, 0, 0, 1);
    }
    return texture(palette, mod(float(iteration / paletteInterval + paletteOffset), 1));
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