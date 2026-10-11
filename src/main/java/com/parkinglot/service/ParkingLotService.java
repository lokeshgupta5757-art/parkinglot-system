package com.parkinglot.service;

import com.parkinglot.model.ParkingLotModel;
import com.parkinglot.model.ParkingLotModel.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Main business logic service class containing all service implementations.
 * All services are organized as inner classes for simplicity during initial development.
 */
public class ParkingLotService {

    /**
     * Service: ParkingLotService
     * Main service for parking operations.
     */
    public static class ParkingLotOperationService {
        private final Map<String, ParkingLot> parkingLots = new HashMap<>();
        private final Map<String, ParkingSpot> parkingSpots = new HashMap<>();
        private final ParkingSpotManager spotManager = new ParkingSpotManager();
        private final TicketService ticketService = new TicketService();

        /**
         * Main entry point - park a vehicle in the lot
         */
        public ParkingTicket parkVehicle(Vehicle vehicle, String parkingLotId) 
                throws Exception {
            // Validate parking lot exists and has capacity
            ParkingLot lot = parkingLots.get(parkingLotId);
            if (lot == null) {
                throw new Exception("Parking lot not found");
            }
            if (isLotFull(parkingLotId)) {
                throw new Exception("Parking lot is full");
            }

            // Find available spot
            ParkingSpot spot = spotManager.findAvailableSpot(vehicle, parkingLotId);
            if (spot == null) {
                throw new Exception("No compatible spot available");
            }

            // Reserve spot
            spotManager.reserveSpot(spot, vehicle);

            // Create ticket
            ParkingTicket ticket = ticketService.createTicket(vehicle, spot, parkingLotId);

            // Update lot availability
            lot.setAvailableSpots(lot.getAvailableSpots() - 1);

            return ticket;
        }

        /**
         * Main exit point - checkout vehicle
         */
        public void checkOut(String ticketId, PaymentMethod paymentMethod) 
                throws Exception {
            // This will be implemented with checkout service
            // For now, placeholder
        }

        /**
         * Check if parking lot is at full capacity
         */
        public boolean isLotFull(String parkingLotId) {
            ParkingLot lot = parkingLots.get(parkingLotId);
            if (lot != null) {
                return lot.getAvailableSpots() <= 0;
            }
            return false;
        }

        /**
         * Get all available parking spots in the lot
         */
        public List<ParkingSpot> getAvailableSpots(String parkingLotId) {
            List<ParkingSpot> available = new ArrayList<>();
            for (ParkingSpot spot : parkingSpots.values()) {
                if (spot.getParkingLotId().equals(parkingLotId) && 
                    !spot.isOccupied() && 
                    spot.getStatus() == SpotStatus.AVAILABLE) {
                    available.add(spot);
                }
            }
            return available;
        }

        /**
         * Register a parking lot
         */
        public void registerParkingLot(ParkingLot parkingLot) {
            parkingLots.put(parkingLot.getId(), parkingLot);
        }

        /**
         * Register a parking spot
         */
        public void registerParkingSpot(ParkingSpot spot) {
            parkingSpots.put(spot.getId(), spot);
        }
    }

    /**
     * Service: ParkingSpotManager
     * Responsible for finding and managing parking spot allocation.
     */
    public static class ParkingSpotManager {
        private final Map<String, ParkingSpot> spots = new HashMap<>();

        /**
         * Find an available parking spot compatible with the vehicle
         */
        public ParkingSpot findAvailableSpot(Vehicle vehicle, String parkingLotId) {
            return spots.values().stream()
                    .filter(s -> s.getParkingLotId().equals(parkingLotId))
                    .filter(s -> !s.isOccupied())
                    .filter(s -> s.getStatus() == SpotStatus.AVAILABLE)
                    .filter(s -> isSpotCompatible(s, vehicle))
                    .min(Comparator.comparingInt(s -> s.getSpotType().getPriority()))
                    .orElse(null);
        }

