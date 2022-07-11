package io.kontur.disasterninja.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.List;

public class BboxValidator implements ConstraintValidator<ValidBbox, List<BigDecimal>> {

    @Override
    public void initialize(ValidBbox value) {
    }

    @Override
    public boolean isValid(List<BigDecimal> bbox, ConstraintValidatorContext ctx) {
        if (bbox == null || bbox.isEmpty()) {
            return true;
        }
        if (bbox.size() != 4) {
            ctx.buildConstraintViolationWithTemplate("bbox should be provided as 4 numbers.")
                    .addConstraintViolation();
            return false;
        }
        boolean b = checkLon(bbox.get(0)) && checkLat(bbox.get(1)) && checkLon(bbox.get(2)) && checkLat(bbox.get(3));
        if (!b) {
            ctx.buildConstraintViolationWithTemplate("bbox coordinates doesn't conform to WGS84 coordinate system")
                    .addConstraintViolation();
            return false;
        }

        if (bbox.get(0).compareTo(bbox.get(2)) > 0 || bbox.get(1).compareTo(bbox.get(3)) > 0) {
            ctx.buildConstraintViolationWithTemplate("antimeridian coordinates is not supported")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean checkLat(BigDecimal lat) {
        return BigDecimal.valueOf(90).compareTo(lat) >= 0 && BigDecimal.valueOf(-90).compareTo(lat) <= 0;
    }

    private boolean checkLon(BigDecimal lon) {
        return BigDecimal.valueOf(180).compareTo(lon) >= 0 && BigDecimal.valueOf(-180).compareTo(lon) <= 0;
    }

}

