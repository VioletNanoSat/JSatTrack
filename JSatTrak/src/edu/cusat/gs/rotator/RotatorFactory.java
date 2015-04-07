package edu.cusat.gs.rotator;

/**
 * The RotatorFactory is responsible for keeping track of all the rotator types
 * we support, and creating objects of each.
 * 
 * @author Nate Parsons
 * 
 */
public enum RotatorFactory {

    MOCK("Mock Rotator") {
        public Rotator createRotator() {
            return new MockRotator(450, 180, true, true);
        }
    },
    
    YAESUGS232B("Yaesu GS-232B") {
        public Rotator createRotator() { return new YaesuGs232B(); }
    };

    private String name;
    
    private RotatorFactory(String name){ this.name = name; }
    
    public abstract Rotator createRotator();
    
    @Override public String toString() { return name; }
}
