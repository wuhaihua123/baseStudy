package io.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorThreadGroup {
    /**
     * 线程数
     */

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
    private AtomicInteger xid = new AtomicInteger();


    private SelectorThreadGroup stg = this;

    public void setWorker(SelectorThreadGroup  stg){
        this.stg = stg;

    }

    public SelectorThreadGroup(int threads) {
        selectorThreads = new SelectorThread[threads];
        for (int i = 0; i < selectorThreads.length; i++) {
            selectorThreads[i] = new SelectorThread(this);
            new Thread(selectorThreads[i]).start();
        }
    }

    public void bind(int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            //注册到对应的selector上面
            serverSocketChannel.bind(new InetSocketAddress(port));
//            nextSelector(serverSocketChannel);
            nextSelector3(serverSocketChannel);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void nextSelector3(Channel c) {

        try {
            if(c instanceof  ServerSocketChannel){
                SelectorThread st = next();  //listen 选择了 boss组中的一个线程后，要更新这个线程的work组
                st.blockingDeque.put(c);
                //设置work组为当前对象，交给当前对象处理的selector处理
                st.setWorker(this);
                st.selector.wakeup();
            }else {
                SelectorThread st = nextV3();  //在 main线程种，取到堆里的selectorThread对象

                //1,通过队列传递数据 消息
                st.blockingDeque.add(c);
                //2,通过打断阻塞，让对应的线程去自己在打断后完成注册selector
                st.selector.wakeup();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private SelectorThread nextV3() {

        int index = xid.incrementAndGet() % stg.selectorThreads.length;  //动用worker的线程分配
        //work组中的selector分配
        return stg.selectorThreads[index];
    }

//    public void nextSelector(Channel c) {
//        if (c instanceof ServerSocketChannel) {
//            try {
//                //如果是连接的，默认选择第一个selector来处理
//                selectorThreads[0].blockingDeque.put(c);
//                selectorThreads[0].selector.wakeup();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        } else {
//            //否则在1-n个连接中处理
//            SelectorThread s = nextV1();
//            try {
//                s.blockingDeque.put(c);
//                s.selector.wakeup();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private SelectorThread next() {
        int index = xid.incrementAndGet() % selectorThreads.length;
        return selectorThreads[index];
    }

//    /**
//     * 其中
//     *
//     * @return
//     */
//    private SelectorThread nextV1() {
//        int index = xid.incrementAndGet() % (selectorThreads.length - 1);
//        return selectorThreads[index + 1];
//    }
}
