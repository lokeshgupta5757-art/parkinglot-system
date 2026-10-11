package com.parkinglot.service;

import com.parkinglot.model.ParkingLotModel;
import com.parkinglot.model.ParkingLotModel.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Main business logic service class containing all service implementations for the parking lot system.
 *
 * This class is intentionally kept in a single file to match the project's current phase and the requirement
 * that model and business logic classes be created in one main class each. Each nested class represents a
 * distinct service responsibility in the overall parking lot workflow.
 *
 * The services are organized by responsibility:
 * - parking lot lifecycle control
 * - parking spot assignment and release
 * - parking ticket/session management
 * - payment calculation and processing
 * - pricing rule evaluation and admin control
 * - checkout orchestration
 * - validation and date utilities
 *
 * This is a foundational implementation intended for later extension into controller/repository layers and tests.
 */
public class ParkingLotService {

    /**
     * ParkingLotOperationService
     *
     * Purpose:
     * This is the main entry point for vehicle entry and lot-level operations. It acts as the coordinator for
     * the parking process and is responsible for validating the lot state, retrieving an available parking spot,
     * reserving that spot, and creating a parking ticket.
     *
     * Responsibilities:
     * - register parking lots and parking spots
     * - check whether a lot is full
     * - find an available compatible parking spot
     * - create a ticket when a vehicle enters the lot
     * - update lot-level availability counters after assignment
     *
     * This service represents the high-level workflow for parking operations and is the layer that other
     * application layers or controllers would usually call first when a vehicle arrives.
     */
    public static class ParkingLotOperationService {
        private final Map<String, ParkingLot> parkingLots = new HashMap<>();
        private final Map<String, ParkingSpot> parkingSpots = new HashMap<>();
        private final ParkingSpotManager spotManager = new ParkingSpotManager();
        private final TicketService ticketService = new TicketService();

        /**
         * Parks a vehicle in a specific parking lot.
         *
         * Workflow:
         * 1. validate the parking lot exists
         * 2. confirm there is capacity
         * 3. identify a compatible available spot
         * 4. reserve the spot
         * 5. create a ticket for the vehicle
         * 6. update the parking lot's available spot count
         *
         * @param vehicle Vehicle that is trying to enter
         * @param parkingLotId unique identifier of the lot to use
         * @return created parking ticket for the active session
         * @throws Exception if no valid lot is found, lot is full, or no spot is available
         */
        public ParkingTicket parkVehicle(Vehicle vehicle, String parkingLotId) throws Exception {
            ParkingLot lot = parkingLots.get(parkingLotId);
            if (lot == null) {
                throw new Exception("Parking lot not found");
            }
            if (isLotFull(parkingLotId)) {
                throw new Exception("Parking lot is full");
            }

            ParkingSpot spot = spotManager.findAvailableSpot(vehicle, parkingLotId);
            if (spot == null) {
                throw new Exception("No compatible spot available");
            }

            spotManager.reserveSpot(spot, vehicle);
            ParkingTicket ticket = ticketService.createTicket(vehicle, spot, parkingLotId);
            lot.setAvailableSpots(lot.getAvailableSpots() - 1);

            return ticket;
        }

        /**
         * Checks whether the parking lot has reached full capacity.
         *
         * @param parkingLotId lot identifier to inspect
         * @return true if no spots remain available
         */
        public boolean isLotFull(String parkingLotId) {
            ParkingLot lot = parkingLots.get(parkingLotId);
            if (lot != null) {
                return lot.getAvailableSpots() <= 0;
            }
            return false;
        }

        /**
         * Lists all currently available spots for a parking lot.
         *
         * @param parkingLotId parking lot identifier
         * @return list of available spots that are physically free and not marked out of service
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
         * Registers a parking lot in the in-memory runtime state.
         *
         * @param parkingLot the lot to register
         */
        public void registerParkingLot(ParkingLot parkingLot) {
            parkingLots.put(parkingLot.getId(), parkingLot);
        }

        /**
         * Registers a single parking spot in the runtime state.
         *
         * @param spot the spot to register
         */
        public void registerParkingSpot(ParkingSpot spot) {
            parkingSpots.put(spot.getId(), spot);
        }

