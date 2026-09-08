/**
 * Reservation Create Page Module
 *
 * This module handles the reservation creation page.
 *
 * Responsibilities:
 * - Load restaurant details and display them
 * - Load and display restaurant reviews
 * - Populate reservation time options dynamically
 * - Handle availability check flow
 * - Handle reservation creation
 * - Manage UI states (messages, buttons, form reset)
 *
 * Uses shared utilities:
 * - timeSelect.js
 * - common.js
 */

import { populateTimeOptions } from "./timeSelect.js";
import { getIdFromUrl, postJson, showMessage, hideMessage } from "./common.js";

const HALF_HOUR = 30;
const QUARTER_HOUR = 15;
const ONE_HOUR = 1;
const TWO_HOURS = 2;
const LOGIN_REDIRECT_DELAY = 2000;

const restaurantId = getIdFromUrl();

const reservationForm = document.getElementById("reservationForm");
const checkAvailabilityBtn = document.getElementById("checkAvailabilityBtn");
const confirmReservationBtn = document.getElementById("confirmReservationBtn");
const errorMessage = document.getElementById("errorMessage");
const successMessage = document.getElementById("successMessage");

/**
 * Initializes the page on load.
 *
 * - Validates restaurant ID
 * - Loads restaurant data
 * - Loads reviews
 */
document.addEventListener("DOMContentLoaded", () => {
    if (restaurantId && !isNaN(restaurantId)) {
        loadRestaurantData(restaurantId);
        loadRestaurantReviews(restaurantId);
    } else {
        showError("מזהה מסעדה לא תקין.");
    }
});

/**
 * Loads restaurant details and updates the UI.
 *
 * @param {number|string} id - Restaurant ID
 */
async function loadRestaurantData(id) {
    try {
        const response = await fetch(`/restaurants/${id}`);

        if (!response.ok) {
            throw new Error();
        }

        const restaurant = await response.json();

        // Populate UI fields
        document.getElementById("restaurantName").textContent = restaurant.name;
        document.getElementById("restaurantType").textContent = restaurant.restaurantType;
        document.getElementById("restaurantDescription").textContent = restaurant.description;
        document.getElementById("restaurantOpeningHour").textContent = restaurant.openingHour.substring(0, 5);
        document.getElementById("restaurantClosingHour").textContent = restaurant.closingHour.substring(0, 5);

        // Prepare time options based on restaurant hours
        const openH = parseInt(restaurant.openingHour.split(":")[0]);
        let closeH = parseInt(restaurant.closingHour.split(":")[0]);
        let closeM = parseInt(restaurant.closingHour.split(":")[1]);

        if (closeM === HALF_HOUR) {
            closeM = 0;
            closeH -= ONE_HOUR;
        } else {
            closeM = HALF_HOUR;
            closeH -= TWO_HOURS;
        }

        populateTimeOptions("reservationTime", openH, closeH, closeM, QUARTER_HOUR);

        // Ratings
        const avg = restaurant.averageScore ? Number(restaurant.averageScore).toFixed(1) : "0.0";
        const count = restaurant.reviewCount || 0;

        document.getElementById("restaurantRating").textContent = avg;
        document.getElementById("reviewCount").textContent = `(${count})`;
        document.getElementById("reviewsAverage").textContent = avg;
        document.getElementById("reviewsAmount").textContent = `(${count} ביקורות)`;

        // Image
        if (restaurant.imagePath) {
            document.getElementById("restaurantImage").src = restaurant.imagePath;
        }

    } catch (error) {
        showError("שגיאת תקשורת בטעינת נתוני המסעדה.");
    }
}

/**
 * Loads restaurant reviews and renders them.
 *
 * @param {number|string} id - Restaurant ID
 */
