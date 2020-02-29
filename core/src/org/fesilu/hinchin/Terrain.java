package org.fesilu.hinchin;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;


public class Terrain {
    public Terrain() {}
    public Terrain(Vector2 snatch, Fairy fairy, float scale) {
        this.snatch = snatch;
        this.fairy = fairy;
        this.scale = scale;
    }

    /**
     * 使用 batch 来渲染角色内的精灵。
     * @param batch SpriteBatch
     */
    public void draw(SpriteBatch batch) {
        Vector2 position = snatch.cpy().scl(fairy.sw, fairy.sh);
        fairy.draw(batch, position.x, position.y, scale);
    }

    private Fairy fairy;
    private Vector2 snatch;
    private float scale;
}