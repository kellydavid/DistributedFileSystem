package com.kellydavid.dfs;


import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

interface FileListingUpdateListener{

    void advertiseFileListing();
}

public class FileListing implements FileListingUpdateListener{
    private HashMap<String, String> fileList; // filename and absolute file path
    private String directory;
    private InetAddress dsAddress;
    private int dsPort;
    private int listeningPort;

    public FileListing(String directory, int listeningPort, InetAddress dsAddress, int dsPort){
        this.directory = directory;
        this.dsAddress = dsAddress;
        this.dsPort = dsPort;
        this.listeningPort = listeningPort;
    }

    public void initialiseFileListing(){
        File[] files = (new File(directory)).listFiles();
        for(File file: files){
            if(file.isFile())
                fileList.put(file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void advertiseFileListing(){
        String files = " ";
        for(String key : fileList.keySet()){
            files += key + " ";
        }
        try {
            Socket so = new Socket(dsAddress, dsPort);
            so.getOutputStream().write(("ADVERTISE " + listeningPort + files + "\n").getBytes());
            so.getOutputStream().flush();
            so.close();
        }catch(Exception e){
            System.err.println("DFSFS: Error creating socket to directory service\n");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}