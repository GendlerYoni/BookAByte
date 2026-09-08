package yoni.restaurantreservationsystem.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yoni.restaurantreservationsystem.entities.Reservation;
import yoni.restaurantreservationsystem.entities.ReservationStatus;
import yoni.restaurantreservationsystem.entities.Restaurant;
import yoni.restaurantreservationsystem.entities.User;
import yoni.restaurantreservationsystem.entities.UserType;
import yoni.restaurantreservationsystem.repositories.ReservationRepository;
import yoni.restaurantreservationsystem.repositories.RestaurantRepository;
import yoni.restaurantreservationsystem.services.ReservationService;
import yoni.restaurantreservationsystem.services.RestaurantService;
import yoni.restaurantreservationsystem.services.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReservationService.
 *
 * These tests verify reservation availability rules, capacity handling,
 * time-slot overlap behavior, and reservation creation failures.
 *
 * The repositories and dependent services are mocked, so these tests
 * focus only on ReservationService business logic.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private UserService userService;

    @Mock
    private RestaurantRepository restaurantRepository;

    private ReservationService reservationService;
    private Restaurant restaurant;

    private static final Long RESTAURANT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 10, 1);

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                userService,
                restaurantService,
                restaurantRepository
        );

        restaurant = mock(Restaurant.class);

        when(restaurant.getCapacity()).thenReturn(10);
    }

    @Test
    void availabilityShouldReturnFalseWhenRequestedSeatsExceedCapacity() {
        when(restaurantService.getRestaurantById(RESTAURANT_ID))
                .thenReturn(restaurant);

        boolean available = reservationService.checkAvailability(
                RESTAURANT_ID,
                TEST_DATE,
                LocalTime.of(18, 0),
                11
        );

        assertFalse(available);
    }

    @Test
    void availabilityShouldAllowExactlyFullCapacity() {
        Reservation existingReservation = mock(Reservation.class);

        when(existingReservation.getReservationTime())
                .thenReturn(LocalTime.of(18, 0));
        when(existingReservation.getGuests())
                .thenReturn(5);

        when(restaurant.getOpeningHour())
                .thenReturn(LocalTime.of(8, 0));
        when(restaurant.getClosingHour())
                .thenReturn(LocalTime.of(23, 0));

        when(restaurantService.getRestaurantById(RESTAURANT_ID))
                .thenReturn(restaurant);

        when(reservationRepository.findByRestaurant_RestIdAndDateAndStatus(
                RESTAURANT_ID,
                TEST_DATE,
                ReservationStatus.ACTIVE
        )).thenReturn(List.of(existingReservation));

        boolean available = reservationService.checkAvailability(
                RESTAURANT_ID,
                TEST_DATE,
                LocalTime.of(18, 0),
                5
        );

        assertTrue(available);
    }

    @Test
    void overlappingReservationsShouldUseMaximumConcurrentOccupancy() {
        Reservation reservationA = mock(Reservation.class);
        Reservation reservationB = mock(Reservation.class);

        when(reservationA.getReservationTime())
                .thenReturn(LocalTime.of(18, 0));
        when(reservationA.getGuests())
                .thenReturn(5);

        when(reservationB.getReservationTime())
                .thenReturn(LocalTime.of(19, 30));
        when(reservationB.getGuests())
                .thenReturn(5);

        when(restaurant.getOpeningHour())
                .thenReturn(LocalTime.of(8, 0));
        when(restaurant.getClosingHour())
                .thenReturn(LocalTime.of(23, 0));

        when(restaurantService.getRestaurantById(RESTAURANT_ID))
                .thenReturn(restaurant);

        when(reservationRepository.findByRestaurant_RestIdAndDateAndStatus(
                RESTAURANT_ID,
                TEST_DATE,
                ReservationStatus.ACTIVE
        )).thenReturn(List.of(reservationA, reservationB));

        boolean available = reservationService.checkAvailability(
                RESTAURANT_ID,
                TEST_DATE,
                LocalTime.of(19, 0),
                5
        );

        assertTrue(available);
    }

    @Test
    void backToBackReservationsShouldNotOverlap() {
        Reservation existingReservation = mock(Reservation.class);

        when(existingReservation.getReservationTime())
                .thenReturn(LocalTime.of(18, 0));

        when(restaurant.getOpeningHour())
                .thenReturn(LocalTime.of(8, 0));
        when(restaurant.getClosingHour())
                .thenReturn(LocalTime.of(23, 0));

        when(restaurantService.getRestaurantById(RESTAURANT_ID))
                .thenReturn(restaurant);

        when(reservationRepository.findByRestaurant_RestIdAndDateAndStatus(
                RESTAURANT_ID,
                TEST_DATE,
                ReservationStatus.ACTIVE
        )).thenReturn(List.of(existingReservation));

        boolean available = reservationService.checkAvailability(
                RESTAURANT_ID,
                TEST_DATE,
                LocalTime.of(19, 30),
                10
        );

        assertTrue(available);
    }

    @Test
    void availabilityShouldReturnFalseWhenConcurrentCapacityIsExceeded() {
        Reservation existingReservation = mock(Reservation.class);

        when(existingReservation.getReservationTime())
                .thenReturn(LocalTime.of(18, 0));
        when(existingReservation.getGuests())
                .thenReturn(6);

        when(restaurant.getOpeningHour())
                .thenReturn(LocalTime.of(8, 0));
        when(restaurant.getClosingHour())
                .thenReturn(LocalTime.of(23, 0));

        when(restaurantService.getRestaurantById(RESTAURANT_ID))
                .thenReturn(restaurant);

        when(reservationRepository.findByRestaurant_RestIdAndDateAndStatus(
                RESTAURANT_ID,
                TEST_DATE,
                ReservationStatus.ACTIVE
        )).thenReturn(List.of(existingReservation));

        boolean available = reservationService.checkAvailability(
                RESTAURANT_ID,
                TEST_DATE,
                LocalTime.of(18, 30),
                5
        );

        assertFalse(available);
    }

    @Test
    void createReservationShouldFailWhenNoSeatsAreAvailable() {
        User user = mock(User.class);
        Reservation existingReservation = mock(Reservation.class);

        when(user.getUserType())
                .thenReturn(UserType.CUSTOMER);

        when(userService.getUserById(USER_ID))
                .thenReturn(user);

        when(restaurantRepository.findByIdWithLock(RESTAURANT_ID))
                .thenReturn(Optional.of(restaurant));

        when(restaurant.getOpeningHour())
                .thenReturn(LocalTime.of(8, 0));
        when(restaurant.getClosingHour())
                .thenReturn(LocalTime.of(23, 0));

        when(restaurantService.getRestaurantById(RESTAURANT_ID))
                .thenReturn(restaurant);

        when(existingReservation.getReservationTime())
                .thenReturn(LocalTime.of(18, 0));
        when(existingReservation.getGuests())
                .thenReturn(10);

        when(reservationRepository.findByRestaurant_RestIdAndDateAndStatus(
                RESTAURANT_ID,
                TEST_DATE,
                ReservationStatus.ACTIVE
        )).thenReturn(List.of(existingReservation));

        assertThrows(
                IllegalStateException.class,
                () -> reservationService.createReservation(
                        USER_ID,
                        RESTAURANT_ID,
                        TEST_DATE,
                        LocalTime.of(18, 0),
                        1
                )
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }
}