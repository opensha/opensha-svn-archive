package org.scec.sha.util;

import java.util.ListIterator;

import org.scec.sha.imr.attenRelImpl.*;
import org.scec.data.Site;
import org.scec.param.*;
import org.scec.sha.imr.AttenuationRelationship;



/**
 * <p>Title: SiteTranslator</p>
 * <p>Description: This object sets the value of a site parameter according to the
 * Vs30 and (optionally) Basin-Depth-2.5 passed in.  The conversions are as follows (NA means
 * nothing was set):<p>
 *
 *
 * AS_1997_AttenRel.SITE_TYPE_NAME (Abrahamson & Silva (1997) & Abrahamson (2000))<p>
 * <UL>
 * <LI> NA 				if Vs30²180
 * <LI> Deep-Soil			if Vs30 ² 400 m/s & Basin-Depth-2.5 ³ 100 m
 * <LI> Rock/Shallow-Soil		otherwise
 * </UL>
 *
 * SCEMY_1997_AttenRel.SITE_TYPE_NAME (Sadigh et al. (1997)):<p>
 * <UL>
 * <LI> NA 				if Vs30²180
 * <LI> Deep-Soil			if Vs30 ² 400 m/s & Basin-Depth-2.5 ³ 100 m
 * <LI> Rock				otherwise
 * </UL>
 *
 * AttenuationRelationship.VS30_NAME (Boore et al. (1997) & Field (2000))<p>
 * <LI> <UL>
 * <LI> Vs30 = Vs30			(if Vs30 > 180; NA otherwise)
 * <LI> </UL>
 *
 * Campbell_1997_AttenRel.SITE_TYPE_NAME (Campbell (1997))<p>
 * <UL>
 * <LI> NA 				if Vs30²180
 * <LI> Firm-Soil			if 180<Vs30²400
 * <LI> Soft-Rock			if 400<Vs30²500
 * <LI> Hard-Rock			if 500>Vs30
 * </UL>
 *
 * Campbell_1997_AttenRel.BASIN_DEPTH_NAME (Campbell (1997))<p>
 * <UL>
 * <LI> Campbell-Basin-Depth = 0                    	if Vs30 ³ 400
 * <LI> Campbell-Basin-Depth = Basin-Depth-2.5      	if Vs30 < 400
 * </UL>
 *
 * Field_2000_AttenRel.BASIN_DEPTH_NAME (Field (2000))<p>
 * <UL>
 * <LI> Vs30 = Vs30			(if Vs30 > 180; NA otherwise)
 * <LI> Basin-Depth-2.5 = Basin-Depth-2.5
 * </UL>
 *
 * CB_2003_AttenRel.SITE_TYPE_NAME (Campbell & Bozorgnia (2003))<p>
 * <UL>
 * <LI> NA 			if Vs30²180
 * <LI> Firm-Soil		if 180<Vs30²300
 * <LI> Very-Firm-Soil	        if 300<Vs30²400
 * <LI> Soft-Rock		if 400<Vs30²500
 * <LI> Firm-Rock		if 500>Vs30
 * </UL>
 *
 * ShakeMap_2003_AttenRel.WILLS_SITE_NAME (ShakeMap (2003))<p>
 * <LI> <UL>
 * <LI> E                      if Vs30 = 163
 * <LI> DE                     if Vs30 = 298
 * <LI> D                      if Vs30 = 301
 * <LI> CD                     if Vs30 = 372
 * <LI> C                      if Vs30 = 464
 * <LI> BC                     if Vs30 = 724
 * <LI> B                      if Vs30 = 686
 * <LI> NA                     if any other value of Vs30
 * </UL>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field & Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class SiteTranslatorNew implements java.io.Serializable{

  private final static String C = "SiteTranslator";
  private final static boolean D = false;

  public SiteTranslatorNew() {
  }

  /**
   * @param parameter: the parameter object to be set
   * @param vs30 - 30-meter shear-wave velocity in m/sec
   * @param basinDepth - Depth (in meters) to where Vs = 2.5-km/sec
   *
   * @returns a boolean to tell if setting the value was successful (if false
   * it means the parameter value was not changed).
   */
  public boolean setParameterValue(Parameter param, double vs30,double basinDepth ){

      // AS_1997_AttenRel.SITE_TYPE_NAME
      // (e.g., used by Abrahamson & Silva (1997) & Abrahamson (2000))
      if(param.getName().equals(AS_1997_AttenRel.SITE_TYPE_NAME)){
        if(Double.isNaN(basinDepth)){
          if(vs30 > 180 && vs30 <=400) {
            param.setValue(AS_1997_AttenRel.SITE_TYPE_SOIL);
            return true;
          }
          else if(vs30 > 400) {
            param.setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
            return true;
          }
          else {
            return false;
          }
        }
        else {
          if(vs30 > 180 && vs30 <= 400 && basinDepth > 100) {
            param.setValue(AS_1997_AttenRel.SITE_TYPE_SOIL);
            return true;
          }
          else if (vs30 > 400) {
            param.setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
            return true;
          }
          else {
            return false;
          }
        }
      }

      // AttenuationRelationship.VS30_NAME
      // (e.g., used by BJF-1997 and Field-2000) site type
      else if(param.getName().equals(AttenuationRelationship.VS30_NAME)){
        if (vs30 > 180 && vs30 < 4000) {
          param.setValue(new Double(vs30));
          return true;
        }
        else {
          return false;
        }
      }

      // Campbell_1997_AttenRel.BASIN_DEPTH_NAME
      else if(param.getName().equalsIgnoreCase(Campbell_1997_AttenRel.BASIN_DEPTH_NAME)){
        if(vs30>=400) {
          param.setValue(new Double(0));
          return true;
        }
        else {
          if(Double.isNaN(basinDepth)) param.setValue(null);
          else  param.setValue(new Double(basinDepth/1000));  // converted to km
          return true;
        }
      }

      // Campbell_1997_AttenRel.SITE_TYPE_NAME
      else if(param.getName().equals(Campbell_1997_AttenRel.SITE_TYPE_NAME)){
        if(vs30>180 && vs30<=400) {
          param.setValue(Campbell_1997_AttenRel.SITE_TYPE_FIRM_SOIL);
          return true;
        }
        else if(vs30>400 && vs30<=500) {
          param.setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
          return true;
        }
        else if(vs30>500) {
          param.setValue(Campbell_1997_AttenRel.SITE_TYPE_HARD_ROCK);
          return true;
        }
        else {
          return false;
        }
      }

      // CB_2003_AttenRel.SITE_TYPE_NAME
      else if(param.getName().equals(CB_2003_AttenRel.SITE_TYPE_NAME)){
        if(vs30>180 && vs30<=300) {
          param.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_SOIL);
          return true;
        }
        else if(vs30>300 && vs30<=400) {
          param.setValue(CB_2003_AttenRel.SITE_TYPE_VERY_FIRM_SOIL);
          return true;
        }
        else if(vs30 >400 && vs30 <=500) {
          param.setValue(CB_2003_AttenRel.SITE_TYPE_SOFT_ROCK);
          return true;
        }
        else if(vs30 >500) {
          param.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_ROCK);
         return true;
        }
        else {
          return false;
        }
      }


      // SCEMY_1997_AttenRel.SITE_TYPE_NAME
      else if(param.getName().equals(SCEMY_1997_AttenRel.SITE_TYPE_NAME)){
        if(Double.isNaN(basinDepth)){
          if(vs30 > 180 && vs30 <=400) {
            param.setValue(SCEMY_1997_AttenRel.SITE_TYPE_SOIL);
            return true;
          }
          else if(vs30 > 400) {
            param.setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
            return true;
          }
          else {
            return false;
          }
        }
        else {
          if(vs30 > 180 && vs30 <= 400 && basinDepth > 100) {
            param.setValue(SCEMY_1997_AttenRel.SITE_TYPE_SOIL);
            return true;
          }
          else if (vs30 > 400) {
            param.setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
            return true;
          }
          else {
            return false;
          }
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
        if      (vs30 == 163)  {
          param.setValue(ShakeMap_2003_AttenRel.WILLS_SITE_E);
          return true;
        }
        else if (vs30 == 298)  {
          param.setValue(ShakeMap_2003_AttenRel.WILLS_SITE_DE);
          return true;
        }
        else if (vs30 == 301)  {
          param.setValue(ShakeMap_2003_AttenRel.WILLS_SITE_D);
          return true;
        }
        else if (vs30 == 372)  {
          param.setValue(ShakeMap_2003_AttenRel.WILLS_SITE_CD);
          return true;
        }
        else if (vs30 == 464)  {
          param.setValue(ShakeMap_2003_AttenRel.WILLS_SITE_C);
          return true;
        }
        else if (vs30 == 724)  {
          param.setValue(ShakeMap_2003_AttenRel.WILLS_SITE_BC);
          return true;
        }
        else if (vs30 == 686)  {
          param.setValue(ShakeMap_2003_AttenRel.WILLS_SITE_B);
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