        /**
         * This placeholder is reserved for the final checkout coordination logic.
         * In a complete implementation, it would fetch the ticket, calculate charge, process payment,
         * release the spot, and update final state.
         */
        public void checkOut(String ticketId, PaymentMethod paymentMethod) throws Exception {
            // placeholder for later business flow
        }
    }

    /**
     * ParkingSpotManager
     *
     * Purpose:
     * This service owns the logic for choosing the best available compatible parking spot for a vehicle.
     * It is responsible for ensuring the system respects spot-type compatibility rules and that spots are
     * consistently marked as occupied or available.
     *
     * Responsibilities:
     * - determine whether a spot matches the vehicle type
     * - find the best remaining matching spot from a lot
     * - reserve and release a spot during the lifecycle of a ticket
     * - maintain spot state consistency
     *
     * This service intentionally contains only allocation logic; it does not compute pricing or handle payment.
     */
    public static class ParkingSpotManager {
        private final Map<String, ParkingSpot> spots = new HashMap<>();

        /**
         * Finds the best available parking spot for a vehicle in a given parking lot.
         *
         * The method filters for:
         * - same lot
         * - not currently occupied
         * - spot status available
         * - compatible spot type
         *
         * Sorting is based on spot type priority, which allows the parking system to prefer the most suitable
         * option according to the current compatibility rules.
         *
         * @param vehicle the incoming vehicle
         * @param parkingLotId target parking lot
         * @return the best matching available spot, or null if none exists
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
         * Validates whether a particular parking spot can accommodate a vehicle.
         *
         * Compatibility rules should reflect the business logic for real-world parking decisions. For example:
         * - motorcycles can use compact or motorcycle spots
         * - cars can use compact or large spots
         * - trucks require large spots
         * - electric cars prefer electric or compact spots
         *
         * @param spot parking spot to test
         * @param vehicle vehicle to test
         * @return true if the vehicle is allowed to park in that spot
         */
        public boolean isSpotCompatible(ParkingSpot spot, Vehicle vehicle) {
            SpotType spotType = spot.getSpotType();
            VehicleType vehicleType = vehicle.getVehicleType();

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
         * Reserves a spot by marking it occupied and assigning the vehicle to it.
         *
         * @param spot destination spot
         * @param vehicle vehicle using the spot
         */
        public void reserveSpot(ParkingSpot spot, Vehicle vehicle) {
            spot.setOccupied(true);
            spot.setStatus(SpotStatus.OCCUPIED);
            spot.setAssignedVehicleId(vehicle.getId());
        }

        /**
         * Releases a previously occupied spot and resets it to available state.
         *
         * @param spot the spot being exited
         */
        public void releaseSpot(ParkingSpot spot) {
            spot.setOccupied(false);
            spot.setStatus(SpotStatus.AVAILABLE);
            spot.setAssignedVehicleId(null);
        }

        /**
         * Registers a spot in the spot manager so it can participate in assignment decisions.
         *
         * @param spot the spot to store
         */
        public void registerSpot(ParkingSpot spot) {
            spots.put(spot.getId(), spot);
        }

        /**
         * Retrieves a spot by its unique id.
         *
         * @param spotId unique spot id
         * @return matching spot, or null if absent
         */
        public ParkingSpot getSpot(String spotId) {
            return spots.get(spotId);
        }
    }

    /**
     * TicketService
     *
     * Purpose:
     * This service controls the lifecycle of a parking session. It stores the ticket record for a vehicle's stay,
     * tracks the active ticket for each vehicle, updates entry/exit timestamps, and maintains payment-related fields.
     *
     * Responsibilities:
     * - create a ticket when a vehicle enters
     * - retrieve active or historical tickets
     * - update exit time and status
     * - update payment and amount information for the session
     *
     * This service is responsible for the session state, while pricing and billing logic is handled separately.
     */
    public static class TicketService {
        private final Map<String, ParkingTicket> tickets = new HashMap<>();
        private final Map<String, String> vehicleActiveTickets = new HashMap<>();

