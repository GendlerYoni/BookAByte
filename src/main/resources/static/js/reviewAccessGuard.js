/**
 * Review Page Access Guard
 *
 * This script protects the review page by ensuring that:
 * - A valid reservation ID exists in the URL
 * - The reservation actually exists in the backend
 *
 * If validation fails, the user is redirected to the homepage.
 * If validation succeeds, an "authorized" class is added to the document
 * to allow the page content to be displayed.
 */

import { getIdFromUrl } from "./common.js";

/**
 * Immediately-invoked async function that validates access to the review page.
 *
 * Steps:
 * 1. Extract reservation ID from URL
 * 2. Validate ID format
 * 3. Verify reservation exists via API
 * 4. Grant access or redirect accordingly
 */
(async function () {
    const resId = getIdFromUrl();

    if (!resId || isNaN(resId)) {
        window.location.replace("/home");
        return;
    }

    try {
        const response = await fetch(`/reservations/${resId}`);

        if (!response.ok) {
            window.location.replace("/home");
            return;
        }

        document.documentElement.classList.add("authorized");

    } catch (error) {
        console.error("Review guard failed:", error);
        window.location.replace("/home");
    }
})();