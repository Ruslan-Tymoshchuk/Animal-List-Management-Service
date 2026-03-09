package com.system.vetcare.service.strategy;

import static com.system.vetcare.domain.enums.EAuthority.VETERINARIAN;
import org.springframework.stereotype.Service;
import com.system.vetcare.domain.Staff;
import com.system.vetcare.domain.User;
import com.system.vetcare.domain.Veterinarian;
import com.system.vetcare.domain.enums.EAuthority;
import com.system.vetcare.payload.response.PrincipalProfile;
import com.system.vetcare.service.VeterinarianService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VeterinarianStrategyResolver implements PrincipalProfileResolverStrategy {

    private final VeterinarianService veterinarianService;

    @Override
    public EAuthority getSupportedAuthority() {
        return VETERINARIAN;
    }

    @Override
    public PrincipalProfile resolveUserProfileDetails(Integer userId) {
        Veterinarian veterinarian = veterinarianService.findByUserId(userId);
        return new PrincipalProfile(veterinarian.getId(), getSupportedAuthority().name());
    }

    @Override
    public void saveProfileForUser(User user) {
        veterinarianService.save(Veterinarian
                                   .builder()
                                   .staff(Staff
                                           .builder()
                                           .user(user)
                                           .build())
                                   .build());
    }

}