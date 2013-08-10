package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

/**
 * SortedListModel to sort the list of available or selected metrics during metrics selection.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class SortedListModel extends AbstractListModel<String> {
    /**
     * The version number for (de)serialization of this class (UID: universal identifier).
     */
    private static final long serialVersionUID = 1;

    /**
     * The model that stores the metrics in this sorted list model.
     */
    private final SortedSet<String> model;

    /**
     * Create a sorted list model.
     */
    public SortedListModel() {
        model = new TreeSet<>();
    }

    /**
     * Returns the model of this sorted list model.
     *
     * @return the model of this sorted list model.
     */
    public SortedSet<String> getModel() {
        return model;
    }

    @Override
    public int getSize() {
        return model.size();
    }

    @Override
    public String getElementAt(final int index) {
        return (String) model.toArray()[index];
    }

    /**
     * Add a metric to this sorted list model.
     *
     * @param element the metric to add.
     */
    public void add(final String element) {
        if (model.add(element)) {
            fireContentsChanged(this, 0, getSize());
        }
    }

    /**
     * Remove the metric at the specified index from this sorted list model.
     *
     * @param index the index of the metric to remove.
     */
    public void removeElementAt(final int index) {
        if (model.remove(getElementAt(index))) {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
