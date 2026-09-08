/**
 * Admin Access Guard
 * Runs immediately on load to verify the user has the "ADMIN" role.
 * Redirects unauthorized users to the login or home page.
 */
import { requireRole } from "./common.js";

requireRole("ADMIN");