package org.scec.sha.util;

import java.util.ListIterator;

import org.scec.sha.imr.attenRelImpl.*;
import org.scec.data.Site;
import org.scec.param.*;
import org.scec.sha.imr.AttenuationRelationship;



/**
 * <p>Title: SiteTranslator</p>
 * <p>Description: This object sets the value of a site parameter from the
 * Wills Site Type (Wills et al., 2000, BSSA, v. 90, S187-S208) and (optionally) from
 * Basin-Depth-2.5 (the depth in m where the shear-wave velocity equals 2.5 km/sec).
 * The conversions from the Wills Site Types (E, DE, D, CD, C, BC, B) and basin-depth
 * are as follows (NA means nothing is set):<p>
 *
 *
 * AS_1997_AttenRel.SITE_TYPE_NAME (Abrahamson & Silva (1997) & Abrahamson (2000))<p>
 * <UL>
 * <LI> NA 				if E
 * <LI> Deep-Soil			if DE, D, or CD
 * <LI> Rock/Shallow-Soil		if C, BC, or B
 * </UL>
 *
 * SCEMY_1997_AttenRel.SITE_TYPE_NAME (Sadigh et al. (1997)):<p>
 * <UL>
 * <LI> NA 				if E
 * <LI> Deep-Soil			if DE, D, or CD
 * <LI> Rock		                if C, BC, or B
 * </UL>
 *
 * AttenuationRelationship.VS30_NAME (Boore et al. (1997) & Field (2000))<p>
 * <LI> <UL>
 * <LI> Vs30 = NA			if E
 * <LI> Vs30 = 180			if DE
 * <LI> Vs30 = 270			if D
 * <LI> Vs30 = 360			if CD
 * <LI> Vs30 = 560			if C
 * <LI> Vs30 = 760			if BC
 * <LI> Vs30 = 1000			if B
 * <LI> </UL>
 *
 * Campbell_1997_AttenRel.SITE_TYPE_NAME (Campbell (1997))<p>
 * <UL>
 * <LI> NA 				if E
 * <LI> Firm-Soil			if DE, D, or CD
 * <LI> Soft-Rock			if C
 * <LI> Hard-Rock			if BC or B
 * </UL>
 *
 * Campbell_1997_AttenRel.BASIN_DEPTH_NAME (Campbell (1997))<p>
 * <UL>
 * <LI> Campbell-Basin-Depth = NaN      if E
 * <LI> Campbell-Basin-Depth = 0.0      if B ot BC
 * <LI> Campbell-Basin-Depth = 1.0      if C
 * <LI> Campbell-Basin-Depth = 5.0      if CD, D, or DE
 * </UL>
 *
 * Field_2000_AttenRel.BASIN_DEPTH_NAME (Field (2000))<p>
 * <UL>
 * <LI> Basin-Depth-2.5 = Basin-Depth-2.5
 * </UL>
 *
 * CB_2003_AttenRel.SITE_TYPE_NAME (Campbell & Bozorgnia (2003))<p>
 * <UL>
 * <LI> NA 			if E
 * <LI> Firm-Soil		if DE or D
 * <LI> Very-Firm-Soil	        if CD
 * <LI> Soft-Rock		if C
 * <LI> BC-Bounday              if BC
 * <LI> Firm-Rock		if BÊ
 * </UL>
 *
 * ShakeMap_2003_AttenRel.WILLS_SITE_NAME (ShakeMap (2003))<p>
 * <LI> <UL>
 * <LI> E                      if E
 * <LI> DE                     if DE
 * <LI> D                      if D
 * <LI> CD                     if CD
 * <LI> C                      if C
 * <LI> BC                     if BC
 * <LI> B                      if B
 * </UL>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field & Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class SiteTranslator implements java.io.Serializable{

  private final static String C = "SiteTranslator";
  private final static boolean D = false;

  public final static String WILLS_B = "B";
  public final static String WILLS_BC = "BC";
  public final static String WILLS_C = "C";
  public final static String WILLS_CD = "CD";
  public final static String WILLS_D = "D";
  public final static String WILLS_DE = "DE";
  public final static String WILLS_E = "E";


  public SiteTranslator() {
  }

  /**
   * @param parameter: the parameter object to be set
   * @param willsClass - a String with one of the folowing ("E", "DE", "D", "CD", "C", "BC", or "B")
   * @param basinDepth - Depth (in meters) to where Vs = 2.5-km/sec
   *
   * @returns a boolean to tell if setting the value was successful (if false
   * it means the parameter value was not changed).  A basinDepth value of NaN is allowed
   * (it will not cause the returned value to be false).
   */
  public boolean setParameterValue(ParameterAPI param, String willsClass,double basinDepth ){

      // shorten name for convenience
      String wc = willsClass;

      // AS_1997_AttenRel.SITE_TYPE_NAME
      // (e.g., used by Abrahamson & Silva (1997) & Abrahamson (2000))
      if(param.getName().equals(AS_1997_AttenRel.SITE_TYPE_NAME)){

          if(wc.equals(WILLS_DE) || wc.equals(WILLS_D) || wc.equals(WILLS_CD)) {
            param.setValue(AS_1997_AttenRel.SITE_TYPE_SOIL);
            return true;
          }
          else if(wc.equals(WILLS_C) || wc.equals(WILLS_BC) || wc.equals(WILLS_B)) {
            param.setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
            return true;
          }
          else {
            return false;
          }
      }

      // SCEMY_1997_AttenRel.SITE_TYPE_NAME
      else if(param.getName().equals(SCEMY_1997_AttenRel.SITE_TYPE_NAME)){

        if(wc.equals(WILLS_DE) || wc.equals(WILLS_D) || wc.equals(WILLS_CD)) {
          param.setValue(SCEMY_1997_AttenRel.SITE_TYPE_SOIL);
          return true;
        }
        else if(wc.equals(WILLS_C) || wc.equals(WILLS_BC) || wc.equals(WILLS_B)) {
          param.setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
          return true;
        }
        else {
          return false;
        }
      }

      // AttenuationRelationship.VS30_NAME
      // (e.g., used by BJF-1997 and Field-2000) site type
      else if(param.getName().equals(AttenuationRelationship.VS30_NAME)){
        if (wc.equals(WILLS_DE)) {
          param.setValue(new Double(180));
          return true;
        }
        else if (wc.equals(WILLS_D)) {
          param.setValue(new Double(270));
          return true;
        }
        else if (wc.equals(WILLS_CD)) {
          param.setValue(new Double(360));
          return true;
        }
        else if (wc.equals(WILLS_C)) {
          param.setValue(new Double(560));
          return true;
        }
        else if (wc.equals(WILLS_BC)) {
          param.setValue(new Double(760));
          return true;
        }
        else if (wc.equals(WILLS_B)) {
          param.setValue(new Double(1000));
          return true;
        }
        else {
          return false;
        }
      }

      // Campbell_1997_AttenRel.BASIN_DEPTH_NAME
      // (these are as Ken Campbell requested)
      else if(param.getName().equals(Campbell_1997_AttenRel.BASIN_DEPTH_NAME)){
        if(wc.equals(WILLS_DE) || wc.equals(WILLS_D) || wc.equals(WILLS_CD)) {
          param.setValue(new Double(5.0));
          return true;
        }
        else if(wc.equals(WILLS_C)) {
          param.setValue(new Double(1.0));
          return true;
        }
          else if(wc.equals(WILLS_BC) || wc.equals(WILLS_B)) {
            param.setValue(new Double(0.0));
            return true;
        }
        else {
          return false;
        }
      }

      // Campbell_1997_AttenRel.SITE_TYPE_NAME
      else if(param.getName().equals(Campbell_1997_AttenRel.SITE_TYPE_NAME)){

        if(wc.equals(WILLS_DE) || wc.equals(WILLS_D) || wc.equals(WILLS_CD)) {
          param.setValue(Campbell_1997_AttenRel.SITE_TYPE_FIRM_SOIL);
          return true;
        }
        else if(wc.equals(WILLS_C)) {
          param.setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
          return true;
        }
        else if(wc.equals(WILLS_BC) || wc.equals(WILLS_B)) {
          param.setValue(Campbell_1997_AttenRel.SITE_TYPE_HARD_ROCK);
          return true;
        }
        else {
          return false;
        }
      }

      // CB_2003_AttenRel.SITE_TYPE_NAME
      else if(param.getName().equals(CB_2003_AttenRel.SITE_TYPE_NAME)){
        if(wc.equals(WILLS_DE) || wc.equals(WILLS_D)) {
          param.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_SOIL);
          return true;
        }
        else if(wc.equals(WILLS_CD)) {
          param.setValue(CB_2003_AttenRel.SITE_TYPE_VERY_FIRM_SOIL);
          return true;
        }
        else if(wc.equals(WILLS_C)) {
          param.setValue(CB_2003_AttenRel.SITE_TYPE_SOFT_ROCK);
          return true;
        }
        else if(wc.equals(WILLS_BC)) {
          param.setValue(CB_2003_AttenRel.SITE_TYPE_NEHRP_BC);
          return true;
        }
        else if (wc.equals(WILLS_B)) {
          param.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_ROCK);
         return true;
        }
        else {
          return false;
        }
      }



      // Field_2000_AttenRel.BASIN_DEPTH_NAME
      else if(param.getName().equals(Field_2000_AttenRel.BASIN_DEPTH_NAME)){
        // set basin depth in kms
          if(Double.isNaN(basinDepth)) param.setValue(null);
          else  param.setValue(new Double(basinDepth/1000));
          return true;
      }

      // ShakeMap_2003_AttenRel.WILLS_SITE_NAME
      else if(param.getName().equals(ShakeMap_2003_AttenRel.WILLS_SITE_NAME)){

          if(param.isAllowed(wc)) {
            param.setValue(wc);
            return true;
          }
          else {
            return false;
          }
      }

      // site type not found
      else {
        throw new RuntimeException(C+" does not support the site type: "+param.getName());
      }
  }
}