package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.dto.pnl.CostBasisMethod;
import com.ritesh.crypto_portfolio_tracker.dto.pnl.TaxLot;
import com.ritesh.crypto_portfolio_tracker.entity.Trade;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CostBasisService {

    // --------------------------------------------
    // FIFO
    // --------------------------------------------
    public List<TaxLot> computeFifoLots(List<Trade> trades) {
        List<TaxLot> lots = new ArrayList<>();

        for (Trade t : trades) {
            if (t.getSide().equals("BUY")) {
                lots.add(TaxLot.builder()
                        .quantity(t.getQuantity())
                        .costPrice(t.getPrice())
                        .time(t.getTime())
                        .build());
            } else if (t.getSide().equals("SELL")) {
                double qtyToMatch = t.getQuantity();

                while (qtyToMatch > 0 && !lots.isEmpty()) {
                    TaxLot lot = lots.getFirst();

                    if (lot.getQuantity() <= qtyToMatch) {
                        qtyToMatch -= lot.getQuantity();
                        lots.removeFirst();
                    } else {
                        lot.setQuantity(lot.getQuantity() - qtyToMatch);
                        qtyToMatch = 0;
                    }
                }
            }
        }

        return lots;
    }

    // --------------------------------------------
    // LIFO
    // --------------------------------------------
    public List<TaxLot> computeLifoLots(List<Trade> trades) {
        List<TaxLot> lots = new LinkedList<>();

        for (Trade t : trades) {
            if (t.getSide().equals("BUY")) {
                lots.add(TaxLot.builder()
                        .quantity(t.getQuantity())
                        .costPrice(t.getPrice())
                        .time(t.getTime())
                        .build());
            } else if (t.getSide().equals("SELL")) {
                double qtyToMatch = t.getQuantity();

                while (qtyToMatch > 0 && !lots.isEmpty()) {
                    TaxLot last = lots.getLast();

                    if (last.getQuantity() <= qtyToMatch) {
                        qtyToMatch -= last.getQuantity();
                        lots.removeLast();
                    } else {
                        last.setQuantity(last.getQuantity() - qtyToMatch);
                        qtyToMatch = 0;
                    }
                }
            }
        }

        return lots;
    }

    // --------------------------------------------
    // AVERAGE COST
    // --------------------------------------------
    public double computeAverageCost(List<Trade> trades) {
        double totalQty = 0;
        double totalCost = 0;

        for (Trade t : trades) {
            if (t.getSide().equals("BUY")) {
                totalQty += t.getQuantity();
                totalCost += t.getQuantity() * t.getPrice();
            } else if (t.getSide().equals("SELL")) {
                totalQty -= t.getQuantity();
                // cost decreases but does NOT affect cost basis per unit
            }
        }

        if (totalQty == 0) return 0;

        return totalCost / totalQty;
    }

    // --------------------------------------------
    // SWITCH METHOD
    // --------------------------------------------
    public List<TaxLot> getTaxLots(List<Trade> trades, CostBasisMethod method) {
        return switch (method) {
            case FIFO -> computeFifoLots(trades);
            case LIFO -> computeLifoLots(trades);
            case AVERAGE -> List.of(); // average cost does not have lots
        };
    }
}
