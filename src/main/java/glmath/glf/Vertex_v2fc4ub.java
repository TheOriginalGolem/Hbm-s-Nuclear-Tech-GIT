/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glmath.glf;

import java.nio.ByteBuffer;

import glmath.dev.Vec4u8;
import glmath.glm.vec._2.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Vertex_v2fc4ub {

    public static final int SIZE = Vec2.SIZE + Vec4u8.SIZE;

    public Vec2 position;
    public Vec4u8 color;

    public Vertex_v2fc4ub(Vec2 position, Vec4u8 color) {
        this.position = position;
        this.color = color;
    }

    public void toBb(ByteBuffer bb, int index) {
        bb
                .putFloat(index * SIZE, position.x)
                .putFloat(index * SIZE + Float.BYTES, position.y)
                .put(index * SIZE + 2 * Float.BYTES, color.x)
                .put(index * SIZE + 2 * Float.BYTES + Byte.BYTES, color.y)
                .put(index * SIZE + 2 * Float.BYTES + 2 * Byte.BYTES, color.z)
                .put(index * SIZE + 2 * Float.BYTES + 3 * Byte.BYTES, color.w);
    }
}