        /**
         * Check if a spot is compatible with the vehicle type
         */
        public boolean isSpotCompatible(ParkingSpot spot, Vehicle vehicle) {
            SpotType spotType = spot.getSpotType();
            VehicleType vehicleType = vehicle.getVehicleType();

            // Define compatibility rules
            switch (vehicleType) {
                case MOTORCYCLE:
                    return spotType == SpotType.MOTORCYCLE || spotType == SpotType.COMPACT;
                case CAR:
                    return spotType == SpotType.COMPACT || spotType == SpotType.LARGE;
                case TRUCK:
                    return spotType == SpotType.LARGE;
                case ELECTRIC_CAR:
                    return spotType == SpotType.ELECTRIC || spotType == SpotType.COMPACT;
                case SUV:
                    return spotType == SpotType.LARGE;
                default:
                    return false;
            }
        }

        /**
         * Reserve a spot for a vehicle
         */
        public void reserveSpot(ParkingSpot spot, Vehicle vehicle) {
            spot.setOccupied(true);
            spot.setStatus(SpotStatus.OCCUPIED);
            spot.setAssignedVehicleId(vehicle.getId());
        }

        /**
         * Release a spot after vehicle exits
         */
        public void releaseSpot(ParkingSpot spot) {
            spot.setOccupied(false);
            spot.setStatus(SpotStatus.AVAILABLE);
            spot.setAssignedVehicleId(null);
        }

        /**
         * Register a spot in the manager
         */
        public void registerSpot(ParkingSpot spot) {
            spots.put(spot.getId(), spot);
        }

        /**
         * Get spot by ID
         */
        public ParkingSpot getSpot(String spotId) {
            return spots.get(spotId);
        }
    }

    /**
     * Service: TicketService
     * Responsible for managing parking tickets/sessions.
     */
    public static class TicketService {
        private final Map<String, ParkingTicket> tickets = new HashMap<>();
        private final Map<String, String> vehicleActiveTickets = new HashMap<>();

        /**
         * Create a new parking ticket for vehicle entry
         */
        public ParkingTicket createTicket(Vehicle vehicle, ParkingSpot spot, String parkingLotId) {
            ParkingTicket ticket = new ParkingTicket(vehicle.getId(), spot.getId(), parkingLotId);
            tickets.put(ticket.getId(), ticket);
            vehicleActiveTickets.put(vehicle.getId(), ticket.getId());
            return ticket;
        }

        /**
         * Get active ticket for a vehicle
         */
        public ParkingTicket getActiveTicketByVehicleId(String vehicleId) {
            String ticketId = vehicleActiveTickets.get(vehicleId);
            if (ticketId != null) {
                return tickets.get(ticketId);
            }
            return null;
        }

        /**
         * Get ticket by ticket ID
         */
        public ParkingTicket getTicketById(String ticketId) {
            return tickets.get(ticketId);
        }

        /**
         * Record exit time for a ticket
         */
        public void updateTicketExitTime(String ticketId) {
            ParkingTicket ticket = tickets.get(ticketId);
            if (ticket != null) {
                ticket.setExitDateTime(LocalDateTime.now());
                ticket.setStatus(TicketStatus.CHECKED_OUT);
            }
        }

        /**
         * Update ticket status
         */
        public void updateTicketStatus(String ticketId, TicketStatus status) {
            ParkingTicket ticket = tickets.get(ticketId);
            if (ticket != null) {
                ticket.setStatus(status);
            }
        }

        /**
         * Update payment status on ticket
         */
        public void updateTicketPaymentStatus(String ticketId, PaymentStatus status, BigDecimal amount) {
            ParkingTicket ticket = tickets.get(ticketId);
            if (ticket != null) {
                ticket.setPaymentStatus(status);
                ticket.setAmountCharged(amount);
            }
        }

        /**
         * Mark ticket as paid
         */
        public void markTicketAsPaid(String ticketId) {
            ParkingTicket ticket = tickets.get(ticketId);
            if (ticket != null) {
                ticket.setStatus(TicketStatus.PAID);
                ticket.setPaymentStatus(PaymentStatus.SUCCESS);
            }
        }

        /**
         * Clear active ticket for vehicle
         */
        public void clearActiveTicket(String vehicleId) {
            vehicleActiveTickets.remove(vehicleId);
        }
    }

