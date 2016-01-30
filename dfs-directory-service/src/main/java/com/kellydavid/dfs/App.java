package com.kellydavid.dfs;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App 
{
    public static final int CONNECTION_POOL_SIZE = 10;

    private static ServerSocket ss;
    private static ThreadPoolExecutor requestThreads;

    public static void main( String[] args )
    {
        // Get hostname and port number
        if(args.length != 2){
            System.out.println("Must use arguments <hostname> <port-number>");
            System.exit(-1);
        }
        String hostname = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // setup thread pool
        requestThreads = new ThreadPoolExecutor(CONNECTION_POOL_SIZE, CONNECTION_POOL_SIZE, 1000,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        listen(hostname, portNumber);
    }

    private static void listen(String hostname, int portNumber){
        try {
            // Create server socket
            ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(hostname, portNumber));
            while(true){
                // Setup a new Connection thread when a new client connects.
                Socket so = ss.accept();
                requestThreads.execute(new RequestProcessor(so));
            }
        } catch (Exception e) {
            System.err.println("Error creating socket.\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
