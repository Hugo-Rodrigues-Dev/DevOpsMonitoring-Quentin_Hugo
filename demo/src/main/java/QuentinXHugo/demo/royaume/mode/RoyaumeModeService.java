package QuentinXHugo.demo.royaume.mode;

import QuentinXHugo.demo.royaume.config.ExecutionMode;
import QuentinXHugo.demo.royaume.config.RoyaumeApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class RoyaumeModeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoyaumeModeService.class);

    private final AtomicReference<ExecutionMode> mode = new AtomicReference<>(ExecutionMode.AUTO);

    public RoyaumeModeService(RoyaumeApiProperties properties) {
        this.mode.set(properties.getMode());
    }

    public ExecutionMode getMode() {
        return mode.get();
    }

    public boolean isAuto() {
        return ExecutionMode.AUTO.equals(getMode());
    }

    public void setMode(ExecutionMode newMode) {
        if (newMode == null) {
            return;
        }
        mode.set(newMode);
        LOGGER.info("Royaume execution mode switched to {}", newMode);
    }
}
