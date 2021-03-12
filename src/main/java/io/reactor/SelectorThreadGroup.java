package io.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorThreadGroup {
    /**
     * 线程数
     */
//    private int threads;


    /**
     * server
     */
    private ServerSocketChannel serverSocketChannel = null;

    /**
     * selector线程数组
     */
    private SelectorThread[] selectorThreads;

    /**
     * @param threads
     */
    private AtomicInteger xid;

    public SelectorThreadGroup(int threads) {
        selectorThreads = new SelectorThread[threads];
        for (int i = 0; i < selectorThreads.length; i++) {
            selectorThreads[i] = new SelectorThread();
            new Thread(selectorThreads[i]).start();
        }
    }

    public void bind(int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            //注册到对应的selector上面
            serverSocketChannel.bind(new InetSocketAddress(port));
            nextSelector(serverSocketChannel);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void nextSelector(Channel c) {
        SelectorThread next = next();
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) c;
        next.blockingDeque.add(serverSocketChannel);
        next.selector.wakeup();//让selector的select方法立刻放回，不阻塞


    }

    private SelectorThread next() {
        int index = xid.incrementAndGet() % selectorThreads.length;
        return selectorThreads[index];
    }
}
