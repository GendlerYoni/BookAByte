/**
 * Data Transfer Object containing analytical data for the restaurant dashboard.
 * This complex response aggregates multiple metrics such as cancellation rates,
 * daily/hourly order distributions, and customer loyalty rates, formatted specifically
 * for consumption by charting libraries (like Chart.js).
 */
package yoni.restaurantreservationsystem.responses;

import java.util.Map;

public class StatisticsResponse {
    private float cancellationRate;
    private Map<String, Long> ordersByDay;
    private Map<Integer, Long> ordersByHour;
    private float avgGroupSize;
    private float returnCustomerRate;
    private Map<Integer, Long> ratingDistribution;

    public StatisticsResponse(float cancellationRate,
                              Map<String, Long> ordersByDay,
                              Map<Integer, Long> ordersByHour,
                              float avgGroupSize,
                              float returnCustomerRate,
                              Map<Integer, Long> ratingDistribution) {
        this.cancellationRate = cancellationRate;
        this.ordersByDay = ordersByDay;
        this.ordersByHour = ordersByHour;
        this.avgGroupSize = avgGroupSize;
        this.returnCustomerRate = returnCustomerRate;
        this.ratingDistribution = ratingDistribution;
    }

    public float getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(float cancellationRate) {
        this.cancellationRate = cancellationRate;
    }

    public Map<String, Long> getOrdersByDay() {
        return ordersByDay;
    }

    public void setOrdersByDay(Map<String, Long> ordersByDay) {
        this.ordersByDay = ordersByDay;
    }

    public Map<Integer, Long> getOrdersByHour() {
        return ordersByHour;
    }

    public void setOrdersByHour(Map<Integer, Long> ordersByHour) {
        this.ordersByHour = ordersByHour;
    }

    public float getReturnCustomerRate() {
        return returnCustomerRate;
    }

    public void setReturnCustomerRate(float returnCustomerRate) {
        this.returnCustomerRate = returnCustomerRate;
    }

    public float getAvgGroupSize() {
        return avgGroupSize;
    }

    public void setAvgGroupSize(float avgGroupSize) {
        this.avgGroupSize = avgGroupSize;
    }

    public Map<Integer, Long> getRatingDistribution() {
        return ratingDistribution;
    }

    public void setRatingDistribution(Map<Integer, Long> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }
}