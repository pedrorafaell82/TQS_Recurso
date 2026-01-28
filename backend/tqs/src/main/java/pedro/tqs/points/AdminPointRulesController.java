package pedro.tqs.points;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pedro.tqs.points.dto.PointRulesResponse;
import pedro.tqs.points.dto.UpdatePointRulesRequest;

@RestController
@RequestMapping("/api/admin/points/rules")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPointRulesController {

    private final PointsService points;

    public AdminPointRulesController(PointsService points) {
        this.points = points;
    }

    @GetMapping
    public PointRulesResponse get() {
        return new PointRulesResponse(points.getApprovalMultiplier());
    }

    @PutMapping
    public PointRulesResponse update(@Valid @RequestBody UpdatePointRulesRequest req) {
        return new PointRulesResponse(points.updateApprovalMultiplier(req.approvalMultiplier()));
    }
}
