
package org.galatea.starter.entrypoint;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.galatea.starter.domain.TradeAgreement;
import org.galatea.starter.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;


@RequiredArgsConstructor
@Slf4j
@Component
public class SettlementJmsListener {
  @NonNull
  @Autowired
  protected SettlementService settlementService;

  @JmsListener(destination = "${jms.agreement-queue}", concurrency = "${jms.listener-concurrency}")
  public void settleAgreement(final TradeAgreement agreements) {
    log.info("Handling agreements {}", agreements);
    Set<Long> missionIds = settlementService.spawnMissions(Arrays.asList(agreements));
    log.info("Created missions {}", missionIds);
  }

}
