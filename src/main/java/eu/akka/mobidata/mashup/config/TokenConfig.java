package eu.akka.mobidata.mashup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tokens")
public class TokenConfig {

    private String navitiaUserToken;
    private String navitiaDocToken;

    public String getNavitiaUserToken() {
        return navitiaUserToken;
    }

    public void setNavitiaUserToken(String navitiaUserToken) {
        this.navitiaUserToken = navitiaUserToken;
    }

    public String getNavitiaDocToken() {
        return navitiaDocToken;
    }

    public void setNavitiaDocToken(String navitiaDocToken) {
        this.navitiaDocToken = navitiaDocToken;
    }
}