package uk.co.eightmile.racs.reports;

import uk.co.eightmile.racs.auth.SecurityRules;
import uk.co.eightmile.racs.permissions.Authority;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ReportSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                // POST /api/report/general-summary
                .requestMatchers(HttpMethod.POST, "/api/report/general-summary").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_GENERAL_SUMMARY_REPORT.name()
                )

                // POST /api/report/daily-usage
                .requestMatchers(HttpMethod.POST, "/api/report/daily-usage").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_DAILY_USAGE_REPORT.name()
                )

                // POST /api/report/location
                .requestMatchers(HttpMethod.POST, "/api/report/location").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_LOCATION_REPORT.name()
                )

                // POST /api/report/multiple-scans
                .requestMatchers(HttpMethod.POST, "/api/report/multiple-scans").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_MULTIPLE_SCAN_REPORT.name()
                )

                // POST /api/report/reader
                .requestMatchers(HttpMethod.POST, "/api/report/reader").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_READER_REPORT.name()
                )

                // POST /api/report/serial-number
                .requestMatchers(HttpMethod.POST, "/api/report/serial-number").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_SERIAL_NUMBER_REPORT.name()
                );
    }
}