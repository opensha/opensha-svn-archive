/* ==================================================
 * JCommon : a general purpose class library for Java
 * ==================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
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
 * ------------------------
 * DefaultMeterDataset.java
 * ------------------------
 * (C) Copyright 2002, by Hari and Contributors.
 *
 * Original Author:  Hari;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *
 * $Id$
 *
 * Changes
 * -------
 * 02-Apr-2002 : Version 1, based on code contributed by Hari (DG);
 * 16-Apr-2002 : Updated to the latest version from Hari (DG);
 *
 */

package com.jrefinery.data;

/**
 * A default implementation of the MeterDataset interface.
 */
public class DefaultMeterDataset extends AbstractDataset implements MeterDataset {

    static final double DEFAULT_ADJ = 1.0;

    Number minCritical;
    Number maxCritical;
    Number minWarning;
    Number maxWarning;
    Number minNormal;
    Number maxNormal;
    Number min;
    Number max;
    Number value;
    int borderType;
    String units;

    public DefaultMeterDataset() {
        this(new Double(0), new Double(0), null, null);
    }

    public DefaultMeterDataset(Number min, Number max, Number value, String units) {
        this(min, max, value, units, null, null, null, null, null, null, NORMAL_DATA);
    }

    public DefaultMeterDataset(Number min, Number max, Number value, String units,
                               Number minCritical, Number maxCritical,
                               Number minWarning, Number maxWarning,
                               Number minNormal, Number maxNormal,
                               int borderType) {

        setRange(min, max);
        setValue(value);
                setUnits( units);
        setCriticalRange(minCritical, maxCritical);
        setWarningRange(minWarning, maxWarning);
        setNormalRange(minNormal, maxNormal);
        setBorderType(borderType);

    }

    public Number getMinimumCriticalValue() { return minCritical; }

    public Number getMaximumCriticalValue() { return maxCritical; }

    public Number getMinimumWarningValue() { return minWarning; }

    public Number getMaximumWarningValue() { return maxWarning; }

    public Number getMinimumNormalValue() { return minNormal; }

    public Number getMaximumNormalValue() { return maxNormal; }

    public Number getMinimumValue() { return min; }

    public Number getMaximumValue() { return max; }

    public Number getValue() { return value; }

    public String getUnits() { return units; }

    public boolean isValueValid() {
        return (value!=null);
    }

    public int getBorderType() { return borderType; }

        public void setUnits( String units) {
                this.units = units;
        this.fireDatasetChanged();
        }

    public void setBorderType( int borderType) {
        this.borderType = borderType;
        this.fireDatasetChanged();
    }

    /**
     * Sets the value for the dataset.
     *
     * @param value The new value.
     */
    public void setValue(Number value) {

                if( value != null && min != null && max != null) {
                        if( value.doubleValue() < min.doubleValue() || value.doubleValue() > max.doubleValue()) {
                                throw new IllegalArgumentException( "Value is out of range for min/max");
                }
            }
        this.value = value;
        if( value != null && min != null && max != null) {
                if( min.doubleValue() == max.doubleValue()) {
                        min = new Double( value.doubleValue() - DEFAULT_ADJ);
                    max = new Double( value.doubleValue() + DEFAULT_ADJ);
                }
            }
        this.fireDatasetChanged();

    }

    /**
     * Sets the range for the dataset.  Registered listeners are notified of the change.
     *
     * @param min The new minimum.
     * @param max The new maximum.
     */
    public void setRange(Number min, Number max) {

        if (min==null || max==null) {
            throw new IllegalArgumentException( "Min/Max should not be null");
        }

        if (min.doubleValue()>max.doubleValue()) {
            Number temp = min;
            min = max;
            max = temp;
        }
        if( value != null) {
                if( min.doubleValue() == max.doubleValue()) {
                min = new Double( value.doubleValue() - DEFAULT_ADJ);
                    max = new Double( value.doubleValue() + DEFAULT_ADJ);
                }
            }
        this.min = min;
        this.max = max;
        this.fireDatasetChanged();

    }

    /**
     * Sets the normal range for the dataset.  Registered listeners are notified of the change.
     *
     * @param min The new minimum.
     * @param max The new maximum.
     */
    public void setNormalRange(Number minNormal, Number maxNormal) {
        this.minNormal = minNormal;
        this.maxNormal = maxNormal;
        if( this.minNormal != null && this.minNormal.doubleValue() < this.min.doubleValue()) {
                this.min = this.minNormal;
        }
        if( this.maxNormal != null && this.maxNormal.doubleValue() > this.max.doubleValue()) {
                this.max = this.maxNormal;
        }
        this.fireDatasetChanged();
    }

    /**
     * Sets the warning range for the dataset.  Registered listeners are notified of the change.
     *
     * @param min The new minimum.
     * @param max The new maximum.
     */
    public void setWarningRange( Number minWarning, Number maxWarning) {

        this.minWarning = minWarning;
        this.maxWarning = maxWarning;
        if( this.minWarning != null && this.minWarning.doubleValue() < this.min.doubleValue()) {
                this.min = this.minWarning;
        }
        if( this.maxWarning != null && this.maxWarning.doubleValue() > this.max.doubleValue()) {
                this.max = this.maxWarning;
        }
        this.fireDatasetChanged();

    }

    /**
     * Sets the critical range for the dataset.  Registered listeners are notified of the change.
     *
     * @param min The new minimum.
     * @param max The new maximum.
     */
    public void setCriticalRange( Number minCritical, Number maxCritical) {

        this.minCritical = minCritical;
        this.maxCritical = maxCritical;
        if( this.minCritical != null && this.minCritical.doubleValue() < this.min.doubleValue()) {
                this.min = this.minCritical;
        }
        if( this.maxCritical != null && this.maxCritical.doubleValue() > this.max.doubleValue()) {
                this.max = this.maxCritical;
        }
        this.fireDatasetChanged();

    }

}
