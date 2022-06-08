package eu.akka.mobidata.mashup.controllers;

import com.jayway.jsonpath.JsonPath;
import eu.akka.mobidata.mashup.enumeration.APIFormatEnum;
import eu.akka.mobidata.mashup.exceptions.MobilityDataNotFoundException;
import eu.akka.mobidata.mashup.services.interfaces.INavitiaService;
import eu.akka.mobidata.mashup.services.interfaces.IOsmService;
import eu.akka.mobidata.mashup.util.GeoJsonManager;
import eu.akka.mobidata.mashup.util.OsmManager;
import io.swagger.annotations.ApiParam;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles the mobility data / lines in the REST API.
 *
 * @author Mohamed.KARAMI
 */
@Controller
@RequestMapping(value = "/api/v1/navitia", produces = MediaType.APPLICATION_JSON_VALUE)
public class LineController {

    @Autowired
    private INavitiaService navitiaService;

    @Autowired
    private IOsmService osmService;

    /**
     * Returns the aggregated information for lines.
     *
     * @return the JSON representation of the lines
     */
    @RequestMapping(value = "getLines", method = RequestMethod.GET)
    public @ResponseBody
    String getLines(@ApiParam(value = "Navitia API authorization token", example = "3b036afe-0110-4202-b9ed-99718476c2e0") String targetToken,
                    @ApiParam(value = "Coordinates of starting point: latitude, longitude", required = true, example = "48.8345631,2.2433581") String fromCoordinates,
                    @ApiParam(value = "Coordinates of the arrival point: latitude, longitude", required = true, example = "48.8775033,2.4400646") String toCoordinates,
                    @ApiParam(value = "Attributes to be enriched on the target api, separated with commas", example = "wheelchair, shelter, tactile_paving, bench, bin, lit") String enrichAttributes,
                    @ApiParam(value = "API format", allowableValues = "GeoJson, OSM", required = true) APIFormatEnum apiFormat,
                    @ApiParam(value = "API full url", required = true, example = "https://www.overpass-api.de/api/interpreter?data=[out:json];node[highway=bus_stop](48.8345631,2.2433581,48.8775033,2.4400646);out%20meta;") String apiUrl,
                    @ApiParam(value = "Source API authorization token") String sourceToken) {
        // Get lines from Navitia
        String lines = navitiaService.findLines(targetToken, fromCoordinates, toCoordinates);
        if (lines == null) {
            throw new MobilityDataNotFoundException("No Navitia lines found!");
        }

        apiUrl = URLDecoder.decode(apiUrl, StandardCharsets.UTF_8);
        if (APIFormatEnum.OSM.equals(apiFormat)) {
            // get bus stops for the same coordinates from osm
            String busStops = osmService.getGeoJsonBusStops(apiUrl, sourceToken);
            JSONArray osmElements = JsonPath.read(busStops, "$.elements");

            // aggregate and enrich navitia's bus stops from osm response
            OsmManager osmManager = new OsmManager(lines);
            return osmManager.aggregateBusStops(osmElements, enrichAttributes);
        } else if (APIFormatEnum.GeoJson.equals(apiFormat)) {
            // get bus stops for the same coordinates from osm
            String osmBusStops = osmService.getGeoJsonFromOsmBusStops(apiUrl, sourceToken);

            // load features from geo json response
            GeoJsonManager geoJsonManager = new GeoJsonManager(lines, osmBusStops);
            return geoJsonManager.aggregateNavitiaBusStops(enrichAttributes);
        }
        return null;
    }
}
