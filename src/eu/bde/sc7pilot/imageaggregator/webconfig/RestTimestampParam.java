 package eu.bde.sc7pilot.imageaggregator.webconfig;

import java.net.HttpURLConnection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class RestTimestampParam {
    private DateTime date;
 
    public RestTimestampParam( String dateTimeStr ) throws WebApplicationException {
        try {
        	DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeParser();
        	date=parser2.parseDateTime(dateTimeStr) ;
        } catch ( final IllegalArgumentException ex ) {
        	ResponseMessage respMessage=new ResponseMessage();
        	respMessage.setMessage("date should be ISO8601 format");
			respMessage.setCode(400);
            throw new WebApplicationException( Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
			        .entity(respMessage)
			        .type(MediaType.APPLICATION_JSON)
			        .build() );
        }
    }
 
    public DateTime getDate() {
        return date;
    }
 
    @Override
    public String toString() {
        if ( date != null ) {
            return date.toString();
        } else {
            return "";
        }
    }
}
