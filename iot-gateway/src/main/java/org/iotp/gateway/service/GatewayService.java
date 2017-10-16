package org.iotp.gateway.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.iotp.gateway.service.data.AttributeRequest;
import org.iotp.gateway.service.data.AttributeResponse;
import org.iotp.gateway.service.data.AttributesUpdateSubscription;
import org.iotp.gateway.service.data.RpcCommandResponse;
import org.iotp.gateway.service.data.RpcCommandSubscription;
import org.iotp.infomgt.data.kv.KvEntry;
import org.iotp.infomgt.data.kv.TsKvEntry;

/**
 */
public interface GatewayService {

  /**
   * Inform gateway service that device is connected
   * 
   * @param deviceName
   */
  MqttDeliveryFuture onDeviceConnect(String deviceName);

  /**
   * Inform gateway service that device is disconnected
   * 
   * @param deviceName
   */
  Optional<MqttDeliveryFuture> onDeviceDisconnect(String deviceName);

  /**
   * Report device attributes change to Thingsboard
   * 
   * @param deviceName
   *          - the device name
   * @param attributes
   *          - the attribute values list
   */
  MqttDeliveryFuture onDeviceAttributesUpdate(String deviceName, List<KvEntry> attributes);

  /**
   * Report device telemetry to Thingsboard
   * 
   * @param deviceName
   *          - the device name
   * @param telemetry
   *          - the telemetry values list
   */
  MqttDeliveryFuture onDeviceTelemetry(String deviceName, List<TsKvEntry> telemetry);

  /**
   * Report attributes request to Thingsboard
   * 
   * @param attributeRequest
   *          - attributes request
   * @param listener
   *          - attributes response
   */
  void onDeviceAttributeRequest(AttributeRequest attributeRequest, Consumer<AttributeResponse> listener);

  /**
   * Report response from device to the server-side RPC call from Thingsboard
   * 
   * @param response
   *          - the device response to RPC call
   */
  void onDeviceRpcResponse(RpcCommandResponse response);

  /**
   * Subscribe to attribute updates from Thingsboard
   * 
   * @param subscription
   *          - the subscription
   * @return true if successful, false if already subscribed
   *
   */
  boolean subscribe(AttributesUpdateSubscription subscription);

  /**
   * Subscribe to server-side rpc commands from Thingsboard
   * 
   * @param subscription
   *          - the subscription
   * @return true if successful, false if already subscribed
   */
  boolean subscribe(RpcCommandSubscription subscription);

  /**
   * Unsubscribe to attribute updates from Thingsboard
   * 
   * @param subscription
   *          - the subscription
   * @return true if successful, false if already unsubscribed
   */
  boolean unsubscribe(AttributesUpdateSubscription subscription);

  /**
   * Unsubscribe to server-side rpc commands from Thingsboard
   * 
   * @param subscription
   *          - the subscription
   * @return true if successful, false if already unsubscribed
   */
  boolean unsubscribe(RpcCommandSubscription subscription);

  /**
   * Report generic error from one of gateway components
   * 
   * @param e
   *          - the error
   */
  void onError(Exception e);

  /**
   * Report error related to device
   * 
   * @param deviceName
   *          - the device name
   * @param e
   *          - the error
   */
  void onError(String deviceName, Exception e);

}
