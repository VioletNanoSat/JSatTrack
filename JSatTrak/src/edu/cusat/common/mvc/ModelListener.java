package edu.cusat.common.mvc;
/**
 * A generic MVC view, or model listener.
 */
public interface ModelListener<M> {
    void modelChanged(final M model);
}
