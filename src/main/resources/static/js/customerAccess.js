/**
 * Customer Access Guard
 * Runs immediately on load to verify the user has the "CUSTOMER" role.
 * Redirects unauthorized users (or guests) to the login or home page.
 */
import { requireRole } from "./common.js";

requireRole("CUSTOMER");