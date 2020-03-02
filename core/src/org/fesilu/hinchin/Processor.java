package org.fesilu.hinchin;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import sun.jvm.hotspot.tools.JMap;
import sun.jvm.hotspot.tools.PMap;

import java.awt.image.ImageProducer;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Processor 可以用来读取一系列的 Hinchin 脚本，语法由我们自己瞎几把决定。
 * 在语法内，可以做出很多事情。
 */
public class Processor {
    public Processor() {}

    /**
     * 默认的构造函数可以输入一个路径。
     * 以下随便介绍一下 Hinchin 脚本 (.hc):
     * 由空格 ( ) 分割表达式。但其实我们没有表达式，一切都根据 GPP 中的字节码来做。
     * 推荐的 reserve 是 512。
     * texture 可以为空；这个只是 fairy 初值。
     * @param file 文件
     * @param reserve 内存大小 （以 int 算）
     */
    public Processor(File file, int reserve) {
        // 读取指令
        instructions = readOrDie(file).trim().toLowerCase().split("[\\s\\n]");
        counter = 0;
        // 初始化虚拟机内存
        memory = new float[reserve];
        data = new String[reserve];
        pointer = -1;

        // 用于贴图读取脚本
        fairies = new HashMap<>();

        // 是否在注解区
        commenting = false;
    }

    /**
     * run() 执行字节码。他会返回在栈最顶端的数字，作为整个函数的返回值。
     * @return 字节码返回值
     */
    public int run() {
        String fetched = fetch();
        while (fetched != null) {
            if (execute(fetched)) {
                break;
            }
            fetched = fetch();
        }
        return pop();
    }

