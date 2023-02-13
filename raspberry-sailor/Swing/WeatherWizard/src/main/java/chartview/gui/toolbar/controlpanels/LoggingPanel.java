package chartview.gui.toolbar.controlpanels;

import chartview.ctx.ApplicationEventListener;

import chartview.ctx.WWContext;

import java.awt.BorderLayout;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.Font;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class LoggingPanel
  extends JPanel
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTextPane loggingTextZone = new JTextPane();

  private LoggingPanelPopup rootPopup = new LoggingPanelPopup(loggingTextZone);
  
  public final static int GREEN_STYLE  = 0;
  public final static int RED_STYLE    = 1;
  public final static int YELLOW_STYLE = 2;
  public final static int WHITE_STYLE  = 3;
  
  private final static String[] STYLES = new String[] {"GreenStyle",
                                                       "RedStyle",
                                                       "YellowStyle",
                                                       "WhiteStyle"};

  private StyledDocument doc = (StyledDocument)loggingTextZone.getDocument();
  
  public LoggingPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout1);

    this.setPreferredSize(new Dimension(ControlPane.WIDTH, 200));
    this.setMinimumSize(new Dimension(ControlPane.WIDTH, 200));
    this.setSize(new Dimension(ControlPane.WIDTH, 200));

    loggingTextZone.setFont(new Font("Dialog", 0, 9));
    loggingTextZone.setCaretColor(Color.lightGray);
    loggingTextZone.setBackground(Color.black);
    loggingTextZone.setForeground(Color.green);
    loggingTextZone.setSelectedTextColor(Color.white);
    jScrollPane1.getViewport().add(loggingTextZone, null);
    jScrollPane1.setAutoscrolls(true);
    this.add(jScrollPane1, BorderLayout.CENTER);

    Style style = doc.addStyle(STYLES[GREEN_STYLE], null);
    StyleConstants.setForeground(style, Color.green);
    style = doc.addStyle(STYLES[RED_STYLE], null);
    StyleConstants.setForeground(style, Color.red);
    style = doc.addStyle(STYLES[YELLOW_STYLE], null);
    StyleConstants.setForeground(style, Color.yellow);
    style = doc.addStyle(STYLES[WHITE_STYLE], null);
    StyleConstants.setForeground(style, Color.white);
    
    try
    {
      doc.insertString(doc.getLength(), "Logging output\n", doc.getStyle(STYLES[GREEN_STYLE]));
    }
    catch (BadLocationException e)
    {
      System.err.println("Logging with Style");
      e.printStackTrace();
      System.err.println("------------------");
    }

    WWContext.getInstance().addApplicationListener(new ApplicationEventListener()
      {
        public String toString()
        {
          return "from LoggingPanel.";
        }
        public void log(String str) 
        {
          String content = loggingTextZone.getText();
//        System.out.println("Content length is " + content.length());
          // Limit amount of text to 3000 characters
          if (content.length() > 3000)
          {
//          System.out.print("Cutting text zone from " + content.length() + "...");
            content = content.substring(content.length() - 3000);
//          System.out.println("to " + content.length());
            loggingTextZone.setText(content);
          }
          
          loggingTextZone.setText(content + "\n" + str);
//        System.out.println("TextArea size:" + loggingTextArea.getBounds().height);
          int w = loggingTextZone.getVisibleRect().width;
          int h = loggingTextZone.getVisibleRect().height;
          int y = loggingTextZone.getBounds().height - h;
          if (y < 0) y = 0;
//        System.out.println("y:" + y + ", height:" + loggingTextArea.getBounds().height + ", h:" + h);
          jScrollPane1.getViewport().scrollRectToVisible(new Rectangle(0, y, w, h));
        }
        
        public void log(String str, int styleIndex)
        {
          String content = loggingTextZone.getText();
//        System.out.println("Content length is " + content.length());
          // Limit amount of text to 3000 characters
          if (content != null && content.length() > 3000)
          {
//          System.out.print("Cutting text zone from " + content.length() + "...");
            content = content.substring(content.length() - 3000);
//          System.out.println("to " + content.length());
            try { doc.insertString(0, content, doc.getStyle(STYLES[styleIndex])); } catch (BadLocationException ble) {}
//          loggingTextZone.setText(content);
          }
          
//        System.out.println("Logging [" + str + "] in " + STYLES[styleIndex]);
          Style style = doc.getStyle(STYLES[styleIndex]);
          try
          {
            doc.insertString(doc.getLength(), str, style);
          }
          catch (BadLocationException e)
          {
            System.err.println("Logging with Style");
            e.printStackTrace();
            System.err.println("------------------");
          }
          try
          {
  //        System.out.println("TextArea size:" + loggingTextArea.getBounds().height);
            int w = loggingTextZone.getVisibleRect().width;
            int h = loggingTextZone.getVisibleRect().height;
            int y = loggingTextZone.getBounds().height - h;
            if (y < 0) y = 0;
  //        System.out.println("y:" + y + ", height:" + loggingTextArea.getBounds().height + ", h:" + h);
            try { jScrollPane1.getViewport().scrollRectToVisible(new Rectangle(0, y, w, h)); } catch (Exception ignore) {}
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      });    

    loggingTextZone.addMouseListener(new MouseListener()
        {
          public void mouseClicked(MouseEvent e)
          {
          }

          public void mousePressed(MouseEvent e)
          {
            tryPopup(e);
          }

          public void mouseReleased(MouseEvent e)
          {
            if (e.getClickCount() == 2)
            {
              dblClicked(e);
            }
            else
            {
              tryPopup(e);
            }
          }

          public void mouseEntered(MouseEvent e)
          {
          }

          public void mouseExited(MouseEvent e)
          {
          }

          private void dblClicked(MouseEvent e)
          {
            if (e.isConsumed())
            {
              return;
            }
            // Let's make sure we only invoke double click action when
            // we have a treepath. For example; This avoids opening an editor on a
            // selected node when the user double clicks on the expand/collapse icon.
            if (e.getClickCount() == 2)
            {
            }
            else if (e.getClickCount() > 2)
            {
              // Fix triple-click wanna-be drag events...
              e.consume();
            }
          }

          private void tryPopup(MouseEvent e)
          {
            if (e.isPopupTrigger())
            {
              rootPopup.show(loggingTextZone, e.getX(), e.getY());
            }
          }
        });
  }
}
