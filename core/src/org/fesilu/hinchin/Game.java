package org.fesilu.hinchin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
		shape = new ShapeRenderer();

		// 时间
		lastInstant = System.currentTimeMillis();

		cosmetics = loadFairies(Gdx.files.internal("cosmetics.hc").file());
		terrains = loadFairies(Gdx.files.internal("terrain.hc").file());

		// 初始化玩家和实体列表
		playerCharacter = new Entity(new Vector2(0.0f, 0.0f), cosmetics.get("man"), 2.0f);
		playerCharacter.setSnatch(new Vector2(0.0f, 0.0f));

		// 摄像头
		float aspect = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
		camera = new OrthographicCamera(1500.0f * aspect, 1500.0f);

		// 测试地图
		map = Generator.generate(this, new Processor(Gdx.files.internal("island_gen.hc").file(), 512), 100, 80);

		// 运行初试 Hinchin 脚本
		Processor initiator = new Processor(Gdx.files.internal("init.hc").file(), 512);
		initiator.attach(this);
		initiator.run();
		playerCharacter.immediatelyJump();
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

		// 更新摄像头位置
		camera.position.set(playerCharacter.getPosition().x * 2.0f, playerCharacter.getPosition().y * 2.0f, 0.01f);

		// 更新怪物
		for (int i = 0; i < map.entities.size(); i++) {
			map.entities.get(i).update(deltaTime);
		}

		// 更新关卡
		changeMap(canChange);
	}

	/**
	 * render() 是 Game 的渲染方法。这是往屏幕上画东西的地方。
	 * 一开始会调用 OpenGL 的清屏，之后会把各种材质渲染上去。
	 */
	@Override
	public void render() {
		// 更新游戏
		updatePlayerCharacter();
		update();

		// 清除屏幕
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// 开始绘画材质
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (int y = 0; y < map.mapSize.y; y++) {
			for (int x = 0; x < map.mapSize.x; x++) {
				if (map.map[y][x].isVisibleTo(map, playerCharacter.getSnatch())) {
					map.map[y][x].draw(batch);
				} else if (map.map[y][x].discovered) {
					map.map[y][x].draw(batch);
					batch.end();
					shape.begin(ShapeRenderer.ShapeType.Filled);
					shape.setProjectionMatrix(camera.combined);
					map.map[y][x].drawSilhouette(shape);
					shape.end();
					batch.begin();
				}
			}
		}
		for (int i = 0; i < map.entities.size(); i++) {
			Entity entity = map.entities.get(i);
			int rx = Math.round(entity.getSnatch().x), ry = Math.round(entity.getSnatch().y);
			if (map.map[ry][rx].isVisibleTo(map, playerCharacter.getSnatch())) {
				map.entities.get(i).draw(batch);
			}
		}
		Vector2 snatch = playerCharacter.getSnatch().cpy().scl(cosmetics.get("man").sw, cosmetics.get("man").sh);
		cosmetics.get("grass-hat").draw(batch, playerCharacter.getPosition().x, playerCharacter.getPosition().y, 2.0f);
		cosmetics.get("axe").draw(batch, playerCharacter.getPosition().x, playerCharacter.getPosition().y, 2.0f);
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
	 *
	 * @param file 文件
	 */
	private HashMap<String, Fairy> loadFairies(File file) {
		Processor processor = new Processor(file, 512);
		processor.run();
		return processor.fairies;
	}

	/**
	 * 挪动角色。
	 */
	void updatePlayerCharacter() {
		Vector2 oldSnatch = playerCharacter.getSnatch().cpy();
		if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
				playerCharacter.getSnatch().add(0, 1.0f);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
				playerCharacter.getSnatch().add(0, -1.0f);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
				playerCharacter.getSnatch().add(-1.0f, 0);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
				playerCharacter.getSnatch().add(1.0f, 0);
		}
		if (map.map[(int)playerCharacter.getSnatch().y]
				[(int)playerCharacter.getSnatch().x].isObstacle()) {
			playerCharacter.setSnatch(oldSnatch);
		}
		if (!(comparedDownstairs())) {
			canChange = true;
		}

	}

	void changeMap(boolean canChange) {
		if (canChange && comparedDownstairs()) {
			canChange = false;
			mapList.add(map);
			map = Generator.generate(this, new Processor(Gdx.files.internal("island_gen.hc").file(), 512), 100, 80);
			floorCounter += 1;
		}
	}

	boolean comparedDownstairs() {
		for (int i = 0; i < map.entities.size(); i++) {
			if (map.entities.get(i).getName().equals("downstairs")) {
				if (map.entities.get(i).getSnatch().equals(playerCharacter.getSnatch())) {
					return true;
				}
			}
		}
		return false;
	}

	// deltaTime 是每一帧和上一帧的时差。这样可以保证不稳定的 FPS 下游戏一样正常运行。单位是秒。
	private float deltaTime;

	// lastInstant 是上一个瞬间的 System.currentTimeMillis. 用于计算毫秒级的 deltaTime。
	private long lastInstant;
	SpriteBatch batch;
	ShapeRenderer shape;

	// 人，动物等
	// 因为在同一个包内，这些是 Processor (脚本) 可以访问的东西
	HashMap<String, Fairy> cosmetics;
	HashMap<String, Fairy> terrains;
	Entity playerCharacter;
	GameMap map;

	// 摄像头，跟着主角动
	OrthographicCamera camera;

	int floorCounter;
	boolean canChange = true;
	ArrayList<GameMap> mapList = new ArrayList<>();
}
