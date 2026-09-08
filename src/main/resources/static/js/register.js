/**
 * Register Page Module
 *
 * Handles user registration flow.
 *
 * Responsibilities:
 * - Validate form input
 * - Send registration request to backend
 * - Handle success and error states
 * - Store authenticated user and redirect
 */

import { showMessage, hideMessage, postJson, saveUser, redirectHome } from "./common.js";

const registerForm = document.getElementById("registerForm");
const errorMessage = document.getElementById("errorMessage");

registerForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    hideMessage(errorMessage);

    const usernameValue = document.getElementById("username").value.trim();
    const passwordValue = document.getElementById("password").value;
    const confirmPasswordValue = document.getElementById("confirmPassword").value;
    const selectedRole = document.querySelector('input[name="role"]:checked');

    if (!usernameValue || !passwordValue || !confirmPasswordValue) {
        showMessage(errorMessage, "יש למלא את כל השדות.");
        return;
    }

    if (passwordValue !== confirmPasswordValue) {
        showMessage(errorMessage, "הסיסמאות אינן תואמות.");
        return;
    }

    if (!selectedRole) {
        showMessage(errorMessage, "יש לבחור סוג משתמש.");
        return;
    }

    const registerData = {
        username: usernameValue,
        password: passwordValue,
        userType: selectedRole.value
    };

    try {
        const response = await postJson("/register", registerData);

        if (response.ok) {
            const authData = await response.json();

            saveUser(authData);
            showMessage(errorMessage, "ההרשמה הצליחה!", "success");
            redirectHome();
        } else {
            showMessage(errorMessage, "ההרשמה נכשלה. ייתכן ששם המשתמש כבר קיים.");
        }

    } catch (error) {
        console.error("Register error:", error);
        showMessage(errorMessage, "אירעה שגיאה. נסה שוב מאוחר יותר.");
    }
});