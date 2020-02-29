package org.fesilu.hinchin;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

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
     * @param game 游戏，可传可不传。可以传就可以操控游戏的各种数据。
     */
    public Processor(File file, int reserve, Game game) {
        // 读取指令
        instructions = readOrDie(file).trim().toLowerCase().split("[\\s\\n]");
        counter = 0;
        // 初始化虚拟机内存
        memory = new float[reserve];
        data = new String[reserve];
        pointer = -1;

        // 用于贴图读取脚本
        fairies = new HashMap<>();
        this.game = game;
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
            return false;
        }

        // 遍历指令
        if (instruction.equals("end")) {
            return true;
        } else if (instruction.equals("push")) {
            pushf(toNumber(fetch()));
        } else if (instruction.equals("say")) {
            System.out.println("INFO: Say at " + pointer + ": " + popf());
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
                counter += toNumber(fetch());
            }
        } else if (instruction.equals("jne")) {
            int a = pop();
            if (a != 0) {
                counter += toNumber(fetch());
            }
        } else if (instruction.startsWith("jg")) {
            int a = pop();
            boolean result = a > 0;
            if (instruction.endsWith("e")) {
                result = a >= 0;
            }
            if (result) {
                counter += toNumber(fetch());
            }
        } else if (instruction.startsWith("jl")) {
            int a = pop();
            boolean result = a < 0;
            if (instruction.endsWith("e")) {
                result = a <= 0;
            }
            if (result) {
                counter += toNumber(fetch());
            }
        } else if (instruction.startsWith("j")) {
            counter += toNumber(fetch());
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
            int y = pop(), x = pop();
            Fairy fairy = new Fairy(texture, data[pop()], x * 16, y * 16, 16, 16, pop() == 1);
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
                System.err.println("ERR! loading in " + counter + " will result in a NullPointerException!");
            }
            float a = Float.parseFloat(value);
            pushf(a);
        } else if (instruction.equals("sps")) {
            // SPS = Set Player Pos

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
            System.err.println("WARNING! Pointer is underflowing at " + counter);
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
            System.err.println("WARNING! Pointer is underflowing at " + counter);
            return 0;
        }
        float ret = memory[pointer];
        pointer--;
        return ret;
    }

    public void push(int value) {
        pointer++;
        if (pointer >= memory.length || pointer < 0) {
            System.err.println("WARNING! Pointer is overflowing at " + counter);
            return;
        }
        memory[pointer] = value;
    }

    public void pushf(float value) {
        pointer++;
        if (pointer >= memory.length || pointer < 0) {
            System.err.println("WARNING! Pointer is overflowing at " + counter);
            return;
        }
        memory[pointer] = value;
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

    private String[] instructions;
    private int counter;
    private float[] memory;
    private String[] data;
    private int pointer;
    private int waypoint;

    public HashMap<String, Fairy> fairies;
    private Texture texture;
    private Game game;
}