    /**
     * execute() 执行指令。
     * @param instruction 指令
     */
    private boolean execute(String instruction) {
        // 检查是不是注解
        if (instruction.startsWith("#")) {
            commenting = !commenting;
        }
        if (commenting) { return false; }

        // 遍历指令
        if (instruction.equals("end")) {
            return true;
        } else if (instruction.equals("push")) {
            pushf(toNumber(fetch()));
        } else if (instruction.equals("say")) {
            System.out.println("INFO: Say at " + pointer + ": " + popf());
        } else if (instruction.equals("sayl")) {
            System.out.print(popf() + " ");
        } else if (instruction.equals("el")) {
            System.out.println();
        } else if (instruction.equals("add")) {
            float a = popf(), b = popf();
            pushf(a + b);
        } else if (instruction.equals("sub")) {
            float a = popf(), b = popf();
            pushf(b - a);
        } else if (instruction.equals("mul")) {
            float a = popf(), b = popf();
            pushf(a * b);
        } else if (instruction.equals("div")) {
            float a = popf(), b = popf();
            pushf(b / a);
        } else if (instruction.equals("data")) {
            int a = (int) toNumber(fetch());
            String b = fetch();
            data[a] = b;
        } else if (instruction.equals("saydata")) {
            int a = pop();
            System.out.println("INFO: SayData at " + pointer + ": " + data[a]);
        } else if (instruction.equals("cmp")) {
            int b = pop(), a = pop();
            int res = 0;
            if (a > b) {
                res = 1;
            } else if (a < b) {
                res = -1;
            }
            push(res);
        } else if (instruction.equals("cmpf")) {
            float b = popf(), a = popf();
            int res = 0;
            if (a > b) {
                res = 1;
            } else if (a < b) {
                res = -1;
            }
            push(res);
        } else if (instruction.equals("je")) {
            int a = pop();
            if (a == 0) {
                counter = locate(fetch());
            }
        } else if (instruction.equals("jne")) {
            int a = pop();
            if (a != 0) {
                counter = locate(fetch());
            }
        } else if (instruction.startsWith("jg")) {
            int a = pop();
            boolean result = a > 0;
            if (instruction.endsWith("e")) {
                result = a >= 0;
            }
            if (result) {
                String tag = fetch();
                counter = locate(tag);
            }
        } else if (instruction.startsWith("jl")) {
            int a = pop();
            boolean result = a < 0;
            if (instruction.endsWith("e")) {
                result = a <= 0;
            }
            if (result) {
                counter = locate(fetch());
            }
        } else if (instruction.startsWith("j")) {
            counter = locate(fetch());
        } else if (instruction.equals("clone")) {
            float a = popf();
            pushf(a);
            pushf(a);
        } else if (instruction.equals("do")) {
            waypoint = counter;
        } else if (instruction.equals("loop")) {
            counter = waypoint;
        } else if (instruction.equals("fairy")) {
            int fairySize = 16;
            boolean obstacle = (int) toNumber(fetch()) == 1;
            int x = (int) toNumber(fetch()), y = (int) toNumber(fetch());
            Fairy fairy = new Fairy(texture, fetch(), x * 16, y * 16, 16, 16, obstacle);
            fairies.put(fairy.name, fairy);
            System.out.println("INFO: Fairy added: " + fairy.name + " at " + x + ", " + y);
        } else if (instruction.equals("texture")) {
            int a = pop();
            texture = new Texture(data[a]);
        } else if (instruction.equals("perlin")) {
            float y = popf(), x = popf();
            float p = Generator.perlin(new Vector2(x, y));
            pushf(p);
        } else if (instruction.equals("store")) {
            int index = (int) toNumber(fetch());
            float a = popf();
            data[index] = "" + a;
        } else if (instruction.equals("load")) {
            int index = (int) toNumber(fetch());
            String value = data[index];
            if (value == null) {
                pushf(0.0f);
            } else {
                float a = Float.parseFloat(value);
                pushf(a);
            }
        } else if (instruction.equals("sps")) {
            // SPS = Set Player Pos
            int y = pop(), x = pop();
            ((Game) attachment).playerCharacter.setSnatch(new Vector2(x, y));
        } else if (instruction.equals("abs")) {
            float a = popf();
            a = Math.abs(a);
            pushf(a);
        } else if (instruction.equals("nml")) {
            float a = popf();
            pushf(a * 2.0f - 1.0f);
        } else if (instruction.equals("tel")) {
            float a = popf();
            pushf(a * 0.5f + 0.5f);
        } else if (instruction.equals("len")) {
            float a = popf(), b = popf();
            pushf((float) Math.sqrt(a * a + b * b));
        } else if (instruction.equals("plant")) {
            float y = popf(), x = popf();
            int index = pop();
            GameMap map = (GameMap) attachment;
            int sx = standarize(x, map.mapSize.x);
            int sy = standarize(y, map.mapSize.y);
            map.entities.add(new Entity(new Vector2(sx, sy), map.game.cosmetics.get(data[index]), 2.0f));
        } else if (instruction.equals("rnd")) {
            float gen = Generator.getRng().nextFloat();
            pushf(gen);
        } else if (instruction.equals("room")) {
            GameMap map = (GameMap) attachment;
            int h = standarize(popf(), map.mapSize.y) + 3,
                    w = standarize(popf(), map.mapSize.x) + 3,
                    y = standarize(popf(), map.mapSize.y),
                    x = standarize(popf(), map.mapSize.x);
//            // 连续 Processor
            Processor processor = new Processor(Gdx.files.internal(fetch()).file(), 512);
            processor.attach(attachment);
            Generator.placeRoom((GameMap) attachment, processor, x, y, w, h);
        } else if (instruction.equals("getterrain")) {
            GameMap map = (GameMap) attachment;
            int y = standarize(popf(), map.mapSize.y), x = standarize(popf(), map.mapSize.x);
            int index = (int) toNumber(fetch());
            if (map.map[y][x] != null) {
                data[index] = map.map[y][x].getName();
            } else {
                data[index] = "null";
            }
        } else if (instruction.equals("cmpdata")) {
            int b = pop(), a = pop();
            push(data[a].equals(data[b]) ? 0 : 1);
        } else if (instruction.equals("rmplant")) {
            GameMap map = (GameMap) attachment;
            int y = standarize(popf(), map.mapSize.y),
                    x = standarize(popf(), map.mapSize.x);
            System.out.println("Removing plant at " + x + ", " + y);
            for (int i = 0; i < map.entities.size(); i++) {
                if (map.entities.get(i).getSnatch().equals(new Vector2(x, y))) {
                    map.entities.remove(i);
                    i--;
                }
            }
        }
        return false;
    }

