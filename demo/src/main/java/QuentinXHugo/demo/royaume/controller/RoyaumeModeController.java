package QuentinXHugo.demo.royaume.controller;

import QuentinXHugo.demo.royaume.config.ExecutionMode;
import QuentinXHugo.demo.royaume.mode.RoyaumeModeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/royaume/mode")
public class RoyaumeModeController {

    private final RoyaumeModeService modeService;

    public RoyaumeModeController(RoyaumeModeService modeService) {
        this.modeService = modeService;
    }

    @GetMapping
    public Map<String, Object> getMode() {
        return Map.of(
                "mode", modeService.getMode(),
                "auto", modeService.isAuto()
        );
    }

    @PostMapping
    public Map<String, Object> setMode(@RequestParam("mode") ExecutionMode mode) {
        modeService.setMode(mode);
        return getMode();
    }
}
