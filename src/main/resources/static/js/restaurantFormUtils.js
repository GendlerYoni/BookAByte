/**
 * Restaurant Form Utilities Module
 *
 * This module provides shared helper functions for restaurant create/update forms.
 *
 * Responsibilities:
 * - Populate opening and closing time select inputs
 * - Clear and display form messages
 * - Build restaurant request payloads
 * - Upload restaurant images when selected
 *
 * Used by:
 * - restaurantCreate.js
 * - restaurantUpdate.js
 */

import { hideMessage, showMessage, uploadImage } from "./common.js";

const BASE_TEN = 10;
const TIME_SECONDS_SUFFIX = ":00";

/**
 * Populates the opening and closing hour select elements.
 *
 * @param {Function} populateTimeOptions - Function used to fill time select elements
 */
export function setupRestaurantTimeOptions(populateTimeOptions) {
    populateTimeOptions("openingHour");
    populateTimeOptions("closingHour");
}

/**
 * Clears both restaurant form message elements.
 *
 * @param {HTMLElement} errorMessage - Error message element
 * @param {HTMLElement} successMessage - Success message element
 */
export function clearRestaurantMessages(errorMessage, successMessage) {
    hideMessage(errorMessage);
    hideMessage(successMessage);
}

/**
 * Displays an error message and hides the success message.
 *
 * @param {HTMLElement} errorMessage - Error message element
 * @param {HTMLElement} successMessage - Success message element
 * @param {string} message - Error text
 */
export function showRestaurantError(errorMessage, successMessage, message) {
    showMessage(errorMessage, message, "error");
    hideMessage(successMessage);
}

/**
 * Displays a success message and hides the error message.
 *
 * @param {HTMLElement} errorMessage - Error message element
 * @param {HTMLElement} successMessage - Success message element
 * @param {string} message - Success text
 */
export function showRestaurantSuccess(errorMessage, successMessage, message) {
    showMessage(successMessage, message, "success");
    hideMessage(errorMessage);
}

/**
 * Builds the request payload for creating or updating a restaurant.
 *
 * @returns {Object} Restaurant request data
 */
export function buildRestaurantRequest() {
    return {
        name: document.getElementById("name").value,
        description: document.getElementById("description").value,
        capacity: parseInt(document.getElementById("capacity").value, BASE_TEN),
        restaurantType: document.getElementById("restaurantType").value,
        openingHour: document.getElementById("openingHour").value + TIME_SECONDS_SUFFIX,
        closingHour: document.getElementById("closingHour").value + TIME_SECONDS_SUFFIX
    };
}

/**
 * Uploads a restaurant image only if the user selected one.
 *
 * @param {number|string} restId - Restaurant ID
 * @returns {Promise<Response|null>} Upload response, or null if no image was selected
 */
export async function uploadRestaurantImageIfSelected(restId) {
    const imageFile = document.getElementById("image").files[0];

    if (!imageFile) {
        return null;
    }

    return uploadImage(restId, imageFile);
}