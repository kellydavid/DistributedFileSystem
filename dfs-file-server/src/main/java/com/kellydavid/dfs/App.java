package com.kellydavid.dfs;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App
{
    public static final int CONNECTION_POOL_SIZE = 10;

    private static ServerSocket ss;
    private static ThreadPoolExecutor requestThreads;
    private static Advertiser advertiser;
    private static InetAddress dsAddress; // IP address directory service
    private static int dsPort; // port number directory service

    public static void main( String[] args )
    {
        // Get hostname and port number
        if(args.length != 2){
            System.out.println("DFSFS: Must use arguments <hostname> <port-number>\n");
            System.exit(-1);
        }
        String hostname = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // intialise variables
        initialiseAdvertiser();
        initialiseServerSocket(hostname, portNumber);
        requestThreads = new ThreadPoolExecutor(CONNECTION_POOL_SIZE, CONNECTION_POOL_SIZE, 1000,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        // listen for incoming requests
        listen();
    }

    private static void generateFileListing(){}

    private static void initialiseAdvertiser(){
        System.out.print("Directory Service IP address: ");
        String address = System.console().readLine();
        System.out.print("\nDFSFS: Directory Service port number: ");
        String port = System.console().readLine();
        System.out.println();
        try {
            dsAddress = InetAddress.getByName(address);
        }catch(Exception e){
            System.err.println("DFSFS: Error getting directory service address\n");
            e.printStackTrace();
            System.exit(-1);
        }
        dsPort = Integer.parseInt(port);

        advertiser = new Advertiser(dsAddress, dsPort);
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
                requestThreads.execute(new RequestProcessor(so, dsAddress, dsPort));
            }
        } catch (Exception e) {
            System.err.println("DFSFS: Error while listening for connections.\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
