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
import javax.swing.*;
import javax.jnlp.*;

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
        final WindowState ws = new WindowState();
        SwingWorker<WindowAdapter,Void> worker = new SwingWorker<WindowAdapter,Void>() {
            @Override public WindowAdapter doInBackground() {
                PersistenceService ps;
                BasicService bs;
                try{
                    bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
                    ps = (PersistenceService)ServiceManager.lookup("javax.jnlp.PersistenceService");
                }catch(Throwable use) { //UnavailableServiceException use) {
                    use.printStackTrace();
                    ps = null;
                    bs = null;
                }
                if(ps != null && bs != null) {
                    final PersistenceService persistenceService = ps;
                    final URL codebase = bs.getCodeBase();
                    loadWindowState(persistenceService, codebase, ws);
                    return new WindowAdapter() {
                        @Override public void windowClosing(WindowEvent e) {
                            JFrame f = (JFrame)e.getComponent();
                            if(f.getExtendedState()==JFrame.NORMAL) {
                                ws.setSize(f.getSize());
                                //Point pt = f.getLocationOnScreen();
                                //if(pt.x<0 || pt.y<0) return;
                                ws.setLocation(f.getLocationOnScreen());
                            }
                            saveWindowState(persistenceService, codebase, ws);
                        }
                    };
                }else{
                    return null;
                }
            }
            @Override public void done() {
                WindowAdapter wa = null;
                try{
                    wa = get();
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }catch(Exception e) {
                    e.printStackTrace();
                }
                JFrame frame = new JFrame("@title@");
                if(wa!=null) frame.addWindowListener(wa);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.getContentPane().add(new MainPanel());
                frame.setSize(ws.getSize());
                frame.setLocation(ws.getLocation());
                frame.setVisible(true);
            }
        };
        worker.execute();
    }
    private static void loadWindowState(PersistenceService ps, URL codebase, WindowState ws) {
        try{
            FileContents fc = ps.get(codebase);
            XMLDecoder d = new XMLDecoder(new BufferedInputStream(fc.getInputStream()));
            @SuppressWarnings("unchecked") Map<String, Serializable> map = (Map<String, Serializable>)d.readObject();
            d.close();
            ws.setSize((Dimension)map.get("size"));
            ws.setLocation((Point)map.get("location"));
//             //Test:
//             //ObjectInputStream d = new ObjectInputStream(appSettings.getInputStream());
//             //WindowState cache = (WindowState)d.readObject();

//             //Test:
//             WindowState cache = (WindowState)map.get("setting");
//             System.out.println("aaa: "+cache.getSize());
//             System.out.println("aaa: "+cache.getLocation());
        }catch(FileNotFoundException fnfe) {
            //create the cache
            try{
                long size = ps.create(codebase, 64000);
                System.out.println( "Cache created - size: " + size );
            }catch(MalformedURLException murle) {
                //System.err.println( "Application codebase is not a valid URL?!" );
                murle.printStackTrace();
            }catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    private static void saveWindowState(PersistenceService ps, URL codebase, WindowState ws) {
        try{
            //Test: delete muf ex. C:\Users\(user)\AppData\LocalLow\Sun\Java\Deployment\cache\6.0\muffin\xxxxxx-xxxxx.muf
            //ps.delete(codebase);
            FileContents fc = ps.get(codebase);
            //ObjectOutputStream e = new ObjectOutputStream(fc.getOutputStream(true));
            XMLEncoder e = new XMLEncoder(new BufferedOutputStream(fc.getOutputStream(true)));
            Map<String, Serializable> map = new HashMap<String, Serializable>();
            map.put("size", (Serializable)ws.getSize());
            map.put("location", (Serializable)ws.getLocation());
            //Test1: map.put("setting", (Serializable)ws);
            //Test2: e.writeObject(ws);
            e.writeObject(map);
            e.flush();
            e.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
