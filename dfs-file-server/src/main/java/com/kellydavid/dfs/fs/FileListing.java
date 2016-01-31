package com.kellydavid.dfs.fs;


import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import com.kellydavid.dfs.config.Configurator;

interface FileListingUpdateListener{

    void advertiseFileListing();
}

public class FileListing implements FileListingUpdateListener{
    private HashMap<String, String> fileList; // filename and absolute file path
    private String directory;
    private InetAddress dsAddress;
    private int dsPort;
    private int listeningPort;

    public FileListing(Configurator config, int listeningPort){
        fileList = new HashMap<>();
        this.directory = config.getValue("DIRECTORY");
        try {
            this.dsAddress = InetAddress.getByName(config.getValue("DS_ADDRESS"));
        }catch(UnknownHostException e){
            System.err.println("DFSFS: Could not find dsAddress for " + config.getValue("DS_ADDRESS"));
            e.printStackTrace();
            System.exit(-1);
        }
        this.dsPort = Integer.parseInt(config.getValue("DS_PORT"));
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