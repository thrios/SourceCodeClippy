package uk.ac.glasgow.scclippy.plugin.editor;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;

/**
 * Class for creating notifications
 */
public class Notification {

    private static final NotificationGroup notificationGroup =
            new NotificationGroup("My notification group", NotificationDisplayType.BALLOON, true);

    /**
     * Displays new notification
     * @param message the message of the notification
     */
    public static void createErrorNotification(String message) {
        createNotification(message, NotificationType.ERROR);
    }

    public static void createInfoNotification(String message) {
        createNotification(message, NotificationType.INFORMATION);
    }

    private static void createNotification(String message, NotificationType type) {
        ApplicationManager.getApplication().invokeLater(() -> {
            com.intellij.notification.Notification notification
                    = notificationGroup.createNotification(message, type);

            com.intellij.openapi.editor.Editor editor = Editor.getEditor();
            if (editor != null) {
                Notifications.Bus.notify(notification, editor.getProject());
            }
        });
    }
}
