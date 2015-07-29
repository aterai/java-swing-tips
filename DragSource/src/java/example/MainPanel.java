package example;
//-*- mode:java; encoding:utf-8 -*-
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

public final class MainPanel extends JPanel {
    private final JLabel label = new JLabel();
    private final URL u1 = getClass().getResource("i03-04.gif");
    private final URL u2 = getClass().getResource("i03-10.gif");
    private final ImageIcon i1 = new ImageIcon(u1);
    private final ImageIcon i2 = new ImageIcon(u2);
    private File file;

    public MainPanel() {
        super(new BorderLayout());
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createTitledBorder("Drag Source JLabel"));
        clearFile();

//*     //JDK 1.5.0
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_MOVE, new DragGestureListener() {
            @Override public void dragGestureRecognized(DragGestureEvent dge) {
                File tmpfile = getFile();
                if (Objects.isNull(tmpfile)) {
                    return;
                }
                DragSourceAdapter dsa = new DragSourceAdapter() {
                    @Override public void dragDropEnd(DragSourceDropEvent dsde) {
                        if (dsde.getDropSuccess()) {
                            clearFile();
                        }
                    }
                };
                dge.startDrag(DragSource.DefaultMoveDrop, new TempFileTransferable(tmpfile), dsa);
            }
        });
/*/     //JDK 1.6.0
        label.setTransferHandler(new TransferHandler() {
            @Override public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }
            @Override protected Transferable createTransferable(JComponent c) {
                File tmpfile = getFile();
                if (Objects.nonNull(tmpfile)) {
                    return new TempFileTransferable(tmpfile);
                } else {
                    return null;
                }
            }
            @Override protected void exportDone(JComponent c, Transferable data, int action) {
                cleanup(c, action == MOVE);
            }
            private void cleanup(JComponent c, boolean isMoved) {
                if (isMoved) {
                    clearFile();
                    c.repaint();
                }
            }
        });
        label.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                System.out.println(e);
                JComponent c = (JComponent) e.getComponent();
                c.getTransferHandler().exportAsDrag(c, e, TransferHandler.COPY);
            }
        });
//*/
        final Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("Create Temp File") {
            @Override public void actionPerformed(ActionEvent ae) {
                File outfile;
                try {
                    outfile = File.createTempFile("test", ".tmp");
                    outfile.deleteOnExit();
                } catch (IOException ioe) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(box, "Could not create file.", "Error", JOptionPane.ERROR_MESSAGE);
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
        setPreferredSize(new Dimension(320, 240));
    }
    private File getFile() {
        return file;
    }
    private void setFile(File file) {
        this.file = file;
        label.setIcon(i2);
        label.setText("tmpfile#exists(): true(draggable)");
    }
    private void clearFile() {
        file = null;
        label.setIcon(i1);
        label.setText("tmpfile#exists(): false");
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TempFileTransferable implements Transferable {
    private final File file;
    public TempFileTransferable(File file) {
        this.file = file;
    }
    @Override public Object getTransferData(DataFlavor flavor) {
        return Arrays.asList(file);
    }
    @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {DataFlavor.javaFileListFlavor};
    }
    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.javaFileListFlavor);
    }
}
