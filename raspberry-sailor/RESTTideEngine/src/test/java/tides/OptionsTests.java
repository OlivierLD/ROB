package tides;

import calc.GeoPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import tiderest.RESTImplementation;

import static org.junit.Assert.fail;

public class OptionsTests {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void optionFormat() {
        RESTImplementation.PublishingOptions publishingOptions = new RESTImplementation.PublishingOptions();
        publishingOptions.setPosition(new GeoPoint(47.34, -3.12));
        publishingOptions.setTimeZone("Europe/Paris");
        publishingOptions.setNb(1);
        publishingOptions.setStartYear(2022);
        publishingOptions.setStartMonth(1);
        publishingOptions.setQuantity(RESTImplementation.Quantity.YEAR);
        publishingOptions.setStationName("Whatever");

        try {
            String toJson = mapper.writeValueAsString(publishingOptions);
            // Like {"startMonth":1,"startYear":2022,"nb":1,"quantity":"YEAR","position":{"latitude":47.34,"longitude":-3.12},"timeZone":"Europe/Paris","stationName":"Whatever"}
            System.out.println(toJson);
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
            fail(jpe.getMessage());
        }
    }
}
