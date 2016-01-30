package com.kellydavid.dfs;

import java.net.Socket;

public class RequestProcessor implements Runnable{

    private Socket so;

    public RequestProcessor(Socket so){
        this.so = so;
    }

    @Override
    public void run(){
        System.out.println("hi");
    }
}
