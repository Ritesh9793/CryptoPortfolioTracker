package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.dto.pnl.RealizedPnlEntry;
import com.ritesh.crypto_portfolio_tracker.dto.pnl.RealizedPnlSummary;
import com.ritesh.crypto_portfolio_tracker.entity.Trade;
import com.ritesh.crypto_portfolio_tracker.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Computes realized P&L using FIFO by default.
 */
@Service
public class RealizedPnlService {

    private final TradeRepository tradeRepo;

    public RealizedPnlService(TradeRepository tradeRepo) {
        this.tradeRepo = tradeRepo;
    }

    /**
     * Compute realized PnL for all symbols for given user using FIFO.
     */
    public RealizedPnlSummary computeRealizedPnl(Long userId) {
        List<Trade> trades = tradeRepo.findByUserIdOrderByTimeAsc(userId);

        // group by symbol preserving chronological order
        Map<String, List<Trade>> bySymbol = new LinkedHashMap<>();
        for (Trade t : trades) {
            bySymbol.computeIfAbsent(t.getSymbol(), k -> new ArrayList<>()).add(t);
        }

        List<RealizedPnlEntry> entries = new ArrayList<>();
        double totalGain = 0.0;

        for (Map.Entry<String, List<Trade>> e : bySymbol.entrySet()) {
            String symbol = e.getKey();
            List<Trade> list = e.getValue();

            // FIFO queue of buy lots: each element = [remainingQty, pricePerUnit, time]
            Deque<double[]> buyQueue = new ArrayDeque<>();

            for (Trade t : list) {
                String side = t.getSide().toUpperCase();
                double qty = t.getQuantity();
                double price = t.getPrice();
                LocalDateTime time = t.getTime();

                if (side.equals("BUY")) {
                    buyQueue.addLast(new double[]{qty, price, time.atZone(java.time.ZoneId.systemDefault()).toEpochSecond(), 0});
                    // third element is epoch seconds placeholder (we'll convert as needed)
                } else if (side.equals("SELL")) {
                    double qtyToSell = qty;
                    while (qtyToSell > 0 && !buyQueue.isEmpty()) {
                        double[] buyLot = buyQueue.peekFirst();
                        double buyQty = buyLot[0];
                        double buyPrice = buyLot[1];
                        long buyEpoch = (long) buyLot[2];
                        LocalDateTime buyTime = LocalDateTime.ofEpochSecond(buyEpoch, 0, java.time.ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now()));

                        double matchedQty = Math.min(buyQty, qtyToSell);

                        double proceeds = matchedQty * price;
                        double costBasis = matchedQty * buyPrice;
                        double gain = proceeds - costBasis;

                        RealizedPnlEntry entry = RealizedPnlEntry.builder()
                                .tradeSellId(t.getId())
                                .symbol(symbol)
                                .quantity(matchedQty)
                                .buyPricePerUnit(buyPrice)
                                .buyTime(buyTime)
                                .sellPricePerUnit(price)
                                .sellTime(time)
                                .proceeds(proceeds)
                                .costBasis(costBasis)
                                .gain(gain)
                                .build();

                        entries.add(entry);
                        totalGain += gain;

                        // decrement lot
                        if (buyQty > matchedQty) {
                            buyLot[0] = buyQty - matchedQty;
                            break; // sale satisfied
                        } else {
                            buyQueue.removeFirst();
                        }

                        qtyToSell -= matchedQty;
                    }
                    // if qtyToSell remains >0 => short-sell or missing buys; we ignore unmatched sells for now
                }
            }
        }

        return RealizedPnlSummary.builder()
                .userId(userId)
                .entries(entries)
                .totalRealizedGain(totalGain)
                .build();
    }
}
