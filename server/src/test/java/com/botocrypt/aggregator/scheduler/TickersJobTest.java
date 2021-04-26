package com.botocrypt.aggregator.scheduler;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.processor.CryptoProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;

@ExtendWith(MockitoExtension.class)
public class TickersJobTest {

  @Mock
  private CryptoProcessor cryptoProcessor;

  @InjectMocks
  private TickersJob tickersJob;

  @Test
  void testExecuteInternal() throws JobExecutionException {
    final JobExecutionContext context = mock(JobExecutionContext.class);

    doReturn(mock(Scheduler.class)).when(context).getScheduler();
    doNothing().when(cryptoProcessor).processCryptoFromExchanges();
    tickersJob.execute(context);
    verify(cryptoProcessor).processCryptoFromExchanges();
  }
}
