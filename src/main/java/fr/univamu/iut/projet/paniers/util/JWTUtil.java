package fr.univamu.iut.projet.paniers.util;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fr.univamu.iut.projet.paniers.entity.User; // Votre entité User
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject; // Si vous injectez UserService

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JWTUtil {
    public static final String JWT_SECRET = "CLEFTROPPRIVEESECRETOMGCLEFTROPPRIVEESECRETOMG";
    public static final String JWT_ISSUER = "appli_de_fou";
    public static final long JWT_EXPIRATION_MS = 3600 * 1000;

    /**
     * Génère un token JWT pour un utilisateur.
     */
    public String generateToken(User user, List<String> roles) throws JOSEException {
        Instant now = Instant.now();
        Instant expiryTime = now.plusMillis(JWT_EXPIRATION_MS);

        JWSSigner signer = new MACSigner(JWT_SECRET);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(JWT_ISSUER)
                .subject(user.getId())
                .claim("upn", user.getEmail())
                .claim("groups", roles)
                .expirationTime(Date.from(expiryTime))
                .issueTime(Date.from(now))
                .jwtID(UUID.randomUUID().toString())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
                claimsSet);

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    /**
     * Valide un token JWT et retourne les claims si valide.
     */
    public Optional<JWTClaimsSet> validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(JWT_SECRET);

            if (!signedJWT.verify(verifier)) {
                System.err.println("JWT Validation Failed: Invalid Signature");
                return Optional.empty();
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            Instant expiration = claimsSet.getExpirationTime().toInstant();
            if (expiration.isBefore(Instant.now())) {
                System.err.println("JWT Validation Failed: Token Expired at " + expiration);
                return Optional.empty();
            }

            if (!JWT_ISSUER.equals(claimsSet.getIssuer())) {
                System.err.println("JWT Validation Failed: Invalid Issuer - Expected: " + JWT_ISSUER + ", Got: " + claimsSet.getIssuer());
                return Optional.empty();
            }


            return Optional.of(claimsSet);

        } catch (ParseException e) {
            System.err.println("JWT Validation Failed: Cannot parse token - " + e.getMessage());
            return Optional.empty();
        } catch (JOSEException e) {
            System.err.println("JWT Validation Failed: JOSE error - " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("JWT Validation Failed: Unexpected error - " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> getUserIdFromToken(String token) {
        return validateToken(token).map(JWTClaimsSet::getSubject);
    }

    public Optional<List<String>> getRolesFromToken(String token) {
        return validateToken(token).map(claims -> {
            try {
                return claims.getStringListClaim("groups");
            } catch (ParseException e) {
                System.err.println("Error parsing 'groups' claim: " + e.getMessage());
                return null;
            }
        });
    }
}
