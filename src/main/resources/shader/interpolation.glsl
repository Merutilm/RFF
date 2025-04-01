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
    float singlePixel = 1 / resolution.x;
    vec4 c1 = texture(inputTex, coord + vec2(singlePixel, 0));
    vec4 c2 = texture(inputTex, coord + vec2(0, singlePixel));
    vec4 c3 = texture(inputTex, coord + vec2(-singlePixel, 0));
    vec4 c4 = texture(inputTex, coord + vec2(0, -singlePixel));
    vec4 c5 = texture(inputTex, coord);
    color = (c1 + c2 + c3 + c4 + c5) / 5;

}
