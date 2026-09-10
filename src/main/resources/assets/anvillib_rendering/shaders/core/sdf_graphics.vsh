#version 150

// 1.20.1 版本：原 26.x 实现用 std140 UBO 装 256 个形状、顶点靠 UV1.x 索引。
// 1.20.1 的 ShaderInstance 既不支持 UBO 也不支持 uniform 数组，
// 因此改为「一形状一次 draw call + 逐形状 uniform」——GUI 一帧几十个形状，开销可忽略。

uniform mat4            ModelViewMat;
uniform mat4            ProjMat;
uniform vec4            SdfRect;

in      vec3            Position;
in      vec4            Color;
in      vec2            UV0;

out     vec2            vPosition;
out     vec4            vColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vPosition   = (UV0 - 0.5) * SdfRect.zw;
    vColor      = Color;
}
