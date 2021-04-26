package com.botocrypt.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.botocrypt.aggregator.processor.InitProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

@ExtendWith(MockitoExtension.class)
public class InitRunnerTest {

  @Mock
  private InitProcessor initProcessor;

  @InjectMocks
  private InitRunner initRunner;

  @Test
  void testRun() throws Exception {
    doNothing().when(initProcessor).initRepositoryWithNecessaryData();
    initRunner.run(mock(ApplicationArguments.class));
    verify(initProcessor).initRepositoryWithNecessaryData();
  }

  @Test
  void testRunWithException() {
    final String exceptionMessage = "Exception on initRepositoryWithNecessaryData method call.";
    doThrow(new RuntimeException(exceptionMessage)).when(initProcessor)
        .initRepositoryWithNecessaryData();
    Exception exception = assertThrows(RuntimeException.class,
        () -> initRunner.run(mock(ApplicationArguments.class)));
    assertEquals(exceptionMessage, exception.getMessage());
    verify(initProcessor).initRepositoryWithNecessaryData();
  }
}
