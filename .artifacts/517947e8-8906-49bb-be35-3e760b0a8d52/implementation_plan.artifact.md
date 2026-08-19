# Implementation Plan - Delivery/Restock Screen

Create a new screen to handle incoming deliveries from trucks, allowing users to increment the stock of existing products.

## User Review Required

> [!IMPORTANT]
> The bottom navigation bar currently has 5 items (Home, Sales, Post, Inventory, Reports), which is the standard limit for Material Design. I propose adding "Delivery" as a new screen and either:
> 1.  Adding a "Receive Delivery" button on the **Dashboard** or **Inventory** screen.
> 2.  Adding it to the **Bottom Navigation** (which might make it feel cramped).
>
> I will start by implementing it as a reachable screen via the **Navigation Host** and adding a "Receive Delivery" button to the **Dashboard** for easy access.

## Proposed Changes

### UI Components

#### [NEW] [DeliveryScreen.kt](file:///C:/Users/User/AndroidStudioProjects/InventoryManager/app/src/main/java/com/example/inventorymanager/ui/screens/DeliveryScreen.kt)
- **Header**: "Receive Delivery".
- **Search Bar**: To quickly find products to restock.
- **Product List**: Each row will show current stock and an input field for "Received Quantity".
- **Bottom Summary**: Shows total items being added and a "Confirm Delivery" button.

#### [MODIFY] [Navigation.kt](file:///C:/Users/User/AndroidStudioProjects/InventoryManager/app/src/main/java/com/example/inventorymanager/ui/screens/Navigation.kt)
- Add `Delivery` route to `Screen` sealed class.
- Update `AppNavHost` to include the `DeliveryScreen` destination.
- Expose `onReceiveDelivery` callback through the navigation container.

#### [MODIFY] [DashBoard.kt](file:///C:/Users/User/AndroidStudioProjects/InventoryManager/app/src/main/java/com/example/inventorymanager/ui/screens/DashBoard.kt)
- Add a new "Receive Delivery" action button (likely in the top bar or as a floating action/card).

### Business Logic

#### [MODIFY] [MainActivity.kt](file:///C:/Users/User/AndroidStudioProjects/InventoryManager/app/src/main/java/com/example/inventorymanager/MainActivity.kt)
- Implement `onReceiveDelivery(Map<Int, Int>)` logic:
  - For each ID in the map, find the item in `sampleItems` and update its `quantity`: `item.quantity + receivedValue`.
  - Update `stockCount` total.

## Verification Plan

### Manual Verification
- Navigate to the new Delivery Screen.
- Search for a product (e.g., "Coke").
- Enter a received quantity (e.g., 50).
- Click "Confirm Delivery".
- Verify that the Inventory list and Dashboard show the updated stock count.
