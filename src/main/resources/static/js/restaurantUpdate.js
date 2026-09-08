/**
 * Restaurant Update Page Module
 *
 * This module handles the restaurant update page.
 *
 * Responsibilities:
 * - Extract restaurant ID from URL
 * - Load existing restaurant data into the form
 * - Handle form submission (update restaurant)
 * - Optionally upload a new image
 * - Display success/error messages
 *
 * Uses shared utilities to reduce duplication:
 * - timeSelect.js (time options)
 * - common.js (HTTP + helpers)
 * - restaurantFormUtils.js (form logic)
 */

import { populateTimeOptions } from "./timeSelect.js";
import { getIdFromUrl, putJson } from "./common.js";
import { setupRestaurantTimeOptions, clearRestaurantMessages, showRestaurantError,
    showRestaurantSuccess, buildRestaurantRequest, uploadRestaurantImageIfSelected
} from "./restaurantFormUtils.js";

const CACHE_BUSTER_PARAM = "?t=";

const restaurantId = getIdFromUrl();

const updateForm = document.getElementById("updateRestaurantForm");
const errorMessage = document.getElementById("errorMessage");
const successMessage = document.getElementById("successMessage");

document.addEventListener("DOMContentLoaded", () => {
    setupRestaurantTimeOptions(populateTimeOptions);

    if (restaurantId && !isNaN(restaurantId)) {
        loadRestaurantData(restaurantId);
    } else {
        showRestaurantError(errorMessage, successMessage, "מזהה מסעדה לא תקין.");
    }
});

/**
 * Loads existing restaurant data and fills the update form.
 *
 * @param {string} id - Restaurant ID
 * @throws {Error} If restaurant cannot be fetched
 */
async function loadRestaurantData(id) {
    try {
        const response = await fetch(`/restaurants/${id}`);

        if (!response.ok) {
            throw new Error("לא ניתן למצוא את המסעדה המבוקשת.");
        }

        const restaurant = await response.json();

        document.getElementById("name").value = restaurant.name;
        document.getElementById("description").value = restaurant.description;
        document.getElementById("capacity").value = restaurant.capacity;
        document.getElementById("restaurantType").value = restaurant.restaurantType;

        document.getElementById("openingHour").value = restaurant.openingHour.substring(0, 5);
        document.getElementById("closingHour").value = restaurant.closingHour.substring(0, 5);

        const img = document.getElementById("currentRestaurantImage");

        if (img && restaurant.imagePath) {
            img.src = restaurant.imagePath;
        }

    } catch (error) {
        console.error("Load restaurant error:", error);
        showRestaurantError(errorMessage, successMessage, "שגיאת תקשורת בטעינת הנתונים.");
    }
}

/**
 * Handles form submission for updating a restaurant.
 *
 * Flow:
 * 1. Clear messages
 * 2. Build request payload
 * 3. Send PUT request
 * 4. Upload image if selected
 * 5. Display result to user
 */
if (updateForm) {
    updateForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        clearRestaurantMessages(errorMessage, successMessage);

        const requestData = buildRestaurantRequest();

        try {
            const response = await putJson(`/restaurants/${restaurantId}`, requestData);

            if (!response.ok) {
                const errorMsg = await response.text();
                showRestaurantError(errorMessage, successMessage, "שגיאה בעדכון המסעדה: " + errorMsg);
                return;
            }

            const imageResponse = await uploadRestaurantImageIfSelected(restaurantId);

            if (!imageResponse) {
                showRestaurantSuccess(errorMessage, successMessage, "המסעדה עודכנה בהצלחה!");
                return;
            }

            if (imageResponse.ok) {
                showRestaurantSuccess(errorMessage, successMessage, "המסעדה עודכנה והתמונה הוחלפה בהצלחה!");

                const imgResult = await imageResponse.json();
                const imgElement = document.getElementById("currentRestaurantImage");

                if (imgElement) {
                    imgElement.src = imgResult.imagePath + CACHE_BUSTER_PARAM + new Date().getTime();
                }

            } else {
                const imageErrorMsg = await imageResponse.text();
                showRestaurantError(errorMessage, successMessage,
                    "המסעדה עודכנה אך התמונה נכשלה: " + imageErrorMsg);
            }

        } catch (error) {
            console.error("Update restaurant error:", error);
            showRestaurantError(errorMessage, successMessage, "לא ניתן להתחבר לשרת.");
        }
    });
}