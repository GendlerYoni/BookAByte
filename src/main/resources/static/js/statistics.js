/**
 * Restaurant Statistics Page Module
 *
 * This module handles the frontend logic for the restaurant statistics page.
 *
 * It is responsible for:
 * - Reading the restaurant ID from the URL
 * - Fetching statistics data from the backend
 * - Rendering quick statistic values
 * - Creating Chart.js doughnut and bar charts
 * - Displaying error messages when statistics cannot be loaded
 */

import { getIdFromUrl, showMessage, hideMessage } from "./common.js";

const PERCENTAGE_TOTAL = 100;
const DOUGHNUT_CUTOUT = "80%";
const CHART_BORDER_RADIUS = 6;
const DECIMAL_PLACES = 1;
const ZERO_VALUE = 0;

const restaurantId = getIdFromUrl();
const statusMessage = document.getElementById("statusMessage");

const cancellationRateElem = document.getElementById("cancellationRateValue");
const avgGroupSizeElem = document.getElementById("avgGroupSizeValue");
const returnCustomerRateElem = document.getElementById("returnCustomerRateValue");

const rootStyles = getComputedStyle(document.documentElement);
const PRIMARY_COLOR = rootStyles.getPropertyValue("--primary-color").trim();
const SECONDARY_COLOR = rootStyles.getPropertyValue("--chart-secondary-color").trim();

const DAY_MAP = {
    Sunday: "ראשון",
    Monday: "שני",
    Tuesday: "שלישי",
    Wednesday: "רביעי",
    Thursday: "חמישי",
    Friday: "שישי",
    Saturday: "שבת"
};

document.addEventListener("DOMContentLoaded", () => {
    if (restaurantId && !isNaN(restaurantId)) {
        loadStatistics(restaurantId);
    } else {
        showMessage(statusMessage, "מזהה מסעדה לא תקין.", "error");
    }
});

/**
 * Fetches statistics data for a specific restaurant and renders it on the page.
 *
 * @param {string} id - The restaurant ID taken from the URL
 * @throws {Error} If the statistics request fails
 */
async function loadStatistics(id) {
    try {
        const response = await fetch(`/restaurant/api/statistics/${id}`);

        if (!response.ok) {
            throw new Error("נכשלה טעינת הנתונים.");
        }

        const statsData = await response.json();

        hideMessage(statusMessage);
        renderQuickStats(statsData);
        renderCharts(statsData);

    } catch (error) {
        showMessage(statusMessage, error.message, "error");
    }
}

/**
 * Renders the summary statistics shown at the top of the page.
 *
 * @param {Object} data - The statistics response object from the backend
 */
function renderQuickStats(data) {
    createDoughnut("cancellationPie", data.cancellationRate, "ביטולים", "תקינות");
    cancellationRateElem.textContent = data.cancellationRate.toFixed(DECIMAL_PLACES) + "%";

    createDoughnut("returnCustomerPie", data.returnCustomerRate, "חוזרים", "חדשים");
    returnCustomerRateElem.textContent = data.returnCustomerRate.toFixed(DECIMAL_PLACES) + "%";

    avgGroupSizeElem.textContent = data.avgGroupSize.toFixed(DECIMAL_PLACES);
}

/**
 * Creates a doughnut chart showing a percentage value versus the remaining percentage.
 *
 * @param {string} canvasId - The ID of the canvas element
 * @param {number} value - The percentage value to display
 * @param {string} labelTarget - Label for the main percentage part
 * @param {string} labelOther - Label for the remaining percentage part
 */
function createDoughnut(canvasId, value, labelTarget, labelOther) {
    const ctx = document.getElementById(canvasId).getContext("2d");

    new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: [labelTarget, labelOther],
            datasets: [{
                data: [value, PERCENTAGE_TOTAL - value],
                backgroundColor: [PRIMARY_COLOR, SECONDARY_COLOR],
                borderWidth: ZERO_VALUE
            }]
        },
        options: {
            cutout: DOUGHNUT_CUTOUT,
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    enabled: true,
                    callbacks: {
                        label: function(context) {
                            return ` ${context.label}: ${context.raw.toFixed(DECIMAL_PLACES)}%`;
                        }
                    }
                }
            }
        }
    });
}

/**
 * Renders all bar charts on the statistics page.
 *
 * @param {Object} data - The statistics response object from the backend
 */
function renderCharts(data) {
    const dayLabels = Object.values(DAY_MAP);
    const dayValues = Object.keys(DAY_MAP).map(key => data.ordersByDay[key] || ZERO_VALUE);

    createChart("ordersByDayChart", "bar", dayLabels, dayValues, "הזמנות");

    createChart(
        "ratingDistributionChart",
        "bar",
        Object.keys(data.ratingDistribution),
        Object.values(data.ratingDistribution),
        "ביקורות"
    );

    const hours = Object.keys(data.ordersByHour);
    const hourValues = Object.values(data.ordersByHour);

    createChart("ordersByHourChart", "bar", hours, hourValues, "הזמנות");
}

/**
 * Creates a Chart.js chart with the provided labels and data.
 *
 * @param {string} canvasId - The ID of the canvas element
 * @param {string} type - The chart type, such as "bar" or "line"
 * @param {Array<string>} labels - Labels displayed on the chart axis
 * @param {Array<number>} dataPoints - Numeric values displayed in the chart
 * @param {string} labelName - Dataset label name
 * @returns {Chart} The created Chart.js chart instance
 */
function createChart(canvasId, type, labels, dataPoints, labelName) {
    const ctx = document.getElementById(canvasId).getContext("2d");

    return new Chart(ctx, {
        type: type,
        data: {
            labels: labels,
            datasets: [{
                label: labelName,
                data: dataPoints,
                backgroundColor: PRIMARY_COLOR,
                borderRadius: CHART_BORDER_RADIUS
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: ZERO_VALUE }
                }
            }
        }
    });
}