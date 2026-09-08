/**
 * Home Page Module (Restaurant Discovery)
 *
 * This module controls the main restaurant browsing page.
 *
 * Responsibilities:
 * - Fetch all restaurants from the backend
 * - Render restaurant cards dynamically
 * - Provide client-side filtering:
 *   • Text search (restaurant name)
 *   • Restaurant type/category
 *   • Minimum rating
 * - Handle UI states (empty results / errors)
 */
import { showMessage, hideMessage } from "./common.js";

const searchInput = document.getElementById("searchInput");
const typeButtons = document.querySelectorAll(".type-filter-btn");
const ratingInputs = document.querySelectorAll('input[name="ratingFilter"]');
const clearRatingBtn = document.getElementById("clearRatingFilterBtn");
const resetAllBtn = document.getElementById("resetAllBtn");
const restaurantsContainer = document.getElementById("restaurantsContainer");
const statusMessage = document.getElementById("statusMessage");

let allRestaurants = [];
let selectedTypes = [];
let selectedRating = 0;
let searchText = "";

document.addEventListener("DOMContentLoaded", () => {
    loadRestaurants();
});

/**
 * Fetches all restaurants from backend and initializes UI.
 */
async function loadRestaurants() {
    try {
        const response = await fetch("/restaurants");

        if (!response.ok) {
            throw new Error(`Failed to fetch restaurants: HTTP ${response.status}`);
        }

        allRestaurants = await response.json();

        if (allRestaurants.length === 0) {
            showStatus("אין עדיין מסעדות במערכת.");
        } else {
            renderRestaurants(allRestaurants);
        }

    } catch (error) {
        console.error("Error loading restaurants:", error);
        showStatus("שגיאה בטעינת המסעדות. נסה לרענן את הדף.");
    }
}

/**
 * Renders restaurant cards into the DOM.
 */
function renderRestaurants(restaurants) {
    const fragment = document.createDocumentFragment();

    restaurantsContainer.innerHTML = "";
    hideMessage(statusMessage);

    if (restaurants.length === 0) {
        showStatus("לא נמצאו מסעדות שמתאימות לסינון שבחרת.");
        return;
    }

    for (const res of restaurants) {
        const card = document.createElement("a");
        card.href = `/reservation/create/${res.restId}`;
        card.className = "restaurant-card interactive-card";

        const formattedOpen = res.openingHour.substring(0, 5);
        const formattedClose = res.closingHour.substring(0, 5);
        const displayScore = res.averageScore ? res.averageScore.toFixed(1) : "0.0";
        const displayImage = res.imagePath || "/restImg/default.png";

        card.innerHTML = `
            <img src="${displayImage}" class="restaurant-card-image" alt="${res.name}">
            <h3 class="restaurant-card-title">${res.name}</h3>
            <div class="restaurant-card-info">
                <span class="restaurant-card-type">${res.restaurantType}</span>
                <span class="restaurant-card-hours">${formattedOpen} - ${formattedClose}</span>
                <span class="restaurant-card-rating">
                    ⭐ ${displayScore} (${res.reviewCount || 0})
                </span>
            </div>
        `;

        fragment.appendChild(card);
    }

    restaurantsContainer.appendChild(fragment);
}

/**
 * Displays a status message and clears the UI.
 */
function showStatus(text) {
    restaurantsContainer.innerHTML = "";
    showMessage(statusMessage, text, "error");
}

/**
 * Applies all active filters and re-renders results.
 */
function applyFilters() {
    const filtered = allRestaurants.filter(res => {
        const matchesSearch = res.name.toLowerCase().includes(searchText);
        const matchesType = selectedTypes.length === 0 || selectedTypes.includes(res.restaurantType);
        const matchesRating = (res.averageScore || 0) >= selectedRating;

        return matchesSearch && matchesType && matchesRating;
    });

    renderRestaurants(filtered);
}


// Text Search
searchInput.addEventListener("input", (e) => {
    searchText = e.target.value.toLowerCase();
    applyFilters();
});

// Type Filter
for (const btn of typeButtons) {
    btn.addEventListener("click", () => {
        const type = btn.getAttribute("data-type");
        btn.classList.toggle("active");

        if (btn.classList.contains("active")) {
            selectedTypes.push(type);
        } else {
            selectedTypes = selectedTypes.filter(t => t !== type);
        }

        applyFilters();
    });
}

// Rating Filter
for (const radio of ratingInputs) {
    radio.addEventListener("change", (e) => {
        selectedRating = parseFloat(e.target.value);
        applyFilters();
    });
}

// Clear Rating
clearRatingBtn.addEventListener("click", () => {
    selectedRating = 0;

    for (const radio of ratingInputs) {
        radio.checked = false;
    }

    applyFilters();
});

// Reset All Filters
resetAllBtn.addEventListener("click", () => {
    searchText = "";
    selectedRating = 0;
    selectedTypes = [];

    searchInput.value = "";

    for (const radio of ratingInputs) {
        radio.checked = false;
    }

    for (const btn of typeButtons) {
        btn.classList.remove("active");
    }

    applyFilters();
});