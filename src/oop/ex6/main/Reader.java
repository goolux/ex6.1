package oop.ex6.main;

import java.io.*;
import java.util.ArrayList;

import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.readAllLines;

/**
 * a single tone class which read the file from a given path.
 */
class Reader {
    /**
    instance member
     */
    private static Reader instance=new Reader();

    /**default constructor*/
    private Reader(){
    }

    /**getter for instance*/
    static Reader getInstance() {
        return instance;
    }

    /**
     * Gets all List with all files
     * @return LinkedList with all lines in file, each line a String in the list.
     */
    ArrayList<String> readLines(String path) throws IOException {
        ArrayList<String> sjavaData=new ArrayList<>();
            try (BufferedReader bufferedReader=new BufferedReader(new FileReader(path))) {
                File f = new File(path);
                if(!f.exists()){
                    throw new IOException();
                }
                int counter = 0;
                String line;
                while ((line = bufferedReader.readLine() )!= null) {
                    sjavaData.add(counter, line);
                    counter++;
                }
        } catch (IOException e) {
            throw e;
        }
        return sjavaData;
    }
}
