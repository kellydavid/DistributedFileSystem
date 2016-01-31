package com.kellydavid.dfs;


import java.net.InetAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RequestProcessor implements Runnable{

    private Socket so;
    private FileListing fileListing;

    public RequestProcessor(Socket so, FileListing fileListing){
        this.so = so;
        this.fileListing = fileListing;
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
            System.err.println("DFSFS: Error processing request\n");
            e.printStackTrace();
        }
    }

    private void process(String request){
        if(request.startsWith("DOWNLOAD")){
            System.out.print("DFSFS: Received DOWNLOAD request\n");
            downloadHandler(request);
        }else if(request.startsWith("UPLOAD")){
            System.out.print("DFSFS: Received UPLOAD request\n");
            uploadHandler(request);
        }else if(request.startsWith("DELETE")){
            System.out.print("DFSFS: Received DELETE request\n");
            deleteHandler(request);
        }
        else if(request.startsWith("KILL_SERVICE")){
            System.out.print("DFSFS: Received KILL_SERVICE request\n");
            System.exit(0);
        }else{
            System.out.print("DFSFS: Received unknown request\n");
        }
    }

    private void downloadHandler(String request){

    }

    private void uploadHandler(String request){

    }

    private void deleteHandler(String request){

    }

    private void sendResponse(String response){
        try {
            so.getOutputStream().write(response.getBytes());
            so.getOutputStream().flush();
        }catch(Exception e){
            System.err.println("DFSFS: Error sending response.\n");
            e.printStackTrace();
        }
    }
}
