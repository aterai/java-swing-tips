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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import javax.jnlp.BasicService;
import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public MainPanel() {
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
    JScrollPane scroll = new JScrollPane(new JTree());
    add(scroll);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    WindowState windowState = new WindowState();
    SwingWorker<WindowListener, Void> worker = new LoadSaveTask(windowState) {
      @Override protected void done() {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException ignored) {
          Toolkit.getDefaultToolkit().beep();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
          ex.printStackTrace();
          return;
        }
        JFrame frame = new JFrame("@title@");
        try {
          WindowListener windowListener = get();
          if (Objects.nonNull(windowListener)) {
            frame.addWindowListener(windowListener);
          }
        } catch (InterruptedException | ExecutionException ex) {
          Thread.currentThread().interrupt();
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
    Object bs = getService("javax.jnlp.BasicService");
    Object ps = getService("javax.jnlp.PersistenceService");
    return ps instanceof PersistenceService && bs instanceof BasicService
        ? makeWindowAdapter((PersistenceService) ps, ((BasicService) bs).getCodeBase())
        : null;
  }

  private static Object getService(String service) {
    Optional<Object> op;
    try {
      op = Optional.ofNullable(ServiceManager.lookup(service));
    } catch (UnavailableServiceException ex) {
      op = Optional.empty();
    }
    return op.orElse(null);
  }

  private WindowAdapter makeWindowAdapter(PersistenceService service, URL codebase) {
    loadWindowState(service, codebase, windowState);
    return new WindowAdapter() {
      @Override public void windowClosing(WindowEvent e) {
        Frame f = (Frame) e.getComponent();
        if (f.getExtendedState() == Frame.NORMAL) {
          windowState.setSize(f.getSize());
          // Point pt = f.getLocationOnScreen();
          // if (pt.x < 0 || pt.y < 0) {
          //   return;
          // }
          windowState.setLocation(f.getLocationOnScreen());
        }
        saveWindowState(service, codebase, windowState);
      }
    };
  }

  private static void loadWindowState(PersistenceService ps, URL codebase, WindowState state) {
    try {
      FileContents fc = ps.get(codebase);
      try (XMLDecoder d = new XMLDecoder(new BufferedInputStream(fc.getInputStream()))) {
        @SuppressWarnings("unchecked")
        Map<String, Serializable> map = (Map<String, Serializable>) d.readObject();
        // d.close();
        state.setSize((Dimension) map.get("size"));
        state.setLocation((Point) map.get("location"));
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
        ps.create(codebase, 64_000);
        // System.out.println("Cache created - size: " + ps.create(codebase, 64_000));
      } catch (IOException ignore) {
        // PMD:
        // PreserveStackTrace false positive when using UncheckedIOException
        // in the nested try-catch blocks?
        // throw new UncheckedIOException("Application codebase is not a valid URL?!", ignore);
        assert false : "Application codebase is not a valid URL?!";
      }
    }
  }

  protected static void saveWindowState(PersistenceService ps, URL codebase, WindowState state) {
    try {
      FileContents fc = ps.get(codebase);
      try (XMLEncoder e = new XMLEncoder(new BufferedOutputStream(fc.getOutputStream(true)))) {
        // Test: delete muf ex.
        // C:\Users\(user)\AppData\LocalLow\Sun\Java\Deployment\cache\6.0\muffin\xxx-xxx.muf
        // ps.delete(codebase);
        // ObjectOutputStream e = new ObjectOutputStream(fc.getOutputStream(true));
        Map<String, Serializable> map = new ConcurrentHashMap<>();
        map.put("size", (Serializable) state.getSize());
        map.put("location", (Serializable) state.getLocation());
        // Test1: map.put("setting", (Serializable) state);
        // Test2: e.writeObject(state);
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
//
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
//
//   @Override public void close() throws IOException {
//     System.out.println("BufferedOutputStream");
//     super.close();
//   }
// }
