#version 450

uniform sampler2D inputTex;
uniform ivec2 resolution;

uniform float gamma;
uniform float exposure;
uniform float saturation;
uniform float brightness;
uniform float contrast;

in vec4 fColor;
out vec4 color;

float grayScale(vec3 c){
    return c.r * 0.3 + c.g * 0.59 + c.b * 0.11;
}

vec3 fixColor(vec3 col){
    return clamp(col, vec3(0, 0, 0), vec3(1, 1, 1));
}

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

    vec3 c = texture(inputTex, coord).rgb;

    c = fixColor(pow(c.rgb, vec3(1 / gamma)));
    c = fixColor(c * (1 + exposure) / (1 - exposure));
    float gray = grayScale(c);
    c = fixColor(c + (c - vec3(gray, gray, gray)) * saturation);
    c = fixColor(c + brightness);
    c = fixColor((c - 0.5) / (1 - contrast) * (1 + contrast) + 0.5);
    color = vec4(c, 1);
}
