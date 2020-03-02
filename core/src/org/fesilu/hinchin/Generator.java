package org.fesilu.hinchin;

import com.badlogic.gdx.math.Vector2;
import sun.security.provider.NativePRNG;

import java.util.HashMap;
import java.util.Random;

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
        getRng();
        Vector2 cpy = position.cpy();
        Vector2 dotted = new Vector2(
                cpy.cpy().dot(new Vector2(r1, r2)),
                cpy.cpy().dot(new Vector2(r3, r4))
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

    public static GameMap generate(Game game, Processor processor, int w, int h) {
        GameMap map = new GameMap(game, new Vector2(w, h));
        map.entities.add(game.playerCharacter);
        processor.attach(map);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                processor.reset();
                processor.pushf((float) x / w);
                processor.pushf((float) y / h);
                processor.run();
                if (map.map[y][x] != null) {
                    continue;
                }
                Fairy fairy = game.terrains.get(processor.getData()[0]);
                map.map[y][x] = new Terrain(new Vector2(x, y), fairy.obstacle, fairy, 2.0f);
            }
        }
        return map;
    }

    public static void placeRoom(GameMap map, Processor processor, int x, int y, int w, int h) {
        processor.attach(map);
        for (int b = 0; b < h; b++) {
            for (int a = 0; a < w; a++) {
                processor.reset();
                boolean aAtBorders = a == 0 || a == w - 1;
                boolean bAtBorders = b == 0 || b == h - 1;
                boolean inDoors = (!aAtBorders && !bAtBorders);
                boolean atCorners = (aAtBorders && !bAtBorders) || (!aAtBorders && bAtBorders);
                processor.pushf((float) (x + a) / map.mapSize.x);
                processor.pushf((float) (y + b) / map.mapSize.y);
                processor.push(inDoors ? 1 : atCorners ? -1 : 0);
                processor.run();
                Fairy fairy = map.game.terrains.get(processor.getData()[0]);
                map.map[y + b][x + a] = new Terrain(new Vector2(x + a, y + b), fairy.obstacle, fairy, 2.0f);
            }
        }
    }

    /**
     * 获取 RNG，是单例模式。
     * @return 随机数生成器
     */
    public static Random getRng() {
        if (rng == null) {
            rng = new Random(System.currentTimeMillis());
            r1 = rng.nextFloat() * 100.0f;
            r2 = rng.nextFloat() * 100.0f;
            r3 = rng.nextFloat() * 100.0f;
            r4 = rng.nextFloat() * 100.0f;
        }
        return rng;
    }

    /**
     * 随机数生成器。每一次运行都会有独特的种子，确保每次地图都有一点不一样
     */
    public static Random rng;
    public static float r1, r2, r3, r4;
}
