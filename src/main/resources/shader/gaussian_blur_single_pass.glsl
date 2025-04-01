#version 450

uniform sampler2D inputTex;
uniform ivec2 resolution;

uniform float radius;

in vec4 fColor;
out vec4 color;


float weight(float distance){
    return exp(-2 * distance * distance / (radius * radius));
}

void main() {

    vec2 coord = gl_FragCoord.xy / resolution;

    float x = coord.x;
    float y = coord.y;
    float i = 0.002;

    if(radius <= 0){
        color = texture(inputTex, coord);
        return;
    }

    if (x < 0 || y < 0){
        discard;
    }

    if (x >= 1 || y >= 1){
        discard;
    }

    vec3 sum = vec3(0, 0, 0);
    int counts = int(2 * radius / i);
    float weightSum = 0;

    float x1 = x - radius;
    float y1 = y - radius;

    for (int j = 0; j < counts; j++){
        float w = weight(x - x1);
        sum += texture(inputTex, vec2(x1, y)).rgb * w;
        sum += texture(inputTex, vec2(x, y1)).rgb * w;
        x1 += i;
        y1 += i;
        weightSum += 2 * w;
    }


    color = vec4(sum / weightSum, 1);
}
