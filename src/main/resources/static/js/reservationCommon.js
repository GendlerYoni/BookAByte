/**
 * Reservation UI Utilities Module
 *
 * This module provides shared functionality for reservation-related pages.
 *
 * Responsibilities:
 * - Fetch reservation data from the backend
 * - Render reservation cards dynamically
 * - Display status messages (empty state / errors / success)
 * - Handle UI state resets
 *
 * Used by:
 * - reservationsUpcoming.js
 * - reservationHistory.js
 */

export const CONFIRM_TIMEOUT = 4000;
export const FAILURE_RESET_TIMEOUT = 2500;

const DEFAULT_IMAGE_PATH = "/restImg/default.png";
const TIME_DISPLAY_LENGTH = 5; // "HH:mm"

/**
 * Displays a status message and clears existing reservation cards.
 *
 * @param {HTMLElement} container - The container for reservation cards
 * @param {HTMLElement} statusElement - Element used to display the message
 * @param {string} text - Message to display
 * @param {string} [type="error"] - Message type ("error" or "success")
 */
export function showStatus(container, statusElement, text, type = "error") {
    container.innerHTML = "";
    statusElement.textContent = text;

    statusElement.classList.remove("hidden");
    statusElement.classList.remove("error-message", "success-message");

    if (type === "success") {
        statusElement.classList.add("success-message");
    } else {
        statusElement.classList.add("error-message");
    }
}

/**
 * Hides the status message and clears its content.
 *
 * @param {HTMLElement} statusElement - The message element
 */
export function hideStatus(statusElement) {
    statusElement.textContent = "";
    statusElement.classList.add("hidden");
    statusElement.classList.remove("error-message", "success-message");
}

/**
 * Fetches reservation data from a given endpoint.
 *
 * @param {string} url - API endpoint URL
 * @returns {Promise<Array>} List of reservations
 * @throws {Error} If the request fails
 */
export async function fetchReservations(url) {
    const response = await fetch(url);

    if (!response.ok) {
        throw new Error();
    }

    return response.json();
}

/**
 * Renders reservation cards into the UI.
 *
 * Handles:
 * - Empty state (no reservations)
 * - Dynamic card creation
 * - Action buttons (cancel / review) based on page type
 *
 * @param {Array} reservations - List of reservation objects
 * @param {HTMLElement} container - Cards container
 * @param {HTMLElement} statusElement - Status message element
 * @param {string} emptyMessage - Message for empty state
 * @param {string} pageType - Page type ("upcoming" or "past")
 */
export function renderReservations(reservations, container, statusElement, emptyMessage, pageType) {
    container.innerHTML = "";
    hideStatus(statusElement);

    if (reservations.length === 0) {
        showStatus(container, statusElement, emptyMessage);
        return;
    }

    const fragment = document.createDocumentFragment();

    for (const res of reservations) {
        const card = document.createElement("div");
        card.className = "reservation-card";

        let actionButtonHTML = "";

        if (pageType === "upcoming") {
            actionButtonHTML = `<button class="cancel-btn" data-id="${res.reservationId}">ביטול הזמנה</button>`;
        } else if (pageType === "past") {
            actionButtonHTML = `<button class="review-btn" data-id="${res.reservationId}">כתוב ביקורת</button>`;
        }

        card.innerHTML = `
            <img src="${res.imagePath || DEFAULT_IMAGE_PATH}" 
                 class="reservation-card-image" 
                 alt="${res.restaurantName}">

            <div class="reservation-card-body">
                <h3 class="reservation-card-title">${res.restaurantName}</h3>

                <div class="reservation-details-list">
                    <div class="reservation-detail-item">
                        <span class="detail-label">תאריך:</span>
                        <span>${res.date}</span>
                    </div>

                    <div class="reservation-detail-item">
                        <span class="detail-label">שעה:</span>
                        <span>${res.time.substring(0, TIME_DISPLAY_LENGTH)}</span>
                    </div>

                    <div class="reservation-detail-item">
                        <span class="detail-label">סועדים:</span>
                        <span>${res.numOfPeople} סועדים</span>
                    </div>
                </div>
            </div>

            <div class="reservation-card-actions">
                ${actionButtonHTML}
            </div>
        `;

        fragment.appendChild(card);
    }

    container.appendChild(fragment);
}