package jsattrak.utilities;

import jsattrak.objects.AbstractSatellite;

public interface SatListener {

    public void satAdded(AbstractSatellite sat);
    
    public void satRemoved(AbstractSatellite sat);
    
}
