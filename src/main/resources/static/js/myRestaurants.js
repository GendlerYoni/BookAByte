/**
 * My Restaurants Page Module
 *
 * Handles the admin page that displays and manages restaurants owned by
 * the currently logged-in admin.
 *
 * Responsibilities:
 * - Load admin restaurants from backend
 * - Render restaurant management cards
 * - Handle restaurant deletion with confirmation flow
 * - Display empty/error states
 */
import { deleteRequest, showMessage, hideMessage } from "./common.js";

const restaurantsContainer = document.getElementById("restaurantsContainer");
const statusMessage = document.getElementById("statusMessage");

document.addEventListener("DOMContentLoaded", () => {
    loadMyRestaurants();
    setupDeleteEvents();
});

/**
 * Loads restaurants owned by the logged-in admin.
 */
async function loadMyRestaurants() {
    try {
        const response = await fetch("/restaurants/my");

        if (!response.ok) {
            throw new Error("Failed to load admin restaurants");
        }

        const restaurants = await response.json();

        if (!restaurants || restaurants.length === 0) {
            showStatus("עדיין לא הקמת מסעדות במערכת. זה הזמן להתחיל!");
            return;
        }

        renderRestaurants(restaurants);

    } catch (error) {
        console.error("Load my restaurants error:", error);
        showStatus("שגיאה בטעינת המסעדות שלך. נסה לרענן את הדף.");
    }
}

/**
 * Renders restaurant cards for admin management.
 */
function renderRestaurants(restaurants) {
    const fragment = document.createDocumentFragment();

    restaurantsContainer.innerHTML = "";
    hideMessage(statusMessage);

    for (const res of restaurants) {
        const card = document.createElement("div");
        card.className = "restaurant-card";

        card.innerHTML = `
            <img src="${res.imagePath || "/restImg/default.png"}" class="restaurant-card-image" alt="${res.name}">

            <h3 class="restaurant-card-title">${res.name}</h3>

            <div class="reservation-details-list">
                <div class="reservation-detail-item">
                    <span class="detail-label">סוג:</span>
                    <span>${res.restaurantType}</span>
                </div>

                <div class="reservation-detail-item">
                    <span class="detail-label">שעות:</span>
                    <span>${res.openingHour.substring(0, 5)} - ${res.closingHour.substring(0, 5)}</span>
                </div>

                <div class="reservation-detail-item">
                    <span class="detail-label">דירוג:</span>
                    <span>⭐ ${res.averageScore ? res.averageScore.toFixed(1) : "0.0"}</span>
                </div>
            </div>

            <div class="restaurant-card-actions">
                <a href="/restaurant/statistics/${res.restId}" class="action-btn">סטטיסטיקה</a>
                <a href="/restaurant/update/${res.restId}" class="action-btn">עדכון</a>
                <button class="action-btn delete-btn" data-id="${res.restId}">מחיקת מסעדה</button>
            </div>
        `;

        fragment.appendChild(card);
    }

    restaurantsContainer.appendChild(fragment);
}

/**
 * Adds event delegation for delete buttons.
 */
function setupDeleteEvents() {
    restaurantsContainer.addEventListener("click", async (event) => {
        const btn = event.target.closest(".delete-btn");

        if (!btn) {
            return;
        }

        await handleDeleteLogic(btn);
    });
}

/**
 * Handles two-step delete confirmation.
 */
async function handleDeleteLogic(btn) {
    if (!btn.classList.contains("confirm-mode")) {
        btn.classList.add("confirm-mode", "danger");
        btn.textContent = "בטוחים שרוצים למחוק?";

        setTimeout(() => {
            if (btn.textContent === "בטוחים שרוצים למחוק?") {
                btn.classList.remove("confirm-mode", "danger");
                btn.textContent = "מחיקת מסעדה";
            }
        }, 4000);

        return;
    }

    const restaurantId = btn.getAttribute("data-id");
    await performDeletion(restaurantId, btn);
}

/**
 * Deletes a restaurant and updates the UI.
 */
async function performDeletion(id, button) {
    try {
        button.disabled = true;
        button.textContent = "מוחק...";

        const response = await deleteRequest(`/restaurants/${id}`);

        if (!response.ok) {
            throw new Error("מחיקה נכשלה");
        }

        const card = button.closest(".restaurant-card");

        card.style.opacity = "0.5";
        button.textContent = "נמחק בהצלחה";

        setTimeout(() => {
            card.remove();

            if (restaurantsContainer.children.length === 0) {
                showStatus("אין מסעדות.");
            }
        }, 800);

    } catch (error) {
        console.error("Delete restaurant error:", error);

        button.disabled = false;
        button.classList.remove("confirm-mode", "danger");
        button.textContent = "המחיקה נכשלה";
    }
}

/**
 * Displays a status message and clears the restaurant list.
 */
function showStatus(text) {
    restaurantsContainer.innerHTML = "";
    showMessage(statusMessage, text, "error");
}