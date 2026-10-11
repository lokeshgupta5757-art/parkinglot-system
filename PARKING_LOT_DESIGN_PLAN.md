# Parking Lot System - High-Level Design Plan

## Current Status
- Step 1 (High-level design) : Completed
- Step 3 (Model classes only) : Completed
- Current project state: Model classes have been created in `src/main/java/com/parkinglot/model/ParkingLotModel.java` using a single main model class with nested static classes and enums.
- Next expected phase: Step 2/Review of model layer, then Step 5 logic implementation and tests.

## 1. Objective
Build a Java-based parking lot system that supports:
- Vehicle entry and parking spot allocation
- Multiple parking spot types (electric, compact, etc.)
- Checkout based on parking duration
- Payment calculation using admin-managed pricing rules
- Extensibility for future features

This document covers **Step 1 only**: the domain model, business logic classes, layer boundaries, folder structure, and responsibilities.

---

## 2. Core Business Rules
1. A vehicle can request parking when it arrives.
2. If a compatible spot is available, assign the most suitable parking spot.
3. Spot selection depends on vehicle type and spot type compatibility.
4. A vehicle can be parked only in an available spot.
5. Payment is charged only at checkout based on actual duration spent in the lot.
6. Pricing and charge rules are configurable and maintained by admin users.
7. A spot is released when the vehicle exits and payment is finalized.

---

## 3. High-Level Domain Model

### 3.1 ParkingLot
Represents the whole parking facility.

**Responsibilities:**
- Holds parking floors/zones
- Manages total capacity and current occupancy
- Exposes operations for entry and exit
- Coordinates services for assignment and checkout

**Key fields:**
- `id` (UUID)
- `name` (String)
- `address` (String)
- `parkingFloors` (List<ParkingFloor>)
- `status` (ACTIVE, CLOSED, MAINTENANCE)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**Relationships:**
- One parking lot contains many parking floors
- One parking lot contains many parking spots

---

### 3.2 ParkingFloor
Represents a floor or zone inside the parking lot.

**Responsibilities:**
- Organizes parking spots by floor/zone
- Helps in spot lookup and assignment

**Key fields:**
- `id` (UUID)
- `floorNumber` (Integer)
- `parkingLotId` (UUID)
- `parkingSpots` (List<ParkingSpot>)
- `status` (ACTIVE, CLOSED)

**Relationships:**
- Many parking spots belong to one floor
- One floor belongs to one parking lot

---

### 3.3 ParkingSpot
Base model for all types of parking spaces.

**Responsibilities:**
- Stores spot identity and availability
- Defines common behavior across all spots
- Supports assignment and release logic

**Key fields:**
- `id` (UUID)
- `spotNumber` (String)
- `floorId` (UUID)
- `spotType` (SpotType enum: COMPACT, ELECTRIC, HANDICAP, LARGE, MOTORCYCLE)
- `status` (AVAILABLE, OCCUPIED, OUT_OF_SERVICE)
- `isOccupied` (boolean)
- `assignedVehicleId` (UUID, nullable)
- `createdAt` (LocalDateTime)

**Relationships:**
- One spot belongs to one floor
- One spot may be assigned to at most one vehicle at a time

**Note:** Start with a single `ParkingSpot` class with `spotType` enum. Derive subclasses only if behavior significantly differs later.

---

### 3.4 Vehicle
Represents a motor vehicle entering the lot.

**Responsibilities:**
- Stores vehicle identity and type
- Identifies compatibility with parking spots

**Key fields:**
- `id` (UUID)
- `licensePlate` (String, unique)
- `vehicleType` (VehicleType enum: CAR, MOTORCYCLE, TRUCK, ELECTRIC_CAR)
- `color` (VehicleColor enum)
- `ownerName` (String, optional)
- `createdAt` (LocalDateTime)

**Relationships:**
- A vehicle can have zero or one active ticket
- A vehicle may have many historical tickets over time

---

