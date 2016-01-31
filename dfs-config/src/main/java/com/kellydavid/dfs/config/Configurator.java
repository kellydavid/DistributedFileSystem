package com.kellydavid.dfs.config;

import java.io.*;
import java.util.HashMap;

/**
 * Created by david on 31/01/2016.
 */
public class Configurator {

    private String filename;
    private HashMap<String, String> configuration;

    public Configurator(String filename){
        this.filename = filename;
        configuration = new HashMap<String, String>();
    }

    public void loadConfiguration(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                addConfigLine(line);
                line = reader.readLine();
            }
            reader.close();
        }catch(FileNotFoundException fnfe){
            System.err.println("Could not find configuration file.");
        }catch(IOException ioe){
            System.err.println("Error reading from configuration file.");
        }finally{
            try {
                if (reader != null)
                    reader.close();
            }catch(IOException ioe){
                System.err.println("Error closing file.");
            }
        }
    }

    private void addConfigLine(String line){
        String split[] = line.split("=");
        if(split.length != 2){
            return;
        }else {
            String key = split[0];
            String value = split[1];
            if (!configuration.containsKey(key)) {
                configuration.put(key, value);
            } else {
                System.err.println("Warning duplicate value for " + key + " encountered.");
            }
        }
    }

    public String getValue(String key){
        return configuration.get(key);
    }
}