        /**
         * Creates a ticket for a vehicle parked in a specific spot.
         *
         * @param vehicle the vehicle entering the lot
         * @param spot the assigned parking spot
         * @param parkingLotId lot where the vehicle is parked
         * @return newly created parking ticket
         */
        public ParkingTicket createTicket(Vehicle vehicle, ParkingSpot spot, String parkingLotId) {
            ParkingTicket ticket = new ParkingTicket(vehicle.getId(), spot.getId(), parkingLotId);
            tickets.put(ticket.getId(), ticket);
            vehicleActiveTickets.put(vehicle.getId(), ticket.getId());
            return ticket;
        }

        /**
         * Returns the active ticket currently associated with a vehicle.
         *
         * @param vehicleId vehicle identifier
         * @return active ticket if found, otherwise null
         */
        public ParkingTicket getActiveTicketByVehicleId(String vehicleId) {
            String ticketId = vehicleActiveTickets.get(vehicleId);
            if (ticketId != null) {
                return tickets.get(ticketId);
            }
            return null;
        }

        /**
         * Retrieves a ticket using its unique ticket identifier.
         *
         * @param ticketId unique ticket value
         * @return ticket or null if it does not exist
         */
        public ParkingTicket getTicketById(String ticketId) {
            return tickets.get(ticketId);
        }

        /**
         * Records the checkout time on the ticket and marks it checked out.
         *
         * @param ticketId ticket to update
         */
        public void updateTicketExitTime(String ticketId) {
            ParkingTicket ticket = tickets.get(ticketId);
            if (ticket != null) {
                ticket.setExitDateTime(LocalDateTime.now());
                ticket.setStatus(TicketStatus.CHECKED_OUT);
            }
        }

        /**
         * Updates the session status to a new value.
         *
         * @param ticketId ticket identifier
         * @param status new ticket status
         */
        public void updateTicketStatus(String ticketId, TicketStatus status) {
            ParkingTicket ticket = tickets.get(ticketId);
            if (ticket != null) {
                ticket.setStatus(status);
            }
        }

        /**
         * Saves the payment status and final charged amount on the ticket.
         *
         * @param ticketId ticket identifier
         * @param status payment status for the payment operation
         * @param amount final computed amount
         */
        public void updateTicketPaymentStatus(String ticketId, PaymentStatus status, BigDecimal amount) {
            ParkingTicket ticket = tickets.get(ticketId);
            if (ticket != null) {
                ticket.setPaymentStatus(status);
                ticket.setAmountCharged(amount);
            }
        }

        /**
         * Marks the ticket as fully paid.
         *
         * @param ticketId ticket to close out as paid
         */
        public void markTicketAsPaid(String ticketId) {
            ParkingTicket ticket = tickets.get(ticketId);
            if (ticket != null) {
                ticket.setStatus(TicketStatus.PAID);
                ticket.setPaymentStatus(PaymentStatus.SUCCESS);
            }
        }

        /**
         * Clears the active ticket mapping for a vehicle after checkout completion.
         *
         * @param vehicleId vehicle identifier
         */
        public void clearActiveTicket(String vehicleId) {
            vehicleActiveTickets.remove(vehicleId);
        }
    }

    /**
     * PaymentService
     *
     * Purpose:
     * This service is responsible for calculating the final parking fee and processing the payment for a ticket.
     * It is the core service for the billing functionality and sits between the ticket session record and the
     * active pricing policy rules.
     *
     * Responsibilities:
     * - calculate parking duration and total due
     * - select a matching pricing rule
     * - apply the rule to compute amount
     * - simulate or execute payment and create payment records
     * - generate invoice data for checkout
     *
     * This service contains financial logic and should be isolated from pure parking spot lifecycle management.
     */
    public static class PaymentService {
        private final Map<String, Payment> payments = new HashMap<>();
        private final PricingRuleService pricingRuleService = new PricingRuleService();

