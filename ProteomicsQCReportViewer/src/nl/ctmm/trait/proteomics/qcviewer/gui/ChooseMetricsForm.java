/*
 * Taken from: http://docs.oracle.com/javase/tutorial/uiswing/dnd/dropactiondemo.html
 * 
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import nl.ctmm.trait.proteomics.qcviewer.Main;
import nl.ctmm.trait.proteomics.qcviewer.input.MetricsParser;

/**
 * The class for displaying Metrics selection form - drag n drop user interface.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */

public class ChooseMetricsForm extends JFrame implements ActionListener {
    
    private static final long serialVersionUID = -4740722105234150323L;
    SortedListModel from = new SortedListModel();
    SortedListModel move = new SortedListModel();
    JList<String> dragFrom, moveTo;
    HashMap<String,String> metricsMap;
    HashMap<String,String> selectedMetrics;
    MetricsParser mParser = null;
    ViewerFrame parent = null;

    /**
     * Constructor of metrics selection form
     * @param parent ViewerFrame is the parent 
     * @param mParser Instance of MetricsParser
     * @param selectedMetrics HashMap of previously selected metrics
     */
    public ChooseMetricsForm(ViewerFrame parent, final MetricsParser mParser, HashMap<String, String> selectedMetrics) {
        super("Select QC-Full Metrics for MSQC Report Viewer");
        this.mParser = mParser;
        this.parent = parent;
        this.selectedMetrics = selectedMetrics;
        metricsMap = this.mParser.getMetricsListing(); 
        for (String key : metricsMap.keySet()) {
            String value = metricsMap.get(key);
            //if selectedMetrics already contains key, then store it in move list
            if (selectedMetrics.containsKey(key)) {
                move.add(key + ":" + value);
            } else {
                from.add(key + ":" + value);
            }
        }
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        dragFrom = new JList<String>(from);
        dragFrom.setTransferHandler(new ToFromTransferHandler("dragFrom", TransferHandler.MOVE));
        dragFrom.setDragEnabled(true);
        dragFrom.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dragFrom.setDropMode(DropMode.INSERT);
        JLabel label = new JLabel("Drag n Drop: Metrics to Hide:");
        label.setAlignmentX(0f);
        p.add(label);
        JScrollPane sp = new JScrollPane(dragFrom);
        sp.setAlignmentX(0f);
        p.add(sp);
        add(p, BorderLayout.WEST);
        moveTo = new JList<String>(move);
        moveTo.setTransferHandler(new ToFromTransferHandler("moveTo", TransferHandler.MOVE));
        moveTo.setDropMode(DropMode.INSERT);
        moveTo.setDragEnabled(true);
        
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        label = new JLabel("Drag n Drop: Metrics to Show (max 6):");
        label.setAlignmentX(0f);
        p.add(label);
        sp = new JScrollPane(moveTo);
        sp.setAlignmentX(0f);
        p.add(sp);
        p.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        add(p, BorderLayout.CENTER);
        
        p = new JPanel();
        JButton SUBMIT = new JButton("OK");
        SUBMIT.setSize(new Dimension(50, 30));
        JButton CANCEL = new JButton("CANCEL"); 
        CANCEL.setSize(new Dimension(50, 30));
            SUBMIT.addActionListener(this);
            SUBMIT.setActionCommand("OK");
            CANCEL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
            p.add(SUBMIT, 0);
            p.add(CANCEL, 1);
            add(p, BorderLayout.PAGE_END);
        
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        getContentPane().setPreferredSize(new Dimension(830, 340));
    }

    /**
     * The user has performed actions such as pressing the OK button
     */
    @Override
     public void actionPerformed(ActionEvent ae) {
        System.out.println("DataEntryFrame Action command = " + ae.getActionCommand());
        if (ae.getActionCommand().equals("OK")) {
            //Send list of selected metrics to mParser for updating appProperties
            mParser.updateMetricsToDisplay(move);
            dispose();
            if (parent != null) {
                System.out.println("Invoke ViewerFrame methods");
                parent.clean();
                parent.dispose();
                //new Main().runReportViewer();
                Main.getInstance().runReportViewer();
            }
        }
    }

    class ToFromTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1L;
		private int index = 0;
        int action; 
        String origin;
        //ToTransferHandler
        public ToFromTransferHandler(String origin, int action) { 
            this.action = action;
            this.origin = origin;
        }
        
        public int getSourceActions(JComponent comp) {
            return COPY_OR_MOVE;
        }
        
        public Transferable createTransferable(JComponent comp) {
            String selection = "";
            if (origin.equalsIgnoreCase("dragFrom")) {
                index = dragFrom.getSelectedIndex();
                if (index < 0 || index >= from.getSize()) {
                    return null;
                }
                selection = (String)dragFrom.getSelectedValue();
            } else if (origin.equalsIgnoreCase("moveTo")) {
                index = moveTo.getSelectedIndex();
                if (index < 0 || index >= move.getSize()) {
                    return null;
                }
                selection = (String)moveTo.getSelectedValue();
            }
            return new StringSelection(selection);
        }

        public void exportDone(JComponent comp, Transferable trans, int action) {
            if (action != MOVE) {
                return;
            }
            if (origin.equalsIgnoreCase("dragFrom")) {
                from.removeElementAt(index);
            } else if (origin.equalsIgnoreCase("moveTo")) {
                move.removeElementAt(index);
            }
        }
        
        public boolean canImport(TransferHandler.TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            //check for size of move
            if (move.getSize() > 5 && origin.equalsIgnoreCase("moveTo")) {
                System.out.println("Limit of move list exceeded. Max 6. Origin = " + origin);
                return false;
            }
            // we only import Strings
            if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }
            boolean actionSupported = (action & support.getSourceDropActions()) == action;
            if (actionSupported) {
                support.setDropAction(action);
                return true;
            }
            return false;
        }

        public boolean importData(TransferHandler.TransferSupport support) {
            // if we can't handle the import, say so
            if (!canImport(support)) {
                return false;
            }
            // fetch the drop location
            JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();
            int index = dl.getIndex();
            // fetch the data and bail if this fails
            String data;
            try {
                data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (java.io.IOException e) {
                return false;
            }
            JList<String> list = (JList)support.getComponent();
            SortedListModel model = (SortedListModel)list.getModel();
            model.add(data);
            Rectangle rect = list.getCellBounds(index, index);
            list.scrollRectToVisible(rect);
            list.setSelectedIndex(index);
            list.requestFocusInWindow();
            return true;
        }  
    } 
}

