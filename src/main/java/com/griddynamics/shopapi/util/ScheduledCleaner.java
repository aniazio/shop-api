package com.griddynamics.shopapi.util;

import static com.griddynamics.shopapi.model.ResetToken.EXPIRATION_TIME_FOR_RESET_TOKENS;

import com.griddynamics.shopapi.repository.ResetTokenRepository;
import jakarta.transaction.Transactional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledCleaner {

  private final ResetTokenRepository tokenRepository;

  public ScheduledCleaner(ResetTokenRepository tokenRepository) {
    this.tokenRepository = tokenRepository;
  }

  @Scheduled(fixedRate = EXPIRATION_TIME_FOR_RESET_TOKENS + 1, timeUnit = TimeUnit.MINUTES)
  @Transactional
  public void deleteExpiredTokens() {
    tokenRepository.deleteExpired();
    log.info("Deletion of expired reset tokens performed");
  }

}
