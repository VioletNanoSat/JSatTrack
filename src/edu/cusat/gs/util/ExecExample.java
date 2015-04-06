package edu.cusat.gs.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * An example of how to use Runtime.exec(). Opens the command prompt, sends a
 * few commands to it, and then exits.
 * 
 * Based off the example found here: {@link http
 * ://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html}
 * 
 * @author natep
 * 
 */
public class ExecExample {

    public static void main(String args[]) throws Exception {

        if (args.length < 1) {
            System.out.println("USAGE: java edu.cusat.util.ExecExample "
                    + "(commands)+");
            System.out.println();
            System.out.println("\tRun from CUSatRepository\\GroundCode\\trunk\\" +
            		"Serial\\bin");
            System.out.println();
            System.out.println("Example:");
            System.out.println("\t{...}\\bin>java edu.cusat.util.ExecExample " +
            		"tree dir hello");
            System.out.println();
            System.exit(1);
        }

        // Send the command
        Process cmd = Runtime.getRuntime().exec("cmd");

        // Start separate threads to handle cmd's output
        new StreamGobbler(cmd.getErrorStream(), "ERR").start();
        new StreamGobbler(cmd.getInputStream(), "OUT").start();

        // Now write our commands
        BufferedWriter os = new BufferedWriter(new OutputStreamWriter(cmd
                .getOutputStream()));
        for (int i = 0; i < args.length; i++) {
            sendCmd(os, args[i]);
        }

        // And, we're done!
        sendCmd(os, "exit");
        System.out.println("Exiting with value " + cmd.waitFor());
        os.close();
    }

    static void sendCmd(BufferedWriter w, String s) throws IOException {
        w.write(s);
        w.newLine();
        w.flush();
    }

    static class StreamGobbler extends Thread {
        InputStream is;
        String type;

        StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null)
                    System.out.println(type + ">" + line);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
