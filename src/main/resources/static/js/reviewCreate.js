/**
 * Review Creation Page Module
 *
 * This module handles the review creation page.
 *
 * Responsibilities:
 * - Extract reservation ID from URL
 * - Load reservation details for display
 * - Check if user can write a review
 * - Populate existing review if already created
 * - Handle review submission
 * - Manage UI states (messages, form disabling, redirects)
 */

import { getIdFromUrl, postJson, showMessage, hideMessage } from "./common.js";

const REVIEW_REDIRECT_TIME = 2500;
const ZERO_VALUE = 0;
const BASE_TEN = 10;

const reservationId = getIdFromUrl();

const errorMessage = document.getElementById("errorMessage");
const successMessage = document.getElementById("successMessage");
const reviewForm = document.getElementById("reviewForm");

document.addEventListener("DOMContentLoaded", () => {
    if (reservationId && !isNaN(reservationId)) {
        loadReservationData(reservationId);
        checkReviewAvailability(reservationId);
    } else {
        showError("מזהה הזמנה לא תקין.");
        disableReviewForm();
    }
});

/**
 * Loads reservation details and displays them on the page.
 *
 * @param {string} id - Reservation ID
 */
async function loadReservationData(id) {
    try {
        const response = await fetch(`/reservations/${id}`);

        if (!response.ok) {
            throw new Error("לא הצלחנו למשוך את נתוני ההזמנה");
        }

        const res = await response.json();

        document.getElementById("restaurantName").textContent = res.restaurantName;
        document.getElementById("reservationDate").textContent = res.date;
        document.getElementById("reservationTime").textContent = res.time.substring(0, 5);
        document.getElementById("guestsCount").textContent = res.numOfPeople;

        if (res.imagePath) {
            document.getElementById("summaryRestaurantImage").src = res.imagePath;
        }

    } catch (error) {
        console.error("Load reservation error:", error);
        showError("שגיאת תקשורת בטעינת נתוני ההזמנה.");
    }
}

/**
 * Checks if the user already has a review or is allowed to write one.
 *
 * @param {string} id - Reservation ID
 */
async function checkReviewAvailability(id) {
    try {
        const existingReviewRes = await fetch(`/reviews/reservation/${id}`);

        if (existingReviewRes.ok) {
            const review = await existingReviewRes.json();
            populateExistingReview(review);
            return;
        }

        const canReviewRes = await fetch(`/reviews/can-review/${id}`);

        if (canReviewRes.ok) {
            const canReview = await canReviewRes.json();

            if (!canReview) {
                showError("לא ניתן לכתוב ביקורת. ייתכן שההזמנה טרם התקיימה או שבוטלה.");
                disableReviewForm();
            }
        }

    } catch (error) {
        console.error("Check review availability error:", error);
    }
}

/**
 * Fills the form with an existing review and disables editing.
 *
 * @param {Object} review - Existing review object
 */
function populateExistingReview(review) {
    const textArea = document.getElementById("revDescription");
    textArea.value = review.description || "";

    const starInput = document.getElementById(`star${review.score}`);

    if (starInput) {
        starInput.checked = true;
    }

    const submitBtn = reviewForm.querySelector('button[type="submit"]');

    if (submitBtn) {
        submitBtn.textContent = "הביקורת שלך כבר פורסמה";
    }

    disableReviewForm();
}

/**
 * Handles review form submission.
 */
reviewForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearMessages();

    const selectedScore = document.querySelector('input[name="score"]:checked');
    const description = document.getElementById("revDescription").value;

    if (!selectedScore) {
        showError("אנא בחר דירוג בכוכבים לפני השליחה.");
        return;
    }

    if (!description || description.trim().length === ZERO_VALUE) {
        showError("יש לכתוב חוות דעת מילולית.");
        return;
    }

    const reviewRequest = {
        reseId: parseInt(reservationId, BASE_TEN),
        revDescription: description.trim(),
        score: parseInt(selectedScore.value, BASE_TEN)
    };

    try {
        const response = await postJson("/reviews", reviewRequest);

        if (response.status === 201 || response.ok) {
            showSuccess("הביקורת פורסמה בהצלחה! תודה על השיתוף.");

            disableReviewForm();

            setTimeout(() => {
                window.location.href = "/reservation/history";
            }, REVIEW_REDIRECT_TIME);

        } else {
            const errorMsg = await response.text();
            showError(errorMsg || "קרתה שגיאה בניסיון לפרסם את הביקורת.");
        }

    } catch (error) {
        console.error("Create review error:", error);
        showError("שגיאת תקשורת. בדוק את החיבור לאינטרנט ונסה שוב.");
    }
});

/**
 * Disables all form inputs and buttons.
 */
function disableReviewForm() {
    if (!reviewForm) return;

    const fields = reviewForm.querySelectorAll("input, textarea, button");
    fields.forEach(field => field.disabled = true);

    const submitBtn = reviewForm.querySelector('button[type="submit"]');

    if (submitBtn) {
        submitBtn.classList.add("disabled-btn");
        submitBtn.style.cursor = "not-allowed";
    }
}

/**
 * Displays an error message.
 *
 * @param {string} message - Error text
 */
function showError(message) {
    showMessage(errorMessage, message, "error");
    hideMessage(successMessage);
}

/**
 * Displays a success message.
 *
 * @param {string} message - Success text
 */
function showSuccess(message) {
    showMessage(successMessage, message, "success");
    hideMessage(errorMessage);
}

/**
 * Clears both success and error messages.
 */
function clearMessages() {
    hideMessage(errorMessage);
    hideMessage(successMessage);
}