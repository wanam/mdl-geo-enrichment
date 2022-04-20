package eu.akka.mobidata.mashup.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.akka.mobidata.mashup.config.EndPointConfig;
import eu.akka.mobidata.mashup.domain.osm.OsmContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Service communicating with the OpenStreetMap API
 *
 * @author Mohamed.KARAMI
 */
@Service
public class OsmService {

    private static final String API_BUS_STOPS = "/interpreter?data=[out:json];node[highway=bus_stop](43.5690569,1.3951577,43.6283803,1.4803165);out%20meta;";

    private static final Logger LOGGER = LoggerFactory.getLogger(OsmService.class);

    @Autowired
    private EndPointConfig endPointConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Finds and returns bus stops.
     *
     * @return the bus stops if found, or null if not found
     */
    @Cacheable("bus-stops")
    public OsmContainer findBusStops() {
        LOGGER.debug("baseURI: {}", endPointConfig.getOverpassUri());
        try {
            String url = URLDecoder.decode(endPointConfig.getOverpassUri().concat(API_BUS_STOPS), "UTF-8");
            Object navObject = restTemplate.getForObject(url, Object.class);

            //@TODO convert osm to geojson
            return new ObjectMapper().convertValue(navObject, OsmContainer.class);
        } catch (RestClientException | UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }
}