### 3.5 ParkingTicket / ParkingSession
Represents the time period when a vehicle is parked.

**Responsibilities:**
- Tracks entry and exit times
- Links vehicle and assigned spot
- Stores ticket state
- Acts as the source for fee calculation

**Key fields:**
- `id` (UUID)
- `ticketNumber` (String, unique)
- `vehicleId` (UUID)
- `spotId` (UUID)
- `entryDateTime` (LocalDateTime)
- `exitDateTime` (LocalDateTime, nullable until checkout)
- `status` (TicketStatus enum: ACTIVE, CHECKED_OUT, CANCELLED, PAID)
- `amountCharged` (BigDecimal, nullable)
- `paymentStatus` (PaymentStatus enum: PENDING, SUCCESS, FAILED)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**Relationships:**
- Many tickets belong to one vehicle
- One ticket is tied to one parking spot
- One ticket may generate one payment record

---

### 3.6 Payment
Represents payment for a completed parking session.

**Responsibilities:**
- Hold final computed amount
- Capture payment method and time
- Track whether payment is successful

**Key fields:**
- `id` (UUID)
- `ticketId` (UUID)
- `amount` (BigDecimal)
- `currency` (String, default "USD")
- `paymentMethod` (PaymentMethod enum: CREDIT_CARD, DEBIT_CARD, CASH, DIGITAL_WALLET)
- `paymentStatus` (PaymentStatus enum: PENDING, SUCCESS, FAILED, REFUNDED)
- `paidAt` (LocalDateTime, nullable)
- `createdAt` (LocalDateTime)

**Relationships:**
- Each ticket may lead to one payment

---

### 3.7 PricingRule / RatePlan
Admin-configurable payment logic.

**Responsibilities:**
- Determine the cost for parking based on time blocks and spot types
- Support flexible pricing policies
- Allow admin updates without changing application code

**Key fields:**
- `id` (UUID)
- `name` (String)
- `spotType` (SpotType enum, nullable for all spot types)
- `vehicleType` (VehicleType enum, nullable for all vehicle types)
- `hourlyRate` (BigDecimal)
- `dailyRate` (BigDecimal, nullable)
- `maxDailyRate` (BigDecimal, nullable)
- `flatFee` (BigDecimal, nullable)
- `minimumDuration` (Integer in minutes, nullable)
- `effectiveFrom` (LocalDateTime)
- `effectiveTo` (LocalDateTime, nullable)
- `isActive` (boolean)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**Possible business rules:**
- Hourly charge for standard parking
- Different rates for compact vs electric vs large spots
- Reduced rates for certain vehicle types
- Daily max cap
- Minimum parking duration charge

**Relationships:**
- Parking lot may use many pricing rules
- A ticket is charged using applicable active pricing rules

---

### 3.8 AdminConfiguration / PricingConfiguration
Represents the admin-controlled configuration.

**Responsibilities:**
- Centralize backend configuration for rates, discounts, and rule versions
- Allow rule activation/deactivation and updates

**Key fields:**
- `id` (UUID)
- `parkingLotId` (UUID)
- `defaultCurrency` (String, default "USD")
- `timezone` (String, default "UTC")
- `defaultRatePlanId` (UUID, nullable)
- `isEnabled` (boolean)
- `updatedBy` (String, username of admin)
- `updatedAt` (LocalDateTime)

**Relationships:**
- One parking lot has one admin configuration

---

## 4. Business Logic / Service Classes

### 4.1 ParkingLotService
**Main service for parking operations.**

**Responsibilities:**
- Handle vehicle entry requests
- Find compatible spot
- Create active ticket
- Mark spot as occupied
- Validate parking capacity

**Key methods:**
- `ParkingTicket parkVehicle(Vehicle vehicle)`
- `void checkOut(String ticketId, PaymentMethod method)`
- `boolean isLotFull()`
- `List<ParkingSpot> getAvailableSpots()`

