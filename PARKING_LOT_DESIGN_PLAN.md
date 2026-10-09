# Parking Lot System - High-Level Design Plan

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
- `color` (String)
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
- `ParkingTicket parkVehicle(Vehicle vehicle)` — Main entry point
- `void checkOut(String ticketId, PaymentMethod method)` — Main exit point
- `boolean isLotFull()` — Capacity check
- `List<ParkingSpot> getAvailableSpots()` — Query current availability

**Scope:**
- Traffic flow and entry/exit orchestration
- High-level business workflow coordinator

---

### 4.2 ParkingSpotManager / SpotAssignmentService
**Responsible for finding appropriate spots.**

**Responsibilities:**
- Determine if a spot matches vehicle and rules
- Sort and choose the best available spot
- Maintain availability state

**Key methods:**
- `ParkingSpot findAvailableSpot(Vehicle vehicle)` — Find best compatible spot
- `boolean isSpotCompatible(ParkingSpot spot, Vehicle vehicle)` — Validate compatibility
- `void reserveSpot(ParkingSpot spot, Vehicle vehicle)` — Mark spot occupied
- `void releaseSpot(ParkingSpot spot)` — Mark spot available

**Scope:**
- Allocation logic only
- No payment logic
- No ticket creation beyond assignment support

---

### 4.3 TicketService / ParkingSessionService
**Responsible for managing parking sessions.**

**Responsibilities:**
- Create tickets
- Update ticket status
- Track active sessions
- Link vehicle to allocated spot and times

**Key methods:**
- `ParkingTicket createTicket(Vehicle vehicle, ParkingSpot spot)` — Create new entry ticket
- `ParkingTicket getActiveTicketByVehicleId(String vehicleId)` — Fetch active session
- `ParkingTicket getTicketById(String ticketId)` — Retrieve ticket details
- `void updateTicketExitTime(String ticketId)` — Record exit time

**Scope:**
- Lifecycle of a parking session
- No direct charge calculation

---

### 4.4 PaymentService
**Handles payment computation and processing.**

**Responsibilities:**
- Calculate parking duration
- Fetch relevant pricing rules
- Compute total amount
- Process payment details
- Update payment status

**Key methods:**
- `BigDecimal calculateCharge(ParkingTicket ticket)` — Compute fee
- `PricingRule findApplicableRule(ParkingTicket ticket)` — Select applicable pricing rule
- `Payment processPayment(ParkingTicket ticket, PaymentMethod method)` — Execute payment
- `Invoice generateInvoice(ParkingTicket ticket)` — Create receipt

**Scope:**
- Pricing calculations
- Payment processing
- Checkout billing workflow

---

### 4.5 PricingRuleService / PricingPolicyService
**Admin-facing pricing logic.**

**Responsibilities:**
- Load active rules from configuration
- Evaluate rules based on spot type, duration, vehicle type, etc.
- Determine the final charge calculation strategy

**Key methods:**
- `List<PricingRule> getActiveRulesForLot(String lotId)` — Fetch all active rules
- `PricingRule findMatchingRule(Vehicle vehicle, ParkingSpot spot, Duration duration)` — Select best rule
- `boolean validateRule(PricingRule rule)` — Check rule validity
- `void activateRule(String ruleId)` — Enable rule
- `void deactivateRule(String ruleId)` — Disable rule

**Scope:**
- Rule selection and evaluation
- Admin-controlled business policy

---

### 4.6 CheckoutService
**Responsible for final vehicle exit workflow.**

**Responsibilities:**
- Finalize ticket
- Trigger payment calculation
- Complete payment
- Free parking spot
- Create history entries

**Key methods:**
- `void checkoutVehicle(String ticketId, PaymentMethod method)` — Complete exit
- `void closeSession(ParkingTicket ticket)` — Mark ticket as closed
- `void releaseSpotForTicket(ParkingTicket ticket)` — Free up spot

**Scope:**
- Orchestrates exit and settlement
- Works as a coordinator service, not a primitive business rule calculator

