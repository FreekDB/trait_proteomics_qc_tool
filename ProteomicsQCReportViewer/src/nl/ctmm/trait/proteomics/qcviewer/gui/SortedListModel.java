package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;

/**
 * SortedListModel to sort the list of available or selected metrics during metrics selection.
 */
public class SortedListModel extends AbstractListModel<String> {
    /**
     * The version number for (de)serialization of this class (UID: universal identifier).
     */
    private static final long serialVersionUID = 1;

    private final SortedSet<String> model;

    public SortedListModel() {
        model = new TreeSet<>();
    }

    public SortedSet<String> getModel() {
        return model;
    }

    public int getSize() {
        return model.size();
    }

    public String getElementAt(final int index) {
        return (String) model.toArray()[index];
    }

    public void add(final String element) {
        if (model.add(element)) {
            fireContentsChanged(this, 0, getSize());
        }
    }

    public void removeElementAt(final int index) {
        if (model.remove(getElementAt(index))) {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
