package org.iotp.gateway.service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

/**
 */
public class MqttDeliveryFuture implements Future<Void> {

  private final IMqttDeliveryToken token;
  private final Exception e;

  public MqttDeliveryFuture(IMqttDeliveryToken token) {
    this.token = token;
    this.e = null;
  }

  public MqttDeliveryFuture(Exception e) {
    this.token = null;
    this.e = e;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCancelled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDone() {
    return e != null || token.isComplete();
  }

  @Override
  public Void get() throws InterruptedException, ExecutionException {
    return get(Optional.empty());
  }

  @Override
  public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return get(Optional.of(unit.toMillis(timeout)));
  }

  private Void get(Optional<Long> duration) throws ExecutionException {
    try {
      if (e != null) {
        throw e;
      } else if (duration.isPresent()) {
        token.waitForCompletion(duration.get());
      } else {
        token.waitForCompletion();
      }
      return null;
    } catch (Exception e) {
      throw new ExecutionException(e);
    }
  }
}
