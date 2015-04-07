package edu.cusat.gs.rotator;

/**
 * The basic filter works by eliminating repeats. TODO sync up commands and pure
 * data
 * 
 * @author Nate Parsons nsp25
 * 
 */
public class BasicFilter extends Filter {

    private String oldCommand = "";
    private int oldAz = Integer.MIN_VALUE;
    private int oldEl = Integer.MIN_VALUE;

    public BasicFilter(String currentPos) {
        oldCommand = currentPos == null ? "" : currentPos;
    }

    public BasicFilter(int currAz, int currEl) {
        oldAz = currAz;
        oldEl = currEl;
    }

    @Override
    public boolean passes(String command) {
        if (valid(command)) {
            boolean ret = !oldCommand.equals(command);
            oldCommand = command;
            return ret;
        }
        return false;
    }

    @Override
    public boolean passes(int az, int el) {
        if (valid(az, el)) {
            boolean ret = az != oldAz || el != oldEl;
            oldAz = az;
            oldEl = el;
            return ret;
        }
        return false;
    }

}
