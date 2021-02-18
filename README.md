<h1 align="center"><a href="https://github.com/xkcoding" target="_blank">Transaction Isolation Level Demo</a></h1>


## 项目简介

`Transaction Isolation Level` 正如项目的名字一样，事务隔离级别，在我们学习数据库时，事务的隔离级别已经老生长谈，面试也是必考知识点，然而在学习的时候，我们基本上全都是在背概念，或者有的良心老师，也会给我们演示隔离级别，但是我们背的概念，或者别人的演示基本都是基于数据库的操作，而不是spring的代码操作。所以作者以自我学习为目的创建本项目，并且也希望分享给大家，能做从代码的层面感受事务隔离级别的真实后的状况。

## 开发环境

- **JDK 1.8 +**
- **Maven 3.5 +**
- **IntelliJ IDEA ULTIMATE 2018.2 +** (*注意：务必使用 IDEA 开发，同时保证安装 `lombok` 插件*)
- **Mysql 5.7 +** (*尽量保证使用 5.7 版本以上，因为 5.7 版本加了一些新特性，同时不向下兼容。本 demo 里会尽量避免这种不兼容的地方，但还是建议尽量保证 5.7 版本以上*)

## 数据库配置

- 推荐使用docker直接run一个mysql，本测试环境后期考虑放制作一个docker镜像，目前先把整体代码完成

## 运行方式

1. `git clone https://github.com/HolleQH/transaction-isolation-level.git`
2. 使用 IDEA 打开 clone 下来的项目
3. 在 IDEA 中 Maven Projects 的面板导入项目根目录下 的 `pom.xml` 文件
4. Maven Projects 找不到的童鞋，可以勾上 IDEA 顶部工具栏的 View -> Tool Buttons ，然后 Maven Projects 的面板就会出现在 IDEA 的右侧
5. 找到 test 下的各个测试类 类就可以运行了，每个测试demo可以通过查看控制台日志输出看到各种隔离级别发生的具体现象
7. **`注意：运行各个 demo 之前，需要事先初始化数据库数据的，亲们别忘记了哦~`**

