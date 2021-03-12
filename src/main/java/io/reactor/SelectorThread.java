package io.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * eventLoop的同比
 */
public class SelectorThread extends Thread {
    //一个线程对应一个selector
    //每个客户端只会绑定到其中一个selector
    Selector selector = null;


    LinkedBlockingDeque<Channel> blockingDeque = new LinkedBlockingDeque<>();

    public SelectorThread() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                //select
                int nums = selector.select();//线程阻塞  有个wakeup可以唤醒
                //处理selectKeys
                if (nums > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                            //接收客户端的注册
                        } else if (key.isReadable()) {
                            //处理读取
                            readHandler(key);
                        } else if (key.isWritable()) {

                        }
                    }


                }
                //处理一些task
                if (!blockingDeque.isEmpty()) {//堆里的对象，共享的   只有方法的本地变量线程隔离的
                    Channel c =  blockingDeque.take();
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                    } else if (c instanceof SocketChannel) {
                        SocketChannel server = (SocketChannel) c;
                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
                        server.register(selector, SelectionKey.OP_READ, byteBuffer);
                    }
                }


            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void readHandler(SelectionKey key) {
        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        byteBuffer.clear();
        while (true) {
            try {
                int nums = channel.read(byteBuffer);
                if (nums > 0) {
                    //将督导的内容反转，直接写出
                    byteBuffer.flip();
                    while (byteBuffer.hasRemaining()) {
                        channel.write(byteBuffer);
                    }
                    byteBuffer.clear();
                } else if (nums == 0) {
                    break;

                } else if (nums < 0) {
                    //客户端断开
                    System.out.println("clinet " + channel.getRemoteAddress() + " closed");
                    key.channel();
                    break;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    private void acceptHandler(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = serverSocketChannel.accept();
            //选择一个selector去注册
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
