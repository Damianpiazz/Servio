package com.servio.backend.identity.application.port.in.usecase;

import com.servio.backend.identity.domain.UserLogo;

import java.util.List;

public interface GetUserLogosUseCase {
    List<UserLogo> getHistory(Integer userId);
    UserLogo getActive(Integer userId);
}