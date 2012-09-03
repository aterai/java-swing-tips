package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.EventListenerList;

public class MainPanel extends JPanel{
    private static final Font FONT12 = new Font("Sans-serif", Font.PLAIN, 12);
    private static final Font FONT32 = new Font("Sans-serif", Font.PLAIN, 32);
    private final MyButton   button = new MyButton("dummy");
    private final MyLabel    label  = new MyLabel("test");
    private final MyComboBox combo  = new MyComboBox();

    static class MyComboBox extends JComboBox implements FontChangeListener{
        @SuppressWarnings("unchecked")
        public MyComboBox() {
            super(new DefaultComboBoxModel(new Object[] {"test test"}));
            setFont(FONT12);
        }
        public void fontStateChanged(FontChangeEvent e) {
            setFont(e.getFont());
        }
    }
    static class MyLabel extends JLabel implements FontChangeListener{
        public MyLabel(String str) {
            super(str);
            setFont(FONT12);
        }
        public void fontStateChanged(FontChangeEvent e) {
            setFont(e.getFont());
        }
    }
    static class MyButton extends JButton implements FontChangeListener{
        public MyButton(String str) {
            super(str);
            setFont(FONT12);
        }
        public void fontStateChanged(FontChangeEvent e) {
            setFont(e.getFont());
        }
    }
//*
    //http://docs.oracle.com/javase/jp/6/api/javax/swing/event/EventListenerList.html
    EventListenerList listenerList = new EventListenerList();
    //FontChangeEvent fontChangeEvent = null;
    public void addFontChangeListener(FontChangeListener l) {
        listenerList.add(FontChangeListener.class, l);
    }
    public void removeFontChangeListener(FontChangeListener l) {
        listenerList.remove(FontChangeListener.class, l);
    }
    // Notify all listeners that have registered interest for
    // notification on this event type.The event instance
    // is lazily created using the parameters passed into
    // the fire method.
    protected void fireFontChangeEvent(String cmd, Font font) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        FontChangeEvent evt = new FontChangeEvent(this, cmd, font);
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==FontChangeListener.class) {
                // Lazily create the event:
//                 if(fontChangeEvent == null)
//                   fontChangeEvent = new FontChangeEvent(this);
                ((FontChangeListener)listeners[i+1]).fontStateChanged(evt);
            }
        }
    }
/*/
    // http://www.asahi-net.or.jp/~dp8t-asm/java/tips/Event.html
    private final Vector<FontChangeListener> listenerList = new Vector<FontChangeListener>();
    public void addFontChangeListener(FontChangeListener listener) {
        if(!listenerList.contains(listener)) listenerList.add(listener);
    }
    public void removeFontChangeListener(FontChangeListener listener) {
        listenerList.remove(listener);
    }
    public void fireFontChangeEvent(String cmd, Font font) {
        Vector list = (Vector)listenerList.clone();
        Enumeration e = list.elements();
        FontChangeEvent evt = new FontChangeEvent(this, cmd, font);
        while(e.hasMoreElements()) {
            FontChangeListener listener = (FontChangeListener)e.nextElement();
            listener.fontStateChanged(evt);
        }
        revalidate();
    }
//*/
    public MainPanel() {
        super(new BorderLayout());

        addFontChangeListener(button);
        //addFontChangeListener(combo);
        addFontChangeListener(label);

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("Font");
        menu.setToolTipText("Select font size");
        JMenuItem font32 = new JMenuItem("32pt");
        JMenuItem font12 = new JMenuItem("12pt");
        menu.add(font12);
        menu.add(font32);
        menubar.add(menu);

        font12.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                fireFontChangeEvent("font12", FONT12);
            }
        });
        font32.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                fireFontChangeEvent("font32", FONT32);
            }
        });

        JPanel panel = new JPanel();
        panel.add(label);
        panel.add(combo);
        panel.add(button);
        add(menubar, BorderLayout.NORTH);
        add(panel);
        setPreferredSize(new Dimension(320, 200));
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

interface FontChangeListener extends EventListener{
    public void fontStateChanged(FontChangeEvent e);
}
class FontChangeEvent extends EventObject{
    private final String command;
    private final Font font;
    public String getCommand() {
        return command;
    }
    public Font getFont() {
        return font;
    }
    public FontChangeEvent(Object source, String cmd, Font font) {
        super(source);
        this.command = cmd;
        this.font = font;
    }
}
