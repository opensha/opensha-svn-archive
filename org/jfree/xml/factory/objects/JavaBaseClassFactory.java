/* ===================================================
 * JCommon : a free general purpose Java class library
 * ===================================================
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Simba Management Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * ----------------
 * JavaBaseClassFactory.java
 * ----------------
 * (C)opyright 2002, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner (taquera@sherito.org);
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 14-Apr-2003 : Initial version
 * 29-Apr-2003 : Destilled from the JFreeReport project and moved into JCommon
 */
package org.jfree.xml.factory.objects;

import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.List;
import java.text.Format;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;

import org.jfree.ui.FloatDimension;

/**
 * A default factory for all commonly used java base classes from java.lang, java.awt
 * etc.
 *
 * @author Thomas Morgner
 */
public class JavaBaseClassFactory extends ClassFactoryImpl {

    /**
     * DefaultConstructor. Creates the object factory for all java base classes.
     */
    public JavaBaseClassFactory() {
        registerClass(Dimension2D.class, new BeanObjectDescription(FloatDimension.class));
        registerClass(Date.class, new DateObjectDescription());
        registerClass(Boolean.TYPE, new BooleanObjectDescription());
        registerClass(Byte.TYPE, new ByteObjectDescription());
        registerClass(Double.TYPE, new DoubleObjectDescription());
        registerClass(Float.TYPE, new FloatObjectDescription());
        registerClass(Integer.TYPE, new IntegerObjectDescription());
        registerClass(Long.TYPE, new LongObjectDescription());
        registerClass(Short.TYPE, new ShortObjectDescription());
        registerClass(Character.TYPE, new CharacterObjectDescription());
        registerClass(Character.class, new CharacterObjectDescription());
        registerClass(Boolean.class, new BooleanObjectDescription());
        registerClass(Byte.class, new ByteObjectDescription());
        registerClass(Double.class, new DoubleObjectDescription());
        registerClass(Float.class, new FloatObjectDescription());
        registerClass(Integer.class, new IntegerObjectDescription());
        registerClass(Long.class, new LongObjectDescription());
        registerClass(Short.class, new ShortObjectDescription());
        registerClass(Line2D.class, new Line2DObjectDescription());
        registerClass(Point2D.class, new Point2DObjectDescription());
        registerClass(Rectangle2D.class, new Rectangle2DObjectDescription());
        registerClass(String.class, new StringObjectDescription());
        registerClass(Color.class, new ColorObjectDescription());
        registerClass(BasicStroke.class, new BasicStrokeObjectDescription());
        registerClass(Object.class, new ClassLoaderObjectDescription());

        registerClass(Format.class, new ClassLoaderObjectDescription());
        registerClass(NumberFormat.class, new BeanObjectDescription(NumberFormat.class));
        registerClass(DecimalFormat.class, new DecimalFormatObjectDescription());
        registerClass(DecimalFormatSymbols.class,
            new BeanObjectDescription(DecimalFormatSymbols.class));
        registerClass(DateFormat.class, new ClassLoaderObjectDescription());
        registerClass(SimpleDateFormat.class,
            new BeanObjectDescription(DecimalFormatSymbols.class));
        registerClass(DateFormatSymbols.class, new ClassLoaderObjectDescription());

        registerClass(ArrayList.class, new CollectionObjectDescription(ArrayList.class));
        registerClass(Vector.class, new CollectionObjectDescription(Vector.class));
        registerClass(HashSet.class, new CollectionObjectDescription(HashSet.class));
        registerClass(TreeSet.class, new CollectionObjectDescription(TreeSet.class));
        registerClass(Set.class, new CollectionObjectDescription(HashSet.class));
        registerClass(List.class, new CollectionObjectDescription(ArrayList.class));
        registerClass(Collection.class, new CollectionObjectDescription(ArrayList.class));

    }
}
