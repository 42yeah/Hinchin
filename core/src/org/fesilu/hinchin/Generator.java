package org.fesilu.hinchin;

import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

/**
 * Generator 是地图生成器，用来生成各种地图。
 * 里面应该只有静态类。
 */
public class Generator {
    /**
     * 2D Hash 函数
     * @param position 坐标
     * @return hash 后坐标
     */
    public static Vector2 rand2d(Vector2 position) {
        Vector2 cpy = position.cpy();
        Vector2 dotted = new Vector2(
                cpy.cpy().dot(new Vector2(12.9898f, 78.233f)),
                cpy.cpy().dot(new Vector2(42.523f, 117.625f))
        );
        dotted.x = (float) Math.sin(dotted.x) * 42978.5253f;
        dotted.y = (float) Math.sin(dotted.y) * 42978.5253f;
        dotted = fract(dotted);
        return dotted.scl(2.0f).add(-1.0f, -1.0f);
    }

    /**
     * 佩林噪音
     * @param position 坐标
     * @return 这个点的佩林噪音
     */
    public static float perlin(Vector2 position) {
        Vector2 u = floor(position);
        Vector2 f = fract(position);
        Vector2 s = smoothstep(0.0f, 1.0f, f);

        Vector2 a = rand2d(u);
        Vector2 b = rand2d(u.cpy().add(1.0f, 0.0f));
        Vector2 c = rand2d(u.cpy().add(0.0f, 1.0f));
        Vector2 d = rand2d(u.cpy().add(1.0f, 1.0f));

        float p = mix(
                mix(a.dot(f.cpy().add(0.0f, 0.0f)), b.dot(f.cpy().add(-1.0f, 0.0f)), s.x),
                mix(c.dot(f.cpy().add(0.0f, -1.0f)), b.dot(f.cpy().add(-1.0f, -1.0f)), s.x),
                s.y
        );
        return p * 0.5f + 0.5f;
    }

    public static Vector2 fract(Vector2 position) {
        Vector2 cpy = position.cpy();
        cpy.x = cpy.x - (float) Math.floor(cpy.x);
        cpy.y = cpy.y - (float) Math.floor(cpy.y);
        return cpy;
    }

    public static Vector2 floor(Vector2 position) {
        Vector2 cpy = position.cpy();
        cpy.x = (float) Math.floor(cpy.x);
        cpy.y = (float) Math.floor(cpy.y);
        return cpy;
    }

    public static Vector2 smoothstep(float min, float max, Vector2 position) {
        Vector2 cpy = position.cpy();
        cpy.x = mix(min, max, 3.0f * cpy.x * cpy.x - 2.0f * cpy.x * cpy.x * cpy.x);
        cpy.y = mix(min, max, 3.0f * cpy.y * cpy.y - 2.0f * cpy.y * cpy.y * cpy.y);
        return cpy;
    }

    public static float mix(float min, float max, float v) {
        return min * (1.0f - v) + max * v;
    }

    public static Vector2 mix(Vector2 a, Vector2 b, float v) {
        return a.cpy().scl(1.0f - v, 1.0f - v).add(b.cpy().scl(v, v));
    }

    public static Terrain[][] generate(Processor processor, HashMap<String, Fairy> fairies, int w, int h) {
        Terrain[][] map = new Terrain[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                processor.reset();
                processor.pushf((float) x / w);
                processor.pushf((float) y / h);
                processor.run();
                Fairy fairy = fairies.get(processor.getData()[0]);
                map[y][x] = new Terrain(new Vector2(x, y), fairy.obstacle, fairy, 2.0f);
            }
        }
        return map;
    }
}
