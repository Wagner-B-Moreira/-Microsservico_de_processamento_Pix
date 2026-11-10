package wasoftware.microsservicosimulaoprocessamento.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import java.net.URI;

@Configuration
public class SnsConfig {
    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${aws.sns.endpoint:}")
    private String snsEndpoint;

    @Bean
    public SnsClient snsClient() {
        var builder = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (!snsEndpoint.isBlank()) {
            builder.endpointOverride(URI.create(snsEndpoint));
        }

        return builder.build();
    }
}
