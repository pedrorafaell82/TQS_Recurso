package pedro.tqs.points;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pedro.tqs.points.dto.PointsBalanceResponse;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointsService service;

    public PointsController(PointsService service) {
        this.service = service;
    }

    @GetMapping("/balance")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public PointsBalanceResponse balance(Authentication auth) {
        int balance = service.getBalanceForUser(auth.getName());
        return new PointsBalanceResponse(balance);
    }
}
