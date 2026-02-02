package QuentinXHugo.demo.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "royaume.professor")
public class ProfessorProperties {

	private String baseUrl = "https://royaume.devonn.io";
	private String group = "observabilia";
	private Duration fetchDelay = Duration.ofSeconds(60);
	private Duration processingDelay = Duration.ofSeconds(5);
	private boolean enabled = true;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Duration getFetchDelay() {
		return fetchDelay;
	}

	public void setFetchDelay(Duration fetchDelay) {
		this.fetchDelay = fetchDelay;
	}

	public Duration getProcessingDelay() {
		return processingDelay;
	}

	public void setProcessingDelay(Duration processingDelay) {
		this.processingDelay = processingDelay;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
