/**
 * Upcoming Reservations Page Module
 *
 * This module handles the "Upcoming Reservations" page.
 *
 * Responsibilities:
 * - Fetch upcoming reservations from backend
 * - Render reservation cards
 * - Handle reservation cancellation flow
 * - Provide confirmation UI before canceling
 * - Handle success and failure states of cancellation
 */

import {
    showStatus,
    renderReservations,
    fetchReservations,
    CONFIRM_TIMEOUT,
    FAILURE_RESET_TIMEOUT
} from "./reservationCommon.js";

const reservationsContainer = document.getElementById("reservationsContainer");
const statusMessage = document.getElementById("statusMessage");

document.addEventListener("DOMContentLoaded", () => {
    loadUpcomingReservations();
});

/**
 * Loads upcoming reservations and renders them.
 */
async function loadUpcomingReservations() {
    try {
        const reservations = await fetchReservations("/reservations/upcoming");

        renderReservations(
            reservations,
            reservationsContainer,
            statusMessage,
            "אין כרגע הזמנות קרובות. נשמח לראות אתכם מזמינים מקום באחת המסעדות.",
            "upcoming"
        );

        setupCancellationEvents();

    } catch (error) {
        showStatus(
            reservationsContainer,
            statusMessage,
            "חלה שגיאה בטעינת המידע. כדאי לנסות שוב בעוד מספר דקות."
        );
    }
}

/**
 * Attaches click listener for cancellation buttons.
 */
function setupCancellationEvents() {
    reservationsContainer.addEventListener("click", async (event) => {
        const btn = event.target.closest(".cancel-btn");

        if (!btn) {
            return;
        }

        await handleCancelLogic(btn);
    });
}

/**
 * Handles the cancel button logic.
 *
 * @param {HTMLElement} btn - The cancel button element
 */
async function handleCancelLogic(btn) {
    if (!btn.classList.contains("confirm-mode")) {
        btn.classList.add("confirm-mode");
        btn.textContent = "בטוחים שרוצים לבטל?";

        setTimeout(() => {
            if (btn.textContent === "בטוחים שרוצים לבטל?") {
                btn.classList.remove("confirm-mode");
                btn.textContent = "ביטול הזמנה";
            }
        }, CONFIRM_TIMEOUT);

        return;
    }

    const reservationId = btn.getAttribute("data-id");
    await performCancellation(reservationId, btn);
}

/**
 * Sends cancellation request to backend and updates UI.
 *
 * @param {string} id - Reservation ID
 * @param {HTMLElement} button - The button element
 */
async function performCancellation(id, button) {
    try {
        button.disabled = true;
        button.textContent = "מבצעים ביטול...";

        const response = await fetch(`/reservations/cancel/${id}`, {
            method: "POST"
        });

        if (!response.ok) {
            throw new Error();
        }

        button.textContent = "ההזמנה בוטלה";
        button.classList.remove("confirm-mode");
        button.classList.add("cancelled-status");

    } catch (error) {
        button.disabled = false;
        button.classList.remove("confirm-mode");
        button.textContent = "הביטול נכשל, כדאי לנסות שוב";

        setTimeout(() => {
            button.textContent = "ביטול הזמנה";
        }, FAILURE_RESET_TIMEOUT);
    }
}