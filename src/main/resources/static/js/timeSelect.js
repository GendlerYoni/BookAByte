/**
 * Time Select Utility Module
 *
 * This module provides a reusable helper for filling HTML select elements with time options.
 *
 * It is used in forms that require choosing opening hours, closing hours, or reservation times.
 */

const MIN_HOUR = 0;
const MAX_HOUR = 23;
const HALF_HOUR = 30;
const MINUTES_IN_HOUR = 60;
const TIME_PART_PADDING = 2;
const EMPTY_STRING = "";
const TIME_SEPARATOR = ":";
const ZERO_PADDING_CHAR = "0";

/**
 * Populates a select element with time options.
 *
 * By default, it creates options from 00:00 to 23:30
 * in 30-minute intervals.
 *
 * @param {string} selectId - The ID of the select element to populate
 * @param {number} [openingHour=MIN_HOUR] - The first hour to display
 * @param {number} [closingHour=MAX_HOUR] - The last hour to display
 * @param {number} [closingMinute=HALF_HOUR] - The latest allowed minute in the closing hour
 * @param {number} [timeStep=HALF_HOUR] - The interval between time options, in minutes
 */
export function populateTimeOptions(selectId,
                                    openingHour = MIN_HOUR,
                                    closingHour = MAX_HOUR,
                                    closingMinute = HALF_HOUR,
                                    timeStep = HALF_HOUR) {
    const select = document.getElementById(selectId);

    if (!select) {
        return;
    }

    select.innerHTML = EMPTY_STRING;

    for (let hour = openingHour; hour <= closingHour; hour++) {
        for (let minute = 0; minute < MINUTES_IN_HOUR; minute += timeStep) {
            if (hour === closingHour && minute > closingMinute) {
                break;
            }

            const formattedHour = String(hour).padStart(TIME_PART_PADDING, ZERO_PADDING_CHAR);
            const formattedMinute = String(minute).padStart(TIME_PART_PADDING, ZERO_PADDING_CHAR);
            const timeValue = `${formattedHour}${TIME_SEPARATOR}${formattedMinute}`;

            const option = document.createElement("option");
            option.value = timeValue;
            option.textContent = timeValue;

            select.appendChild(option);
        }
    }
}