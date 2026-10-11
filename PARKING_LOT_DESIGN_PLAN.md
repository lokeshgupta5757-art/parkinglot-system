# Parking Lot System - High-Level Design Plan

## Current Status
- Step 1 (High-level design): Completed
- Model layer: Completed
- Business logic layer: Completed
- Current state: All business logic classes have been created in one main service file, and each nested class now includes detailed description comments and responsibilities.
- Next expected phase: Review before creating test classes and logic enhancements.

## 1. Objective
Build a Java-based parking lot system that supports:
- Vehicle entry and parking spot allocation
- Multiple parking spot types (electric, compact, etc.)
- Checkout based on parking duration
- Payment calculation using admin-managed pricing rules
- Extensibility for future features

This document covers the system design and current project status.

---

## 2. Current Implementation Snapshot
The project currently includes:
- Model layer in `src/main/java/com/parkinglot/model/ParkingLotModel.java`
- Business logic layer in `src/main/java/com/parkinglot/service/ParkingLotService.java`
- Detailed description comments added to each nested business logic class
- Domain types, enums, and state models included in the main model class

---

## 3. Core Business Rules
1. A vehicle can request parking when it arrives.
2. If a compatible spot is available, assign the most suitable parking spot.
3. Spot selection depends on vehicle type and spot type compatibility.
4. A vehicle can be parked only in an available spot.
5. Payment is charged only at checkout based on actual duration spent in the lot.
6. Pricing and charge rules are configurable and maintained by admin users.
7. A spot is released when the vehicle exits and payment is finalized.

---

## 4. Main Business Logic Classes
- ParkingLotOperationService
- ParkingSpotManager
- TicketService
- PaymentService
- PricingRuleService
- CheckoutService
- AdminConfigurationService
- DateUtilService
- ValidationUtilService
- exception classes for parking lot and payment failures

Each class has been documented with responsibilities and purpose comments in the service file.

---

## 5. Next Step
After review, the next stage will be:
- Step 5: test classes and business logic validation

---

## 6. Summary
The project is progressing as planned:
- design completed
- model classes completed
- business logic classes completed with descriptions
- next focus: test classes and validation logic
