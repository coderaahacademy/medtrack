package com.medtrack.specification;

import com.medtrack.entity.Medication;
import org.springframework.data.jpa.domain.Specification;

public class MedicationSpecification {
    public static Specification<Medication> search(String q, String name, String genericName, String brand) {
        boolean hasQ = q != null && !q.isBlank();
        boolean hasName = name != null && !name.isBlank();
        boolean hasGeneric = genericName != null && !genericName.isBlank();
        boolean hasBrand = brand != null && !brand.isBlank();
        Specification<Medication> spec = Specification.where(isActive());
        if (hasName) spec = spec.and(nameContains(name));
        if (hasGeneric) spec = spec.and(genericNameContains(genericName));
        if (hasBrand) spec = spec.and(brandContains(brand));
        if (hasQ) {
            Specification<Medication> specQ = null;
            if (!hasName) {
                specQ = Specification.where(nameContains(q));
            }
            if (!hasGeneric) {
                specQ = (specQ == null) ? Specification.where(genericNameContains(q)) : specQ.or(genericNameContains(q));
            }
            if (!hasBrand) {
                specQ = (specQ == null) ? Specification.where(brandContains(q)) : specQ.or(brandContains(q));
            }
            if (specQ != null) {
                spec = spec.and(specQ);
            }
        }
        return spec;
    }
    public static Specification<Medication> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }
    public static Specification<Medication> nameContains(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
    public static Specification<Medication> genericNameContains(String genericName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("genericName")), "%" + genericName.toLowerCase() + "%");
    }
    public static Specification<Medication> brandContains(String brand) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%");
    }
}
