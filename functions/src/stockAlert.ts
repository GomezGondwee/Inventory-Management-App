import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";

admin.initializeApp();

export const checkStockAlert = onDocumentUpdated(
  "inventory/{productId}",
  async (event) => {
    const newValue = event.data?.after.data();
    const oldValue = event.data?.before.data();

    if (!newValue) return;

    const isNowLow = newValue.quantity < 20;
    const wasAlreadyLow = oldValue && oldValue.quantity < 20;

    if (isNowLow && !wasAlreadyLow) {
      const productName = newValue.name || "Unknown Product";

      const message = {
        notification: {
          title: "Low Stock Alert! ⚠️",
          body: `"${productName}" is running low. ` +
            `Only ${newValue.quantity} left!`,
        },
        android: {
          notification: {
            channelId: "stock_alerts_channel",
          },
        },
        topic: "stock_alerts",
      };

      try {
        await admin.messaging().send(message);
        console.log(`Successfully sent stock alert for ${productName}`);
      } catch (error) {
        console.error("Error sending FCM notification:", error);
      }
    }
  });
