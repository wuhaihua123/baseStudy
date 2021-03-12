package io.reactor;

public class MainThread {

    public static void main(String[] args) {
        //创建IOthrads

        //应该把监听的server  注册到某一个selector上
        SelectorThreadGroup stg = new SelectorThreadGroup(1);


        //绑定端口
        stg.bind(9999);
    }
}
