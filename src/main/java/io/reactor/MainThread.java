package io.reactor;

public class MainThread {

    public static void main(String[] args) {
        //创建IOthrads

        //应该把监听的server  注册到某一个selector上
        SelectorThreadGroup boss = new SelectorThreadGroup(2);
        //work组，负责处理读写请求
        SelectorThreadGroup worker = new SelectorThreadGroup(3);

        //设置boss组的work
        boss.setWorker(worker);


        //一个group处理连接，客户端读写
        //绑定端口
        boss.bind(9999);
        boss.bind(6666);
        boss.bind(8888);
    }
}
