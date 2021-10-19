package com.ccgautomation;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.event.DeviceEventListener;
import com.serotonin.bacnet4j.npdu.Network;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.service.Service;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.*;
import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.EventType;
import com.serotonin.bacnet4j.type.enumerated.MessagePriority;
import com.serotonin.bacnet4j.type.enumerated.NotifyType;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import java.util.Hashtable;

public class WhoIs {

    //static final Logger LOG = LoggerFactory.getLogger(ApplicationTest.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {

        LocalDevice localDevice = null;

        try {
            int localDeviceId = 1234;                       // -D
            int localPort = 47808;                          // -P
            int localNetworkNumber = 2500;                  // -N
            String localBindAddress = "192.168.168.153";    // -A
            String broadcastAddress = "192.168.168.255";
            int networkPrefixLength = 24;
            boolean reuseAddress = false;
            long timeout = 10000;

            for(String arg : args) {
                if (arg.contains("-D")) { localDeviceId = Integer.parseInt(arg.substring(2));}
                if (arg.contains("-P")) { localPort = Integer.parseInt(arg.substring(2)); }
                if (arg.contains("-N")) { localNetworkNumber = Integer.parseInt(arg.substring(2));}
                if (arg.contains("-A")) { localBindAddress = arg.substring(2); }
                if (arg.contains("-B")) { broadcastAddress = arg.substring(2); }
                if (arg.contains("-X")) { networkPrefixLength = Integer.parseInt(arg.substring(2)); }
                if (arg.contains("-T")) { timeout = Long.parseLong(arg.substring(2)); }
            }

            Hashtable<Long, String> ObjectIDs = new Hashtable<Long, String>();

            IpNetworkBuilder ipNetworkBuilder = new IpNetworkBuilder();

            ipNetworkBuilder.withLocalNetworkNumber(localNetworkNumber);
            ipNetworkBuilder.withPort(localPort);
            ipNetworkBuilder.withReuseAddress(reuseAddress);
            ipNetworkBuilder.withLocalBindAddress(localBindAddress);
            ipNetworkBuilder.withBroadcast(broadcastAddress, networkPrefixLength);

            Network network = ipNetworkBuilder.build();
            Transport transport = new DefaultTransport(network);

            localDevice = new LocalDevice(localDeviceId, transport);

            localDevice.initialize();


            // Register a listener that prints out the Device Infomation
            localDevice.getEventHandler().addListener(new DeviceEventListener() {
                @Override
                public void listenerException(Throwable throwable) {

                }

                @Override
                public void iAmReceived(RemoteDevice remoteDevice) {
                    //LOG.debug("I-AM Received: Adding {} to queue: {}", remoteDevice.toString(), localDevice.getRemoteDevices().size());

                    if (ObjectIDs.containsKey(new Long(remoteDevice.getInstanceNumber()))) {
                        System.out.print("DUPLICATE: ");
                    }
                    else
                    {
                        System.out.print("---------: ");
                        ObjectIDs.put(new Long(remoteDevice.getInstanceNumber()), remoteDevice.getAddress().toString());
                    }
                    System.out.println(remoteDevice.getInstanceNumber() + ","
                            + remoteDevice.getAddress().getNetworkNumber() + ","
                            + remoteDevice.getAddress().getMacAddress());
                }

                @Override
                public boolean allowPropertyWrite(Address address, BACnetObject baCnetObject, PropertyValue propertyValue) {
                    return false;
                }

                @Override
                public void propertyWritten(Address address, BACnetObject baCnetObject, PropertyValue propertyValue) {

                }

                @Override
                public void iHaveReceived(RemoteDevice remoteDevice, RemoteObject remoteObject) {

                }

                @Override
                public void covNotificationReceived(UnsignedInteger unsignedInteger, ObjectIdentifier objectIdentifier, ObjectIdentifier objectIdentifier1, UnsignedInteger unsignedInteger1, SequenceOf<PropertyValue> sequenceOf) {

                }

                @Override
                public void eventNotificationReceived(UnsignedInteger unsignedInteger, ObjectIdentifier objectIdentifier, ObjectIdentifier objectIdentifier1, TimeStamp timeStamp, UnsignedInteger unsignedInteger1, UnsignedInteger unsignedInteger2, EventType eventType, CharacterString characterString, NotifyType notifyType, Boolean aBoolean, EventState eventState, EventState eventState1, NotificationParameters notificationParameters) {

                }

                @Override
                public void textMessageReceived(ObjectIdentifier objectIdentifier, Choice choice, MessagePriority messagePriority, CharacterString characterString) {

                }

                @Override
                public void synchronizeTime(Address address, DateTime dateTime, boolean b) {

                }

                @Override
                public void requestReceived(Address address, Service service) {

                }
            });
            localDevice.sendGlobalBroadcast(new WhoIsRequest(0, 5000000));

            Thread.sleep(timeout);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            if (localDevice != null) localDevice.terminate();
        }

    }
}
