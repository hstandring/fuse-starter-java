
package org.galatea.starter.utils.jms;

import static org.galatea.starter.utils.Tracer.addTraceInfo;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.galatea.starter.utils.FuseTraceRepository;
import org.galatea.starter.utils.Tracer.AutoClosedTrace;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;


@RequiredArgsConstructor
@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FuseMessageListenerContainer extends DefaultMessageListenerContainer {

  @NonNull
  protected final FuseTraceRepository repository;


  @Override
  @SneakyThrows
  protected void invokeListener(final Session session, final Message message) throws JMSException {

    try (AutoClosedTrace t = new AutoClosedTrace(repository, this.getClass())) {

      addMessageInfoToTracer(message);

      t.runAndTraceSuccess("message", () -> {
        super.invokeListener(session, message);
        return Void.TYPE;
      });
    }

  }

  protected void addMessageInfoToTracer(final Message msg) {
    String dest = "UNKNOWN";
    String text = "UNKNOWN";
    String msgId = "UNKNOWN";
    long msgTs = -1;
    try {
      dest = msg.getJMSDestination().toString();
      msgId = msg.getJMSMessageID();
      msgTs = msg.getJMSTimestamp();

      if (msg instanceof TextMessage) {
        text = ((TextMessage) msg).getText();
      }
    } catch (JMSException jmse) {
      log.error("Could not extract useful info from message.  Using unknown for tracer", jmse);
    }

    addTraceInfo(this.getClass(), "jms-destination", dest);
    addTraceInfo(this.getClass(), "jms-messageId", msgId);
    addTraceInfo(this.getClass(), "jms-messageTs", msgTs);
    addTraceInfo(this.getClass(), "jms-payload", text);


  }
}
