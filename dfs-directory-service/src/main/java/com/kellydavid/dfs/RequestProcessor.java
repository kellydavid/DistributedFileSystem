package com.kellydavid.dfs;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class RequestProcessor implements Runnable{

    private Socket so;
    private HashMap<String, LinkedList<String>> fileServerNodes;

    /**
     * Constructor for RequestProcessor accepts socket of client.
     * @param so
     */
    public RequestProcessor(Socket so, HashMap<String, LinkedList<String>> fileServerNodes){
        this.so = so;
        this.fileServerNodes = fileServerNodes;
    }

    public void run(){
        try{
            // receive data
            String recvd = new BufferedReader(new InputStreamReader(so.getInputStream())).readLine();
            // process request
            process(recvd);
            // close socket
            so.close();
        }catch(Exception e){
            System.err.println("DFSDS: Error processing request\n");
            e.printStackTrace();
        }
    }

    private void process(String request){
        if(request.startsWith("GET_FILE")){
            System.out.print("DFSDS: Received GET_FILE request\n");
            getHandler(request);
        }else if(request.startsWith("PUT_FILE")){
            System.out.print("DFSDS: Received PUT_FILE request\n");
            putHandler(request);
        }else if(request.startsWith("ADVERTISE")){
            System.out.print("DFSDS: Received ADVERTISE request\n");
            advertiseHandler(request);
        }
        else if(request.startsWith("KILL_SERVICE")){
            System.out.print("DFSDS: Received KILL_SERVICE request\n");
            System.exit(0);
        }else{
            System.out.print("DFSDS: Received unknown request\n");
        }
    }

    private synchronized void getHandler(String request){
        // Takes request of the form
        // "GET_FILE: file_name \n"

        String filename = request.split(" ")[1];
        String fsnId = findFileLocation(filename);

        // send file location
        if(fsnId == null){
            sendResponse("FILE_NOT_FOUND\n");
        }else {
            sendResponse("FILE_LOCATED " + fsnId + " \n");
        }
    }

    private synchronized String findFileLocation(String filename){
        for(Map.Entry<String, LinkedList<String>> entry : fileServerNodes.entrySet()){
            if(entry.getValue().contains(filename)){
                return (String)entry.getKey();
            }
        }
        return null;
    }

    private synchronized void putHandler(String request){
        // Takes request of the form
        // "PUT_FILE: file_name \n"

        String filename = request.split(" ")[1];
        String fsnId = findFileLocation(filename);

        // no file servers available
        if(fileServerNodes.size() == 0){
            sendResponse("NO_FILE_SERVERS_AVAILABLE\n");
        }
        // file already in a location, update
        else if (fsnId != null){
            sendResponse("FILE_LOCATED " + fsnId + " \n");
        }
        // find a file server with least amount of files as new location
        else{
            for(Map.Entry<String, LinkedList<String>> entry : fileServerNodes.entrySet()){
                if(fsnId.equals("")){
                    fsnId = (String)entry.getKey();
                }else if(entry.getValue().size() < fileServerNodes.get(fsnId).size()){
                    fsnId = (String)entry.getKey();
                }
            }
            sendResponse("FILE_LOCATED " + fsnId + " \n");
        }
    }

    private synchronized void advertiseHandler(String request){
        // takes request of the form
        // "ADVERTISE: <port-number> file1 file2 ....fileN \n"

        String components[] = request.split(" ");
        // FSN id is the <ip:port>
        String id = so.getInetAddress().getHostAddress() + ":";
        id += components[1];

        // create list of files
        LinkedList<String> files = new LinkedList<String>();
        for(int i = 2; i < components.length; i++){
            if(!components[i].equals("\n"))
                files.add(components[i]);
        }

        // if entry for file server node exists, delete then add new version
        if(fileServerNodes.containsKey(id)){
            fileServerNodes.remove(id);
        }
        fileServerNodes.put(id, files);

        // send acknowledgement to file server node
        sendResponse("RECEIVED_ADVERTISE\n");
    }

    private void sendResponse(String response){
        try {
            so.getOutputStream().write(response.getBytes());
            so.getOutputStream().flush();
        }catch(Exception e){
            System.err.println("DFSDS: Error sending response.\n");
            e.printStackTrace();
        }
    }
}
