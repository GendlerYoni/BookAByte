/**
 * Reservation History Page Module
 *
 * This module handles the reservation history page.
 *
 * Responsibilities:
 * - Fetch past reservations for the logged-in customer
 * - Render reservation history cards
 * - Display empty/error states
 * - Redirect the user to the review creation page
 */

import { showStatus, renderReservations, fetchReservations } from "./reservationCommon.js";

const reservationsContainer = document.getElementById("reservationsContainer");
const statusMessage = document.getElementById("statusMessage");

document.addEventListener("DOMContentLoaded", () => {
    loadPastReservations();
});

/**
 * Loads the user's past reservations and renders them on the page.
 */
async function loadPastReservations() {
    try {
        const reservations = await fetchReservations("/reservations/past");

        renderReservations(
            reservations,
            reservationsContainer,
            statusMessage,
            "טרם נמצאו הזמנות קודמות בהיסטוריה.",
            "past"
        );

        setupReviewEvents();

    } catch (error) {
        showStatus(
            reservationsContainer,
            statusMessage,
            "חלה שגיאה בטעינת היסטוריית ההזמנות. כדאי לנסות שוב מאוחר יותר."
        );
    }
}

/**
 * Adds click handling for review buttons.
 */
function setupReviewEvents() {
    reservationsContainer.addEventListener("click", (event) => {
        const btn = event.target.closest(".review-btn");

        if (!btn) {
            return;
        }

        const reservationId = btn.getAttribute("data-id");
        window.location.href = `/review/create/${reservationId}`;
    });
}