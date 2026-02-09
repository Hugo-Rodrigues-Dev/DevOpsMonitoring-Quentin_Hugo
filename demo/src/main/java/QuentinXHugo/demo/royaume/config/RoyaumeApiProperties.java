package QuentinXHugo.demo.royaume.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "royaume.api")
public class RoyaumeApiProperties {

    private URI baseUrl = URI.create("https://royaume.devonn.io/api");
    private String defaultGroup = "QuentinXHugo";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(5);
    private Duration pollInterval = Duration.ofSeconds(5);
    private Duration defaultResolveDelay = Duration.ofSeconds(30);
    private ExecutionMode mode = ExecutionMode.AUTO;

    public URI getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }

    public Duration getDefaultResolveDelay() {
        return defaultResolveDelay;
    }

    public void setDefaultResolveDelay(Duration defaultResolveDelay) {
        this.defaultResolveDelay = defaultResolveDelay;
    }

    public ExecutionMode getMode() {
        return mode;
    }

    public void setMode(ExecutionMode mode) {
        this.mode = mode;
    }
}
