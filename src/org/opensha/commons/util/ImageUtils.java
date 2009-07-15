package org.opensha.commons.util;

import java.awt.Image;
import java.awt.Toolkit;
/**
 * <p>Title: ImageUtils.java </p>
 * <p>Description: This class will be used to load the image from the jar file.
 *   The image will be used to be shown in frames.  </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */

public class ImageUtils {

  /**
   * this is the path where images will be put into
   */
  private static final String imagePath = "/resources/images/";


  /**
   * this method accepts the filename and loads the image from the jar file
   * @param fileName
   * @return
   */
  public static Image loadImage(String fileName) {
    String imageFileName = imagePath+fileName;
    java.net.URL url = ImageUtils.class.getResource(imageFileName);
    Image img=Toolkit.getDefaultToolkit().getImage(url);
    return img;
  }


}

