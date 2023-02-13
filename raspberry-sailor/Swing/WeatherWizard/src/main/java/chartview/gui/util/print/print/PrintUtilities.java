package chartview.gui.util.print.print;

import chartview.ctx.WWContext;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.RepaintManager;

public class PrintUtilities
  implements Printable
{
  private Component componentToBePrinted;
  private String title;
  private Color titleColor;
  private Font titleFont;
  private int titleX, titleY;

  public static void printComponent(Component c)
  {
    printComponent(c, (String)null, (Color)null, (Font)null, 0, 0);
  }
  
  public static void printComponent(Component c, String str, Color tc, Font f, int x, int y)
  {
    new PrintUtilities(c, str, tc, f, x, y).print();
  }

  public PrintUtilities(Component componentToBePrinted,
                        String title,
                        Color titleColor,
                        Font titleFont,
                        int x,
                        int y)
  {
    this.componentToBePrinted = componentToBePrinted;
    this.title = title;
    this.titleColor = titleColor;
    this.titleFont = titleFont;
    this.titleX = x;
    this.titleY = y;
  }

  public void print()
  {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    PageFormat pf = printJob.defaultPage();
    if (this.componentToBePrinted.getSize().getWidth() > this.componentToBePrinted.getSize().getHeight())
      pf.setOrientation(PageFormat.LANDSCAPE);
    else
      pf.setOrientation(PageFormat.PORTRAIT);      
    printJob.setPrintable(this, pf);
    if (printJob.printDialog())
    {
      try
      {
        printJob.print();
      }
      catch (PrinterException pe)
      {
        WWContext.getInstance().fireLogging("Error printing: " + pe);
      }
    }
  }

  public int print(Graphics g, PageFormat pageFormat, int pageIndex)
  {
    if (pageIndex > 0)
    {
      return (NO_SUCH_PAGE);
    }
    else
    {
      Graphics2D g2d = (Graphics2D) g;
      g2d.translate(pageFormat.getImageableX(), 
                    pageFormat.getImageableY());
      disableDoubleBuffering(componentToBePrinted);
      componentToBePrinted.paint(g2d);
      if (this.title != null)
      {
        g2d.setColor(this.titleColor);
        g2d.setFont(this.titleFont);
        g2d.drawString(this.title, titleX, titleY);
      }
      enableDoubleBuffering(componentToBePrinted);
      return (PAGE_EXISTS);
    }
  }

  public static void disableDoubleBuffering(Component c)
  {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(false);
  }

  public static void enableDoubleBuffering(Component c)
  {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(true);
  }
}
