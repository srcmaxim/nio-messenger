package me.srcmaxim.server;

import me.srcmaxim.Commands;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final int BSIZE = 1024;
    private ByteBuffer buffer = ByteBuffer.allocate(BSIZE);

    private Set<SocketChannel> clientChannels;
    private ServerSocketChannel serverSocket = null;
    private Selector selector = null;
    private String host = "localhost";

    private int port = 6001;

    public Server() {
        try {
            clientChannels = new HashSet<>();
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(host, port));
            selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(1);
        }
        System.out.println("SERVER LOG: Server started and ready for handling requests");
    }

    public void run() {
        boolean running = true;

        while (running) try {
            selector.select();
            for (Iterator i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                SelectionKey key = (SelectionKey) i.next();
                if (key.isAcceptable()) {
                    accept(key);
                }
                if (key.isReadable()) {
                    read(key);
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        SocketChannel sc = serverSocket.accept();
        clientChannels.add(sc);
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) {
        SocketChannel cc = (SocketChannel) key.channel();
        if (!cc.isOpen()) {
            return;
        }
        StringBuilder request = new StringBuilder();
        try {
            buffer.clear();
            while (cc.read(buffer) > 0) {
                buffer.flip();
                request.append(new String(buffer.array(), buffer.position(),
                        buffer.limit(), CHARSET));
                buffer.clear();
            }
            String message = processRequest(cc, request);
            write(message);
        } catch (Exception exc) {
            exc.printStackTrace();
            try {
                cc.close();
                cc.socket().close();
            } catch (Exception e) {
            }
        }
    }

    private String processRequest(SocketChannel cc, StringBuilder request) throws IOException {
        Matcher matcher = Pattern.compile("([A-Z]{4,6})\\s(.*)").matcher(request);
        matcher.find();
        String command = matcher.group(1);
        String message = matcher.group(2);
        Commands commands = Commands.valueOf(command);
        switch (commands) {
            case LOGIN:
                System.out.printf("SERVER LOG: %s %s%n", message, Commands.LOGIN);
                return message + Commands.LOGIN;
            case SEND:
                System.out.printf("SERVER LOG: %s %s%n", Commands.SEND, message);
                return message;
            case LOGOUT:
                clientChannels.remove(cc);
                cc.close();
                System.out.printf("SERVER LOG: %s %s%n", message, Commands.LOGOUT);
                return message + Commands.LOGOUT;
            default:
                System.out.println("INVALID COMMAND!");
                return "INVALID COMMAND!";
        }
    }

    private void write(String massage) throws IOException {
        for (Iterator<SocketChannel> i = clientChannels.iterator(); i.hasNext(); ) {
            SocketChannel client = i.next();
            ByteBuffer encodedMessage = ByteBuffer.wrap(massage.getBytes(CHARSET));
            if (client.isConnected()) {
                client.write(encodedMessage);
            } else {
                i.remove();
            }
        }
    }

    public static void main(String[] args) {
        new Server().run();
    }
}
