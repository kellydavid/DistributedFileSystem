package com.kellydavid.dfs;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.kellydavid.dfs.config.Configurator;

public class App
{
    public static final int CONNECTION_POOL_SIZE = 10;

    private static ServerSocket ss;
    private static ThreadPoolExecutor requestThreads;
    private static FileListing fileListing;
    private static int listeningPort;
    private static Configurator config;

    public static void main( String[] args )
    {
        // Get hostname and port number
        if(args.length != 1){
            System.out.println("DFSFS: Must supply path to config file as argument.\n");
            System.exit(-1);
        }

        config = new Configurator(args[0]);
        config.loadConfiguration();
        String address = config.getValue("ADDRESS");
        listeningPort = Integer.parseInt(config.getValue("PORT"));

        // intialise variables
        initialiseFileListing(listeningPort);
        initialiseServerSocket(address, listeningPort);
        requestThreads = new ThreadPoolExecutor(CONNECTION_POOL_SIZE, CONNECTION_POOL_SIZE, 1000,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        // listen for incoming requests
        listen();
    }

    private static void initialiseFileListing(int listeningPort){
        // create new file listing and advertise it
        fileListing = new FileListing(config, listeningPort);
        fileListing.initialiseFileListing();
        fileListing.advertiseFileListing();
    }

    private static void initialiseServerSocket(String hostname, int portNumber){
        try {
            // Create server socket
            ss = new ServerSocket();
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(hostname, portNumber));
        } catch (Exception e) {
            System.err.println("DFSFS: Error creating socket.\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void listen(){
        try {
            while(true){
                System.out.print("DFSFS: Listening for connections...\n");
                // Setup a new Connection thread when a new client connects.
                Socket so = ss.accept();
                requestThreads.execute(new RequestProcessor(so, fileListing));
            }
        } catch (Exception e) {
            System.err.println("DFSFS: Error while listening for connections.\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
