package uk.co.eightmile.racs.common.servers;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class HttpConnectorConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${server.port}")
    private int httpsPort;

    @Value("${server.http-port:5014}")
    private int httpPort;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        if (httpsPort == httpPort) {
            throw new IllegalStateException(
                    "HTTP connector port must not be the same as HTTPS server.port. Both are " + httpPort
            );
        }

        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");

        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);

        factory.addAdditionalConnectors(connector);
    }
}