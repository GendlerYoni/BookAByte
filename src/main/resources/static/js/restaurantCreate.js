/**
 * Restaurant Create Page Module
 *
 * This module handles the creation of a new restaurant.
 *
 * Responsibilities:
 * - Initialize time selection inputs
 * - Handle form submission
 * - Send create request to backend
 * - Upload image if selected
 * - Display success/error messages
 * - Disable submit button after successful creation
 */

import { populateTimeOptions } from "./timeSelect.js";
import { postJson } from "./common.js";
import { setupRestaurantTimeOptions, clearRestaurantMessages, showRestaurantError,
    showRestaurantSuccess, buildRestaurantRequest, uploadRestaurantImageIfSelected
} from "./restaurantFormUtils.js";

const CREATE_URL = "/restaurants";
const SUCCESS_BUTTON_TEXT = "המסעדה נוצרה בהצלחה";

const createForm = document.getElementById("createRestaurantForm");
const errorMessage = document.getElementById("errorMessage");
const successMessage = document.getElementById("successMessage");
const submitBtn = createForm.querySelector('button[type="submit"]');

document.addEventListener("DOMContentLoaded", () => {
    setupRestaurantTimeOptions(populateTimeOptions);
});

/**
 * Handles form submission for creating a restaurant.
 */
createForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    clearRestaurantMessages(errorMessage, successMessage);

    const requestData = buildRestaurantRequest();

    try {
        const response = await postJson(CREATE_URL, requestData);

        if (!response.ok) {
            const errorMsg = await response.text();
            showRestaurantError(errorMessage, successMessage,
                "שגיאה ביצירת המסעדה: " + errorMsg);
            return;
        }

        const result = await response.json();
        const imageResponse = await uploadRestaurantImageIfSelected(result.restId);

        handleSuccess(imageResponse);

    } catch (error) {
        console.error("Create restaurant error:", error);
        showRestaurantError(
            errorMessage,
            successMessage,
            "לא ניתן להתחבר לשרת."
        );
    }
});

/**
 * Handles success flow (with or without image upload).
 *
 * @param {Response|null} imageResponse - Response from image upload, or null if not uploaded
 */
function handleSuccess(imageResponse) {
    if (!imageResponse) {
        showRestaurantSuccess(errorMessage, successMessage, "המסעדה נוצרה בהצלחה!");
        disableSubmitButton();
        return;
    }

    if (imageResponse.ok) {
        showRestaurantSuccess(errorMessage, successMessage, "המסעדה נוצרה והתמונה הועלתה בהצלחה!");
        disableSubmitButton();
    } else {
        imageResponse.text().then(msg => {
            showRestaurantError(
                errorMessage,
                successMessage,
                "המסעדה נוצרה, אך העלאת התמונה נכשלה: " + msg
            );
        });
    }
}

/**
 * Disables submit button after successful creation.
 */
function disableSubmitButton() {
    submitBtn.disabled = true;
    submitBtn.textContent = SUCCESS_BUTTON_TEXT;
}