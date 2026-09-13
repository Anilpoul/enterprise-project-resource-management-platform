package com.enterprise.platform.analytics.mapper;

import com.enterprise.platform.analytics.dto.response.EmployeeScorecardResponse;
import com.enterprise.platform.analytics.dto.response.ProjectAnalyticsResponse;
import com.enterprise.platform.analytics.dto.response.SprintAnalyticsResponse;
import com.enterprise.platform.analytics.entity.EmployeePerformanceMetric;
import com.enterprise.platform.analytics.entity.ProjectMetricSnapshot;
import com.enterprise.platform.analytics.entity.SprintMetricSnapshot;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AnalyticsMapper {

    @Mapping(source = "updatedAt", target = "lastCalculatedAt")
    ProjectAnalyticsResponse toResponse(ProjectMetricSnapshot snapshot);

    SprintAnalyticsResponse toResponse(SprintMetricSnapshot snapshot);

    EmployeeScorecardResponse toResponse(EmployeePerformanceMetric metric);
}
