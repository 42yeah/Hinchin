package org.fesilu.hinchin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Game 是核心游戏类。游戏的渲染，创建，和扔掉都会在这里进行。
 * Game 应该调用各种已经包装好的函数 / 类。其他写好的玩意儿应该无缝的衔接到 Game 中，
 * 假如其他写好的玩意儿要通信，他们应该要比 Game 低一个等级。
 */
public class Game extends ApplicationAdapter {
	/**
	 * create() 是在游戏创建的时候调用的第一个函数。
	 * 其中应该包括材质加载，地图生成，还有各种值初始化啥的。
	 */
	@Override
	public void create() {
		batch = new SpriteBatch();

		// 时间
		lastInstant = System.currentTimeMillis();

		cosmetics = loadFairies(Gdx.files.internal("cosmetics.hc").file());
		terrains = loadFairies(Gdx.files.internal("terrain.hc").file());

		// 初始化玩家和实体列表
		entities = new ArrayList<>();
		playerCharacter = new Entity(new Vector2(0.0f, 0.0f), cosmetics.get("man"), 2.0f);
		playerCharacter.setSnatch(new Vector2(1.0f, 0.0f));
		entities.add(playerCharacter);

		// 测试地图
		map = new Terrain[80][100]; // 100x80 的地图
		for (int y = 0; y < 80; y++) {
			for (int x = 0; x < 100; x++) {
				map[y][x] = new Terrain(new Vector2(x, y), terrains.get("grass"), 2.0f);
			}
		}

		// 运行初试 Hinchin 脚本
		Processor processor = new Processor(Gdx.files.internal("init.hc").file(), 512, this);
		processor.run();
	}

	/**
	 * update() 是 Game 更新方法。每一帧都在 render() 最开始调用，
	 * 更新的东西包括人物走动，饥饿减少之类。
	 */
	public void update() {
		// 计算时差
		long thisInstant = System.currentTimeMillis();
		deltaTime = (float) (thisInstant - lastInstant) / 1000.0f;
		lastInstant = thisInstant;

		// 更新怪物
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).update(deltaTime);
		}
	}

	/**
	 * render() 是 Game 的渲染方法。这是往屏幕上画东西的地方。
	 * 一开始会调用 OpenGL 的清屏，之后会把各种材质渲染上去。
	 */
	@Override
	public void render() {
		// 更新游戏
		update();

		// 清除屏幕
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// 开始绘画材质
		batch.begin();
		for (int y = 0; y < 80; y++) {
			for (int x = 0; x < 100; x++) {
				map[y][x].draw(batch);
			}
		}
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).draw(batch);
		}
		batch.end();
	}

	/**
	 * dispose() 是 Game 的退出方法。
	 * 包括把材质扔掉，模型扔掉啥的。
	 */
	@Override
	public void dispose() {
		batch.dispose();
	}

	/**
	 * 大规模加载小贴图。
	 * @param file 文件
	 */
	private HashMap<String, Fairy> loadFairies(File file) {
		Processor processor = new Processor(file, 512, null);
		processor.run();
		return processor.fairies;
	}

	// deltaTime 是每一帧和上一帧的时差。这样可以保证不稳定的 FPS 下游戏一样正常运行。单位是秒。
	private float deltaTime;

	// lastInstant 是上一个瞬间的 System.currentTimeMillis. 用于计算毫秒级的 deltaTime。
	private long lastInstant;
	SpriteBatch batch;

	// 人，动物等
	// 因为在同一个包内，这些是 Processor (脚本) 可以访问的东西
	HashMap<String, Fairy> cosmetics;
	HashMap<String, Fairy> terrains;
	ArrayList<Entity> entities;
	Entity playerCharacter;
	Terrain[][] map;
}
