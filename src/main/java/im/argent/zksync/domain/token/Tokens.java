package im.argent.zksync.domain.token;

import lombok.*;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class Tokens {

    Map<String, Token> tokens;

    public Token getTokenBySymbol(String symbol) {
        return tokens.get(symbol);
    }

    public Token getTokenByAddress(String tokenAddress) {
        return tokens
                .values()
                .stream()
                .filter(token -> token.getAddress().equals(tokenAddress))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No token with address " + tokenAddress));
    }

    public Token getToken(String tokenIdentifier) {

        return getTokenBySymbol(tokenIdentifier) != null ?
                getTokenBySymbol(tokenIdentifier) : getTokenByAddress(tokenIdentifier);
    }
}
