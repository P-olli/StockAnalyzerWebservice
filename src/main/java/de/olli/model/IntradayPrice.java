package de.olli.model;

import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

@CompoundIndexes({
        @CompoundIndex(name = "price_unique",
                unique = true,
                def = "{'stockId' : 1, 'day' : 1}")
})
@NoArgsConstructor
public class IntradayPrice extends PriceBase {

    public IntradayPrice(PriceBase priceBase) {
        super(priceBase.getStockId(), priceBase.getDay(), priceBase.getPrice());
    }
}
