package org.fesilu.hinchin;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

/**
 * 因为 Sprite 被人用球了，所以就用 Fairy 吧，随他妈遍。
 */
public class Fairy {
    Fairy() {}
    Fairy(Texture texture, String name, int sx, int sy, int sw, int sh) {
        this.texture = texture;
        this.name = name;
        this.sx = sx;
        this.sy = sy;
        this.sw = sw;
        this.sh = sh;
    }

    /**
     * 使用 batch 来渲染精灵。材质的一小部分
     * @param batch SpriteBatch
     * @param x x
     * @param y y
     * @param scale 缩放大小，譬如 2.0f 就是两倍
     */
    public void draw(SpriteBatch batch, float x, float y, float scale) {
        batch.setTransformMatrix(new Matrix4().setToScaling(scale, scale, scale));
        batch.draw(texture, x, y, sx, sy, sw, sh);
    }

    String name;
    Texture texture;
    int sx, sy, sw, sh;
}
