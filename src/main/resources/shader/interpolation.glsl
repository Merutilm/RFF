#version 450

uniform sampler2D inputTex;
uniform ivec2 resolution;

in vec4 fColor;
out vec4 color;


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
    float ix = 1.0 / resolution.x;
    float iy = 1.0 / resolution.y;
    vec4 c1 = texture(inputTex, coord + vec2(ix, 0));
    vec4 c2 = texture(inputTex, coord + vec2(0, iy));
    vec4 c3 = texture(inputTex, coord + vec2(-ix, 0));
    vec4 c4 = texture(inputTex, coord + vec2(0, -iy));
    vec4 c5 = texture(inputTex, coord + vec2(ix, iy));
    vec4 c6 = texture(inputTex, coord + vec2(ix, -iy));
    vec4 c7 = texture(inputTex, coord + vec2(-ix, iy));
    vec4 c8 = texture(inputTex, coord + vec2(-ix, -iy));
    vec4 center = texture(inputTex, coord) * 9;
    vec4 near = (c1 + c2 + c3 + c4) * 3;
    vec4 diagonal = (c5 + c6 + c7 + c8);

    color = (center + near + diagonal) / 25;

}
