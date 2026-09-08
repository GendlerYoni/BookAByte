/**
 * Common Utilities Module
 *
 * This module provides shared helper functions used across the application.
 *
 * Responsibilities:
 * - Display and hide UI messages
 * - Perform HTTP requests (POST, PUT, DELETE)
 * - Handle session storage (save/retrieve user)
 * - Manage authentication/session validation
 * - Provide navigation utilities (redirect, logout)
 * - Support file uploads
 */

const REDIRECT_WAIT_TIME = 2500;

/**
 * Displays a message to the user.
 *
 * @param {HTMLElement} element - Target DOM element
 * @param {string} message - Message to display
 * @param {string} [type="error"] - Message type ("error" | "success")
 */
export function showMessage(element, message, type = "error") {
    element.textContent = message;
    element.classList.remove("hidden");
    element.classList.remove("error-message", "success-message");
    element.classList.add(type === "success" ? "success-message" : "error-message");
}

/**
 * Hides a message element and clears its content.
 *
 * @param {HTMLElement} element - Target DOM element
 */
export function hideMessage(element) {
    element.textContent = "";
    element.classList.add("hidden");
    element.classList.remove("error-message", "success-message");
}

/**
 * Extracts the last segment from the current URL (usually an ID).
 *
 * @returns {string} The extracted ID from URL
 */
export function getIdFromUrl() {
    return window.location.pathname.split('/').pop();
}

/**
 * Sends a POST request with JSON payload.
 *
 * @param {string} url - Endpoint URL
 * @param {Object} data - Data to send
 * @returns {Promise<Response>} Fetch response
 */
export async function postJson(url, data) {
    return fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
}

/**
 * Sends a PUT request with JSON payload.
 *
 * @param {string} url - Endpoint URL
 * @param {Object} data - Data to send
 * @returns {Promise<Response>} Fetch response
 */
export async function putJson(url, data) {
    return fetch(url, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
}

/**
 * Sends a DELETE request.
 *
 * @param {string} url - Endpoint URL
 * @returns {Promise<Response>} Fetch response
 */
export async function deleteRequest(url) {
    return fetch(url, { method: "DELETE" });
}

/**
 * Uploads an image file for a specific restaurant.
 *
 * @param {string|number} restId - Restaurant ID
 * @param {File} file - Image file to upload
 * @returns {Promise<Response>} Fetch response
 */
export async function uploadImage(restId, file) {
    const formData = new FormData();
    formData.append("image", file);

    return fetch(`/restaurants/${restId}/image`, {
        method: 'POST',
        body: formData
    });
}

/**
 * Saves user data to session storage.
 *
 * @param {Object} user - Authenticated user object
 */
export function saveUser(user) {
    sessionStorage.setItem("user", JSON.stringify(user));
}

/**
 * Retrieves stored user data from session storage.
 *
 * @returns {Object|null} Parsed user object or null if not found
 */
export function getStoredUser() {
    const userData = sessionStorage.getItem("user");
    return userData ? JSON.parse(userData) : null;
}

/**
 * Fetches the current session from the backend.
 *
 * @returns {Promise<Object|null>} Current user or null if not authenticated
 */
export async function getCurrentSession() {
    const response = await fetch("/session");
    return response.ok ? response.json() : null;
}

/**
 * Ensures the current user has the required role.
 * Redirects if unauthorized or not logged in.
 *
 * @param {string} requiredRole - Required user role (e.g., "ADMIN", "CUSTOMER")
 */
export async function requireRole(requiredRole) {
    try {
        const user = await getCurrentSession();

        if (!user || user.userType !== requiredRole) {
            window.location.href = !user ? "/login" : "/home";
        }

    } catch (error) {
        window.location.href = "/login";
    }
}

/**
 * Logs out the current user.
 * Clears session and redirects to homepage.
 */
export async function logout() {
    try {
        await fetch("/logout", { method: "POST" });
    } catch (error) {
        console.error("Logout failed:", error);
    }

    sessionStorage.removeItem("user");
    window.location.replace("/home");
}

/**
 * Redirects the user to the homepage after a delay.
 *
 * @param {number} [delay=2500] - Delay in milliseconds
 */
export function redirectHome(delay = REDIRECT_WAIT_TIME) {
    setTimeout(() => {
        window.location.href = "/home";
    }, delay);
}