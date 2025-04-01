#version 450

uniform sampler2D inputTex;
uniform sampler2D original;
uniform ivec2 resolution;

uniform float radius;
uniform float threshold;

in vec4 fColor;
out vec4 color;

float grayScale(vec3 c){
    return c.r * 0.3 + c.g + 0.59 + c.b * 0.11;
}

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

    if(grayScale(texture(original, coord).rgb) < threshold){
        color = vec4(0, 0, 0, 1);
        return;
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
