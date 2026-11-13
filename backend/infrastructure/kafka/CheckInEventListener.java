package com.spata.backend.infrastructure.kafka;

import com.spata.backend.domain.checkin.CheckInRecordedEvent;
import com.spata.backend.domain.checkin.CheckInStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CheckInEventListener {

    @KafkaListener(topics = "spata.checkins", groupId = "spata-notification-group")
    public void handleCheckInEvent(CheckInRecordedEvent event) {
        System.out.println("==========================================");
        System.out.println(">>> KAFKA EVENT RECEIVED <<<");
        System.out.println("Personnel ID: " + event.personnelId());
        System.out.println("Status: " + event.status());
        System.out.println("Location: [" + event.latitude() + ", " + event.longitude() + "]");
        
        // Business Logic: Check if it's an emergency
        if (event.status() == CheckInStatus.EMERGENCY) {
            System.out.println("!!! EMERGENCY ALERT TRIGGERED !!!");
            System.out.println("Sending SMS to Security Operations Center...");
            // In a real app, you would call Twilio/SendGrid here
        }
        System.out.println("==========================================");
    }
}