package com.parkinglot.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Main domain model class containing all parking lot system entities.
 * This is organized as inner classes for simplicity during initial development.
 */
public class ParkingLotModel {

    /**
     * Enum for vehicle colors.
     */
    public enum VehicleColor {
        WHITE("White"),
        BLACK("Black"),
        SILVER("Silver"),
        GRAY("Gray"),
        RED("Red"),
        BLUE("Blue"),
        GREEN("Green"),
        YELLOW("Yellow"),
        ORANGE("Orange"),
        BROWN("Brown"),
        GOLD("Gold"),
        OTHER("Other");

        private final String displayName;

        VehicleColor(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for vehicle types.
     */
    public enum VehicleType {
        CAR("Car"),
        MOTORCYCLE("Motorcycle"),
        TRUCK("Truck"),
        ELECTRIC_CAR("Electric Car"),
        SUV("SUV");

        private final String displayName;

        VehicleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for parking spot types.
     */
    public enum SpotType {
        COMPACT("Compact", 1),
        ELECTRIC("Electric", 2),
        HANDICAP("Handicap", 3),
        LARGE("Large", 4),
        MOTORCYCLE("Motorcycle", 5);

        private final String displayName;
        private final int priority;

        SpotType(String displayName, int priority) {
            this.displayName = displayName;
            this.priority = priority;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * Enum for parking spot status.
     */
    public enum SpotStatus {
        AVAILABLE("Available"),
        OCCUPIED("Occupied"),
        OUT_OF_SERVICE("Out of Service"),
        MAINTENANCE("Maintenance");

        private final String displayName;

        SpotStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for parking lot status.
     */
    public enum ParkingLotStatus {
        ACTIVE("Active"),
        CLOSED("Closed"),
        MAINTENANCE("Maintenance"),
        FULL("Full");

        private final String displayName;

        ParkingLotStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for parking floor status.
     */
    public enum FloorStatus {
        ACTIVE("Active"),
        CLOSED("Closed"),
        MAINTENANCE("Maintenance");

        private final String displayName;

        FloorStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for parking ticket status.
     */
    public enum TicketStatus {
        ACTIVE("Active"),
        CHECKED_OUT("Checked Out"),
        CANCELLED("Cancelled"),
        PAID("Paid");

        private final String displayName;

        TicketStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for payment status.
     */
    public enum PaymentStatus {
        PENDING("Pending"),
        SUCCESS("Success"),
        FAILED("Failed"),
        REFUNDED("Refunded");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Enum for payment methods.
     */
    public enum PaymentMethod {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        CASH("Cash"),
        DIGITAL_WALLET("Digital Wallet"),
        UPI("UPI");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Entity: ParkingLot.
     */
    public static class ParkingLot {
        private String id;
        private String name;
        private String address;
        private ParkingLotStatus status;
        private int totalSpots;
        private int availableSpots;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ParkingLot() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.status = ParkingLotStatus.ACTIVE;
        }

        public ParkingLot(String name, String address, int totalSpots) {
            this();
            this.name = name;
            this.address = address;
            this.totalSpots = totalSpots;
            this.availableSpots = totalSpots;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public ParkingLotStatus getStatus() { return status; }
        public void setStatus(ParkingLotStatus status) { this.status = status; this.updatedAt = LocalDateTime.now(); }
        public int getTotalSpots() { return totalSpots; }
        public void setTotalSpots(int totalSpots) { this.totalSpots = totalSpots; }
        public int getAvailableSpots() { return availableSpots; }
        public void setAvailableSpots(int availableSpots) { this.availableSpots = availableSpots; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "ParkingLot{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", status=" + status +
                    ", totalSpots=" + totalSpots +
                    ", availableSpots=" + availableSpots +
                    '}';
        }
    }

    /**
     * Entity: ParkingFloor.
     */
    public static class ParkingFloor {
        private String id;
        private int floorNumber;
        private String parkingLotId;
        private FloorStatus status;
        private int totalSpots;
        private int availableSpots;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ParkingFloor() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.status = FloorStatus.ACTIVE;
        }

        public ParkingFloor(int floorNumber, String parkingLotId, int totalSpots) {
            this();
            this.floorNumber = floorNumber;
            this.parkingLotId = parkingLotId;
            this.totalSpots = totalSpots;
            this.availableSpots = totalSpots;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getFloorNumber() { return floorNumber; }
        public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }
        public String getParkingLotId() { return parkingLotId; }
        public void setParkingLotId(String parkingLotId) { this.parkingLotId = parkingLotId; }
        public FloorStatus getStatus() { return status; }
        public void setStatus(FloorStatus status) { this.status = status; this.updatedAt = LocalDateTime.now(); }
        public int getTotalSpots() { return totalSpots; }
        public void setTotalSpots(int totalSpots) { this.totalSpots = totalSpots; }
        public int getAvailableSpots() { return availableSpots; }
        public void setAvailableSpots(int availableSpots) { this.availableSpots = availableSpots; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "ParkingFloor{" +
                    "id='" + id + '\'' +
                    ", floorNumber=" + floorNumber +
                    ", status=" + status +
                    ", totalSpots=" + totalSpots +
                    ", availableSpots=" + availableSpots +
                    '}';
        }
    }

    /**
     * Entity: ParkingSpot.
     */
    public static class ParkingSpot {
        private String id;
        private String spotNumber;
        private String floorId;
        private String parkingLotId;
        private SpotType spotType;
        private SpotStatus status;
        private boolean isOccupied;
        private String assignedVehicleId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ParkingSpot() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.status = SpotStatus.AVAILABLE;
            this.isOccupied = false;
        }

        public ParkingSpot(String spotNumber, String floorId, String parkingLotId, SpotType spotType) {
            this();
            this.spotNumber = spotNumber;
            this.floorId = floorId;
            this.parkingLotId = parkingLotId;
            this.spotType = spotType;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSpotNumber() { return spotNumber; }
        public void setSpotNumber(String spotNumber) { this.spotNumber = spotNumber; }
        public String getFloorId() { return floorId; }
        public void setFloorId(String floorId) { this.floorId = floorId; }
        public String getParkingLotId() { return parkingLotId; }
        public void setParkingLotId(String parkingLotId) { this.parkingLotId = parkingLotId; }
        public SpotType getSpotType() { return spotType; }
        public void setSpotType(SpotType spotType) { this.spotType = spotType; }
        public SpotStatus getStatus() { return status; }
        public void setStatus(SpotStatus status) { this.status = status; this.updatedAt = LocalDateTime.now(); }
        public boolean isOccupied() { return isOccupied; }
        public void setOccupied(boolean occupied) { isOccupied = occupied; this.updatedAt = LocalDateTime.now(); }
        public String getAssignedVehicleId() { return assignedVehicleId; }
        public void setAssignedVehicleId(String assignedVehicleId) { this.assignedVehicleId = assignedVehicleId; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "ParkingSpot{" +
                    "id='" + id + '\'' +
                    ", spotNumber='" + spotNumber + '\'' +
                    ", spotType=" + spotType +
                    ", status=" + status +
                    ", isOccupied=" + isOccupied +
                    '}';
        }
    }

    /**
     * Entity: Vehicle.
     */
    public static class Vehicle {
        private String id;
        private String licensePlate;
        private VehicleType vehicleType;
        private VehicleColor color;
        private String ownerName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Vehicle() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        public Vehicle(String licensePlate, VehicleType vehicleType, VehicleColor color) {
            this();
            this.licensePlate = licensePlate;
            this.vehicleType = vehicleType;
            this.color = color;
        }

        public Vehicle(String licensePlate, VehicleType vehicleType, VehicleColor color, String ownerName) {
            this(licensePlate, vehicleType, color);
            this.ownerName = ownerName;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLicensePlate() { return licensePlate; }
        public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; this.updatedAt = LocalDateTime.now(); }
        public VehicleType getVehicleType() { return vehicleType; }
        public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; this.updatedAt = LocalDateTime.now(); }
        public VehicleColor getColor() { return color; }
        public void setColor(VehicleColor color) { this.color = color; this.updatedAt = LocalDateTime.now(); }
        public String getOwnerName() { return ownerName; }
        public void setOwnerName(String ownerName) { this.ownerName = ownerName; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "Vehicle{" +
                    "id='" + id + '\'' +
                    ", licensePlate='" + licensePlate + '\'' +
                    ", vehicleType=" + vehicleType +
                    ", color=" + color +
                    ", ownerName='" + ownerName + '\'' +
                    '}';
        }
    }

    /**
     * Entity: ParkingTicket.
     */
    public static class ParkingTicket {
        private String id;
        private String ticketNumber;
        private String vehicleId;
        private String spotId;
        private String parkingLotId;
        private LocalDateTime entryDateTime;
        private LocalDateTime exitDateTime;
        private TicketStatus status;
        private BigDecimal amountCharged;
        private PaymentStatus paymentStatus;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ParkingTicket() {
            this.id = UUID.randomUUID().toString();
            this.ticketNumber = "TICKET-" + System.currentTimeMillis();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.status = TicketStatus.ACTIVE;
            this.paymentStatus = PaymentStatus.PENDING;
            this.entryDateTime = LocalDateTime.now();
        }

        public ParkingTicket(String vehicleId, String spotId, String parkingLotId) {
            this();
            this.vehicleId = vehicleId;
            this.spotId = spotId;
            this.parkingLotId = parkingLotId;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTicketNumber() { return ticketNumber; }
        public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
        public String getVehicleId() { return vehicleId; }
        public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
        public String getSpotId() { return spotId; }
        public void setSpotId(String spotId) { this.spotId = spotId; }
        public String getParkingLotId() { return parkingLotId; }
        public void setParkingLotId(String parkingLotId) { this.parkingLotId = parkingLotId; }
        public LocalDateTime getEntryDateTime() { return entryDateTime; }
        public void setEntryDateTime(LocalDateTime entryDateTime) { this.entryDateTime = entryDateTime; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getExitDateTime() { return exitDateTime; }
        public void setExitDateTime(LocalDateTime exitDateTime) { this.exitDateTime = exitDateTime; this.updatedAt = LocalDateTime.now(); }
        public TicketStatus getStatus() { return status; }
        public void setStatus(TicketStatus status) { this.status = status; this.updatedAt = LocalDateTime.now(); }
        public BigDecimal getAmountCharged() { return amountCharged; }
        public void setAmountCharged(BigDecimal amountCharged) { this.amountCharged = amountCharged; this.updatedAt = LocalDateTime.now(); }
        public PaymentStatus getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "ParkingTicket{" +
                    "id='" + id + '\'' +
                    ", ticketNumber='" + ticketNumber + '\'' +
                    ", vehicleId='" + vehicleId + '\'' +
                    ", spotId='" + spotId + '\'' +
                    ", status=" + status +
                    ", amountCharged=" + amountCharged +
                    ", paymentStatus=" + paymentStatus +
                    '}';
        }
    }

    /**
     * Entity: Payment.
     */
    public static class Payment {
        private String id;
        private String ticketId;
        private BigDecimal amount;
        private String currency;
        private PaymentMethod paymentMethod;
        private PaymentStatus paymentStatus;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Payment() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.paymentStatus = PaymentStatus.PENDING;
            this.currency = "USD";
        }

        public Payment(String ticketId, BigDecimal amount, PaymentMethod paymentMethod) {
            this();
            this.ticketId = ticketId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; this.updatedAt = LocalDateTime.now(); }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; this.updatedAt = LocalDateTime.now(); }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; this.updatedAt = LocalDateTime.now(); }
        public PaymentStatus getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getPaidAt() { return paidAt; }
        public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "Payment{" +
                    "id='" + id + '\'' +
                    ", ticketId='" + ticketId + '\'' +
                    ", amount=" + amount +
                    ", currency='" + currency + '\'' +
                    ", paymentMethod=" + paymentMethod +
                    ", paymentStatus=" + paymentStatus +
                    '}';
        }
    }

    /**
     * Entity: PricingRule.
     */
    public static class PricingRule {
        private String id;
        private String name;
        private String parkingLotId;
        private SpotType spotType;
        private VehicleType vehicleType;
        private BigDecimal hourlyRate;
        private BigDecimal dailyRate;
        private BigDecimal maxDailyRate;
        private BigDecimal flatFee;
        private Integer minimumDurationMinutes;
        private LocalDateTime effectiveFrom;
        private LocalDateTime effectiveTo;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PricingRule() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.isActive = true;
            this.effectiveFrom = LocalDateTime.now();
        }

        public PricingRule(String name, String parkingLotId, BigDecimal hourlyRate) {
            this();
            this.name = name;
            this.parkingLotId = parkingLotId;
            this.hourlyRate = hourlyRate;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; this.updatedAt = LocalDateTime.now(); }
        public String getParkingLotId() { return parkingLotId; }
        public void setParkingLotId(String parkingLotId) { this.parkingLotId = parkingLotId; }
        public SpotType getSpotType() { return spotType; }
        public void setSpotType(SpotType spotType) { this.spotType = spotType; this.updatedAt = LocalDateTime.now(); }
        public VehicleType getVehicleType() { return vehicleType; }
        public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; this.updatedAt = LocalDateTime.now(); }
        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; this.updatedAt = LocalDateTime.now(); }
        public BigDecimal getDailyRate() { return dailyRate; }
        public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; this.updatedAt = LocalDateTime.now(); }
        public BigDecimal getMaxDailyRate() { return maxDailyRate; }
        public void setMaxDailyRate(BigDecimal maxDailyRate) { this.maxDailyRate = maxDailyRate; this.updatedAt = LocalDateTime.now(); }
        public BigDecimal getFlatFee() { return flatFee; }
        public void setFlatFee(BigDecimal flatFee) { this.flatFee = flatFee; this.updatedAt = LocalDateTime.now(); }
        public Integer getMinimumDurationMinutes() { return minimumDurationMinutes; }
        public void setMinimumDurationMinutes(Integer minimumDurationMinutes) { this.minimumDurationMinutes = minimumDurationMinutes; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getEffectiveFrom() { return effectiveFrom; }
        public void setEffectiveFrom(LocalDateTime effectiveFrom) { this.effectiveFrom = effectiveFrom; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getEffectiveTo() { return effectiveTo; }
        public void setEffectiveTo(LocalDateTime effectiveTo) { this.effectiveTo = effectiveTo; this.updatedAt = LocalDateTime.now(); }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "PricingRule{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", spotType=" + spotType +
                    ", vehicleType=" + vehicleType +
                    ", hourlyRate=" + hourlyRate +
                    ", isActive=" + isActive +
                    '}';
        }
    }

    /**
     * Entity: AdminConfiguration.
     */
    public static class AdminConfiguration {
        private String id;
        private String parkingLotId;
        private String defaultCurrency;
        private String timezone;
        private String defaultRatePlanId;
        private boolean isEnabled;
        private String updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AdminConfiguration() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.defaultCurrency = "USD";
            this.timezone = "UTC";
            this.isEnabled = true;
        }

        public AdminConfiguration(String parkingLotId) {
            this();
            this.parkingLotId = parkingLotId;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getParkingLotId() { return parkingLotId; }
        public void setParkingLotId(String parkingLotId) { this.parkingLotId = parkingLotId; }
        public String getDefaultCurrency() { return defaultCurrency; }
        public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; this.updatedAt = LocalDateTime.now(); }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; this.updatedAt = LocalDateTime.now(); }
        public String getDefaultRatePlanId() { return defaultRatePlanId; }
        public void setDefaultRatePlanId(String defaultRatePlanId) { this.defaultRatePlanId = defaultRatePlanId; this.updatedAt = LocalDateTime.now(); }
        public boolean isEnabled() { return isEnabled; }
        public void setEnabled(boolean enabled) { isEnabled = enabled; this.updatedAt = LocalDateTime.now(); }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; this.updatedAt = LocalDateTime.now(); }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        @Override
        public String toString() {
            return "AdminConfiguration{" +
                    "id='" + id + '\'' +
                    ", parkingLotId='" + parkingLotId + '\'' +
                    ", defaultCurrency='" + defaultCurrency + '\'' +
                    ", timezone='" + timezone + '\'' +
                    ", isEnabled=" + isEnabled +
                    '}';
        }
    }

    /**
     * Entity: Invoice.
     */
    public static class Invoice {
        private String id;
        private String ticketId;
        private String vehicleId;
        private BigDecimal amount;
        private String currency;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private LocalDateTime generatedAt;
        private LocalDateTime createdAt;

        public Invoice() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.generatedAt = LocalDateTime.now();
            this.currency = "USD";
        }

        public Invoice(String ticketId, String vehicleId, BigDecimal amount) {
            this();
            this.ticketId = ticketId;
            this.vehicleId = vehicleId;
            this.amount = amount;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        public String getVehicleId() { return vehicleId; }
        public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }

        @Override
        public String toString() {
            return "Invoice{" +
                    "id='" + id + '\'' +
                    ", ticketId='" + ticketId + '\'' +
                    ", amount=" + amount +
                    ", totalAmount=" + totalAmount +
                    '}';
        }
    }
}
