/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */

// Border Extension and Ouline Effect Fragment Shader for TwoColorPolygonBatch.

varying vec2 v_texCoords;       // From VS
uniform sampler2D u_texture;    // From TCPB
uniform float u_pma;            // From TCPB
uniform vec3 u_outlineColor;    // Required
uniform float u_outlineWidth;   // Required

const float c_opacity_threshold = 0.1;

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    u_pma; // Not used

    if (u_outlineWidth != 0.0) {
        ivec2 texSize = textureSize(u_texture, 0);
        vec2 relOutlineWidth = vec2(u_outlineWidth * (1.0 / texSize.x), u_outlineWidth * (1.0 / texSize.y));

        float neighborAlpha = 0.0;
        neighborAlpha += texture2D(u_texture, v_texCoords + vec2(relOutlineWidth.x, 0.0)).a;
        neighborAlpha += texture2D(u_texture, v_texCoords - vec2(relOutlineWidth.x, 0.0)).a;
        neighborAlpha += texture2D(u_texture, v_texCoords + vec2(0.0, relOutlineWidth.y)).a;
        neighborAlpha += texture2D(u_texture, v_texCoords - vec2(0.0, relOutlineWidth.y)).a;
        neighborAlpha += texture2D(u_texture, v_texCoords + vec2(relOutlineWidth.x, relOutlineWidth.y)).a;
        neighborAlpha += texture2D(u_texture, v_texCoords - vec2(relOutlineWidth.x, relOutlineWidth.y)).a;
        neighborAlpha += texture2D(u_texture, v_texCoords + vec2(relOutlineWidth.x, -relOutlineWidth.y)).a;
        neighborAlpha += texture2D(u_texture, v_texCoords - vec2(relOutlineWidth.x, -relOutlineWidth.y)).a;

        if (texColor.a < c_opacity_threshold && neighborAlpha > c_opacity_threshold) {
            gl_FragColor.rgb = u_outlineColor.rgb;
            gl_FragColor.a = min(1.0, neighborAlpha);
        } else {
            gl_FragColor = texColor;
        }
    } else {
        gl_FragColor = texColor;
    }
}
