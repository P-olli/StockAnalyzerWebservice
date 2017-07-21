package de.olli.model;

import lombok.Data;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by olli on 03.07.2017.
 */
@Data
public class Price {

    private final Date day;
    private final Double price;
}
