package org.scec.sha.util;

import java.util.ListIterator;

import org.scec.sha.imr.attenRelImpl.*;
import org.scec.data.Site;
import org.scec.param.*;
import org.scec.sha.imr.AttenuationRelationship;



/**
 * <p>Title: SiteTranslator</p>
 * <p>Description: Translation is performed on the following scale:
 * Converting from Vs30 & Basin-Depth-2.5 to the above:

 Abrahamson & Silva (1997) & Abrahamson (2000):

 NA 				if Vs30�180
 Deep-Soil			if Vs30 � 400 m/s & Basin-Depth-2.5 � 100 m
 Rock/Shallow-Soil		otherwise

 Sadigh et al. (1997):

 NA 				if Vs30�180
 Deep-Soil			if Vs30 � 400 m/s & Basin-Depth-2.5 � 100 m
 Rock				otherwise

 Boore et al. (1997)

 Vs30 = Vs30			(if Vs30 > 180; NA otherwise)

 Campbell (1997)

 NA 				if Vs30�180
 Firm-Soil			if 180<Vs30�400
 Soft-Rock			if 400<Vs30�500
 Hard-Rock			if 500>Vs30

 Campbell-Basin-Depth = 0                    		if Vs30 � 400
 Campbell-Basin-Depth = Basin-Depth-2.5      	if Vs30 < 400

 Field (2000)

 Vs30 = Vs30			(if Vs30 > 180; NA otherwise)
 Basin-Depth-2.5 = Basin-Depth-2.5

 Campbell & Bozorgnia (2003)

 NA 			if Vs30�180
 Firm-Soil		if 180<Vs30�300
 Very-Firm-Soil	if 300<Vs30�400
 Soft-Rock		if 400<Vs30�500
 Firm-Rock		if 500>Vs30

 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field & Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class SiteTranslator implements java.io.Serializable{


  public SiteTranslator() {
  }

  public void setSiteParams(Site s, double vs30,double basinDepth ){
    if(vs30 <180)
      throw new RuntimeException("Site Params not applicable for this region");
    ListIterator  it = s.getParametersIterator();
    while(it.hasNext()){
      ParameterAPI tempParam = (ParameterAPI)it.next();
      //Abrahamson site type
      if(tempParam.getName().equalsIgnoreCase(AS_1997_AttenRel.SITE_TYPE_NAME)){
        if(vs30 <=400 && vs30>180 && basinDepth>=100)
          tempParam.setValue(AS_1997_AttenRel.SITE_TYPE_SOIL);
        else
          tempParam.setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
      }
      //BJF site type
      else if(tempParam.getName().equalsIgnoreCase(BJF_1997_AttenRel.VS30_NAME)){
        if(vs30>180)
          tempParam.setValue(new Double(vs30));
      }
      //Cambell basin depth
      else if(tempParam.getName().equalsIgnoreCase(Campbell_1997_AttenRel.BASIN_DEPTH_NAME)){
        if(vs30>=400)
          tempParam.setValue(new Double(0));
        else // set basin depth in kms
          tempParam.setValue(new Double(basinDepth/1000));
      }
      //Cambell site type(Vs30)
      else if(tempParam.getName().equalsIgnoreCase(Campbell_1997_AttenRel.SITE_TYPE_NAME)){
        if(vs30>180 && vs30<=400)
          tempParam.setValue(Campbell_1997_AttenRel.SITE_TYPE_FIRM_SOIL);
        else if(vs30>400 && vs30<=500)
          tempParam.setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
        else if(vs30>500)
          tempParam.setValue(Campbell_1997_AttenRel.SITE_TYPE_HARD_ROCK);
      }
      //Campbell & Bozorgnia (2003) site type
      else if(tempParam.getName().equalsIgnoreCase(CB_2003_AttenRel.SITE_TYPE_NAME)){
        if(vs30>180 && vs30<=300)
          tempParam.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_SOIL);
        if(vs30>300 && vs30<=400)
          tempParam.setValue(CB_2003_AttenRel.SITE_TYPE_VERY_FIRM_SOIL);
        if(vs30 >400 && vs30 >=500)
          tempParam.setValue(CB_2003_AttenRel.SITE_TYPE_SOFT_ROCK);
        if(vs30 >500)
          tempParam.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_ROCK);
      }
      //Abrahamson site type
      else if(tempParam.getName().equalsIgnoreCase(Abrahamson_2000_AttenRel.SITE_TYPE_NAME)){
        if(vs30 <=400 && vs30>180 && basinDepth>=100)
          tempParam.setValue(Abrahamson_2000_AttenRel.SITE_TYPE_SOIL);
        else
          tempParam.setValue(Abrahamson_2000_AttenRel.SITE_TYPE_ROCK);
      }
      //SCEMY Site type
      else if(tempParam.getName().equalsIgnoreCase(SCEMY_1997_AttenRel.SITE_TYPE_NAME)){
        if(vs30 <=400 && vs30>180 && basinDepth>=100)
          tempParam.setValue(SCEMY_1997_AttenRel.SITE_TYPE_SOIL);
        else
          tempParam.setValue(SCEMY_1997_AttenRel.SITE_TYPE_ROCK);
      }

      //Field site type(Basin Depth)
      else if(tempParam.getName().equalsIgnoreCase(Field_2000_AttenRel.BASIN_DEPTH_NAME)){
        tempParam.setValue(new Double(basinDepth/1000));
      }
      //Field site type (Vs30)
      else if(tempParam.getName().equalsIgnoreCase(Field_2000_AttenRel.VS30_NAME)){
        if(vs30>180)
          tempParam.setValue(new Double(vs30));
      }
    }
  }
}