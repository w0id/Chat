package ru.gb.chat.server;

import java.io.*;

public class ReverseRead {

    private final String historyFile;

    @Override
    public String toString(){
        return read(historyFile);
    }

    public ReverseRead(final String historyFile) {
        this.historyFile = historyFile;
    }

    public String read(String historyFile) {
        int count = 0;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(new File(historyFile))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        do {
            count++;
            String line = null;
            try {
                if (in != null) {
                    line = in.readLine();
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            sb.insert(0, line + "\n");
        } while (count < 100);
        return sb.toString();
    }
}