        /**
         * Calculates the charge due for a completed ticket.
         *
         * The calculation is based on:
         * - ticket entry and exit times
         * - applicable active pricing rule
         * - duration in minutes
         * - hourly rate and optional cap/flat fee rules
         *
         * @param ticket parking session to charge for
         * @return total amount due, or zero if no exit time exists or no rule is available
         */
        public BigDecimal calculateCharge(ParkingTicket ticket) {
            if (ticket.getExitDateTime() == null) {
                return BigDecimal.ZERO;
            }

            Duration parkingDuration = Duration.between(ticket.getEntryDateTime(), ticket.getExitDateTime());
            long minutes = parkingDuration.toMinutes();

            PricingRule rule = pricingRuleService.findApplicableRule(ticket);
            if (rule == null) {
                return BigDecimal.ZERO;
            }

            return calculateAmountFromRule(rule, minutes);
        }

        /**
         * Converts a pricing rule and duration into a concrete amount.
         *
         * @param rule pricing rule to apply
         * @param minutes number of minutes parked
         * @return calculated total charge
         */
        private BigDecimal calculateAmountFromRule(PricingRule rule, long minutes) {
            BigDecimal amount = BigDecimal.ZERO;

            if (rule.getFlatFee() != null) {
                amount = amount.add(rule.getFlatFee());
            }

            if (rule.getMinimumDurationMinutes() != null && minutes < rule.getMinimumDurationMinutes()) {
                minutes = rule.getMinimumDurationMinutes();
            }

            if (rule.getHourlyRate() != null) {
                long hours = (minutes + 59) / 60;
                BigDecimal hourlyCharge = rule.getHourlyRate().multiply(BigDecimal.valueOf(hours));
                amount = amount.add(hourlyCharge);
            }

            if (rule.getMaxDailyRate() != null && amount.compareTo(rule.getMaxDailyRate()) > 0) {
                amount = rule.getMaxDailyRate();
            }

            return amount;
        }

        /**
         * Processes the actual payment using a payment method and amount.
         *
         * The current implementation is intentionally simple and simulates a successful or failed payment
         * based on whether the amount is non-negative. In a production version, this method would call a third-party
         * payment gateway or a local payment integration service.
         *
         * @param ticket ticket associated with the payment
         * @param paymentMethod method selected for checkout
         * @param amount total amount due
         * @return created payment record
         * @throws Exception if payment processing fails
         */
        public Payment processPayment(ParkingTicket ticket, PaymentMethod paymentMethod, BigDecimal amount) throws Exception {
            Payment payment = new Payment(ticket.getId(), amount, paymentMethod);
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
         * Simulates a payment gateway interaction.
         *
         * @param method chosen payment method
         * @param amount payment amount
         * @return true if payment is accepted, otherwise false
         */
        private boolean simulatePaymentProcessing(PaymentMethod method, BigDecimal amount) {
            return amount.compareTo(BigDecimal.ZERO) >= 0;
        }

        /**
         * Generates an invoice object based on the ticket and total amount.
         *
         * @param ticket parking session
         * @param totalAmount final charge for the session
         * @return invoice object ready to display or print
         */
        public Invoice generateInvoice(ParkingTicket ticket, BigDecimal totalAmount) {
            Invoice invoice = new Invoice(ticket.getId(), ticket.getVehicleId(), ticket.getAmountCharged());
            invoice.setTotalAmount(totalAmount);
            invoice.setGeneratedAt(LocalDateTime.now());
            return invoice;
        }

        /**
         * Retrieves a payment by its identifier.
         *
         * @param paymentId payment id
         * @return matching payment object or null if absent
         */
        public Payment getPayment(String paymentId) {
            return payments.get(paymentId);
        }
    }

    /**
     * PricingRuleService
     *
     * Purpose:
     * This service handles the policy side of billing. It loads active pricing rules, validates them, and selects
     * the one that best matches a vehicle, a parking spot, and a parking duration when a ticket is checked out.
     *
     * Responsibilities:
     * - fetch active rules for a parking lot
     * - validate rules before acceptance
     * - activate or deactivate rules
     * - find the matching rule for a given ticket
     * - create or update admin-defined pricing policies
     *
     * This is the service that gives the admin full control over how charges are calculated without changing code.
     */
    public static class PricingRuleService {
        private final Map<String, PricingRule> rules = new HashMap<>();

