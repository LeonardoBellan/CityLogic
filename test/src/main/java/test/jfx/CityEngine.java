package test.jfx;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CityEngine {
    public final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    // 0=ROAD  1=BUILDING  2=PARK  3=WATER  4=PAVEMENT  5=HIGHWAY  6=ROOF
    private int[][] map = {{0}};

    public void changeMap(int[][] newMap) {
        int[][] oldMap = map;
        this.map = newMap;
        pcs.firePropertyChange("map", oldMap, newMap); // notifies listeners
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
         this.pcs.addPropertyChangeListener(l);
    }
}