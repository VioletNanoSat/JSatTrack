package jsattrak.gui;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

import java.util.Enumeration;

import javax.swing.JComboBox;

/**
 * A combo box to display all the serial ports available, and can be limited by
 * type.
 * 
 * @author Nate Parsons nsp25
 * 
 */
public class CommPortComboBox extends JComboBox {
    
    private static final long serialVersionUID = 8166089315323027777L;

    @SuppressWarnings("unchecked")
    public CommPortComboBox() {
        Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) 
            addItem(ports.nextElement().getName());
    }
    
    @SuppressWarnings("unchecked")
    public CommPortComboBox(final int portType){
        Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier port = ports.nextElement();
            if (port.getPortType() == portType)
                addItem(port.getName());
        }
    }

    /**
     * @return the identifier corresponding to the selected port name
     */
    public CommPortIdentifier getSelectedPort() {
        Object selected = dataModel.getSelectedItem();
        if( selected == null ) return null; // To prevent NPE's
        try {
            return CommPortIdentifier.getPortIdentifier(selected.toString());
        } catch (NoSuchPortException e) {
            return null;
        }
    }
}
