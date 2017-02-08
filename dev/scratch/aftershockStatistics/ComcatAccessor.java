package scratch.aftershockStatistics;

import gov.usgs.earthquake.event.EventQuery;
import gov.usgs.earthquake.event.EventWebService;
import gov.usgs.earthquake.event.Format;
import gov.usgs.earthquake.event.JsonEvent;
import scratch.aftershockStatistics.cmu.EqkEventIdComparator;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;

import com.google.common.base.Preconditions;

public class ComcatAccessor {
	
	private static final boolean D = true;
	
	private EventWebService service;
	
	public ComcatAccessor() {
		try {
			service = new EventWebService(new URL("http://earthquake.usgs.gov/fdsnws/event/1/"));
		} catch (MalformedURLException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
	}
	
	/**
	 * Fetches an event with the given ID, e.g. "ci37166079"
	 * @param eventID
	 * @return
	 */
	public ObsEqkRupture fetchEvent(String eventID) {
		EventQuery query = new EventQuery();
		query.setEventId(eventID);
		List<JsonEvent> events;
		try {
			events = service.getEvents(query);
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		if (events.isEmpty())
			return null;
		Preconditions.checkState(events.size() == 1, "More that 1 match? "+events.size());
		
		JsonEvent event = events.get(0);
//		printJSON(event);
		
		return eventToObsRup(events.get(0));
	}
	
	public static void printJSON(JSONObject json) {
		printJSON(json, "");
	}
	private static void printJSON(JSONObject json, String prefix) {
		for (Object key : json.keySet()) {
			Object val = json.get(key);
			if (val != null && val.toString().startsWith("[{")) {
				String str = val.toString();
				try {
					val = new JSONParser().parse(str.substring(1, str.length()-1));
				} catch (ParseException e) {
//					e.printStackTrace();
				}
			}
			if (val != null && val instanceof JSONObject) {
				System.out.println(prefix+key+":");
				String prefix2 = prefix;
				if (prefix2 == null)
					prefix2 = "";
				prefix2 += "\t";
				printJSON((JSONObject)val, prefix2);
			} else {
				System.out.println(prefix+key+": "+val);
			}
		}
	}
	
	static final double day_millis = 24d*60d*60d*1000d;
	
	/**
	 * Fetch all aftershocks of the given event. Returned list will not contain the mainshock
	 * even if it matches the query.
	 * @param mainshock
	 * @param minDays
	 * @param maxDays
	 * @param minDepth
	 * @param maxDepth
	 * @param region
	 * @return
	 */
	public ObsEqkRupList fetchAftershocks(ObsEqkRupture mainshock, double minDays, double maxDays,
			double minDepth, double maxDepth, Region region) {
		EventQuery query = new EventQuery();
		
		Preconditions.checkState(minDepth < maxDepth, "Min depth must be less than max depth");
		query.setMinDepth(new BigDecimal(minDepth));
		query.setMaxDepth(new BigDecimal(maxDepth));
		
		Preconditions.checkState(minDays < maxDays, "Min days must be less than max days");
		// time zones shouldn't be an issue since we're just adding to the original catalog time, whatever
		// time zone that is in.
		long eventTime = mainshock.getOriginTime();
		long startTime = eventTime + (long)(minDays*day_millis);
		long endTime = eventTime + (long)(maxDays*day_millis);
		query.setStartTime(new Date(startTime));
		if(endTime==startTime)
			endTime=Instant.now().toEpochMilli();
		query.setEndTime(new Date(endTime));
		
		Preconditions.checkState(startTime < System.currentTimeMillis(), "Start time is before now!");
		
		query.setMinLatitude(new BigDecimal(region.getMinLat()));
		query.setMaxLatitude(new BigDecimal(region.getMaxLat()));
		query.setMinLongitude(new BigDecimal(region.getMinLon()));
		query.setMaxLongitude(new BigDecimal(region.getMaxLon()));
		query.setLimit(20000);
		List<JsonEvent> events;
		int count=20000;
		ObsEqkRupList rups = new ObsEqkRupList();
		Date latest=new Date(endTime);
		Date endTimeStamp;
		do{
			endTimeStamp=latest;
			query.setEndTime(latest);
			if (D)
				try {
					System.out.println(service.getUrl(query, Format.GEOJSON));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

			try {
				events = service.getEvents(query);
				count = events.size();
				System.out.println(count);
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}


			for (JsonEvent event : events) {
				ObsEqkRupture rup = eventToObsRup(event);
				if (rup !=null)
					rups.add(rup);
			}
			rups.sortByOriginTime();
			if(count==0)
				break;
			latest=rups.get(0).getOriginTimeCal().getTime();
		}while(count==20000 && endTimeStamp.compareTo(latest)!=0);
		Collections.sort(rups, new EqkEventIdComparator());
		ObsEqkRupList delrups=new ObsEqkRupList();
		ObsEqkRupture previous =null;
		for (ObsEqkRupture rup : rups) {
			if (rup.getEventId().equals(mainshock.getEventId()) || (previous!=null && rup.getEventId().equals(previous.getEventId()))) {
				//if (D) System.out.println("Removing mainshock (M="+rup.getMag()+") from aftershock list");
				delrups.add(rup);
			}
		}
		rups.removeAll(delrups);
		if (!region.isRectangular()) {
			if (D) System.out.println("Fetched "+rups.size()+" events before region filtering");
			for (int i=rups.size(); --i>=0;)
				if (!region.contains(rups.get(i).getHypocenterLocation()))
					rups.remove(i);
		}
		
		if (D) System.out.println("Returning "+rups.size()+" aftershocks");
		
		return rups;
	}
	
	public static ObsEqkRupture eventToObsRup(JsonEvent event) {
		double lat = event.getLatitude().doubleValue();
		double lon = event.getLongitude().doubleValue();
		double dep = event.getDepth().doubleValue();
		Location hypo = new Location(lat, lon, dep);
		double mag=0;
		try{
			mag = event.getMag().doubleValue();
		}catch(Exception e){
			System.out.println(event.toString());
			return null;
		}
		ObsEqkRupture rup = new ObsEqkRupture(event.getEventId().toString(),
				event.getTime().getTime(), hypo, mag);
		
		return rup;
	}

}
