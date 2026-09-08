/**
 * Navbar Module
 *
 * This module dynamically updates the navigation bar
 * based on the current user's session.
 *
 * Responsibilities:
 * - Display user name
 * - Render different menu options for ADMIN / CUSTOMER / Guest
 * - Handle logout action
 */

import { getStoredUser, logout } from "./common.js";

const ADMIN = "ADMIN";
const CUSTOMER = "CUSTOMER";

const GUEST_NAME = "אורח";

const ADMIN_NAVBAR_HTML = `
    <a href="/myrestaurants">המסעדות שלי</a>
    <a href="/restaurant/create">יצירת מסעדה</a>
    <a href="#" id="logoutLink">התנתקות</a>
`;

const CUSTOMER_NAVBAR_HTML = `
    <a href="/reservation/upcoming">הזמנות קרובות</a>
    <a href="/reservation/history">היסטוריית הזמנות</a>
    <a href="#" id="logoutLink">התנתקות</a>
`;

const GUEST_NAVBAR_HTML = `
    <a href="/login">התחברות</a>
    <a href="/register">הרשמה</a>
`;

document.addEventListener("DOMContentLoaded", function () {
    const user = getStoredUser();

    const userNameSpan = document.getElementById("userName");
    const userDropdown = document.getElementById("userDropdown");

    if (!userNameSpan || !userDropdown) {
        return;
    }

    if (!user) {
        showGuestNavbar(userNameSpan, userDropdown);
        return;
    }

    userNameSpan.textContent = user.username;

    if (user.userType === ADMIN) {
        userDropdown.innerHTML = ADMIN_NAVBAR_HTML;

    } else if (user.userType === CUSTOMER) {
        userDropdown.innerHTML = CUSTOMER_NAVBAR_HTML;

    } else {
        showGuestNavbar(userNameSpan, userDropdown);
        return;
    }

    addLogoutListener();
});

/**
 * Renders navbar for guest users.
 *
 * @param {HTMLElement} userNameSpan - Element displaying username
 * @param {HTMLElement} userDropdown - Dropdown menu container
 */
function showGuestNavbar(userNameSpan, userDropdown) {
    userNameSpan.textContent = GUEST_NAME;
    userDropdown.innerHTML = GUEST_NAVBAR_HTML;
}

/**
 * Attaches logout event handler.
 */
function addLogoutListener() {
    const logoutLink = document.getElementById("logoutLink");

    if (!logoutLink) {
        return;
    }

    logoutLink.addEventListener("click", async function (event) {
        event.preventDefault();
        await logout();
    });
}