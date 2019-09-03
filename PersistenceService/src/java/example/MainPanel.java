// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JScrollPane scroll = new JScrollPane(new JTree());

  protected MainPanel() {
    super(new BorderLayout());
    // // Test
    // JButton clearButton = new JButton(new AbstractAction("clear muf and JFrame#dispose()") {
    //   @Override public void actionPerformed(ActionEvent e) {
    //     // PersistenceService persistenceService = ...
    //     persistenceService.delete(codebase);
    //     Window frame = SwingUtilities.getWindowAncestor((Component) e.getSource());
    //     frame.dispose();
    //   }
    // });
    // Box box = Box.createHorizontalBox();
    // box.add(Box.createHorizontalGlue());
    // box.add(clearButton);
    // add(box, BorderLayout.SOUTH);
    add(scroll);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    WindowState windowState = new WindowState();
    SwingWorker<WindowListener, Void> worker = new LoadSaveTask(windowState) {
      @Override public void done() {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
          ex.printStackTrace();
          Toolkit.getDefaultToolkit().beep();
        }

        JFrame frame = new JFrame("@title@");
        try {
          WindowListener windowListener = get();
          if (Objects.nonNull(windowListener)) {
            frame.addWindowListener(windowListener);
          }
        } catch (InterruptedException | ExecutionException ex) {
          ex.printStackTrace();
          Toolkit.getDefaultToolkit().beep();
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

class LoadSaveTask extends SwingWorker<WindowListener, Void> {
  protected final WindowState windowState;

  protected LoadSaveTask(WindowState windowState) {
    super();
    this.windowState = windowState;
  }

  @Override public WindowListener doInBackground() {
    PersistenceService ps;
    BasicService bs;
    try {
      bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
      ps = (PersistenceService) ServiceManager.lookup("javax.jnlp.PersistenceService");
    } catch (UnavailableServiceException ex) {
      // ex.printStackTrace();
      ps = null;
      bs = null;
    }
    if (Objects.nonNull(ps) && Objects.nonNull(bs)) {
      PersistenceService persistenceService = ps;
      URL codebase = bs.getCodeBase();
      loadWindowState(persistenceService, codebase, windowState);
      return new WindowAdapter() {
        @Override public void windowClosing(WindowEvent e) {
          JFrame f = (JFrame) e.getComponent();
          if (f.getExtendedState() == Frame.NORMAL) {
            windowState.setSize(f.getSize());
            // Point pt = f.getLocationOnScreen();
            // if (pt.x < 0 || pt.y < 0) {
            //   return;
            // }
            windowState.setLocation(f.getLocationOnScreen());
          }
          saveWindowState(persistenceService, codebase, windowState);
        }
      };
    } else {
      return null;
    }
  }

  private static void loadWindowState(PersistenceService ps, URL codebase, WindowState windowState) {
    try {
      FileContents fc = ps.get(codebase);
      try (XMLDecoder d = new XMLDecoder(new BufferedInputStream(fc.getInputStream()))) {
        @SuppressWarnings("unchecked") Map<String, Serializable> map = (Map<String, Serializable>) d.readObject();
        // d.close();
        windowState.setSize((Dimension) map.get("size"));
        windowState.setLocation((Point) map.get("location"));
        // // Test:
        // // ObjectInputStream d = new ObjectInputStream(appSettings.getInputStream());
        // // WindowState cache = (WindowState) d.readObject();
        // // Test:
        // WindowState cache = (WindowState) map.get("setting");
        // System.out.println("aaa: " + cache.getSize());
        // System.out.println("aaa: " + cache.getLocation());
      }
    } catch (IOException ex) {
      // create the cache
      try {
        long size = ps.create(codebase, 64_000);
        System.out.println("Cache created - size: " + size);
      } catch (IOException ioex) {
        // PMD: PreserveStackTrace false positive when using UncheckedIOException in the nested try-catch blocks?
        // throw new UncheckedIOException("Application codebase is not a valid URL?!", ioex);
        assert false : "Application codebase is not a valid URL?!";
      }
    }
  }

  protected static void saveWindowState(PersistenceService ps, URL codebase, WindowState windowState) {
    try {
      FileContents fc = ps.get(codebase);
      try (XMLEncoder e = new XMLEncoder(new BufferedOutputStream(fc.getOutputStream(true)))) {
        // Test: delete muf ex. C:\Users\(user)\AppData\LocalLow\Sun\Java\Deployment\cache\6.0\muffin\xxxxxx-xxxxx.muf
        // ps.delete(codebase);
        // ObjectOutputStream e = new ObjectOutputStream(fc.getOutputStream(true));
        HashMap<String, Serializable> map = new HashMap<>();
        map.put("size", (Serializable) windowState.getSize());
        map.put("location", (Serializable) windowState.getLocation());
        // Test1: map.put("setting", (Serializable) windowState);
        // Test2: e.writeObject(windowState);
        e.writeObject(map);
        e.flush();
        // e.close();
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}

// class TestBufferedInputStream extends BufferedInputStream {
//   protected TestBufferedInputStream(InputStream in) {
//     super(in);
//   }
//   @Override public void close() throws IOException {
//     System.out.println("BufferedInputStream");
//     super.close();
//   }
//
// }
//
// class TestBufferedOutputStream extends BufferedOutputStream {
//   protected TestBufferedOutputStream(OutputStream out) {
//     super(out);
//   }
//   @Override public void close() throws IOException {
//     System.out.println("BufferedOutputStream");
//     super.close();
//   }
// }