    /**
     * Service: PaymentService
     * Handles payment computation and processing.
     */
    public static class PaymentService {
        private final Map<String, Payment> payments = new HashMap<>();
        private final PricingRuleService pricingRuleService = new PricingRuleService();

        /**
         * Calculate parking charge based on ticket and applicable pricing rule
         */
        public BigDecimal calculateCharge(ParkingTicket ticket) {
            if (ticket.getExitDateTime() == null) {
                return BigDecimal.ZERO;
            }

            Duration parkingDuration = Duration.between(ticket.getEntryDateTime(), ticket.getExitDateTime());
            long minutes = parkingDuration.toMinutes();

            // Get applicable pricing rule
            PricingRule rule = pricingRuleService.findApplicableRule(ticket);
            if (rule == null) {
                return BigDecimal.ZERO;
            }

            return calculateAmountFromRule(rule, minutes);
        }

        /**
         * Calculate amount based on pricing rule and duration
         */
        private BigDecimal calculateAmountFromRule(PricingRule rule, long minutes) {
            BigDecimal amount = BigDecimal.ZERO;

            // Apply flat fee if exists
            if (rule.getFlatFee() != null) {
                amount = amount.add(rule.getFlatFee());
            }

            // Check minimum duration
            if (rule.getMinimumDurationMinutes() != null && 
                minutes < rule.getMinimumDurationMinutes()) {
                minutes = rule.getMinimumDurationMinutes();
            }

            // Calculate hourly rate
            if (rule.getHourlyRate() != null) {
                long hours = (minutes + 59) / 60; // Round up
                BigDecimal hourlyCharge = rule.getHourlyRate().multiply(BigDecimal.valueOf(hours));
                amount = amount.add(hourlyCharge);
            }

            // Apply daily rate cap if exists
            if (rule.getMaxDailyRate() != null && amount.compareTo(rule.getMaxDailyRate()) > 0) {
                amount = rule.getMaxDailyRate();
            }

            return amount;
        }

        /**
         * Process payment
         */
        public Payment processPayment(ParkingTicket ticket, PaymentMethod paymentMethod, BigDecimal amount) 
                throws Exception {
            Payment payment = new Payment(ticket.getId(), amount, paymentMethod);
            
            // Simulate payment processing
            boolean paymentSuccess = simulatePaymentProcessing(paymentMethod, amount);
            
            if (paymentSuccess) {
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                throw new Exception("Payment processing failed");
            }

            payments.put(payment.getId(), payment);
            return payment;
        }

        /**
         * Simulate payment processing (placeholder)
         */
        private boolean simulatePaymentProcessing(PaymentMethod method, BigDecimal amount) {
            // Placeholder - in real implementation, call payment gateway
            return amount.compareTo(BigDecimal.ZERO) >= 0;
        }

        /**
         * Generate invoice for a parking session
         */
        public Invoice generateInvoice(ParkingTicket ticket, BigDecimal totalAmount) {
            Invoice invoice = new Invoice(ticket.getId(), ticket.getVehicleId(), ticket.getAmountCharged());
            invoice.setTotalAmount(totalAmount);
            invoice.setGeneratedAt(LocalDateTime.now());
            return invoice;
        }

        /**
         * Get payment by ID
         */
        public Payment getPayment(String paymentId) {
            return payments.get(paymentId);
        }
    }

    /**
     * Service: PricingRuleService
     * Admin-facing pricing logic for rule evaluation.
     */
    public static class PricingRuleService {
        private final Map<String, PricingRule> rules = new HashMap<>();

        /**
         * Get all active pricing rules for a parking lot
         */
        public List<PricingRule> getActiveRulesForLot(String parkingLotId) {
            List<PricingRule> activeRules = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (PricingRule rule : rules.values()) {
                if (rule.getParkingLotId().equals(parkingLotId) &&
                    rule.isActive() &&
                    rule.getEffectiveFrom().isBefore(now) &&
                    (rule.getEffectiveTo() == null || rule.getEffectiveTo().isAfter(now))) {
                    activeRules.add(rule);
                }
            }

            return activeRules;
        }

