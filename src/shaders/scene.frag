#version 330

in  vec2 outTextCoord;
out vec4 fragColor;

uniform sampler2D txtSampler;

void main()
{
    vec4 texColor = texture(txtSampler, outTextCoord);

    vec3 lightColor = vec3(1.0, 0.7, 0.4);
    float ambientStrength = 0.9;

    // Apply lighting only to the RGB parts
    vec3 ambient = ambientStrength * lightColor;
    vec3 finalRGB = ambient * texColor.rgb;

    // Use original alpha so transparent parts of the texture stay transparent
    fragColor = vec4(finalRGB, texColor.a);
}