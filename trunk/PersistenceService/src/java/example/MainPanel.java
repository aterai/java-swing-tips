package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.jnlp.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
//         //Test
//         JButton clearButton = new JButton(new AbstractAction("clear muf and JFrame#dispose()") {
//             @Override public void actionPerformed(ActionEvent e) {
//                 //PersistenceService persistenceService = ...
//                 persistenceService.delete(codebase);
//                 Window frame = SwingUtilities.getWindowAncestor((Component)e.getSource());
//                 frame.dispose();
//             }
//         });
//         Box box = Box.createHorizontalBox();
//         box.add(Box.createHorizontalGlue());
//         box.add(clearButton);
//         add(box, BorderLayout.SOUTH);

        add(new JScrollPane(new JTree()));
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        final WindowState windowState = new WindowState();
        SwingWorker<WindowAdapter, Void> worker = new LoadSaveTask(windowState) {
            @Override public void done() {
                WindowAdapter windowListener = null;
                try{
                    windowListener = get();
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }catch(InterruptedException   | ExecutionException |
                       ClassNotFoundException | InstantiationException |
                       IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                JFrame frame = new JFrame("@title@");
                if(windowListener!=null) {
                    frame.addWindowListener(windowListener);
                }
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.getContentPane().add(new MainPanel());
                frame.setSize(windowState.getSize());
                frame.setLocation(windowState.getLocation());
                frame.setVisible(true);
            }
        };
        worker.execute();
    }
}

class LoadSaveTask extends SwingWorker<WindowAdapter, Void> {
    private final WindowState windowState;
    public LoadSaveTask(WindowState windowState) {
        this.windowState = windowState;
    }
    @Override public WindowAdapter doInBackground() {
        PersistenceService ps;
        BasicService bs;
        try{
            bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
            ps = (PersistenceService)ServiceManager.lookup("javax.jnlp.PersistenceService");
        }catch(UnavailableServiceException use) {
            //use.printStackTrace();
            ps = null;
            bs = null;
        }
        if(ps != null && bs != null) {
            final PersistenceService persistenceService = ps;
            final URL codebase = bs.getCodeBase();
            loadWindowState(persistenceService, codebase, windowState);
            return new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) {
                    JFrame f = (JFrame)e.getComponent();
                    if(f.getExtendedState()==JFrame.NORMAL) {
                        windowState.setSize(f.getSize());
                        //Point pt = f.getLocationOnScreen();
                        //if(pt.x<0 || pt.y<0) { return; }
                        windowState.setLocation(f.getLocationOnScreen());
                    }
                    saveWindowState(persistenceService, codebase, windowState);
                }
            };
        }else{
            return null;
        }
    }
    private static void loadWindowState(PersistenceService ps, URL codebase, WindowState windowState) {
        try{
            FileContents fc = ps.get(codebase);
            try(XMLDecoder d = new XMLDecoder(new BufferedInputStream(fc.getInputStream()))) {
                @SuppressWarnings("unchecked") Map<String, Serializable> map = (Map<String, Serializable>)d.readObject();
                //d.close();
                windowState.setSize((Dimension)map.get("size"));
                windowState.setLocation((Point)map.get("location"));
                ////Test:
                ////ObjectInputStream d = new ObjectInputStream(appSettings.getInputStream());
                ////WindowState cache = (WindowState)d.readObject();
                ////Test:
                //WindowState cache = (WindowState)map.get("setting");
                //System.out.println("aaa: "+cache.getSize());
                //System.out.println("aaa: "+cache.getLocation());
            }
        }catch(IOException ex) {
            //create the cache
            try{
                long size = ps.create(codebase, 64000);
                System.out.println( "Cache created - size: " + size );
            }catch(IOException ioe) {
                //System.err.println( "Application codebase is not a valid URL?!" );
                ioe.printStackTrace();
            }
        }
    }
    private static void saveWindowState(PersistenceService ps, URL codebase, WindowState windowState) {
        try{
            FileContents fc = ps.get(codebase);
            try(XMLEncoder e = new XMLEncoder(new BufferedOutputStream(fc.getOutputStream(true)))) {
                //Test: delete muf ex. C:\Users\(user)\AppData\LocalLow\Sun\Java\Deployment\cache\6.0\muffin\xxxxxx-xxxxx.muf
                //ps.delete(codebase);
                //ObjectOutputStream e = new ObjectOutputStream(fc.getOutputStream(true));
                Map<String, Serializable> map = new HashMap<String, Serializable>();
                map.put("size", (Serializable)windowState.getSize());
                map.put("location", (Serializable)windowState.getLocation());
                //Test1: map.put("setting", (Serializable)windowState);
                //Test2: e.writeObject(windowState);
                e.writeObject(map);
                e.flush();
                //e.close();
            }
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

// class TestBufferedInputStream extends BufferedInputStream {
//     public TestBufferedInputStream(InputStream in) {
//         super(in);
//     }
//     @Override public void close() throws IOException {
//         System.out.println("BufferedInputStream");
//         super.close();
//     }
//
// }
//
// class TestBufferedOutputStream extends BufferedOutputStream {
//     public TestBufferedOutputStream(OutputStream out) {
//         super(out);
//     }
//     @Override public void close() throws IOException {
//         System.out.println("BufferedOutputStream");
//         super.close();
//     }
// }
