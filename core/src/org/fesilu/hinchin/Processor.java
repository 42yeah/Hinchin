package org.fesilu.hinchin;


import com.badlogic.gdx.graphics.Texture;

import java.io.*;
import java.util.ArrayList;

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
     * @param path 路径
     */
    public Processor(String path, int reserve, Texture texture) {
        // 读取指令
        instructions = readOrDie(path).trim().toLowerCase().split("[\\s\\n]");
        counter = 0;
        // 初始化虚拟机内存
        memory = new int[reserve];
        data = new String[reserve];
        pointer = -1;

        // 用于贴图读取脚本
        this.texture = texture;
        fairies = new ArrayList<>();
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
            push(toNumber(fetch()));
        } else if (instruction.equals("say")) {
            System.out.println("INFO: Say at " + pointer + ": " + pop());
        } else if (instruction.equals("add")) {
            int a = pop(), b = pop();
            push(a + b);
        } else if (instruction.equals("sub")) {
            int a = pop(), b = pop();
            push(b - a);
        } else if (instruction.equals("mul")) {
            int a = pop(), b = pop();
            push(a * b);
        } else if (instruction.equals("div")) {
            int a = pop(), b = pop();
            push(b / a);
        } else if (instruction.equals("data")) {
            int a = toNumber(fetch());
            String b = fetch();
            data[a] = b;
        } else if (instruction.equals("saydata")) {
            int a = pop();
            System.out.println("INFO: SayData at " + pointer + ": " + data[a]);
        } else if (instruction.equals("cmp")) {
            int a = pop(), b = pop();
            push(a == b ? 1 : 0);
        } else if (instruction.equals("je")) {
            int a = pop();
            if (a == 1) {
                counter += toNumber(fetch());
            }
        } else if (instruction.equals("jne")) {
            int a = pop();
            if (a == 0) {
                counter += toNumber(fetch());
            }
        } else if (instruction.equals("clone")) {
            int a = pop();
            push(a);
            push(a);
        } else if (instruction.equals("do")) {
            waypoint = counter;
        } else if (instruction.equals("loop")) {
            counter = waypoint;
        } else if (instruction.equals("fairy")) {
            int fairySize = 16;
            int y = pop(), x = pop();
            Fairy fairy = new Fairy(texture, data[pop()], x * 16, y * 16, 16, 16);
            fairies.add(fairy);
            System.out.println("INFO: Fairy added: " + fairy.name + " at " + x + ", " + y);
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
            System.err.println("WARNING! Pointer is overflowing / underflowing");
            return 0;
        }
        int ret = memory[pointer];
        pointer--;
        return ret;
    }

    private void push(int value) {
        pointer++;
        if (pointer >= memory.length || pointer < 0) {
            System.err.println("WARNING! Pointer is overflowing / underflowing");
            return;
        }
        memory[pointer] = value;
    }

    /**
     * 读取文件。假如失败，返回空字符串（不报错）。
     * @param path 路径
     * @return 文件内容
     */
    private String readOrDie(String path) {
        try {
            FileInputStream reader = new FileInputStream(path);
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
            return baos.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 转换成数字。因为懒得写
     * @param string 数字字符串
     * @return 数字
     */
    private int toNumber(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            System.err.println("ERR! Number format at " + (counter - 1) + ": " + string);
            return 0;
        }
    }

    private String[] instructions;
    private int counter;
    private int[] memory;
    private String[] data;
    private int pointer;
    private int waypoint;

    public ArrayList<Fairy> fairies;
    Texture texture;
}
