package com.system.vetcare.service;

import java.util.List;
import java.util.Set;
import com.system.vetcare.domain.Authority;
import com.system.vetcare.payload.response.AuthorityDetailsResponse;

public interface AuthorityService {

    List<AuthorityDetailsResponse> findAll();

    Set<Authority> findAllById(List<Integer> authorityIds);

}