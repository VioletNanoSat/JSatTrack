package edu.cusat.common.mvc;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic MVC model with generic properties.
 */
@SuppressWarnings("unchecked")
public class Model<L extends ModelListener> {
    private final List<L> listeners;

    public Model() {
        this.listeners = new ArrayList<L>();
    }

    public void addModelListener(final L listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
            notifyModelListener(listener);
        }
    }

    public void removeModelListener(final L listener) {
        this.listeners.remove(listener);
    }

    protected void notifyModelListeners() {
        for (final L listener : this.listeners) {
            notifyModelListener(listener);
        }
    }

    protected void notifyModelListener(final L listener) {
        listener.modelChanged(this);
    }

    public class Property<T> {
        private T value;

        public Property(final T initialValue) {
            this.value = initialValue;
        }

        public void set(final T value) {
        	boolean notify = value != this.value;
            this.value = value;
            if(notify) notifyModelListeners();
        }
        
        
        public T get() { return this.value; }
    }
    
    /** An integer property bound within a range of valid values **/
    public class BoundedInt extends Property<Integer> {

        public final Integer min;
        public final Integer max;
        
        public BoundedInt(Integer min, Integer max, Integer initialValue){
            super(Math.max(min, Math.min(initialValue, max)));
            this.min = min;
            this.max = max;
        }
        
        @Override
        public void set(Integer value) {
            super.set(Math.max(min, Math.min(value, max)));
        }
        
    }

    /** An integer property that cannot be negative **/
    public class NonNegativeInt extends BoundedInt {
        
        public NonNegativeInt(Integer initialValue) {
            super(0, Integer.MAX_VALUE, initialValue);
        }
    }
}
