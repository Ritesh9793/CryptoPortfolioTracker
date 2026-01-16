package com.ritesh.crypto_portfolio_tracker.service;

import com.ritesh.crypto_portfolio_tracker.entity.Token;
import com.ritesh.crypto_portfolio_tracker.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TokenService {

    private final TokenRepository repo;
    private final Map<String, String> nativeTokens = Map.of(
            "ethereum", "native",
            "bsc", "native",
            "polygon", "native"
    );

    public TokenService(TokenRepository repo) {
        this.repo = repo;
    }

    // ----------------------------------------------
    // GET CONTRACT ADDRESS FOR A SYMBOL + CHAIN
    // ----------------------------------------------
    public String getContract(String symbol, String chain) {
        return repo.findBySymbolIgnoreCaseAndChainIgnoreCase(symbol, chain)
                .map(Token::getContractAddress)
                .orElseGet(() -> fallbackContract(symbol, chain));
    }

    // ----------------------------------------------
    // GET COINGECKO ID
    // ----------------------------------------------
    public Optional<String> getCoingeckoId(String symbol, String chain) {
        return repo.findBySymbolIgnoreCaseAndChainIgnoreCase(symbol, chain)
                .map(Token::getCoingeckoId);
    }

    // ----------------------------------------------
    // REGISTER OR UPDATE TOKEN MAPPING
    // ----------------------------------------------
    public Token register(String symbol, String chain, String contract, String coingeckoId) {

        Optional<Token> existing =
                repo.findBySymbolIgnoreCaseAndChainIgnoreCase(symbol, chain);

        if (existing.isPresent()) {
            Token t = existing.get();
            t.setContractAddress(contract);
            t.setCoingeckoId(coingeckoId);
            t.setLastUpdated(LocalDateTime.now());
            return repo.save(t);
        }

        Token created = Token.builder()
                .symbol(symbol)
                .chain(chain)
                .contractAddress(contract)
                .coingeckoId(coingeckoId)
                .lastUpdated(LocalDateTime.now())
                .build();

        return repo.save(created);
    }


    // ----------------------------------------------
    // FALLBACK MECHANISM (Native tokens & unknowns)
    // ----------------------------------------------
    private String fallbackContract(String symbol, String chain) {

        // Case 1: ETH, MATIC, BNB → use "native"
        if (isNativeSymbol(symbol, chain)) return "native";

        // Case 2: Not found → create placeholder token to avoid repeated queries
        Token placeholder = Token.builder()
                .symbol(symbol)
                .chain(chain)
                .contractAddress("unknown")
                .lastUpdated(LocalDateTime.now())
                .build();

        repo.save(placeholder);
        return "unknown";
    }

    private boolean isNativeSymbol(String symbol, String chain) {
        return switch (chain.toLowerCase()) {
            case "ethereum" -> symbol.equalsIgnoreCase("ETH");
            case "bsc" -> symbol.equalsIgnoreCase("BNB");
            case "polygon" -> symbol.equalsIgnoreCase("MATIC");
            default -> false;
        };
    }
}