---

### 4.7 AdminConfigurationService
**Responsible for admin operations.**

**Responsibilities:**
- Create/update pricing rules
- Enable/disable pricing configuration
- Set default currency and policies for a parking lot
- Manage rule versioning or activation windows

**Key methods:**
- `PricingRule createRule(PricingRule rule)` — Add new rule
- `PricingRule updateRule(String ruleId, PricingRule updates)` — Modify rule
- `void activateRule(String ruleId)` — Enable rule
- `void deactivateRule(String ruleId)` — Disable rule
- `AdminConfiguration getConfigForLot(String lotId)` — Fetch config

**Scope:**
- Admin lifecycle management
- Ensures all payment logic remains configurable externally

---

## 5. Recommended Layered Structure

```
parkinglot-system/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── parkinglot/
│   │               ├── config/
│   │               │   └── AppConfig.java
│   │               ├── controller/
│   │               │   ├── ParkingController.java
│   │               │   └── AdminController.java
│   │               ├── dto/
│   │               │   ├── ParkingRequestDTO.java
│   │               │   ├── CheckoutRequestDTO.java
│   │               │   └── PricingRuleDTO.java
│   │               ├── enums/
│   │               │   ├── SpotType.java
│   │               │   ├── VehicleType.java
│   │               │   ├── PaymentStatus.java
│   │               │   ├── PaymentMethod.java
│   │               │   └── TicketStatus.java
│   │               ├── exception/
│   │               │   ├── ParkingLotException.java
│   │               │   ├── NoSpotAvailableException.java
│   │               │   └── InvalidPaymentException.java
│   │               ├── model/
│   │               │   ├── ParkingLot.java
│   │               │   ├── ParkingFloor.java
│   │               │   ├── ParkingSpot.java
│   │               │   ├── Vehicle.java
│   │               │   ├── ParkingTicket.java
│   │               │   ├── Payment.java
│   │               │   ├── PricingRule.java
│   │               │   ├── AdminConfiguration.java
│   │               │   └── Invoice.java
│   │               ├── repository/
│   │               │   ├── ParkingLotRepository.java
│   │               │   ├── ParkingSpotRepository.java
│   │               │   ├── VehicleRepository.java
│   │               │   ├── ParkingTicketRepository.java
│   │               │   ├── PaymentRepository.java
│   │               │   ├── PricingRuleRepository.java
│   │               │   └── AdminConfigRepository.java
│   │               ├── service/
│   │               │   ├── ParkingLotService.java
│   │               │   ├── ParkingSpotManager.java
│   │               │   ├── TicketService.java
│   │               │   ├── PaymentService.java
│   │               │   ├── PricingRuleService.java
│   │               │   ├── CheckoutService.java
│   │               │   └── AdminConfigurationService.java
│   │               └── util/
│   │                   ├── DateUtil.java
│   │                   └── ValidationUtil.java
│   └── test/
│       └── java/
│           └── com/
│               └── parkinglot/
│                   ├── service/
│                   │   ├── ParkingLotServiceTest.java
│                   │   ├── PaymentServiceTest.java
│                   │   ├── PricingRuleServiceTest.java
│                   │   └── CheckoutServiceTest.java
│                   └── model/
│                       └── [Model unit tests]
├── pom.xml
└── README.md
```

---

## 6. Relationship Summary

```
ParkingLot 1 ──→ many ─── ParkingFloor
ParkingFloor 1 ──→ many ─── ParkingSpot
Vehicle 1 ──→ many ─── ParkingTicket
ParkingSpot 1 ──→ many ─── ParkingTicket (historically)
                           (only 1 active at a time)
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
✅ Clear domain model with 8 main entity classes  
✅ 7 service classes with well-defined responsibilities  
✅ Folder structure following Java/Maven conventions  
✅ Support for admin-configurable pricing rules  
✅ Extensible architecture for future enhancements  
✅ Scalable data flow from entry to exit  

Ready for Step 1 review and your feedback.
