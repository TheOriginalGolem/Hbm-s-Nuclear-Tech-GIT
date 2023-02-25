package com.hbm.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

public class BakingUtils {

	public void putVertex(TextureAtlasSprite sprite, int[] data, int offset, float x, float y, float z, float texU, float texV, int normal){
        // Position
        data[offset + 0] = Float.floatToIntBits(x);
        data[offset + 1] = Float.floatToIntBits(y);
        data[offset + 2] = Float.floatToIntBits(z);
        // Color (set to all white)
        data[offset + 3] = 0xFFFFFFFF;
        // Texture coordinates, remapped to the sprite range
        data[offset + 4] = Float.floatToIntBits(sprite.getMinU() + (sprite.getMaxU() - sprite.getMinU()) * texU);
        data[offset + 5] = Float.floatToIntBits(sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * texV);
        // Normal (3 bytes)
        data[offset + 6] = normal;
    }
    
        public BakedQuad makeBakedQuad(
        TextureAtlasSprite sprite, float x1, float y1, float z1, float u1, float v1,
        float x2, float y2, float z2, float u2, float v2,
        float x3, float y3, float z3, float u3, float v3,
        float x4, float y4, float z4, float u4, float v4,
        float normalX, float normalY, float normalZ
    ){
        int encodedNormal = ((int)(normalX * 127.0F)) << 16;
        encodedNormal |= ((int)(normalY * 127.0F)) << 8;
        encodedNormal |= (int)(normalZ * 127.0F);
        
        int[] data = new int[7 * 4];
        putVertex(sprite, data, 0,  x1, y1, z1, u1, v1, encodedNormal);
        putVertex(sprite,data, 7,  x2, y2, z2, u2, v2, encodedNormal);
        putVertex(sprite, data, 14, x3, y3, z3, u3, v3, encodedNormal);
        putVertex(sprite, data, 21, x4, y4, z4, u4, v4, encodedNormal);
        return new BakedQuad(data, -1, EnumFacing.UP, sprite, true, DefaultVertexFormats.ITEM);
    }
        
}

