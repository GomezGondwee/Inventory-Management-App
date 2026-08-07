# Implementation Plan - Grouped Reconciliation UI with Search

This plan outlines enhancing the Reconciliation screen to support grouped products (Glass and Plastics versions of the same product) and adding a search bar for better inventory management.

## Proposed Changes

### UI Layer

#### [MODIFY] [ReconciliationScreen.kt](file:///C:/Users/User/AndroidStudioProjects/InventoryManager/app/src/main/java/com/example/inventorymanager/ui/screens/ReconciliationScreen.kt)
- **Search Bar**:
    - Add a `searchQuery` state variable.
    - Implement an `OutlinedTextField` with a search icon at the top of the screen content.
- **Grouping Logic**:
    - Filter items based on `searchQuery`.
    - Group the filtered items by their `name` property using `.groupBy { it.name }`.
- **UI Structure**:
    - Update the `LazyColumn` to use `stickyHeader` (if supported) or simple header items for each product group.
    - Each group header will show the product name.
    - Under each header, list the variants (Glass or Plastics) as individual rows.
- **Enhanced Rows**:
    - Update `ReconciliationItemRow` to show the price clearly next to the input field.
    - Use the specific icon (bottle for Plastics, wine bar for Glass) next to the variant name.
- **Logic Sync**:
    - Ensure all calculations (sold units, revenue, session total) continue to work correctly across the grouped structure.

## Verification Plan

### Manual Verification
- Open the "Sales" tab.
- Verify the search bar filters products correctly.
- Verify that if multiple items have the same name (e.g., "Coke"), they are grouped together with separate inputs for "Glass" and "Plastics".
- Confirm prices are displayed on every row.
- Ensure the "Finish Reconciliation" button still accurately updates the global state.
