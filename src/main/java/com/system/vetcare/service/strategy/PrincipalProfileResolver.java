package com.system.vetcare.service.strategy;

import static java.util.stream.Collectors.toMap;
import static java.util.function.Function.identity;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.system.vetcare.domain.UserPrincipal;
import com.system.vetcare.domain.User;
import com.system.vetcare.domain.enums.EAuthority;
import com.system.vetcare.payload.response.PrincipalProfile;

@Service
public class PrincipalProfileResolver {

    private final Map<EAuthority, PrincipalProfileResolverStrategy> strategies;

    public PrincipalProfileResolver(List<PrincipalProfileResolverStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(toMap(PrincipalProfileResolverStrategy::getSupportedAuthority, identity()));
    }

    public List<PrincipalProfile> resolvePrincipalProfiles(UserPrincipal principalDetails) {
        return principalDetails.authorityNames().stream().map(authorityName -> {
            PrincipalProfileResolverStrategy strategy = strategies.get(EAuthority.valueOf(authorityName));
            return strategy.resolveUserProfileDetails(principalDetails.id());
        }).toList();
    }

    public void saveUserProfiles(User user) {
        user.getAuthorities().forEach(authority -> {
            PrincipalProfileResolverStrategy strategy = strategies.get(authority.getTitle());
            strategy.saveProfileForUser(user);
        });
    }

}