        /**
         * Find the most applicable pricing rule for a ticket
         */
        public PricingRule findApplicableRule(ParkingTicket ticket) {
            // This would need to access spot and vehicle info
            // For now, return default or first active rule
            List<PricingRule> activeRules = getActiveRulesForLot(ticket.getParkingLotId());
            return activeRules.isEmpty() ? null : activeRules.get(0);
        }

        /**
         * Validate pricing rule
         */
        public boolean validateRule(PricingRule rule) {
            if (rule.getName() == null || rule.getName().isEmpty()) {
                return false;
            }
            if (rule.getHourlyRate() == null || rule.getHourlyRate().compareTo(BigDecimal.ZERO) < 0) {
                return false;
            }
            if (rule.getEffectiveFrom() == null) {
                return false;
            }
            return true;
        }

        /**
         * Create a new pricing rule
         */
        public PricingRule createRule(PricingRule rule) throws Exception {
            if (!validateRule(rule)) {
                throw new Exception("Invalid pricing rule");
            }
            rules.put(rule.getId(), rule);
            return rule;
        }

        /**
         * Update pricing rule
         */
        public PricingRule updateRule(String ruleId, PricingRule updates) throws Exception {
            PricingRule existing = rules.get(ruleId);
            if (existing == null) {
                throw new Exception("Rule not found");
            }

            if (updates.getName() != null) {
                existing.setName(updates.getName());
            }
            if (updates.getHourlyRate() != null) {
                existing.setHourlyRate(updates.getHourlyRate());
            }
            if (updates.getDailyRate() != null) {
                existing.setDailyRate(updates.getDailyRate());
            }

            return existing;
        }

        /**
         * Activate a pricing rule
         */
        public void activateRule(String ruleId) throws Exception {
            PricingRule rule = rules.get(ruleId);
            if (rule == null) {
                throw new Exception("Rule not found");
            }
            rule.setActive(true);
        }

        /**
         * Deactivate a pricing rule
         */
        public void deactivateRule(String ruleId) throws Exception {
            PricingRule rule = rules.get(ruleId);
            if (rule == null) {
                throw new Exception("Rule not found");
            }
            rule.setActive(false);
        }

        /**
         * Register a pricing rule
         */
        public void registerRule(PricingRule rule) {
            rules.put(rule.getId(), rule);
        }
    }

    /**
     * Service: CheckoutService
     * Orchestrates the vehicle exit and payment workflow.
     */
    public static class CheckoutService {
        private final TicketService ticketService = new TicketService();
        private final PaymentService paymentService = new PaymentService();
        private final ParkingSpotManager spotManager = new ParkingSpotManager();
        private final Map<String, ParkingSpot> allSpots = new HashMap<>();

        /**
         * Complete checkout process for a vehicle
         */
        public void checkoutVehicle(String ticketId, PaymentMethod paymentMethod) 
                throws Exception {
            // Get ticket
            ParkingTicket ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                throw new Exception("Ticket not found");
            }

            // Update exit time
            ticketService.updateTicketExitTime(ticketId);

            // Calculate charge
            BigDecimal charge = paymentService.calculateCharge(ticket);

            // Process payment
            Payment payment = paymentService.processPayment(ticket, paymentMethod, charge);

            // Update ticket with payment info
            ticketService.updateTicketPaymentStatus(ticketId, payment.getPaymentStatus(), charge);

            // Release parking spot
            ParkingSpot spot = allSpots.get(ticket.getSpotId());
            if (spot != null) {
                spotManager.releaseSpot(spot);
            }

            // Mark ticket as paid
            ticketService.markTicketAsPaid(ticketId);

            // Generate invoice
            paymentService.generateInvoice(ticket, charge);

            // Clear active ticket for vehicle
            ticketService.clearActiveTicket(ticket.getVehicleId());
        }

