package org.fesilu.hinchin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import javax.xml.crypto.dsig.spec.HMACParameterSpec;


public class Terrain {
    public Terrain() {}
    public Terrain(Vector2 snatch, boolean obstacle, Fairy fairy, float scale) {
        this.snatch = snatch;
        this.fairy = fairy;
        this.scale = scale;
        this.obstacle = obstacle;
        this.discovered = false;
    }

    /**
     * 当前这个物体是不是一个障碍物？
     * @return 是不是障碍物
     */
    public boolean isObstacle() {
        return obstacle;
    }

    /**
     * 返回当前地形名字。
     * @return 地形名字
     */
    public String getName() {
        return fairy.name;
    }

    /**
     * 使用 batch 来渲染角色内的精灵。
     * @param batch SpriteBatch
     */
    public void draw(SpriteBatch batch) {
        Vector2 position = snatch.cpy().scl(fairy.sw, fairy.sh);
        fairy.draw(batch, position.x, position.y, scale);
    }

    public void drawSilhouette(ShapeRenderer shape) {
        // OpenGL Alpha 混合
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setColor(0.1f, 0.1f, 0.1f, 0.8f);
        Vector2 position = snatch.cpy().scl(fairy.sw, fairy.sh);
        shape.rect(position.x * scale, position.y * scale, fairy.sw * scale, fairy.sh * scale);
    }

    public boolean isVisibleTo(GameMap map, Vector2 position) {
        Vector2 dPos = position.cpy().sub(snatch);
        float distance = dPos.len();
        if (distance >= 10.0f) {
            return false;
        }
        Vector2 dir = dPos.cpy().nor();

        Vector2 ray = snatch.cpy();
        while (ray.cpy().sub(snatch).len() <= distance) {
            ray.add(dir);
            int roundX = Math.round(ray.x);
            int roundY = Math.round(ray.y);
            if (roundX == Math.round(position.x) && roundY == Math.round(position.y)) {
                discovered = true;
                return true;
            }
            if (roundX == Math.round(snatch.x) && roundY == Math.round(snatch.y)) {
                continue;
            }
            if (roundX < 0 || roundX >= map.mapSize.x || roundY < 0 || roundY >= map.mapSize.y ||
                    map.map[roundY][roundX].isObstacle()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 视野变量
     */
    boolean discovered;

    private Fairy fairy;
    private Vector2 snatch;
    private float scale;
    private boolean obstacle;
}
