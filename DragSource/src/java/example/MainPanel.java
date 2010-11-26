package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JLabel label = new JLabel();
    private final DragSource dragSource = DragSource.getDefaultDragSource();

    private final URL u1 = MainPanel.class.getResource("i03-04.gif");
    private final URL u2 = MainPanel.class.getResource("i03-10.gif");
    private final ImageIcon i1 = new ImageIcon(u1);
    private final ImageIcon i2 = new ImageIcon(u2);

    public MainPanel() {
        super(new BorderLayout());
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBorder(BorderFactory.createTitledBorder("Drag Source JLabel"));

        clearFile();
        dragSource.createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_MOVE, new MyDragGestureListener());

        final Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("Create Temp File") {
            @Override public void actionPerformed(ActionEvent ae) {
                File outfile;
                try{
                    outfile = File.createTempFile("test",".tmp");
                    outfile.deleteOnExit();
                }catch(IOException ioe) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(box,"Could not create file.","Error",JOptionPane.ERROR_MESSAGE);
                    return;
                }
                setFile(outfile);
            }
        }));
        box.add(Box.createHorizontalStrut(2));
        box.add(new JButton(new AbstractAction("Clear") {
            @Override public void actionPerformed(ActionEvent ae) {
                clearFile();
                repaint();
            }
        }));
        add(label);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 200));
    }

    private File file = null;
    private File getFile() {
        return file;
    }
    private void setFile(File file) {
        this.file = file;
        label.setIcon(i2);
        label.setText("tmpfile#exists(): true(draggable)");
        //label.setBorder(BorderFactory.createRaisedBevelBorder());
    }
    private void clearFile() {
        file = null;
        label.setIcon(i1);
        label.setText("tmpfile#exists(): false");
        //label.setBorder(BorderFactory.createEmptyBorder());
    }
    class MyDragGestureListener implements DragGestureListener {
        @Override public void dragGestureRecognized(DragGestureEvent dge) {
            final File tmpfile = getFile();
            if(tmpfile==null) {
                return;
            }
            Transferable tran = new Transferable() {
                @Override public Object getTransferData(DataFlavor flavor) {
                    return Arrays.asList(tmpfile);
                }
                @Override public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[] { DataFlavor.javaFileListFlavor };
                }
                @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(DataFlavor.javaFileListFlavor);
                }
            };
            DragSourceAdapter dsa = new DragSourceAdapter() {
                @Override public void dragDropEnd(DragSourceDropEvent dsde) {
                    if(dsde.getDropSuccess()) {
                        clearFile();
                    }
                }
            };
            dge.startDrag(DragSource.DefaultMoveDrop, tran, dsa);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