    /**
     * fetch() 会取得最新的指令，然后执行 PC + 1。
     * @return 指令
     */
    private String fetch() {
        if (instructions.length <= counter) {
            System.err.println("WARNING! Counter is overflowing");
            return null;
        }
        String ret = instructions[counter];
        counter++;
        return ret;
    }

    /**
     * pop() 返回在最顶端的数值。假如没有，返回 0。
     * @return
     */
    private int pop() {
        if (pointer >= memory.length || pointer < 0) {
            System.err.println("WARNING! Pointer is underflowing at " + counter + ": " + getNearbyCode(counter));
            return 0;
        }
        int ret = (int) memory[pointer];
        pointer--;
        return ret;
    }

    /**
     * popf() 返回在最顶端的数值。加 f 保存精度。
     * @return 有精度的 f
     */
    private float popf() {
        if (pointer >= memory.length || pointer < 0) {
            System.err.println("WARNING! Pointer is underflowing at " + counter + ": " + getNearbyCode(counter));
            return 0;
        }
        float ret = memory[pointer];
        pointer--;
        return ret;
    }

    public void push(int value) {
        pointer++;
        if (pointer >= memory.length || pointer < 0) {
            System.err.println("WARNING! Pointer is overflowing at " + counter + ": " + getNearbyCode(counter));
            return;
        }
        memory[pointer] = value;
    }

    public void pushf(float value) {
        pointer++;
        if (pointer >= memory.length || pointer < 0) {
            System.err.println("WARNING! Pointer is overflowing at " + counter + ": " + getNearbyCode(counter));
            return;
        }
        memory[pointer] = value;
    }

    public int locate(String key) {
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i].equals(key.substring(1))) {
                return i + 1;
            }
        }
        System.err.println("ERROR! Jump point not found!");
        return 0;
    }

    public int standarize(float x, float edge) {
        return (int) Math.round(x * edge);
    }

    /**
     * 读取文件。假如失败，返回空字符串（不报错）。
     * @param file 文件
     * @return 文件内容
     */
    private String readOrDie(File file) {
        try {
            FileInputStream reader = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (true) {
                byte[] raw = new byte[1024];
                int len = reader.read(raw);
                // 到达文件尾了吗？
                if (len <= 0) {
                    break;
                }
                baos.write(raw, 0, len);
            }
            reader.close();
            return baos.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 接入某物体到 attach 中。
     * 可以自由操控 attach 中的物体。
     * @param object
     */
    public void attach(Object object) {
        this.attachment = object;
    }

    /**
     * 程序重置。
     */
    public void reset() {
        counter = 0;
        pointer = -1;
    }

    /**
     * 转换成数字。因为懒得写
     * @param string 数字字符串
     * @return 数字
     */
    private float toNumber(String string) {
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException e) {
            System.err.println("ERR! Number format at " + (counter - 1) + ": " + string);
            return 0;
        }
    }

    public String[] getData() {
        return data;
    }

    private String getNearbyCode(int counter) {
        String comb = "... ";
        for (int i = counter - 2; i <= counter + 2; i++) {
            if (i <= 0 || i >= instructions.length) { continue; }
            comb += instructions[i] + " ";
        }
        comb += "...";
        return comb;
    }

    private String[] instructions;
    private int counter;
    private float[] memory;
    private String[] data;
    private int pointer;
    private int waypoint;

    public HashMap<String, Fairy> fairies;
    private Texture texture;
    private Object attachment;
    private boolean commenting;
}
