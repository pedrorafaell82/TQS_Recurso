package pedro.tqs.opportunity;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class OpportunitySpecifications {

    public static Specification<Opportunity> isActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return cb.conjunction();
            return cb.equal(root.get("active"), active);
        };
    }

    public static Specification<Opportunity> queryText(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }

    public static Specification<Opportunity> minPoints(Integer min) {
        return (root, query, cb) -> {
            if (min == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("points"), min);
        };
    }

    public static Specification<Opportunity> maxPoints(Integer max) {
        return (root, query, cb) -> {
            if (max == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("points"), max);
        };
    }

    public static Specification<Opportunity> dateFrom(LocalDate from) {
        return (root, query, cb) -> {
            if (from == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("date"), from);
        };
    }

    public static Specification<Opportunity> dateTo(LocalDate to) {
        return (root, query, cb) -> {
            if (to == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("date"), to);
        };
    }
}
