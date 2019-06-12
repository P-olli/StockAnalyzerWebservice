package de.olli.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

public class DepotEntry {
    @Getter
    private String stockId;
    @Getter
    private int count;
    private double averagePrice;
    @JsonIgnore
    private List<StockTransaction> transactions = Lists.newArrayList();

    public static DepotEntry createDepot(StockTransaction stockTransaction) {
        DepotEntry depotEntry = new DepotEntry();
        depotEntry.stockId = stockTransaction.getStockId();
        depotEntry.addTransaction(stockTransaction);
        return depotEntry;
    }

    public void addTransaction(StockTransaction stockTransaction) {
        this.transactions.add(stockTransaction);
        if(stockTransaction.isAcquistion()) {
            this.count +=  stockTransaction.getCount();
        } else {
            this.count -= stockTransaction.getCount();
        }

    }

    public double getAveragePrice() {
        List<StockTransaction> acquistions = Lists.newArrayList();
        List<StockTransaction> dispositions = Lists.newArrayList();
        this.transactions.stream().sorted().forEach(t -> {
            if(t.isAcquistion()) {
                acquistions.add(t);
            } else {
                dispositions.add(t);
            }
        });
        dispositions.stream().sorted().forEach(t -> {
            int count = t.getCount();
            int i = 0;
            while(count > 0) {
                if(acquistions.get(i).getCount() <= count) {
                    acquistions.remove(i);
                     count -= acquistions.get(i).getCount();
                    i--;
                } else {
                    acquistions.get(i).setCount(acquistions.get(i).getCount() - count);
                    count = 0;
                }
                i++;
            }
        });
        return calculateAverage(acquistions);
    }

    private double calculateAverage(List<StockTransaction> transactions) {
        double totalPrioe = 0.0;
        int totalCount = 0;
        for(StockTransaction t : transactions) {
            totalPrioe += t.getCount() * t.getPrice();
            totalCount += t.getCount();
        }
        return totalPrioe / totalCount;
    }
}
