package gov.usgs.util;

import java.awt.*;
import java.awt.print.*;


/**
 * <p>Title: PrintData</p>
 *
 * <p>Description: This class sends the text for printing to the printer.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class PrintData implements Printable{


  private String textToPrint ;

  public int print(Graphics g, PageFormat pf, int pageIndex) throws
      PrinterException {
    /*if (pageIndex >= 1) {
      return Printable.NO_SUCH_PAGE;
    }*/

    Graphics2D g2 = (Graphics2D) g;
    g2.setFont(new Font("Helvetica", Font.PLAIN, 24));
    g2.translate(pf.getImageableX(), pf.getImageableY());

    g2.setColor(Color.gray);
    g2.draw(new Rectangle( (int) pf.getImageableWidth(),
                          (int) pf.getImageableHeight()));

    Paint defaultPaint = new GradientPaint(100f, 100f, Color.blue,
                                           (float) g2.getFontMetrics().
                                           getStringBounds(textToPrint, g2).
                                           getWidth(), 100f, Color.red);

    g2.setClip(100, 175, 401, 51);
    g2.draw(new Rectangle(100, 175, 400, 30));

    g2.setPaint(defaultPaint);
    g2.drawString(textToPrint, 100, 200);

    defaultPaint = new GradientPaint(100f, 100f, Color.green,
                                     (float) g2.getFontMetrics().
                                     getStringBounds(textToPrint, g2).
                                     getWidth(), 100f, Color.yellow);

    g2.translate(0, 0);
    g2.setClip(100, 275, 401, 51);

    g2.setColor(Color.gray);
    g2.draw(new Rectangle(100, 275, 400, 30));

    g2.setPaint(defaultPaint);
    g2.drawString(textToPrint, 100, 300);

    return Printable.PAGE_EXISTS;
  }

  public void print(String textToPrint){
    this.textToPrint = textToPrint;
    // Get a PrintJob
    PrinterJob pj = PrinterJob.getPrinterJob();
    Book book = new Book();
    Printable painter;

    PageFormat pageFormat = pj.pageDialog(pj.defaultPage());
    book.append(this, pageFormat);
    pj.setPageable(book);

    // Show the print dialog
    if (pj.printDialog()) {
      try {
        pj.print();
      }
      catch (PrinterException pe) {
        System.out.println("Exception while printing.\n");
        pe.printStackTrace();
      }
    }
  }

}
