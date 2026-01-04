package com.notifiction.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "notif_logs")
public class NotificationLogs {

    @Id
    private String id;

    private String notificationId; // Parent notification
    private String response;       // Gateway response
    private LocalDateTime loggedAt;
	
    // getters and setters
    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNotificationId() {
		return notificationId;
	}
	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public LocalDateTime getLoggedAt() {
		return loggedAt;
	}
	public void setLoggedAt(LocalDateTime loggedAt) {
		this.loggedAt = loggedAt;
	}

	/**
	 * Allows subclasses to veto equality in tests.
	 */
	protected boolean canEqual(Object other) {
		return other instanceof NotificationLogs;
	}
    
}
