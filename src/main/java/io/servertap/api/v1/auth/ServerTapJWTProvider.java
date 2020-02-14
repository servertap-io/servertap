package io.servertap.api.v1.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import javalinjwt.JWTGenerator;
import javalinjwt.JWTProvider;

public class ServerTapJWTProvider {

    private JWTProvider provider;

    public ServerTapJWTProvider(String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        JWTGenerator<User> generator = (user, alg) -> {
            JWTCreator.Builder token = JWT.create()
                    .withClaim("name", user.getName())
                    .withClaim("level", user.getLevel());
            return token.sign(alg);
        };

        JWTVerifier verifier = JWT.require(algorithm).build();

        provider = new JWTProvider(algorithm, generator, verifier);
    }

    public JWTProvider getProvider() {
        return provider;
    }
}