**Scope:**
- Traffic flow and entry/exit orchestration
- High-level business workflow coordinator

---

## 5. Recommended Layered Structure

```text
parkinglot-system/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── parkinglot/
│   │               ├── config/
│   │               ├── controller/
│   │               ├── dto/
│   │               ├── enums/
│   │               ├── exception/
│   │               ├── model/
│   │               │   └── ParkingLotModel.java
│   │               ├── repository/
│   │               ├── service/
│   │               └── util/
│   └── test/
│       └── java/
│           └── com/
│               └── parkinglot/
├── pom.xml
└── README.md
```

---

## 6. Relationship Summary

```text
ParkingLot 1 ──→ many ─── ParkingFloor
ParkingFloor 1 ──→ many ─── ParkingSpot
Vehicle 1 ──→ many ─── ParkingTicket
ParkingSpot 1 ──→ many ─── ParkingTicket (historically)
ParkingTicket 1 ──→ 1 ─── Payment
ParkingLot 1 ──→ many ─── PricingRule
ParkingLot 1 ──→ 1 ─── AdminConfiguration
```

---

## 7. Data Flow

### Entry Flow
1. Vehicle arrives at entry gate.
2. `ParkingLotService.parkVehicle(Vehicle)` receives entry request.
3. `ParkingSpotManager.findAvailableSpot(Vehicle)` finds compatible spot.
4. `TicketService.createTicket(Vehicle, ParkingSpot)` creates entry ticket.
5. `ParkingSpotManager.reserveSpot()` marks spot as occupied.
6. Ticket with entry time returned to vehicle owner.

### Exit / Checkout Flow
1. Vehicle reaches exit gate with ticket ID.
2. `CheckoutService.checkoutVehicle(ticketId, paymentMethod)` processes exit.
3. `TicketService.getTicketById()` fetches active ticket.
4. `PaymentService.calculateCharge(ticket)` computes parking duration and applicable rule.
5. `PricingRuleService.findApplicableRule()` selects pricing rule.
6. `PaymentService.processPayment()` executes payment.
7. `TicketService.updateTicketExitTime()` records exit time and updates status.
8. `ParkingSpotManager.releaseSpot()` marks spot available.
9. `PaymentService.generateInvoice()` creates receipt.

---

## 8. Design Decisions for Step 1

### Recommended Approach
- Keep model classes simple and focused on state
- Put all business logic into service classes
- Make pricing admin-configurable via dedicated rule classes
- Use `ParkingLotService` as the main orchestrator
- Use enums for static types like spot types and vehicle types
- Keep repository layer for data persistence (even if in-memory initially)

### Why This Is a Good Fit
- Easy to implement in Java
- Easy to test each service in isolation
- Supports requirement that admin can configure payment rules without code changes
- Clear separation of concerns
- Scalable for future additions (reservations, reports, etc.)

---

## 9. Key Design Principles

1. **Single Responsibility**: Each service handles one business function.
2. **No Circular Dependencies**: Services depend on lower layers (models, repositories), not on each other.
3. **Configurable Pricing**: All payment rules live in `PricingRule` and are managed by `AdminConfigurationService`.
4. **Loose Coupling**: Services communicate through well-defined method signatures, not direct object manipulation.
5. **Extensibility**: Easy to add new spot types, vehicle types, or payment methods via enums.

---

## 10. Scope for Next Steps

This plan sets the foundation for the next phases:
- **Step 2**: User review and approval of Step 1
- **Step 3**: Create actual Java model classes with empty service class stubs
- **Step 4**: User review of Step 3
- **Step 5**: Implement business logic and create test classes
- **Step 6**: Final review and refinement

---

## 11. Summary

This design plan provides:
✅ Clear domain model with main entity classes
✅ Structured folder layout for Java/Maven
✅ Support for admin-configurable pricing rules
✅ Extensible architecture for future enhancements
✅ Scalable data flow from entry to exit

Ready for the next review step.
