package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

public class JTabbedPaneWithCloseButton extends JTabbedPane {
    public JTabbedPaneWithCloseButton() {
        super();
        if(getUI() instanceof WindowsTabbedPaneUI) {
            setUI(new CloseButtonWindowsTabbedPaneUI());
        }else{
            setUI(new CloseButtonTabbedPaneUI());
        }
    }
}

class CloseButtonWindowsTabbedPaneUI extends WindowsTabbedPaneUI {
    @Override protected LayoutManager createLayoutManager() {
        return new CloseButtonTabbedPaneLayout();
    }
    //add 40 to the tab size to allow room for the close button and 8 to the height
    @Override protected Insets getTabInsets(int tabPlacement,int tabIndex) {
        //note that the insets that are returned to us are not copies.
        Insets defaultInsets = (Insets)super.getTabInsets(tabPlacement,tabIndex).clone();
        defaultInsets.right += 40;
        defaultInsets.top += 4;
        defaultInsets.bottom += 4;
        return defaultInsets;
    }
    class CloseButtonTabbedPaneLayout extends TabbedPaneLayout {
        //a list of our close buttons
        private final List<JButton> closeButtons = new ArrayList<>();
        @Override public void layoutContainer(Container parent) {
            super.layoutContainer(parent);
            //ensure that there are at least as many close buttons as tabs
            while(tabPane.getTabCount() > closeButtons.size()) {
                closeButtons.add(new CloseButton(closeButtons.size()));
            }
            Rectangle rect = new Rectangle();
            int i;
            for(i = 0; i < tabPane.getTabCount();i++) {
                rect = getTabBounds(i,rect);
                JButton closeButton = closeButtons.get(i);
                //shift the close button 3 down from the top of the pane and 20 to the left
                closeButton.setLocation(rect.x+rect.width-20,rect.y+5);
                closeButton.setSize(15,15);
                tabPane.add(closeButton);
            }
            for(;i < closeButtons.size();i++) {
                //remove any extra close buttons
                tabPane.remove(closeButtons.get(i));
            }
        }

        class CloseButton extends JButton implements UIResource {
            public CloseButton(int index) {
                super(new CloseButtonAction(index));
                setToolTipText("Close this tab");
                setMargin(new Insets(0,0,0,0));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        setForeground(new Color(255,0,0));
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        setForeground(new Color(0,0,0));
                    }
                });
            }
        }
        class CloseButtonAction extends AbstractAction {
            private final int index;
            public CloseButtonAction(int index) {
                super("x");
                this.index = index;
            }
            @Override public void actionPerformed(ActionEvent e) {
                tabPane.remove(index);
            }
        }
    }
}

class CloseButtonTabbedPaneUI extends BasicTabbedPaneUI {
    @Override protected LayoutManager createLayoutManager() {
        return new CloseButtonTabbedPaneLayout();
    }
    //add 40 to the tab size to allow room for the close button and 8 to the height
    @Override protected Insets getTabInsets(int tabPlacement,int tabIndex) {
        //note that the insets that are returned to us are not copies.
        Insets defaultInsets = (Insets)super.getTabInsets(tabPlacement,tabIndex).clone();
        defaultInsets.right += 40;
        defaultInsets.top += 4;
        defaultInsets.bottom += 4;
        return defaultInsets;
    }

    class CloseButtonTabbedPaneLayout extends TabbedPaneLayout {
        //a list of our close buttons
        private final List<JButton> closeButtons = new ArrayList<>();
        @Override public void layoutContainer(Container parent) {
            super.layoutContainer(parent);
            //ensure that there are at least as many close buttons as tabs
            while(tabPane.getTabCount() > closeButtons.size()) {
                closeButtons.add(new CloseButton(closeButtons.size()));
            }
            Rectangle rect = new Rectangle();
            int i;
            for(i = 0; i < tabPane.getTabCount();i++) {
                rect = getTabBounds(i,rect);
                JButton closeButton = closeButtons.get(i);
                //shift the close button 3 down from the top of the pane and 20 to the left
                closeButton.setLocation(rect.x+rect.width-20,rect.y+5);
                closeButton.setSize(15,15);
                tabPane.add(closeButton);
            }
            for(;i < closeButtons.size();i++) {
                //remove any extra close buttons
                tabPane.remove(closeButtons.get(i));
            }
        }

        class CloseButton extends JButton implements UIResource {
            public CloseButton(int index) {
                super(new CloseButtonAction(index));
                setToolTipText("Close this tab");
                setMargin(new Insets(0,0,0,0));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        setForeground(new Color(255,0,0));
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        setForeground(new Color(0,0,0));
                    }
                });
            }
        }
        class CloseButtonAction extends AbstractAction {
            private final int index;
            public CloseButtonAction(int index) {
                super("x");
                this.index = index;
            }
            @Override public void actionPerformed(ActionEvent e) {
                tabPane.remove(index);
            }
        }
    }
}