        /**
         * Register spots for the checkout service
         */
        public void registerSpots(Map<String, ParkingSpot> spots) {
            allSpots.putAll(spots);
        }
    }

    /**
     * Service: AdminConfigurationService
     * Handles admin-level configuration and pricing rule management.
     */
    public static class AdminConfigurationService {
        private final Map<String, AdminConfiguration> configurations = new HashMap<>();
        private final Map<String, PricingRule> pricingRules = new HashMap<>();
        private final PricingRuleService pricingRuleService = new PricingRuleService();

        /**
         * Create a new pricing rule
         */
        public PricingRule createRule(PricingRule rule) throws Exception {
            return pricingRuleService.createRule(rule);
        }

        /**
         * Update an existing pricing rule
         */
        public PricingRule updateRule(String ruleId, PricingRule updates) throws Exception {
            return pricingRuleService.updateRule(ruleId, updates);
        }

        /**
         * Activate a pricing rule
         */
        public void activateRule(String ruleId) throws Exception {
            pricingRuleService.activateRule(ruleId);
        }

        /**
         * Deactivate a pricing rule
         */
        public void deactivateRule(String ruleId) throws Exception {
            pricingRuleService.deactivateRule(ruleId);
        }

        /**
         * Get configuration for a parking lot
         */
        public AdminConfiguration getConfigForLot(String parkingLotId) {
            return configurations.get(parkingLotId);
        }

        /**
         * Create or update admin configuration for a lot
         */
        public AdminConfiguration setConfigForLot(String parkingLotId, AdminConfiguration config) {
            configurations.put(parkingLotId, config);
            return config;
        }

        /**
         * Register a pricing rule
         */
        public void registerRule(PricingRule rule) {
            pricingRules.put(rule.getId(), rule);
            pricingRuleService.registerRule(rule);
        }

        /**
         * Get all pricing rules for a lot
         */
        public List<PricingRule> getRulesForLot(String parkingLotId) {
            return pricingRuleService.getActiveRulesForLot(parkingLotId);
        }
    }

    /**
     * Utility Service: DateUtilService
     * Helper methods for date/time calculations
     */
    public static class DateUtilService {
        /**
         * Calculate duration in minutes between two times
         */
        public static long getMinutesBetween(LocalDateTime from, LocalDateTime to) {
            Duration duration = Duration.between(from, to);
            return duration.toMinutes();
        }

        /**
         * Calculate duration in hours between two times
         */
        public static long getHoursBetween(LocalDateTime from, LocalDateTime to) {
            Duration duration = Duration.between(from, to);
            return duration.toHours();
        }

        /**
         * Check if a time is within a date range
         */
        public static boolean isWithinRange(LocalDateTime time, LocalDateTime from, LocalDateTime to) {
            return !time.isBefore(from) && (to == null || !time.isAfter(to));
        }
    }

    /**
     * Utility Service: ValidationUtilService
     * Helper methods for validation
     */
    public static class ValidationUtilService {
        /**
         * Validate vehicle
         */
        public static boolean isValidVehicle(Vehicle vehicle) {
            return vehicle != null &&
                   vehicle.getLicensePlate() != null &&
                   !vehicle.getLicensePlate().isEmpty() &&
                   vehicle.getVehicleType() != null &&
                   vehicle.getColor() != null;
        }

        /**
         * Validate parking lot
         */
        public static boolean isValidParkingLot(ParkingLot lot) {
            return lot != null &&
                   lot.getName() != null &&
                   !lot.getName().isEmpty() &&
                   lot.getTotalSpots() > 0;
        }

        /**
         * Validate parking ticket
         */
        public static boolean isValidTicket(ParkingTicket ticket) {
            return ticket != null &&
                   ticket.getTicketNumber() != null &&
                   ticket.getVehicleId() != null &&
                   ticket.getSpotId() != null &&
                   ticket.getEntryDateTime() != null;
        }

        /**
         * Validate parking amount
         */
        public static boolean isValidAmount(BigDecimal amount) {
            return amount != null &&
                   amount.compareTo(BigDecimal.ZERO) >= 0;
        }
    }

    /**
     * Exception Classes
     */
    public static class ParkingLotException extends Exception {
        public ParkingLotException(String message) {
            super(message);
        }

        public ParkingLotException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class NoSpotAvailableException extends ParkingLotException {
        public NoSpotAvailableException(String message) {
            super(message);
        }
    }

    public static class InvalidPaymentException extends ParkingLotException {
        public InvalidPaymentException(String message) {
            super(message);
        }
    }

    public static class TicketNotFoundException extends ParkingLotException {
        public TicketNotFoundException(String message) {
            super(message);
        }
    }
}
