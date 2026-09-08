/**
 * User Authentication Module (Login)
 *
 * This module manages the user login flow. It handles the form submission,
 * performs client-side data extraction, sends credentials to the authentication API,
 * and seamlessly handles the session creation using common utilities.
 * Upon success, the user is redirected to the main application interface.
 */

import { showMessage, hideMessage, postJson, saveUser, redirectHome } from "./common.js";

const loginForm = document.getElementById("loginForm");
const statusMessageEl = document.getElementById("errorMessage");

if (loginForm) {
    loginForm.addEventListener("submit", async function(event) {
        event.preventDefault();

        hideMessage(statusMessageEl);

        const loginData = {
            username: document.getElementById("username").value.trim(),
            password: document.getElementById("password").value
        };

        try {
            const response = await postJson("/login", loginData);

            if (response.ok) {
                const authData = await response.json();

                saveUser(authData);
                showMessage(statusMessageEl, "התחברת בהצלחה!", "success");
                redirectHome();

            } else {
                showMessage(statusMessageEl, "שם משתמש או סיסמה לא נכונים.");
            }

        } catch (error) {
            console.error("Login error:", error);
            showMessage(statusMessageEl, "שגיאת רשת קריטית או שהשרת לא זמין.");
        }
    });
}