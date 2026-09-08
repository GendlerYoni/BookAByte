package yoni.restaurantreservationsystem.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import yoni.restaurantreservationsystem.entities.Reservation;
import yoni.restaurantreservationsystem.entities.ReservationStatus;
import yoni.restaurantreservationsystem.entities.Restaurant;
import yoni.restaurantreservationsystem.entities.RestaurantType;
import yoni.restaurantreservationsystem.entities.User;
import yoni.restaurantreservationsystem.entities.UserType;
import yoni.restaurantreservationsystem.repositories.ReservationRepository;
import yoni.restaurantreservationsystem.repositories.RestaurantRepository;
import yoni.restaurantreservationsystem.repositories.UserRepository;
import yoni.restaurantreservationsystem.services.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for concurrent restaurant reservations.
 *
 * This test uses the real Spring context, JPA repositories, transactions,
 * and an H2 database to verify that pessimistic locking prevents
 * two simultaneous booking attempts from overbooking the same restaurant.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReservationConcurrencyIntegrationTest {

    private static final LocalDate TEST_DATE =
            LocalDate.of(2026, 10, 2);

    private static final LocalTime TEST_TIME =
            LocalTime.of(18, 0);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long restaurantId;
    private Long firstCustomerId;
    private Long secondCustomerId;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAllInBatch();
        restaurantRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        User admin = new User(
                "concurrency-admin",
                passwordEncoder.encode("AdminPassword123"),
                UserType.ADMIN
        );

        admin = userRepository.saveAndFlush(admin);

        User firstCustomer = new User(
                "concurrency-customer-1",
                passwordEncoder.encode("CustomerPassword123"),
                UserType.CUSTOMER
        );

        firstCustomer = userRepository.saveAndFlush(firstCustomer);

        User secondCustomer = new User(
                "concurrency-customer-2",
                passwordEncoder.encode("CustomerPassword123"),
                UserType.CUSTOMER
        );

        secondCustomer = userRepository.saveAndFlush(secondCustomer);

        Restaurant restaurant = new Restaurant(
                "Concurrency Test Restaurant",
                "Restaurant used for the concurrency integration test",
                1,
                LocalTime.of(8, 0),
                LocalTime.of(23, 0),
                RestaurantType.PIZZA,
                null,
                admin
        );

        restaurant = restaurantRepository.saveAndFlush(restaurant);

        restaurantId = restaurant.getRestId();
        firstCustomerId = firstCustomer.getUserId();
        secondCustomerId = secondCustomer.getUserId();
    }

    @Test
    void simultaneousBookingsShouldNotOverbookLastAvailableSeat() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<Boolean> firstBooking = () ->
                attemptBooking(
                        firstCustomerId,
                        readyLatch,
                        startLatch
                );

        Callable<Boolean> secondBooking = () ->
                attemptBooking(
                        secondCustomerId,
                        readyLatch,
                        startLatch
                );

        Future<Boolean> firstResult = executor.submit(firstBooking);
        Future<Boolean> secondResult = executor.submit(secondBooking);

        try {
            assertTrue(
                    readyLatch.await(5, TimeUnit.SECONDS),
                    "Both booking threads should be ready before the test starts"
            );

            startLatch.countDown();

            boolean firstSucceeded =
                    firstResult.get(10, TimeUnit.SECONDS);

            boolean secondSucceeded =
                    secondResult.get(10, TimeUnit.SECONDS);

            int successfulBookings = 0;

            if (firstSucceeded) {
                successfulBookings++;
            }

            if (secondSucceeded) {
                successfulBookings++;
            }

            assertEquals(
                    1,
                    successfulBookings,
                    "Exactly one concurrent booking should succeed"
            );

            List<Reservation> reservations =
                    reservationRepository
                            .findByRestaurant_RestIdAndDateAndStatus(
                                    restaurantId,
                                    TEST_DATE,
                                    ReservationStatus.ACTIVE
                            );

            assertEquals(
                    1,
                    reservations.size(),
                    "The database should contain exactly one active reservation"
            );

            Reservation savedReservation = reservations.get(0);

            assertEquals(1, savedReservation.getGuests());
            assertEquals(TEST_TIME, savedReservation.getReservationTime());

        } finally {
            startLatch.countDown();
            executor.shutdownNow();
        }
    }

    private boolean attemptBooking(
            Long customerId,
            CountDownLatch readyLatch,
            CountDownLatch startLatch) throws Exception {

        readyLatch.countDown();

        if (!startLatch.await(5, TimeUnit.SECONDS)) {
            throw new TimeoutException(
                    "Timed out while waiting for concurrent booking start"
            );
        }

        try {
            reservationService.createReservation(
                    customerId,
                    restaurantId,
                    TEST_DATE,
                    TEST_TIME,
                    1
            );

            return true;

        } catch (IllegalStateException exception) {
            return false;
        }
    }
}