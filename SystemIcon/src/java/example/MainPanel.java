package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
//import sun.awt.shell.ShellFolder;

public class MainPanel extends JPanel {
    private final JLabel smallLabel = new JLabel();
    private final JLabel largeLabel = new JLabel();
    public MainPanel() {
        super();
        smallLabel.setPreferredSize(new Dimension(16+1,16+1));
        smallLabel.setMaximumSize(new Dimension(16+1,16+1));
        smallLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY,1));
        smallLabel.setAlignmentY(BOTTOM_ALIGNMENT);

        largeLabel.setPreferredSize(new Dimension(32+1,32+1));
        largeLabel.setMaximumSize(new Dimension(32+1,32+1));
        largeLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY,1));
        largeLabel.setAlignmentY(BOTTOM_ALIGNMENT);

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createTitledBorder("drop File"));
        box.add(smallLabel);
        box.add(Box.createHorizontalStrut(5));
        box.add(largeLabel);
        add(box);
        add(new JLabel("<html>警告:sun.awt.shell.ShellFolder は Sun が所有する"+
                       "<br>API であり、今後のリリースで"+
                       "<br>削除される可能性があります。"));

        DropTargetListener dtl = new DropTargetAdapter() {
            @Override public void dragOver(DropTargetDragEvent dtde) {
                if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    return;
                }
                dtde.rejectDrag();
            }
            @Override public void drop(DropTargetDropEvent dtde) {
                try{
                    if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        Transferable t = dtde.getTransferable();
                        java.util.List list = (java.util.List)t.getTransferData(DataFlavor.javaFileListFlavor);
                        Object o = list.get(0);
                        if(o instanceof File) {
                            File file = (File) o;
                            smallLabel.setIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
                            largeLabel.setIcon(new ImageIcon(
                                sun.awt.shell.ShellFolder.getShellFolder(file).getIcon(true)));
                        }
                        dtde.dropComplete(true);
                        return;
                    }
                }catch(UnsupportedFlavorException ufe) {
                    ufe.printStackTrace();
                }catch(IOException ioe) {
                    ioe.printStackTrace();
                }
                dtde.rejectDrop();
            }
        };
        setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, dtl, true));
        setPreferredSize(new Dimension(320, 160));
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
