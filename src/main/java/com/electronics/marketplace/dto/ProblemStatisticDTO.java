package com.electronics.marketplace.dto;

import java.io.Serializable;

public class ProblemStatisticDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long problemId;
    private String problemName;
    private Long requestCount;
    private Integer estimatedMinDays;
    private Integer estimatedMaxDays;

    public ProblemStatisticDTO() {
    }

    public ProblemStatisticDTO(Long problemId, String problemName, Long requestCount, Integer estimatedMinDays, Integer estimatedMaxDays) {
        this.problemId = problemId;
        this.problemName = problemName;
        this.requestCount = requestCount;
        this.estimatedMinDays = estimatedMinDays;
        this.estimatedMaxDays = estimatedMaxDays;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public Long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }

    public Integer getEstimatedMinDays() {
        return estimatedMinDays;
    }

    public void setEstimatedMinDays(Integer estimatedMinDays) {
        this.estimatedMinDays = estimatedMinDays;
    }

    public Integer getEstimatedMaxDays() {
        return estimatedMaxDays;
    }

    public void setEstimatedMaxDays(Integer estimatedMaxDays) {
        this.estimatedMaxDays = estimatedMaxDays;
    }

    @Override
    public String toString() {
        return "ProblemStatisticDTO{" +
                "problemId=" + problemId +
                ", problemName='" + problemName + '\'' +
                ", requestCount=" + requestCount +
                ", estimatedMinDays=" + estimatedMinDays +
                ", estimatedMaxDays=" + estimatedMaxDays +
                '}';
    }
}
