/**
 * Controller that handles navigation between frontend pages.
 *
 * This controller is responsible for returning the correct HTML views
 * for different application pages such as login, registration, restaurants,
 * reservations, reviews, and the home page.
 *
 * It does not contain business logic and is used only for page routing.
 */
package yoni.restaurantreservationsystem.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    /**
     * Displays the home page.
     *
     * @return the home view
     */
    @GetMapping("/home")
    public String showHomePage(){
        return "home";
    }

    /**
     * Displays the home page for the root URL.
     *
     * @return the home view
     */
    @GetMapping()
    public String showHomePageEmpty(){
        return "home";
    }


    /**
     * Displays the login page.
     *
     * @return the login view
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * Displays the registration page.
     *
     * @return the register view
     */
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    /**
     * Displays the restaurant creation page.
     *
     * @return the restaurant-create view
     */
    @GetMapping("/restaurant/create")
    public String showRestaurantCreatePage() {
        return "restaurant-create";
    }

    /**
     * Displays the restaurant update page.
     *
     * @return the restaurant-update view
     */
    @GetMapping("/restaurant/update/{id}")
    public String showRestaurantUpdatePage() {
        return "restaurant-update";
    }

    /**
     * Displays the restaurant statistics page.
     *
     * @return the statistics view
     */
    @GetMapping("/restaurant/statistics/{id}")
    public String showRestaurantStatisticsPage() {
        return "statistics";
    }

    /**
     * Displays the reservation creation page.
     *
     * @return the reservation-create view
     */
    @GetMapping("/reservation/create/{id}")
    public String showReservationCreatePage(){
        return "reservation-create";
    }

    /**
     * Displays the upcoming reservations page.
     *
     * @return the reservation-upcoming view
     */
    @GetMapping("/reservation/upcoming")
    public String showReservationUpcomingPage(){
        return "reservation-upcoming";
    }

    /**
     * Displays the reservation history page.
     *
     * @return the reservation-history view
     */
    @GetMapping("/reservation/history")
    public String showReservationHistoryPage(){
        return "reservation-history";
    }

    /**
     * Displays the review creation page.
     *
     * @return the review-create view
     */
    @GetMapping("/review/create/{id}")
    public String showReviewCreatePage(){
        return "review-create";
    }

    /**
     * Displays the page showing restaurants owned by the logged-in admin.
     *
     * @return the myRestaurants view
     */
    @GetMapping("/myrestaurants")
    public String showMyRestaurantPage(){
        return "myRestaurants";
    }

}