async function loadRestaurantReviews(id) {
    const reviewsContainer = document.getElementById("reviewsContainer");

    try {
        const response = await fetch(`/reviews/restaurant/${id}`);

        if (!response.ok) {
            throw new Error();
        }

        const reviews = await response.json();

        reviewsContainer.innerHTML = "";

        if (!reviews || reviews.length === 0) {
            reviewsContainer.innerHTML =
                `<p class="empty-message">אין עדיין ביקורות למסעדה זו.</p>`;
            return;
        }

        const fragment = document.createDocumentFragment();

        for (const review of reviews) {
            const reviewDiv = document.createElement("div");
            reviewDiv.classList.add("review-item");

            reviewDiv.innerHTML = `
                <div class="review-header">
                    <span class="review-score">${review.score}/5 ⭐</span>
                    <span>${review.username}</span>
                </div>
                <p class="review-description">${review.description}</p>
                <span class="review-date">${review.reservationDate}</span>
            `;

            fragment.appendChild(reviewDiv);
        }

        reviewsContainer.appendChild(fragment);

    } catch {
        reviewsContainer.innerHTML =
            `<p class="empty-message">לא ניתן לטעון ביקורות.</p>`;
    }
}

/**
 * Handles availability check button click.
 *
 * Flow:
 * 1. Validate inputs
 * 2. Send availability request
 * 3. Show result and update UI
 */
checkAvailabilityBtn.addEventListener("click", async () => {
    clearMessages();

    const requestData = buildReservationRequest();

    if (!requestData.date || !requestData.time || !requestData.guests) {
        showError("יש למלא תאריך, שעה ומספר סועדים.");
        return;
    }

    try {
        const response = await postJson("/reservations/availability", requestData);
        const result = await response.json();

        if (response.ok && result.available) {
            showSuccess("יש מקום! ניתן לאשר את ההזמנה.");
            showConfirmButton();
        } else {
            showError("אין מקום פנוי בזמן שבחרת.");
            hideConfirmButton();
        }

    } catch {
        showError("לא ניתן להתחבר לשרת.");
    }
});

/**
 * Handles reservation form submission.
 *
 * Flow:
 * 1. Build request data
 * 2. Send create request
 * 3. Handle success / errors
 */
if (reservationForm) {
    reservationForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearMessages();

        const requestData = buildReservationRequest();

        try {
            const response = await postJson("/reservations/create", requestData);

            if (response.ok) {
                showSuccess("ההזמנה נוצרה בהצלחה!");
                markReservationCreated();
                hideConfirmButton();
                reservationForm.reset();

            } else if (response.status === 403) {
                showError("עליך להתחבר כלקוח כדי לבצע הזמנה. מעביר אותך...");

                setTimeout(() => {
                    window.location.href = "/login";
                }, LOGIN_REDIRECT_DELAY);

            } else {
                showError(await response.text());
            }

        } catch {
            showError("שגיאת תקשורת בביצוע ההזמנה.");
        }
    });
}

/**
 * Builds reservation request payload.
 *
 * @returns {Object} reservation request data
 */
function buildReservationRequest() {
    const time = document.getElementById("reservationTime").value;

    return {
        restId: parseInt(restaurantId, 10),
        date: document.getElementById("reservationDate").value,
        time: time ? time + ":00" : "",
        guests: parseInt(document.getElementById("guests").value, 10)
    };
}

/**
 * Resets availability state when inputs change.
 */
function resetAvailabilityCheck() {
    hideConfirmButton();
    clearMessages();

    confirmReservationBtn.disabled = false;
    confirmReservationBtn.textContent = "אישור הזמנה";
}

document.getElementById("reservationDate").addEventListener("change", resetAvailabilityCheck);
document.getElementById("reservationTime").addEventListener("change", resetAvailabilityCheck);
document.getElementById("guests").addEventListener("input", resetAvailabilityCheck);

/**
 * Shows confirm button and hides check button.
 */
function showConfirmButton() {
    checkAvailabilityBtn.classList.add("hidden");
    confirmReservationBtn.classList.remove("hidden");
}

/**
 * Hides confirm button and shows check button.
 */
function hideConfirmButton() {
    checkAvailabilityBtn.classList.remove("hidden");
    confirmReservationBtn.classList.add("hidden");
}

/**
 * Updates UI after successful reservation.
 */
function markReservationCreated() {
    confirmReservationBtn.disabled = true;
    confirmReservationBtn.textContent = "ההזמנה נוצרה";
}

/**
 * Displays error message.
 */
function showError(message) {
    showMessage(errorMessage, message, "error");
    hideMessage(successMessage);
}

/**
 * Displays success message.
 */
function showSuccess(message) {
    showMessage(successMessage, message, "success");
    hideMessage(errorMessage);
}

/**
 * Clears all messages.
 */
function clearMessages() {
    hideMessage(errorMessage);
    hideMessage(successMessage);
}