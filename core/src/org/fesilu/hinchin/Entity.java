package org.fesilu.hinchin;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class Entity {
    public Entity() {}
    public Entity(Vector2 snatch, Fairy fairy, float scale) {
        this.fairy = fairy;
        this.snatch = snatch;
        this.scale = scale;

        size = new Vector2(this.fairy.sw, this.fairy.sh);
        size.scl(scale);
        this.position = new Vector2(snatch.cpy().scl(size.cpy().scl(0.5f)));
    }

    /**
     * 使用 batch 来渲染角色内的精灵。
     * @param batch SpriteBatch
     */
    public void draw(SpriteBatch batch) {
        fairy.draw(batch, position.x, position.y, scale);
    }

    /**
     * 更新角色。角色会尝试把自己卡在期望的格子上（假如他不在上面的话）。
     * @param deltaTime 时间差
     */
    public void update(float deltaTime) {
        Vector2 deltaPos = snatch.cpy().scl(size.cpy().scl(0.5f)).sub(position);
        position.add(deltaPos.scl(0.2f));
    }

    /**
     * 返回当前物体名字。
     * @return 物体名字
     */
    public String getName() {
        return fairy.name;
    }

    public void immediatelyJump() {
        position = snatch.cpy().scl(size.cpy().scl(0.5f));
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getSnatch() {
        return snatch;
    }

    public void setSnatch(Vector2 snatch) {
        this.snatch = snatch;
    }

    private Fairy fairy;

    // 连续坐标，因为我们要顺滑移动
    private Vector2 position;

    // 卡着的位置，因为我们是格子游戏
    private Vector2 snatch;

    // 贴图大小，按像素算
    private Vector2 size;

    private float scale;
}
