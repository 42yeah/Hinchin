package org.fesilu.hinchin;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;


public class GameMap {
    GameMap(Game game, Vector2 size) {
        this.game = game;
        this.mapSize = size;
        map = new Terrain[(int) size.y][(int) size.x];
        entities = new ArrayList<>();
    }

    public Game game;
    public Terrain[][] map;
    public ArrayList<Entity> entities;
    public Vector2 mapSize;
}
