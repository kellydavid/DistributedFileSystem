package com.kellydavid.dfs.ds;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App 
{
    public static final int CONNECTION_POOL_SIZE = 10;

    private static ServerSocket ss;
    private static ThreadPoolExecutor requestThreads;
    private static HashMap<String, LinkedList<String>> fileServerNodes;

    public static void main( String[] args )
    {
        // Get hostname and port number
        if(args.length != 2){
            System.out.println("DFSDS: Must use arguments <hostname> <port-number>\n");
            System.exit(-1);
        }
        String hostname = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // intialise variables
        fileServerNodes = new HashMap<String, LinkedList<String>>();
        initialiseServerSocket(hostname, portNumber);
        requestThreads = new ThreadPoolExecutor(CONNECTION_POOL_SIZE, CONNECTION_POOL_SIZE, 1000,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        // listen for incoming requests
        listen();
    }

    private static void initialiseServerSocket(String hostname, int portNumber){
        try {
            // Create server socket
            ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(hostname, portNumber));
        } catch (Exception e) {
            System.err.println("DFSDS: Error creating socket.\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void listen(){
        try {
            while(true){
                System.out.print("DFSDS: Listening for connections...\n");
                // Setup a new Connection thread when a new client connects.
                Socket so = ss.accept();
                requestThreads.execute(new RequestProcessor(so, fileServerNodes));
            }
        } catch (Exception e) {
            System.err.println("DFSDS: Error while listening for connections.\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
