## Hinchin 脚本语言 Spec

Hinchin 有一系列的指令。按空格分割。以下是所有的函数参数。

- \# 注解
- end 马上结束程序。
- push(number) 推入变量。
- say 出栈并独行输出这个变量。
- sayl 出栈并同行输出这个向量。用 el 结束当前行。
- el 结束当前行。
- add 入栈(出栈 + 出栈)。
- sub 入栈(第二个出栈 - 第一个出栈)
- mul 入栈(出栈 * 出栈)
- div 入栈(第二个出栈 / 第一个出栈)
- data(index, str) 往某个内存里放入字符串
- saydata 说内存\[出栈]中的内容。
- cmp 比较(第二个出栈, 第一个出栈)。
- cmpf 比较(第二个浮点，第一个浮点)。
- je(!标签) 跳跃，如果相等
- jne(!标签) 跳跃，如果不相等
- jg<e>(!标签) 跳跃，如果大于<等于>
- jl<e>(!标签) 跳跃，如果小于<等于>
- clone 入栈两次出栈。
- do 测试用循环。遇到 loop 就会跳回来这里。
- loop 测试用循环。跳回去 do
- fairy(是否障碍物, x, y, 精灵名) 输出新的精灵。
- texture 从内存\[出栈]加载材质包。
- perlin 根据 (第二个出栈, 第一个出栈) 生成佩林噪音。
- store(index) 保存出栈到内存\[index]。
- load(index) 从内存\[index]入栈。
- sps 传送主角到 (第二个出栈, 第一个出栈)
- abs 入栈绝对值出栈。
- nml 标准化。(0, 1) => (-1, 1)
- tel 材质坐标化。 (-1, 1) => (0, 1)
- len 向量 (第二个出栈, 第一个出栈) 长度。
- plant 在 (第二个出栈, 第一个出栈) 里面放置内存\[第三个出栈] 的物体。