        /**
         * Retrieves all currently active rules for a parking lot.
         *
         * @param parkingLotId lot identifier
         * @return list of active rules currently in effect
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
         * Finds the most applicable pricing rule for a ticket.
         *
         * In a fuller version, this method would inspect:
         * - spot type
         * - vehicle type
         * - current duration
         * - parking lot rules
         * and return the most specific matching rule.
         *
         * @param ticket ticket being checked out
         * @return the first active rule, or null if none exist
         */
        public PricingRule findApplicableRule(ParkingTicket ticket) {
            List<PricingRule> activeRules = getActiveRulesForLot(ticket.getParkingLotId());
            return activeRules.isEmpty() ? null : activeRules.get(0);
        }

        /**
         * Validates rule integrity before accepting it into the system.
         *
         * @param rule rule to validate
         * @return true if the rule is complete and acceptable for runtime use
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
         * Creates a new pricing rule after validation.
         *
         * @param rule rule to save
         * @return created rule
         * @throws Exception if validation fails
         */
        public PricingRule createRule(PricingRule rule) throws Exception {
            if (!validateRule(rule)) {
                throw new Exception("Invalid pricing rule");
            }
            rules.put(rule.getId(), rule);
            return rule;
        }

        /**
         * Updates an existing pricing rule without creating a new separate rule object.
         *
         * @param ruleId identifier of existing rule
         * @param updates field values to apply
         * @return updated rule
         * @throws Exception if rule does not exist
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
         * Activates a rule so it can be used in pricing calculations.
         *
         * @param ruleId rule identifier
         * @throws Exception if rule is missing
         */
        public void activateRule(String ruleId) throws Exception {
            PricingRule rule = rules.get(ruleId);
            if (rule == null) {
                throw new Exception("Rule not found");
            }
            rule.setActive(true);
        }

        /**
         * Deactivates a rule so it stops being used in future checks.
         *
         * @param ruleId rule identifier
         * @throws Exception if rule is missing
         */
        public void deactivateRule(String ruleId) throws Exception {
            PricingRule rule = rules.get(ruleId);
            if (rule == null) {
                throw new Exception("Rule not found");
            }
            rule.setActive(false);
        }

        /**
         * Registers a pricing rule directly in the service memory.
         *
         * @param rule rule to register
         */
        public void registerRule(PricingRule rule) {
            rules.put(rule.getId(), rule);
        }
    }

    /**
     * CheckoutService
     *
     * Purpose:
     * This service coordinates the final exit flow of a vehicle. It receives the ticket identifier, calculates
     * the due amount, processes the payment, releases the parking spot, and marks the ticket as paid.
     *
     * Responsibilities:
     * - validate and fetch the active ticket
     * - update checkout timestamp
     * - compute the final charge
     * - complete payment processing
     * - free the parking spot
     * - finalize the parking session records
     *
     * This service is the orchestration layer for the end-of-session process and should be the entry point for
     * exit/gate operations.
     */
    public static class CheckoutService {
        private final TicketService ticketService = new TicketService();
        private final PaymentService paymentService = new PaymentService();
        private final ParkingSpotManager spotManager = new ParkingSpotManager();
        private final Map<String, ParkingSpot> allSpots = new HashMap<>();

        /**
         * Executes the complete checkout flow for a vehicle.
         *
         * @param ticketId ticket to be checked out
         * @param paymentMethod payment method used at the exit gate
         * @throws Exception if the ticket is invalid, payment fails, or required data is missing
         */
        public void checkoutVehicle(String ticketId, PaymentMethod paymentMethod) throws Exception {
            ParkingTicket ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                throw new Exception("Ticket not found");
            }

            ticketService.updateTicketExitTime(ticketId);
            BigDecimal charge = paymentService.calculateCharge(ticket);
            Payment payment = paymentService.processPayment(ticket, paymentMethod, charge);

            ticketService.updateTicketPaymentStatus(ticketId, payment.getPaymentStatus(), charge);

