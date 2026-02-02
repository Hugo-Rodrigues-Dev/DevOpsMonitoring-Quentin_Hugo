package QuentinXHugo.demo.persistence;

import java.time.Duration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DurationToLongConverter implements AttributeConverter<Duration, Long> {

	@Override
	public Long convertToDatabaseColumn(Duration attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.toMillis();
	}

	@Override
	public Duration convertToEntityAttribute(Long dbData) {
		if (dbData == null) {
			return null;
		}
		return Duration.ofMillis(dbData);
	}
}
