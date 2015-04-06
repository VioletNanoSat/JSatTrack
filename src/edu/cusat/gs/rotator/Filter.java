package edu.cusat.gs.rotator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A filter determines whether or not a command should be sent in order to
 * maximize the pointing accuracy of the antenna.
 * 
 * @author Nate Parsons nsp25
 * 
 */
public abstract class Filter {
    
    private static final Pattern SYNTAX = Pattern.compile("W(\\d{3}) (\\d{3})");
    
    /**
     * Checks the syntax of a command
     * TODO Better checking
     */
    boolean valid(String command){
        if (command == null) return false; 
        Matcher m = SYNTAX.matcher(command);
        if (!m.matches()) return false;
        return valid(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)));
    }
    
    /**
     * Checks that the azimuth and elevation are achievable
     */
    boolean valid(int az, int el){
       return (0 <= az && az <= 450) && (0 <= el && el <= 180); 
    }
    
    public abstract boolean passes(String command);
    
    public abstract boolean passes(int az, int el);
}