            ParkingSpot spot = allSpots.get(ticket.getSpotId());
            if (spot != null) {
                spotManager.releaseSpot(spot);
            }

            ticketService.markTicketAsPaid(ticketId);
            paymentService.generateInvoice(ticket, charge);
            ticketService.clearActiveTicket(ticket.getVehicleId());
        }

        /**
         * Registers all known parking spots so checkout can release the correct one for a ticket.
         *
         * @param spots map of spot ids to spot objects
         */
        public void registerSpots(Map<String, ParkingSpot> spots) {
            allSpots.putAll(spots);
        }
    }

    /**
     * AdminConfigurationService
     *
     * Purpose:
     * This service manages the configuration settings and pricing policies controlled by an admin user.
     * It provides the technical bridge between the business rule layer and the system's operational configuration.
     *
     * Responsibilities:
     * - create and update pricing rules
     * - activate or deactivate rules
     * - fetch configuration for a parking lot
     * - manage default pricing settings for each lot
     *
     * The idea is that non-developers or admins can adjust rules without modifying core application code.
     */
    public static class AdminConfigurationService {
        private final Map<String, AdminConfiguration> configurations = new HashMap<>();
        private final Map<String, PricingRule> pricingRules = new HashMap<>();
        private final PricingRuleService pricingRuleService = new PricingRuleService();

        /**
         * Creates a new pricing rule through the admin configuration layer.
         *
         * @param rule pricing rule to create
         * @return created rule
         * @throws Exception if validation fails
         */
        public PricingRule createRule(PricingRule rule) throws Exception {
            return pricingRuleService.createRule(rule);
        }

        /**
         * Updates a rule using new values provided by admin.
         *
         * @param ruleId rule to update
         * @param updates new values
         * @return updated rule
         * @throws Exception if rule is not found or invalid
         */
        public PricingRule updateRule(String ruleId, PricingRule updates) throws Exception {
            return pricingRuleService.updateRule(ruleId, updates);
        }

        /**
         * Activates a pricing rule.
         *
         * @param ruleId rule identifier
         * @throws Exception if the rule is not found
         */
        public void activateRule(String ruleId) throws Exception {
            pricingRuleService.activateRule(ruleId);
        }

        /**
         * Deactivates a pricing rule.
         *
         * @param ruleId rule identifier
         * @throws Exception if the rule is not found
         */
        public void deactivateRule(String ruleId) throws Exception {
            pricingRuleService.deactivateRule(ruleId);
        }

        /**
         * Retrieves the admin configuration for a specific lot.
         *
         * @param parkingLotId target lot
         * @return admin configuration, or null if not configured
         */
        public AdminConfiguration getConfigForLot(String parkingLotId) {
            return configurations.get(parkingLotId);
        }

        /**
         * Creates or updates admin configuration for a parking lot.
         *
         * @param parkingLotId target lot
         * @param config configuration object
         * @return saved configuration
         */
        public AdminConfiguration setConfigForLot(String parkingLotId, AdminConfiguration config) {
            configurations.put(parkingLotId, config);
            return config;
        }

        /**
         * Registers a rule directly in the admin service memory.
         *
         * @param rule to be registered
         */
        public void registerRule(PricingRule rule) {
            pricingRules.put(rule.getId(), rule);
            pricingRuleService.registerRule(rule);
        }

        /**
         * Lists all pricing rules associated with a lot.
         *
         * @param parkingLotId target parking lot
         * @return active rules for the lot
         */
        public List<PricingRule> getRulesForLot(String parkingLotId) {
            return pricingRuleService.getActiveRulesForLot(parkingLotId);
        }
    }

    /**
     * DateUtilService
     *
     * Purpose:
     * This helper service centralizes date- and time-related computations used by the parking lot workflow.
     *
     * Responsibilities:
     * - compute elapsed time between entry and exit
     * - determine hours/minutes and validity for time ranges
     *
     * This service keeps business logic clean by isolating calendar-related computations in one place.
     */
    public static class DateUtilService {
        /**
         * Calculates the number of minutes between two timestamps.
         *
         * @param from start time
         * @param to end time
         * @return difference in minutes
         */
        public static long getMinutesBetween(LocalDateTime from, LocalDateTime to) {
            Duration duration = Duration.between(from, to);
            return duration.toMinutes();
        }

