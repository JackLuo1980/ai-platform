package com.aiplatform.console.license;

import com.aiplatform.common.model.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/license")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    @GetMapping
    public R<License> getInfo() {
        return licenseService.getActiveLicense();
    }

    @PostMapping("/activate")
    public R<License> activate(@RequestParam String licenseKey) {
        return licenseService.activate(licenseKey);
    }
}
