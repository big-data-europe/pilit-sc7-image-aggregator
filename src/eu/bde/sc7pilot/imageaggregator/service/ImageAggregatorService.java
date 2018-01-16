package eu.bde.sc7pilot.imageaggregator.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import eu.bde.sc7pilot.imageaggregator.Workflow;
import eu.bde.sc7pilot.imageaggregator.model.ImageData;
import eu.bde.sc7pilot.imageaggregator.webconfig.RestTimestampParam;

@Path("/changes")
public class ImageAggregatorService {
	
	@GET
	@Path("/progress")
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	public EventOutput changeDetectionwithProgress(@QueryParam("extent") String extent,
													@QueryParam("event_date") RestTimestampParam eventDate,
													@QueryParam("reference_date") RestTimestampParam referenceDate,
													@QueryParam("polarization") String selectedPolarisations,
													@QueryParam("username") String username,
													@QueryParam("password") String password) throws Exception {
		final EventOutput eventOutput = new EventOutput();
		if (extent == null) {
			handleServerException(eventOutput, "extent should not be null.");
		}
		DateTime eventDate2 = null;
		DateTime referenceDate2 = null;
		if (eventDate == null)
			eventDate2 = new DateTime();
		else
			eventDate2 = eventDate.getDate();
		if (referenceDate == null)
			referenceDate2 = eventDate2.minusDays(100);
		else
			referenceDate2 = referenceDate.getDate();

		WKTReader wktReader = new WKTReader();
		ImageData imageData = new ImageData(eventDate2, referenceDate2, null, username, password, new String[] { "ff" });
		
		try {
			Geometry geometry = wktReader.read(extent);
			imageData.setArea(geometry);
			Workflow workflow = new Workflow();
			try {
				workflow.downloadImages(imageData).subscribe((value) -> {
					try {
						notifyProgress(eventOutput, value);
					} catch (Exception e1) {
						handleServerException(eventOutput, e1.getMessage());
					}
				} , e -> handleServerException(eventOutput, e.getMessage()), () -> {
					try {
						if (!eventOutput.isClosed())
							eventOutput.close();
					} catch (Exception e1) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e1.getMessage());
					}
				});
			} catch (Exception e) {
				if (!eventOutput.isClosed())
					eventOutput.close();
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage());
			}
		} catch (ParseException e1) {
			handleServerException(eventOutput, "bounding_box is not a valid WKT polygon");
		}
		return eventOutput;
	}

	private void notifyProgress(EventOutput eventOutput, String value) throws IOException {
		final OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		eventBuilder.data(String.class, value);
		final OutboundEvent event = eventBuilder.build();
		try {
			eventOutput.write(event);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (!eventOutput.isClosed())
				eventOutput.close();
		}
	}

	private void handleServerException(EventOutput eventOutput, String message) {
		Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, message);
		final OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
		eventBuilder.data(String.class, message);
		final OutboundEvent event = eventBuilder.build();
		try {
			eventOutput.write(event);
			if (!eventOutput.isClosed())
				eventOutput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
}