        /**
         * Calculates the number of hours between two timestamps.
         *
         * @param from start time
         * @param to end time
         * @return difference in hours
         */
        public static long getHoursBetween(LocalDateTime from, LocalDateTime to) {
            Duration duration = Duration.between(from, to);
            return duration.toHours();
        }

        /**
         * Checks whether a time falls within a defined range.
         *
         * @param time time to evaluate
         * @param from lower bound
         * @param to upper bound
         * @return true if within range, otherwise false
         */
        public static boolean isWithinRange(LocalDateTime time, LocalDateTime from, LocalDateTime to) {
            return !time.isBefore(from) && (to == null || !time.isAfter(to));
        }
    }

    /**
     * ValidationUtilService
     *
     * Purpose:
     * This service validates domain objects before they are processed in business operations.
     *
     * Responsibilities:
     * - ensure a vehicle contains required data
     * - validate parking lot creation data
     * - validate ticket records
     * - validate monetary amounts
     *
     * Validation logic is kept separate so that service code remains cleaner and easier to reason about.
     */
    public static class ValidationUtilService {
        /**
         * Validates a vehicle before allowing it to enter the lot.
         *
         * @param vehicle vehicle to validate
         * @return true if the vehicle contains enough valid data
         */
        public static boolean isValidVehicle(Vehicle vehicle) {
            return vehicle != null &&
                   vehicle.getLicensePlate() != null &&
                   !vehicle.getLicensePlate().isEmpty() &&
                   vehicle.getVehicleType() != null &&
                   vehicle.getColor() != null;
        }

        /**
         * Validates a parking lot definition before registration.
         *
         * @param lot lot to validate
         * @return true if it is structurally valid
         */
        public static boolean isValidParkingLot(ParkingLot lot) {
            return lot != null &&
                   lot.getName() != null &&
                   !lot.getName().isEmpty() &&
                   lot.getTotalSpots() > 0;
        }

        /**
         * Validates a parking ticket before use in payment or checkout.
         *
         * @param ticket ticket to validate
         * @return true if the ticket is structurally valid
         */
        public static boolean isValidTicket(ParkingTicket ticket) {
            return ticket != null &&
                   ticket.getTicketNumber() != null &&
                   ticket.getVehicleId() != null &&
                   ticket.getSpotId() != null &&
                   ticket.getEntryDateTime() != null;
        }

        /**
         * Validates whether an amount is safe to charge or pay.
         *
         * @param amount amount to validate
         * @return true if amount is non-null and non-negative
         */
        public static boolean isValidAmount(BigDecimal amount) {
            return amount != null &&
                   amount.compareTo(BigDecimal.ZERO) >= 0;
        }
    }

    /**
     * ParkingLotException
     *
     * Base exception used by parking lot domain operations. This exception is intended to be the central root
     * for business-specific problems such as invalid vehicle entry, unavailable spots, or failed payment operations.
     */
    public static class ParkingLotException extends Exception {
        public ParkingLotException(String message) {
            super(message);
        }

        public ParkingLotException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * NoSpotAvailableException
     *
     * Thrown when a vehicle arrives but there is no valid parking spot available that matches its compatibility rules.
     */
    public static class NoSpotAvailableException extends ParkingLotException {
        public NoSpotAvailableException(String message) {
            super(message);
        }
    }

    /**
     * InvalidPaymentException
     *
     * Thrown when payment collection cannot be completed because the payment information or amount is not acceptable.
     */
    public static class InvalidPaymentException extends ParkingLotException {
        public InvalidPaymentException(String message) {
            super(message);
        }
    }

    /**
     * TicketNotFoundException
     *
     * Thrown when checkout or session lookup requests a ticket that is not present in the active ticket registry.
     */
    public static class TicketNotFoundException extends ParkingLotException {
        public TicketNotFoundException(String message) {
            super(message);
        }
    }
}
