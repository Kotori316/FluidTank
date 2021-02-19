package com.kotori316.fluidtank.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;

class Wrapper {
    final IVertexBuilder buffer;

    public Wrapper(IVertexBuilder buffer) {
        this.buffer = buffer;
    }

    private static final Vector3f vector3f = new Vector3f();
    private static final Vector4f vector4f = new Vector4f();

    private static Vector4f getPosVector(float x, float y, float z, MatrixStack matrix) {
        Vector3i vec3i = Direction.UP.getDirectionVec();
        vector3f.set(((float) vec3i.getX()), ((float) vec3i.getY()), ((float) vec3i.getZ()));
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        vector3f.transform(matrix.getLast().getNormal());

        vector4f.set(x, y, z, 1.0F);
        vector4f.transform(matrix4f);
        return vector4f;
    }

    Wrapper pos(double x, double y, double z, MatrixStack matrix) {
        Vector4f vector4f = getPosVector(((float) x), ((float) y), ((float) z), matrix);
        buffer.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ());
        return this;
    }

    Wrapper color(int red, int green, int blue, int alpha) {
        buffer.color(red, green, blue, alpha);
        return this;
    }

    Wrapper tex(float u, float v) {
        buffer.tex(u, v);
        return this;
    }

    @SuppressWarnings("SpellCheckingInspection")
    Wrapper lightmap(int sky, int block) {
        buffer.overlay(10, 10).lightmap(block, sky);
        return this;
    }

    Wrapper lightMap(int light, int overlay) {
        buffer.overlay(overlay).lightmap(light);
        return this;
    }

    void endVertex() {
        buffer.normal(0, 1, 0).endVertex();
    }
}
