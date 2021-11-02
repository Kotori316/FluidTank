package com.kotori316.fluidtank.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;

record Wrapper(VertexConsumer buffer) {

    private static final Vector4f vector4f = new Vector4f();

    private static Vector4f getPosVector(float x, float y, float z, PoseStack matrix) {
        Matrix4f matrix4f = matrix.last().pose();

        vector4f.set(x, y, z, 1.0F);
        vector4f.transform(matrix4f);
        return vector4f;
    }

    Wrapper pos(double x, double y, double z, PoseStack matrix) {
        Vector4f vector4f = getPosVector(((float) x), ((float) y), ((float) z), matrix);
        buffer.vertex(vector4f.x(), vector4f.y(), vector4f.z());
        return this;
    }

    Wrapper color(int red, int green, int blue, int alpha) {
        buffer.color(red, green, blue, alpha);
        return this;
    }

    Wrapper tex(float u, float v) {
        buffer.uv(u, v);
        return this;
    }

    @SuppressWarnings("SpellCheckingInspection")
    Wrapper lightmap(int sky, int block) {
        buffer.overlayCoords(10, 10).uv2(block, sky);
        return this;
    }

    Wrapper lightMap(int light, int overlay) {
        buffer.overlayCoords(overlay).uv2(light);
        return this;
    }

    void endVertex() {
        buffer.normal(0, 1, 0).endVertex();
    }
}
