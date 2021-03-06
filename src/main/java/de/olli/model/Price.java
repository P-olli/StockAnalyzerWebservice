package de.olli.model;

import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;

/**
 * Created by olli on 03.07.2017.
 */
@CompoundIndexes({
        @CompoundIndex(name = "price_unique",
                unique = true,
                def = "{'stockId' : 1, 'day' : 1}")
})
@NoArgsConstructor
public class Price extends PriceBase {

    public Price(PriceBase priceBase) {
        super(priceBase.getStockId(), priceBase.getDay(), priceBase.getPrice());
    }

    public Price(String stockId, LocalDateTime day, double price) {
        super(stockId, day, price);
    }

}
